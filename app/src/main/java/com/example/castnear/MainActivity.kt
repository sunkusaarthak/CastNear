package com.example.castnear

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationProvider : FusedLocationProviderClient
    private lateinit var latitude : TextView
    private lateinit var longitude : TextView
    private lateinit var locationRequest : LocationRequest
    private lateinit var locationCallBack : LocationCallback
    private var currLocation : Location? = null
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
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
                getCurrentLocation()
            }
        }
        getCurrentLocation()
    }

    override fun onResume() {
        super.onResume()
        getCurrentLocation()
    }
    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation() {
        if(checkPermission()) {
            if(isLocationEnabled()) {
                /*fusedLocationProvider.lastLocation.addOnCompleteListener {
                    val location : Location = it.result*/
                fusedLocationProvider.requestLocationUpdates(locationRequest,
                locationCallBack, Looper.getMainLooper())
                latitude.text = "Latitude: ${currLocation?.latitude}"
                longitude.text = "Longitude: ${currLocation?.longitude}"
                //}

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