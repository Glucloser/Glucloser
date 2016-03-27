package com.nlefler.glucloser.a.user

import android.content.Context
import android.util.Base64
import bolts.Task
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.dataSource.sync.DDPxSync
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
        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.a.usermanager"
        private val SHARED_PREFS_IDENTITY_KEY = "com.nlefler.glucloser.a.usermanager.identity"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.a.concealentity"
    }

    private class Identity (val uuid: String?, val pushToken: String?,
                            val profile: String) {
    }

    private val crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
    private var identity: UserManager.Identity = emptyIdentity()
    private val identityLock = Semaphore(1)

    init {
        identity = getDecryptedIdentity()
    }

    fun loginOrCreateUser(email: String): Task<Unit> {
        val uuid = identity.uuid ?: UUID.randomUUID().toString()
        return createUserOrLogin(email, uuid).continueWithTask { task ->
            if (task.isFaulted) {
                // TODO(nl) Handle error
                return@continueWithTask Task.forError<Unit>(task.error)
            }
            val profile = task.result
            if (profile != null) {
                updateIdentity(uuid, identity.pushToken, profile)
            }
            return@continueWithTask Task.forResult(Unit)
        }
    }

    fun savePushToken(token: String) {
        val uuid = identity.uuid ?: return
        savePushToken(uuid, token).continueWith { task ->
            if (task.isFaulted) {
                // TODO(nl) Handle error
                return@continueWith
            }
            val profile = task.result
            if (profile != null) {
                updateIdentity(uuid, identity.pushToken, profile)
            }
        }
    }

    fun saveFoursquareId(fsqId: String) {
        val uuid = identity.uuid ?: return
        saveFoursquareId(uuid, fsqId).continueWith { task ->
            if (task.isFaulted) {
                // TODO(nl) Handle error
                return@continueWith
            }
            val profile = task.result
            if (profile != null) {
                updateIdentity(uuid, identity.pushToken, profile)
            }
        }
    }


    private fun createUserOrLogin(email: String, uuid: String): Task<String?> {
        return ddpxSync.call("createOrLogin", arrayOf(email, uuid)).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    private fun savePushToken(uuid: String, token: String): Task<String?> {
        return ddpxSync.call("savePushToken", arrayOf(uuid, token)).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    private fun saveFoursquareId(uuid: String, fsqId: String): Task<String?> {
        return ddpxSync.call("saveFoursquareId", arrayOf(uuid, fsqId)).continueWithTask { task ->
            if (task.isFaulted) {
                val error = Exception(task.error.message)
                return@continueWithTask Task.forError<String>(error)
            }
            return@continueWithTask Task.forResult(task.result.result)
        }
    }

    fun uuid(): String? {
        return identity.uuid
    }

    private fun clearIdentity() {
        identityLock.acquire()
        identity = emptyIdentity()
        encryptAndStoreIdentity(ctx, identity)
        identityLock.release()
    }

    private fun updateIdentity(uuid: String, pushToken: String?, profile: String) {
        identityLock.acquire()
        identity = UserManager.Identity(uuid, pushToken, profile)
        encryptAndStoreIdentity(ctx, identity)
        identityLock.release()
    }

    // Helpers
    private fun encryptAndStoreIdentity(ctx: Context, identity: UserManager.Identity) {
        if (!crypto.isAvailable) {
            return
        }
        val entity = Entity(CONCEAL_ENTITY_NAME)
        try {
            val jsonAdapter = Moshi.Builder().build().adapter(UserManager.Identity::class.java)

            val encryptedToken = this.crypto.encrypt(jsonAdapter.toJson(identity).toByteArray(), entity)
            val encryptedBase64Token = Base64.encodeToString(encryptedToken, Base64.DEFAULT)

            val sharedPreferences = ctx.getSharedPreferences(UserManager.Companion.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(UserManager.Companion.SHARED_PREFS_IDENTITY_KEY, encryptedBase64Token)
            editor.apply()
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getDecryptedIdentity(): UserManager.Identity {
        var identity = emptyIdentity()
        if (!crypto.isAvailable) {
            return identity
        }

        try {
            val sharedPref = ctx.getSharedPreferences(UserManager.Companion.SHARED_PREFS_NAME, Context.MODE_PRIVATE)

            val encryptedBase64Token = sharedPref.getString(UserManager.Companion.SHARED_PREFS_IDENTITY_KEY, null)
            val entity = Entity(CONCEAL_ENTITY_NAME)
            val encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT)

            if (encryptedToken.size > 0) {
                val jsonAdapter = Moshi.Builder().build().adapter(UserManager.Identity::class.java)
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

    private fun emptyIdentity(): UserManager.Identity {
        return UserManager.Identity(null, null, "{}")
    }
}
