package com.nlefler.glucloser.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.nlefler.glucloser.R
import com.nlefler.glucloser.activities.LogBolusEventActivity
import com.nlefler.glucloser.activities.MainActivity
import com.nlefler.glucloser.models.BolusEventType
import com.nlefler.glucloser.ui.BolusEventDetailsFragment

/**
 * Created by nathan on 1/28/16.
 */
class PushListenerService: GcmListenerService() {

    override fun onMessageReceived(channelStr: String, data: Bundle) {
        // TODO(nl) Check if foursquare checkin message
        showNotification(data)
    }

    private fun showNotification(data: Bundle) {
        val title = data.getString("gcm.notification.title")
        val message = data.getString("gcm.notification.body")
        val foursquareId = data.getString("gcm.data.foursquareId")


        val intent = Intent(this, LogBolusEventActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(LogBolusEventActivity.BolusEventTypeKey, BolusEventType.BolusEventTypeMeal.name)
        intent.putExtra(BolusEventDetailsFragment.BolusEventDetailPlaceNameBundleKey, )

        val stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(LogBolusEventActivity::class.java)
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(intent);
        // Gets a PendingIntent containing the entire back stack
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        val notifId = Math.floor(Math.random() % Integer.MAX_VALUE).toInt()
        notificationManager.notify(notifId, notificationBuilder.build());
    }

    companion object {
        val FoursquareCheckinNotificationIntentCode = 4263
    }
}
