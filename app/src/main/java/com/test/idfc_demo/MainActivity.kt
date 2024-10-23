package com.test.idfc_demo

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.permissionx.guolindev.PermissionX
import com.test.idfc_demo.databinding.ActivityMainBinding

private const val TAG = "==>>MainActivity"
class MainActivity : AppCompatActivity(),com.google.android.gms.location.LocationListener  {
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_CODE=1111
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    private var viewbinding :ActivityMainBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewbinding?.root)

        viewbinding?.apply {
            gpsLiveDataBTN?.setOnClickListener {
                checkPermissionIDFC()
            }
        }


        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                location?.let {
                  //  updateLocationUI(it)
                    Log.d(TAG, "onLocationResult: $it")
                }
            }
        }

        // Request Location Updates
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)  // 5 seconds
            setWaitForAccurateLocation(true)   // Wait for accurate location when required
        }.build()

        // Check if permission is granted
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }



    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the activity is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun checkPermissionIDFC() {
        val permissionManifest =
            mutableListOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            permissionManifest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                permissionManifest.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)


            PermissionX.init(this)

                .permissions(permissionManifest)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG)
                            .show()
                        //getLocation()
                    } else {
                        Toast.makeText(
                            this,
                            "These permissions are denied: $deniedList",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onLocationChanged(p0: Location) {
        Log.d(TAG, "onLocationChanged: "+p0)
    }

 
}