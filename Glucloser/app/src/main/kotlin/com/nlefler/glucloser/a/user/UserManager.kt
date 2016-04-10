package com.nlefler.glucloser.a.user

import android.content.Context
import android.util.Base64
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.squareup.moshi.Moshi
import rx.Observable
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Semaphore
import javax.inject.Inject

/**
 * Created by nathan on 2/15/16.
 */
class UserManager @Inject constructor(val userService: CairoUserService, val ctx: Context) {

    companion object {
        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.a.usermanager"
        private val SHARED_PREFS_IDENTITY_KEY = "com.nlefler.glucloser.a.usermanager.identity"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.a.concealentity"
    }

    private class Identity (val sessionID: String?, val pushToken: String?) {
    }

    private val crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
    private var identity: UserManager.Identity = emptyIdentity()
    private val identityLock = Semaphore(1)

    init {
        identity = getDecryptedIdentity()
    }

    fun loginOrCreateUser(email: String): Observable<Unit> {
        val uuid = identity.sessionID ?: UUID.randomUUID().toString()
        updateIdentity(uuid, identity.pushToken)
        return createUserOrLogin(email, uuid)
    }

    fun savePushToken(token: String) {
        val uuid = identity.sessionID ?: return
        updateIdentity(uuid, identity.pushToken)
        savePushToken(uuid, token)
    }

    fun saveFoursquareId(fsqId: String) {
        val uuid = identity.sessionID ?: return
        saveFoursquareId(uuid, fsqId)
    }


    private fun createUserOrLogin(email: String, sessionID: String): Observable<Unit> {
        return userService.createOrLogin(object: CairoUserService.CreateOrLoginBody {
            override val sessionID = sessionID
            override val email = email
        })
   }

    private fun savePushToken(sessionID: String, token: String): Observable<Unit> {
        return userService.savePushToken(object: CairoUserService.SavePushTokenBody {
            override val sessionID = sessionID
            override val token = token
        })
   }

    private fun saveFoursquareId(sessionID: String, fsqId: String): Observable<Unit> {
        return userService.saveFoursquareID(object: CairoUserService.SaveFoursquareIDBody {
            override val sessionID = sessionID
            override val token = fsqId
        })
   }

    fun sessionID(): String? {
        return identity.sessionID
    }

    private fun clearIdentity() {
        identityLock.acquire()
        identity = emptyIdentity()
        encryptAndStoreIdentity(ctx, identity)
        identityLock.release()
    }

    private fun updateIdentity(uuid: String, pushToken: String?) {
        identityLock.acquire()
        identity = UserManager.Identity(uuid, pushToken)
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
        return UserManager.Identity(null, null)
    }
}
