package com.example.castnear

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProvider : FusedLocationProviderClient
    private lateinit var latitude : TextView
    private lateinit var longitude : TextView
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallBack : LocationCallback
    private lateinit var dbRef : DatabaseReference
    private var currLocation : Location? = null
    private var token : String = "Hellodfdhdh"
    private lateinit var id : String
    private lateinit var sharedPreferences : SharedPreferences
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        dbRef = FirebaseDatabase.getInstance().getReference("NearLoc")
        latitude = findViewById(R.id.latitude)
        longitude = findViewById(R.id.longitude)
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
                updateUI()
            }
        }
        getCurrentLocation()
    }

    private fun checkAndSaveUserId(context: Context) {
        sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        if (userId == null) {
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val currentTimeStr = currentTime.toString()
            sharedPreferences.edit().putString("user_id", currentTimeStr).apply()
            id = currentTimeStr
        }
        else {
            id = userId
        }
    }

    override fun onResume() {
        super.onResume()
        //getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if(checkPermission()) {
            if(isLocationEnabled()) {
                fusedLocationProvider.requestLocationUpdates(locationRequest,
                locationCallBack, Looper.getMainLooper())
                updateUI()
            }
            else {
                Toast.makeText(this, "Enable Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else {
            requestPermissions()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        //val id = dbRef.push().key.toString()
        checkAndSaveUserId(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                //Toast.makeText(this, "Failed to Fetch Firebase Token", Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }
            if(it.result == null) {
                Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show()
                token = "Helloadsddf"
            }
            else {
                token = it.result
            }
        }
        val loc = LocationModel(token, currLocation?.latitude.toString(), currLocation?.longitude.toString())
        dbRef.child(id).setValue(loc).addOnCompleteListener {
            Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show()
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
                getCurrentLocation()
            }
        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            this.finishAffinity()
        }
    }
}