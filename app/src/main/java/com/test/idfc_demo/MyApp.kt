package com.test.idfc_demo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    companion object {
        const val CHANNEL_ID_ = "IDFC_GPS"
        const val CHANNEL_ID = "IDFC_GPS"
        const val CALL_SERVICE_ID_ = "GPS"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_,
                "IDFC Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            val serviceChannel_ = NotificationChannel(
                CHANNEL_ID_,
                CALL_SERVICE_ID_,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager_ = getSystemService(NotificationManager::class.java)
            manager_.createNotificationChannel(serviceChannel_)
        }
    }
}