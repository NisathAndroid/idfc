package com.test.idfc_demo

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.permissionx.guolindev.PermissionX
import com.test.idfc_demo.MyApp.Companion.CHANNEL_ID_
import com.test.idfc_demo.MyApp.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.test.idfc_demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

private const val TAG = "==>>IDFCMainActivity"

class MainActivity : AppCompatActivity(), com.google.android.gms.location.LocationListener {
    private var token: String? = null
    private val REQUEST_CHECK_SETTINGS: Int = 100
    private var server: PushLocalServer? = null
    private lateinit var latLongAdapter: LatLanAdapter
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var viewbinding: ActivityMainBinding? = null
    private var list: MutableList<LatLanItems> = mutableListOf()
    private lateinit var dbHelper: DataBaseHelper
    private val _notifyEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val notifyEnabled: LiveData<Boolean> = _notifyEnabled
    private val _locationEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val locationEnabled: LiveData<Boolean> = _locationEnabled
    private val _dbEnabled: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    private val dbEnabled: LiveData<Boolean> = _dbEnabled
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var address: String = ""
    private fun createNotificationChannel(context: Context) {
        var notificationManager: NotificationManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Idfc gps monitor"
            val descriptionText = "This is the idfc default notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID_, name, importance).apply {
                description = descriptionText
                //  setSound(customRingtoneUri,null)
            }
            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }

        //setCustomNotificationUI(notificationManager)

    }

    private fun isNotificationServiceEnabled(): Boolean {
        val contentResolver = this.contentResolver
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledNotificationListeners != null && enabledNotificationListeners.contains(
            packageName
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewbinding?.root)
        checkPermissionIDFC()
        createNotificationChannel(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                token = task.result // This is the valid FCM token
                Log.d(TAG, token ?: "")
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
        dbHelper = DataBaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location: Location? = locationResult.lastLocation
                location?.let { loc ->
                    latitude = loc.latitude
                    longitude = loc.longitude
                    address = GetAddress.getAddressFromLatLng(
                        this@MainActivity,
                        latitude,
                        longitude
                    )

                    list.add(
                        LatLanItems(
                            latitude.toString(),
                            longitude.toString(),
                            address
                        )
                    )


                    viewbinding?.apply {
                        if (showNotificationSW.isChecked) {
                            _notifyEnabled.postValue(true)
                        }
                        if (dbClearSW.isChecked) {
                            _locationEnabled.postValue(true)
                        }
                    }

                }
            }
        }
        uiAction()
        //  startLocationUpdates()
        // server = PushLocalServer(this, 8082)
    }

    fun clearData() {
        list.clear()
        dbHelper.deleteAllData()
        latLongAdapter.updateLatLong(list)
        viewbinding?.apply {
            recyclerView.visibility = View.GONE
            emptyImage.visibility = View.VISIBLE
        }
    }

    fun addData(isAdded: Boolean) {
        if (isAdded) {
            list.forEach {
                val insertResult = dbHelper.insertData(it)
            }

            latLongAdapter.updateLatLong(list)
            viewbinding?.apply {
                if (list.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyImage.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyImage.visibility = View.GONE
                }

            }
        }
    }

    private fun uiAction() {
        viewbinding?.apply {
            showNotificationSW.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    if (!isNotificationServiceEnabled()) {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                    buttonView.setText("FCM ON")
                    startLocationUpdates()
                    _notifyEnabled.postValue(true)
                } else {
                    buttonView.setText("FCM OFF")
                    dbClearSW.isChecked = false
                    _notifyEnabled.postValue(false)
                }
            }
            dbClearSW.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    _dbEnabled.postValue(true)
                    buttonView.setText("add data")

                } else {
                    // startCountdownTimer()
                    _dbEnabled.postValue(false)
                    buttonView.setText("clear data")

                }
            }
            dbEnabled.observe(this@MainActivity) {
                if (it) {
                    startLocationUpdates()
                    //addData(true)
                } else {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    clearData()
                }
            }

            locationEnabled.observe(this@MainActivity) {
                addData(true)
            }
            notifyEnabled.observe(this@MainActivity) {
                Log.d(TAG, "uiAction: notifyEnabled = $it")
                if (it) {
                    val notificationData = NotificationData(
                        title = "IDFC First Bharath",
                        body = "Customer GPS Monitoring",
                        customername = "Nisath",
                        location = address,
                        lat = latitude.toString(),
                        lon = longitude.toString()
                    )
                    val pushNotification = ApiResponse(
                        token = token!!,
                        // token = "c4mQeCS7Qla8U_Bxqa3FpC:APA91bEqUm3XaexYnxnA6aygEtQVdeiUaJ-EIEFSuMLpWzCNvGgHUcpgexbNyvJlFQYvTL1bwDw00bSFAcRIXgo5xIAujR9-Xctt_zyvsHut2L_qDH2agEHoMJXNR_ij-jP4rJhJwucN",
                        data = notificationData
                    )
                    sendPushNotification(pushNotification)
                } else {
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                    val updatedNotificationBuilder: NotificationCompat.Builder
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancelAll()

                    val intent =
                        Intent(this@MainActivity, MainActivity::class.java).apply {

                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                    val pendingIntent = PendingIntent.getActivity(
                        this@MainActivity,
                        0,  // Request code (can be any unique integer)
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE  // Use FLAG_IMMUTABLE for Android 12+
                    )

                    updatedNotificationBuilder =
                        NotificationCompat.Builder(this@MainActivity, CHANNEL_ID_)
                            .setSmallIcon(R.drawable.emptylist)
                            .setContentTitle("IDFC")  // Updated title
                            .setContentText("GPS location")
                            .setAutoCancel(true) // Updated text
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)

                    notificationManager.notify(0, updatedNotificationBuilder.build())
                }
            }
            gpsLiveDataBTN?.setOnClickListener {
                try {
                    //server?.start()
                    list = dbHelper.getAllData()
                    latLongAdapter.updateLatLong(list)
                    viewbinding?.apply {
                        if (list.isEmpty()) {
                            recyclerView.visibility = View.GONE
                            emptyImage.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            emptyImage.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Failed to start server", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            latLongAdapter = LatLanAdapter(list)
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = latLongAdapter
        }
    }

    private fun showNotification(checked: Boolean) {

    }

    private fun sendPushNotification(notification: ApiResponse) {
        //  lifecycleScope.launch(Dispatchers.IO){
        val call = RetrofitInstance.api.sendNotification(notification)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Notification sent successfully! ${response.body()?.token}")
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e(TAG, "Failed to send notification", t)
            }
        })
        //  }


    }


    fun startCountdownTimer() {

        val timer = object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                Log.d(TAG, "onTick: $millisUntilFinished")
            }

            override fun onFinish() {

                list.clear()
                dbHelper.deleteAllData()
                latLongAdapter.updateLatLong(list)
                viewbinding?.apply {
                    recyclerView.visibility = View.GONE
                    emptyImage.visibility = View.VISIBLE
                }
                Log.d(TAG, "onFinish: " + list)
            }
        }

        // Start the countdown timer
        timer.start()
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
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }


    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the activity is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // server?.stop()
    }


    fun promptEnableGPS() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { response ->
            // GPS is already enabled, no need to prompt
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    // Handle the result in onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                // GPS enabled
                Log.d(TAG, "onActivityResult: " + resultCode)
            } else {
                // GPS not enabled
                Log.d(TAG, "onActivityResult: GPS not enabled " + resultCode)
            }
        }
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
                        promptEnableGPS()
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
        Log.d(TAG, "onLocationChanged: " + p0)
    }


}