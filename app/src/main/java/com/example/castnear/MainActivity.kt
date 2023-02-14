package com.example.castnear

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsStatusCodes.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.view.View
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProvider : FusedLocationProviderClient
    private lateinit var latitude : TextView
    private lateinit var longitude : TextView
    //private lateinit var progressBar: ProgressBar
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallBack : LocationCallback
    private lateinit var dbRef : DatabaseReference
    private var currLocation : Location? = null
    private var token : String = "Hellodfdhdh"
    private lateinit var id : String
    private lateinit var sharedPreferences : SharedPreferences
    private var once  = false
    private var inOnActivityResult = false
    private var inOnResume = false
    private var inOnCreate = false
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        FirebaseApp.initializeApp(this)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        dbRef = FirebaseDatabase.getInstance().getReference("NearLoc")
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
        //progressBar = findViewById(R.id.progressBar)
        locationRequest = LocationRequest.create().apply{
            interval = TimeUnit.SECONDS.toMillis(60)
            fastestInterval = TimeUnit.SECONDS.toMillis(30)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                currLocation = p0.lastLocation
/*                Thread{
                    updateUI()
                }.start()*/
                updateUI()
            }
        }
        Thread {
            handleLocationSubRoutine()
        }.start()
        findViewById<Button>(R.id.notify).setOnClickListener {
            val intent = Intent(applicationContext, BroadCastMessage::class.java)
            intent.putExtra("location", currLocation)
            startActivity(intent)
        }
    }

    private fun handleLocationSubRoutine() {
        if(!once) {
            once = true
            enableLocation()
        }
    }

    private fun checkAndSaveUserId(context: Context) {
        sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        id = if (userId == null) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val currentTimeStr = currentTime.toString()
            sharedPreferences.edit().putString("user_id", currentTimeStr).apply()
            currentTimeStr
        } else {
            userId
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionOverAll()
    }

    private fun checkPermissionOverAll() : Boolean {
        if(checkPermission()) {
            if(isLocationEnabled()) {
                return(true)
            }
            else {
                if(!once) {
                    once = true
                    enableLocation()
                }
            }
        }
        else {
            requestPermissions()
        }
        return(false)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions()
            return
        }
        fusedLocationProvider.requestLocationUpdates(locationRequest,
            locationCallBack, Looper.getMainLooper())
/*
        Thread{
            updateUI()
        }.start()
*/
        updateUI()
    }

    private fun enableLocation() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener {
                if(it!!.locationSettingsStates!!.isLocationPresent) {
                    getCurrentLocation()
                }
            }.addOnFailureListener{
                val statusCode = (it as ResolvableApiException).statusCode
                if(statusCode == RESOLUTION_REQUIRED) {
                    try{
                        it.startResolutionForResult(this, 1101)
                    } catch (sendEx : IntentSender.SendIntentException) { }
                }
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1101) {
            if(resultCode == RESULT_OK) {
                getCurrentLocation()
            }
            else {
                finishAffinity()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        //val id = dbRef.push().key.toString()
        if(currLocation?.latitude == null) {
           //Thread.sleep(30000)
        }
        checkAndSaveUserId(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                //Toast.makeText(this, "Failed to Fetch Firebase Token", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }
            token = if(it.result == null) {
                Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show()
                "Helloadsddf"
            } else {
                it.result
            }
        }
        val loc = LocationModel(token, currLocation?.latitude.toString(), currLocation?.longitude.toString())
        dbRef.child(id).setValue(loc).addOnCompleteListener {
            //Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Data Not Updated", Toast.LENGTH_SHORT).show()
        }
        latitude.text = "Latitude: ${currLocation?.latitude}"
        longitude.text = "Longitude: ${currLocation?.longitude}"
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION_CODE)
    }

    private fun checkPermission() : Boolean {
        if(ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            return(true)
        }
        return(false)
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    companion object {
        private const val PERMISSION_REQUEST_LOCATION_CODE = 100
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_LOCATION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                once = false
                handleLocationSubRoutine()
            }
        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            this.finishAffinity()
        }
    }
}