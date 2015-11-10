package com.nlefler.glucloser.foursquare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Base64
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain

import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.foursquare.android.nativeoauth.FoursquareOAuth
import com.foursquare.android.nativeoauth.model.AccessTokenResponse
import com.foursquare.android.nativeoauth.model.AuthCodeResponse
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.R
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters

import java.io.IOException

/**
 * Created by Nathan Lefler on 12/28/14.
 */
public class FoursquareAuthManager private constructor() {

    private val crypto: Crypto
    private var _userAccessToken = ""

    init {
        val ctx = GlucloserApplication.SharedApplication().getApplicationContext()
        this.crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())

        this._userAccessToken = this.getDecryptedAuthToken()
    }

    public fun startAuthRequest(managingActivity: Activity) {
        val intent = FoursquareOAuth.getConnectIntent(managingActivity, managingActivity.getString(R.string.foursquare_app_id))
        managingActivity.startActivityForResult(intent, FOURSQUARE_CONNECT_INTENT_CODE)
    }

    public fun gotAuthResponse(managingActivity: Activity, responseCode: Int, responseData: Intent) {
        val codeResponse = FoursquareOAuth.getAuthCodeFromResult(responseCode, responseData)
        if (codeResponse.getException() == null) {
            val intent = FoursquareOAuth.getTokenExchangeIntent(managingActivity, managingActivity.getString(R.string.foursquare_app_id), managingActivity.getString(R.string.foursquare_app_secret), codeResponse.getCode())
            managingActivity.startActivityForResult(intent, FOURSQUARE_TOKEN_EXCHG_INTENT_CODE)
        }
    }

    public fun gotTokenExchangeResponse(managingActivity: Activity, responseCode: Int, responseData: Intent) {
        val tokenResponse = FoursquareOAuth.getTokenFromResult(responseCode, responseData)
        if (tokenResponse.getException() == null) {
            _userAccessToken = tokenResponse.getAccessToken()
            this.encryptAndStoreAuthToken(managingActivity, this._userAccessToken)

            (FoursquareUserHelper()).fetchAndStoreUserId()
        }
    }

    public fun getClientAuthParameters(ctx: Context): NLFoursquareClientParameters {
        val appId = ctx.getString(R.string.foursquare_app_id)
        val appSecret = ctx.getString(R.string.foursquare_app_secret)

        val params = NLFoursquareClientParameters(appId)
        if (!this._userAccessToken.isEmpty()) {
            params.setUserOAuthToken(this._userAccessToken)
        } else {
            params.setClientSecret(appSecret)
        }

        return params
    }

    private fun encryptAndStoreAuthToken(ctx: Context, token: String?) {
        if (!this.crypto.isAvailable() || token == null || token.isEmpty()) {
            return
        }
        val entity = Entity(CONCEAL_ENTITY_NAME)
        try {
            val encryptedToken = this.crypto.encrypt(token.toByteArray(), entity)
            val encryptedBase64Token = Base64.encodeToString(encryptedToken, Base64.DEFAULT)
            val sharedPreferences = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(SHARED_PREFS_4SQ_TOKEN_KEY, encryptedBase64Token)
            editor.apply()
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getDecryptedAuthToken(): String {
        if (!this.crypto.isAvailable()) {
            return ""
        }
        try {
            val sharedPref = GlucloserApplication.SharedApplication().getApplicationContext().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val encryptedBase64Token = sharedPref.getString(SHARED_PREFS_4SQ_TOKEN_KEY, "")
            val entity = Entity(CONCEAL_ENTITY_NAME)
            val encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT)

            if (encryptedToken.size() > 0) {
                return String(this.crypto.decrypt(encryptedToken, entity), "UTF-8")
            } else {
                return ""
            }
        } catch (e: KeyChainException) {
            e.printStackTrace()
            return ""
        } catch (e: CryptoInitializationException) {
            e.printStackTrace()
            return ""
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

    }

    companion object {
        private val LOG_TAG = "FoursquareAuthManager"

        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.foursquareprefs"
        private val SHARED_PREFS_4SQ_TOKEN_KEY = "com.nlefler.glucloser.4sqtkn"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.concealentity"

        public val FOURSQUARE_CONNECT_INTENT_CODE: Int = 39228
        public val FOURSQUARE_TOKEN_EXCHG_INTENT_CODE: Int = 39229

        private var _sharedInstance: FoursquareAuthManager? = null
        @Synchronized
        public fun SharedManager(): FoursquareAuthManager {
            if (_sharedInstance == null) {
                _sharedInstance = FoursquareAuthManager()
            }
            return _sharedInstance!!
        }
    }
}
