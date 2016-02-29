package com.nlefler.glucloser.push

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.nlefler.glucloser.GlucloserApplication
import com.nlefler.glucloser.R

/**
 * Created by nathan on 1/28/16.
 */
class PushRegistrationIntentService: IntentService("com.nlefler.glucloser.pushreceiver") {

    override fun onHandleIntent(intent: Intent) {
        val instanceId = InstanceID.getInstance(this)
        val token = instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)

        // TODO(nl) Save token to user on server
        val userManager = GlucloserApplication.sharedApplication?.rootComponent?.userManager()
        userManager?.savePushToken(token)
    }

}
