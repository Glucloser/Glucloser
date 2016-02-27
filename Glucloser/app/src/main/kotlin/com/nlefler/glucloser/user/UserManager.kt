package com.nlefler.glucloser.user

import android.content.Context
import android.util.Base64
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.dataSource.sync.DDPxSync
import com.squareup.moshi.Moshi
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Created by nathan on 2/15/16.
 */
class UserManager @Inject constructor(val ddpxSync: DDPxSync, val ctx: Context) {

    companion object {
        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.usermanager"
        private val SHARED_PREFS_IDENTITY_KEY = "com.nlefler.glucloser.usermanager.identity"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.concealentity"
    }

    private class Identity (val uuids: List<String>, val profile: String) {
    }

    private val crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
    private var identity: Identity = emptyIdentity()
    private val identityLock = Semaphore(1)

    init {
        identity = getDecryptedIdentity()
    }

    fun loginOrCreateUser(email: String) {
        // TODO(nl) This check fails on new installs for current users
        if (identity.uuids.isEmpty()) {
            createUser(email)
        }
        else {
            ddpxSync.loginUser(email, identity.uuids.first()).continueWith { task ->
                if (task.isFaulted) {
                    // TODO(nl) Handle error
                    return@continueWith
                }
                updateIdentity(identity.uuids, task.result)
            }
        }
    }

    fun savePushToken(token: String) {
        ddpxSync.savePushToken(identity.profile, token).continueWith { task ->
            if (task.isFaulted) {
                // TODO(nl) Handle error
                return@continueWith
            }
            updateIdentity(identity.uuids, task.result)
        }
    }

    private fun createUser(email: String) {
        clearIdentity()
        val uuid = UUID.randomUUID().toString()
        ddpxSync.createUser(email, uuid).continueWith { task ->
            if (task.isFaulted) {
                // TODO(nl) Message user
                return@continueWith;
            }
            updateIdentity(listOf(uuid), task.result)
        }
    }

    private fun clearIdentity() {
        identityLock.acquire()
        identity = emptyIdentity()
        encryptAndStoreIdentity(ctx, identity)
        identityLock.release()
    }

    private fun updateIdentity(uuids: List<String>, profile: String) {
        identityLock.acquire()
        identity = Identity(uuids, profile)
        encryptAndStoreIdentity(ctx, identity)
        identityLock.release()
    }

    // Helpers
    private fun encryptAndStoreIdentity(ctx: Context, identity: Identity) {
        if (!crypto.isAvailable()) {
            return
        }
        val entity = Entity(CONCEAL_ENTITY_NAME)
        try {
            val jsonAdapter = Moshi.Builder().build().adapter(Identity::class.java)

            val encryptedToken = this.crypto.encrypt(jsonAdapter.toJson(identity).toByteArray(), entity)
            val encryptedBase64Token = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

            val sharedPreferences = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(SHARED_PREFS_IDENTITY_KEY, encryptedBase64Token)
            editor.apply()
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getDecryptedIdentity(): Identity {
        var identity = Identity(emptyList(), "")
        if (!crypto.isAvailable()) {
            return identity
        }

        try {
            val sharedPref = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

            val encryptedBase64Token = sharedPref.getString(SHARED_PREFS_IDENTITY_KEY, null)
            val entity = Entity(CONCEAL_ENTITY_NAME)
            val encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT)

            if (encryptedToken.size > 0) {
                val jsonAdapter = Moshi.Builder().build().adapter(Identity::class.java)
                val json = this.crypto.decrypt(encryptedToken, entity).toString(Charset.forName("UTF-8"))
                identity = jsonAdapter.fromJson(json)
            }
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        finally {
            return identity
        }
    }

    private fun emptyIdentity(): Identity {
        return Identity(emptyList(), "{}")
    }
}
