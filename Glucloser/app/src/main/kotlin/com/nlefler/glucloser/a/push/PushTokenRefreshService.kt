package com.nlefler.glucloser.a.push

import android.content.Intent
import com.google.android.gms.iid.InstanceIDListenerService

/**
 * Created by nathan on 1/28/16.
 */
class PushTokenRefreshService: InstanceIDListenerService() {

    override fun onTokenRefresh() {
        val intent = Intent(this, PushTokenRefreshService::class.java)
        startService(intent)
    }
}
