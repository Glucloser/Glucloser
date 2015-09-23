package com.nlefler.glucloser.foursquare

import android.util.Log

import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.nlfoursquare.Common.NLFoursquareEndpoint
import com.nlefler.nlfoursquare.Model.FoursquareResponse.NLFoursquareResponse
import com.nlefler.nlfoursquare.Model.NLFoursquareClientParameters
import com.nlefler.nlfoursquare.Model.User.NLFoursquareUserInfoResponse
import com.nlefler.nlfoursquare.Users.NLFoursquareUserInfo
import com.parse.ParseInstallation

import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import retrofit.client.Response

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public class FoursquareUserHelper {

    private val restAdapter: RestAdapter

    init {
        this.restAdapter = RestAdapter.Builder().setEndpoint(NLFoursquareEndpoint.NLFOURSQUARE_V2_ENDPOINT).build()
    }

    public fun fetchAndStoreUserId() {
        val info = this.restAdapter.create<NLFoursquareUserInfo>(NLFoursquareUserInfo::class.java)
        val authParams = FoursquareAuthManager.SharedManager().getClientAuthParameters(GlucloserApplication.SharedApplication().getApplicationContext())
        info.getInfo(authParams.authenticationParameters(), NLFoursquareUserInfo.UserIdSelf, object : Callback<NLFoursquareResponse<NLFoursquareUserInfoResponse>> {
            override fun success(nlFoursquareUserInfoResponseNLFoursquareResponse: NLFoursquareResponse<NLFoursquareUserInfoResponse>, response: Response) {
                val userId = nlFoursquareUserInfoResponseNLFoursquareResponse.response.user.id
                if (userId == null || userId.isEmpty()) {
                    Log.e(LOG_TAG, "Unable to get Foursquare user id")
                    return
                }
                val installation = ParseInstallation.getCurrentInstallation()
                installation.put(PARSE_INSTALLATION_FOURSQUARE_USER_ID_KEY, userId)
                installation.saveInBackground()
            }

            override fun failure(error: RetrofitError) {
                Log.e(LOG_TAG, "Unable to get Foursquare user id")
                Log.e(LOG_TAG, error.getBody().toString())
            }
        })

    }

    companion object {
        private val LOG_TAG = "FoursquareUserHelper"

        private val PARSE_INSTALLATION_FOURSQUARE_USER_ID_KEY = "foursquareUserId"
    }
}
