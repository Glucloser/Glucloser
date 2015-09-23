package com.nlefler.glucloser.push

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.nlefler.glucloser.activities.LogBolusEventActivity

import com.nlefler.glucloser.activities.MainActivity
import com.nlefler.glucloser.models.BolusEventType
import com.parse.ParseAnalytics
import com.parse.ParsePushBroadcastReceiver

import org.json.JSONException
import org.json.JSONObject

/**
 * Created by Nathan Lefler on 12/29/14.
 */
public class PushReceiver : ParsePushBroadcastReceiver() {

    override fun onPushReceive(context: Context, intent: Intent) {
        super.onPushReceive(context, intent)
    }

    override fun onPushDismiss(context: Context?, intent: Intent?) {
        super.onPushDismiss(context, intent)
    }

    override fun onPushOpen(context: Context, intent: Intent) {
        //        super.onPushOpen(context, intent);
        Log.d(LOG_TAG, "Push opened")

        ParseAnalytics.trackAppOpenedInBackground(intent)

        var uriString: String? = null
        try {
            val pushData = JSONObject(intent.getStringExtra("com.parse.Data"))
            uriString = pushData.optString("uri")
        } catch (e: JSONException) {
            Log.e(LOG_TAG, "Unexpected JSONException when receiving push data: ", e)
        }

        val cls = getActivity(context, intent)
        val activityIntent: Intent
        if (uriString?.length() ?: 0 > 0) {
            activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString!!))
            activityIntent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeMeal.name())
        } else {
            activityIntent = Intent(context, MainActivity::class.java)
        }
        activityIntent.putExtras(intent.getExtras())
        if (Build.VERSION.SDK_INT >= 16) {
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(cls)
            stackBuilder.addNextIntent(activityIntent)
            stackBuilder.startActivities()
        } else {
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(activityIntent)
        }
    }

    companion object {
        private val LOG_TAG = "PushReceiver"
    }
}
