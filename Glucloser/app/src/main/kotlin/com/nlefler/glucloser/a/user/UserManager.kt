package com.nlefler.glucloser.a.user

import android.content.Context
import android.util.Base64
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain
import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.nlefler.glucloser.a.dataSource.sync.cairo.CairoServices
import com.nlefler.glucloser.a.dataSource.sync.cairo.services.CairoUserService
import com.nlefler.glucloser.a.util.EncryptedPrefsStorageHelper
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
class UserManager @Inject constructor(val cairoServices: CairoServices, ctx: Context) {

    companion object {
        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.a.usermanager"
        private val SHARED_PREFS_IDENTITY_KEY = "com.nlefler.glucloser.a.usermanager.identity"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.a.concealentity"
    }

    private class Identity (val sessionID: String?, val pushToken: String?) {
    }

    private val userService = cairoServices.userService()
    private val crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
    private val storageHelper = EncryptedPrefsStorageHelper(crypto, ctx, SHARED_PREFS_NAME, CONCEAL_ENTITY_NAME)
    private var identity: UserManager.Identity = emptyIdentity()
    private val identityLock = Semaphore(1)

    init {
        identity = getDecryptedIdentity()
    }

    fun loginOrCreateUser(email: String): Observable<Void?> {
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


    private fun createUserOrLogin(email: String, sessionID: String): Observable<Void?> {
        return userService.createOrLogin(CairoUserService.CreateOrLoginBody(email, sessionID))
   }

    private fun savePushToken(sessionID: String, token: String): Observable<Unit> {
        return userService.savePushToken(CairoUserService.SavePushTokenBody(sessionID, token))
   }

    private fun saveFoursquareId(sessionID: String, fsqId: String): Observable<Unit> {
        return userService.saveFoursquareID(CairoUserService.SaveFoursquareIDBody(sessionID, fsqId))
   }

    fun sessionID(): String? {
        return identity.sessionID
    }

    private fun clearIdentity() {
        cairoServices.clearAuthentication()
        identityLock.acquire()
        identity = emptyIdentity()
        encryptAndStoreIdentity(identity)
        identityLock.release()
    }

    private fun updateIdentity(uuid: String, pushToken: String?) {
        identityLock.acquire()
        identity = UserManager.Identity(uuid, pushToken)
        encryptAndStoreIdentity(identity)
        identityLock.release()
    }

    // Helpers
    private fun encryptAndStoreIdentity(identity: UserManager.Identity) {
        val jsonAdapter = Moshi.Builder().build().adapter(UserManager.Identity::class.java)
        storageHelper.store(SHARED_PREFS_IDENTITY_KEY, jsonAdapter.toJson(identity))
    }

    private fun getDecryptedIdentity(): UserManager.Identity {
        val jsonAdapter = Moshi.Builder().build().adapter(UserManager.Identity::class.java)
        val stored = storageHelper.fetch(SHARED_PREFS_IDENTITY_KEY) ?: return emptyIdentity()
        return jsonAdapter.fromJson(stored)
    }

    private fun emptyIdentity(): UserManager.Identity {
        return UserManager.Identity(null, null)
    }
}
