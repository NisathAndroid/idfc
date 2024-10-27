package com.test.idfc_demo

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.test.idfc_demo.MyApp.Companion.CHANNEL_ID_
import kotlinx.coroutines.DelicateCoroutinesApi

private const val TAG = "==>>IDFCPushNotify"
class IDFCPushNotify: NotificationListenerService() {
    private lateinit var updatedNotificationBuilder: NotificationCompat.Builder

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.d(TAG, "onListenerConnected: ")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        Log.d(TAG, "onListenerDisconnected: ")
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "onNotificationPosted: ")
      /*  val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        sbn?.let {
            val packageName = sbn.packageName
            val title = sbn.notification.extras.getString("android.title")
            val text = sbn.notification.extras.getString("android.text")
            val notificationId = sbn.id




           // notificationManager.cancelAll()

            val intent =
                Intent(this@IDFCPushNotify, MainActivity::class.java).apply {

                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

// Step 2: Wrap the intent in a PendingIntent
            val pendingIntent = PendingIntent.getActivity(
                this@IDFCPushNotify,
                0,  // Request code (can be any unique integer)
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE  // Use FLAG_IMMUTABLE for Android 12+
            )

            updatedNotificationBuilder =
                NotificationCompat.Builder(this@IDFCPushNotify, CHANNEL_ID_)
                    .setSmallIcon(R.drawable.emptylist)
                    .setContentTitle(title)  // Updated title
                    .setContentText("GPS location")
                    .setAutoCancel(true) // Updated text
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)

            notificationManager.notify(0, updatedNotificationBuilder.build())
        }*/



    }
}