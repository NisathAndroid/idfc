package com.test.idfc_demo

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat

private const val TAG = "==>>IDFCLocationServices"
class IDFCLocationServices : Service(), LocationListener {
    companion object {
        private const val locationChannelId = "locationChannelId"
        private const val channelName = "locationName"
        private const val minTimeLocationUpdateInMillisecond = 10000L
        private const val minDistanceLocationUpdateInMeter = 1000F
    }

    override fun onCreate() {
        //notificationService()
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!checkIfLocationPermissionIsGrande()) return START_NOT_STICKY

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findTheLocationForSdkBiggerThan30(locationManager = locationManager)
        } else {
            findTheLocationForSdkLowerThe31(locationManager = locationManager)
        }
        return START_STICKY
    }
    private fun findTheLocationForSdkLowerThe31(locationManager: LocationManager) {
        /**
         * this code is deprecated from the SDK 34 but we need it for lower than SDK 34
         * */
        Criteria().apply {
            accuracy = Criteria.ACCURACY_COARSE
            powerRequirement =
                Criteria.POWER_LOW

            val provider = locationManager.getBestProvider(
                this,
                false
            )
            if (provider != null) {
                if (ActivityCompat.checkSelfPermission(
                        this@IDFCLocationServices,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@IDFCLocationServices,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return
                }
                locationManager.requestLocationUpdates(
                    provider,
                    minTimeLocationUpdateInMillisecond,
                    minDistanceLocationUpdateInMeter,
                    this@IDFCLocationServices
                )
            }
        }
    }
    private fun checkIfLocationPermissionIsGrande() = ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged: ")
    }
    private fun findTheLocationForSdkBiggerThan30(locationManager: LocationManager) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER,
            minTimeLocationUpdateInMillisecond,
            minDistanceLocationUpdateInMeter,
            this@IDFCLocationServices
        )
    }
}