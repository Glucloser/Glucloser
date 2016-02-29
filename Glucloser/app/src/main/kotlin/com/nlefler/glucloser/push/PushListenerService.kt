package com.nlefler.glucloser.push

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.nlefler.glucloser.R
import com.nlefler.glucloser.activities.MainActivity

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

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        val pendingIntent = PendingIntent.getActivity(this,
                FoursquareCheckinNotificationIntentCode, intent,
                PendingIntent.FLAG_ONE_SHOT);

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;

        val notifId = Math.floor(Math.random() % Integer.MAX_VALUE).toInt()
        notificationManager.notify(notifId, notificationBuilder.build());
    }

    companion object {
        val FoursquareCheckinNotificationIntentCode = 4263
    }
}
