package com.test.idfc_demo

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.test.idfc_demo.MyApp.Companion.CHANNEL_ID_
import java.util.Locale

private const val TAG = "==>>IDFCMessagingService"
class MessagingService:FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "onMessageReceived: message :$message")
        message.data?.let {
            Log.d(TAG, "Message Data payload: " + message.data)
            handleDataPayload(it)
        }
    }

    private fun startCallService(body: String?, title: String?) {

        Log.d(TAG, "startCallService: ")

    }

    private fun handleDataPayload(it: Map<String, String>) {
        Log.d(TAG, "handleDataPayload() called with: it = $it")
        sendNotification(it["customername"], it["lat"],it["lon"])

    }

    @SuppressLint("ServiceCast")
    private fun sendNotification(name: String?, lat: String?, lon: String?) {
        val channelId = CHANNEL_ID_
        val notificationId = 0

        // Create an intent for the notification
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Create a notification channel for Android O and above
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "FCM Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your notification icon
            .setContentTitle(name ?: "FCM Notification")
            .setContentText((lat + " " + lon+"\n"+GetAddress.getAddressFromLatLng(this,lat!!.toDouble(),lon!!.toDouble())) ?: "You have received a new message.")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}


