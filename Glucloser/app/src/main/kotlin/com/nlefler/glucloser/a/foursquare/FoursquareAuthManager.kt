package com.nlefler.glucloser.a.foursquare

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain

import com.facebook.crypto.Crypto
import com.facebook.crypto.Entity
import com.facebook.crypto.exception.CryptoInitializationException
import com.facebook.crypto.exception.KeyChainException
import com.facebook.crypto.util.SystemNativeCryptoLibrary
import com.foursquare.android.nativeoauth.FoursquareOAuth
import com.foursquare.android.nativeoauth.model.AccessTokenResponse
import com.foursquare.android.nativeoauth.model.AuthCodeResponse
import com.nlefler.glucloser.a.GlucloserApplication
import com.nlefler.glucloser.a.R
import com.nlefler.glucloser.a.dataSource.jsonAdapter.UrlJsonAdapter
import com.nlefler.glucloser.a.user.UserManager
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpoint
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters
import com.nlefler.nlfoursquare.Model.User.NLFoursquareUserInfoResponse
import com.nlefler.nlfoursquare.Users.NLFoursquareUserInfo
import com.squareup.moshi.Moshi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

/**
 * Created by Nathan Lefler on 12/28/14.
 */
class FoursquareAuthManager @Inject constructor(val ctx: Context, val userManager: UserManager) {

    private val crypto: Crypto
    private var _userAccessToken = ""
    private val restAdapter: Retrofit

    init {
        crypto = Crypto(SharedPrefsBackedKeyChain(ctx), SystemNativeCryptoLibrary())
        val moshi = Moshi.Builder().add(UrlJsonAdapter()).build()
        restAdapter = Retrofit.Builder().baseUrl(NLFoursquareEndpoint.NLFOURSQUARE_V2_ENDPOINT)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        _userAccessToken = this.getDecryptedAuthToken()
    }

    fun startAuthRequest(managingActivity: Activity) {
        val intent = FoursquareOAuth.getConnectIntent(managingActivity, managingActivity.getString(R.string.foursquare_app_id))
        managingActivity.startActivityForResult(intent, FoursquareAuthManager.Companion.FOURSQUARE_CONNECT_INTENT_CODE)
    }

    fun gotAuthResponse(managingActivity: Activity, responseCode: Int, responseData: Intent) {
        val codeResponse = FoursquareOAuth.getAuthCodeFromResult(responseCode, responseData)
        if (codeResponse.getException() == null) {
            val intent = FoursquareOAuth.getTokenExchangeIntent(managingActivity, managingActivity.getString(R.string.foursquare_app_id), managingActivity.getString(R.string.foursquare_app_secret), codeResponse.getCode())
            managingActivity.startActivityForResult(intent, FoursquareAuthManager.Companion.FOURSQUARE_TOKEN_EXCHG_INTENT_CODE)
        }
    }

    fun gotTokenExchangeResponse(managingActivity: Activity, responseCode: Int, responseData: Intent) {
        val tokenResponse = FoursquareOAuth.getTokenFromResult(responseCode, responseData)
        if (tokenResponse.getException() == null) {
            _userAccessToken = tokenResponse.getAccessToken()
            this.encryptAndStoreAuthToken(managingActivity, this._userAccessToken)
            fetchAndStoreUserId()
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
            val sharedPreferences = ctx.getSharedPreferences(FoursquareAuthManager.Companion.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(FoursquareAuthManager.Companion.SHARED_PREFS_4SQ_TOKEN_KEY, encryptedBase64Token)
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
            val sharedPref = ctx.getSharedPreferences(FoursquareAuthManager.Companion.SHARED_PREFS_NAME, Context.MODE_PRIVATE)
            val encryptedBase64Token = sharedPref.getString(FoursquareAuthManager.Companion.SHARED_PREFS_4SQ_TOKEN_KEY, "")
            val entity = Entity(CONCEAL_ENTITY_NAME)
            val encryptedToken = Base64.decode(encryptedBase64Token, Base64.DEFAULT)

            if (encryptedToken.size > 0) {
                return this.crypto.decrypt(encryptedToken, entity).toString(Charset.forName("UTF-8"))
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


    fun fetchAndStoreUserId() {
        val info = this.restAdapter.create<NLFoursquareUserInfo>(NLFoursquareUserInfo::class.java)
        val authParams = getClientAuthParameters(ctx)
        info.getInfo(authParams.authenticationParameters(), com.nlefler.nlfoursquare.Users.NLFoursquareUserInfo.UserIdSelf).enqueue(object : Callback<NLFoursquareResponse<NLFoursquareUserInfoResponse>> {
            override fun onResponse(call: Call<NLFoursquareResponse<NLFoursquareUserInfoResponse>>, response: Response<NLFoursquareResponse<NLFoursquareUserInfoResponse>>) {
                val userId = response.body().response.user.id
                if (userId == null || userId.isEmpty()) {
                    Log.e(FoursquareAuthManager.Companion.LOG_TAG, "Unable to get Foursquare user id")
                    return
                }

                userManager.saveFoursquareId(userId)
            }

            override fun onFailure(call: Call<NLFoursquareResponse<NLFoursquareUserInfoResponse>>, t: Throwable) {
                Log.e(FoursquareAuthManager.Companion.LOG_TAG, "Unable to get Foursquare user id")
                if (t.message != null) {
                    Log.e(FoursquareAuthManager.Companion.LOG_TAG, t.message)
                }
            }
        })

    }

    companion object {
        private val LOG_TAG = "FoursquareAuthManager"

        private val SHARED_PREFS_NAME = "com.nlefler.glucloser.a.foursquareprefs"
        private val SHARED_PREFS_4SQ_TOKEN_KEY = "com.nlefler.glucloser.a.4sqtkn"
        private val CONCEAL_ENTITY_NAME = "com.nlefler.glucloser.a.concealentity"

        val FOURSQUARE_CONNECT_INTENT_CODE: Int = 39228
        val FOURSQUARE_TOKEN_EXCHG_INTENT_CODE: Int = 39229
    }
}
