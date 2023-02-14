package com.example.castnear

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.firebase.database.*
import java.lang.Math.*
import kotlin.math.pow

@Suppress("DEPRECATION")
class BroadCastMessage : AppCompatActivity() {

    private lateinit var dbRef : DatabaseReference
    private lateinit var userList : ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_broad_cast_message)
        supportActionBar?.hide()
        val location : Location? = intent.getParcelableExtra<Location>("location")
        dbRef = FirebaseDatabase.getInstance().getReference("NearLoc")
        userList = ArrayList()
        findViewById<Button>(R.id.send).setOnClickListener {
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for(ds in dataSnapshot.children) {
                        //userList.add(ds.child("id").toString())
                        val lat : String = ds.child("latitude").value.toString()
                        val lon : String = ds.child("longitude").value.toString()
                        val latitude : Double = lat.toDouble()
                        val longitude : Double = lon.toDouble()
                        val currLat = location?.latitude
                        val currLon = location?.longitude
                        val disKm = distance(currLat!!, currLon!!, latitude, longitude)
                        if(disKm <= 3) {
                            userList.add(ds.child("id").value.toString())
                        }
                    }

                    val data = Data("Hi There!", "This is just for Fun Beta!")
                    sendNotificationToMultipleUsers(userList, data)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w("TAG", "Failed to read value.", error.toException())
                }
            })
        }
    }

    fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val lat1Rad = toRadians(lat1)
        val lat2Rad = toRadians(lat2)
        val deltaLatRad = toRadians(lat2 - lat1)
        val deltaLonRad = toRadians(lon2 - lon1)
        val a = kotlin.math.sin(deltaLatRad / 2.0).pow(2.0) + (kotlin.math.cos(lat1Rad) * kotlin.math.cos(
            lat2Rad
        ) * kotlin.math.sin(
            deltaLonRad / 2.0
        ).pow(2.0))
        val c = 2 * kotlin.math.asin(kotlin.math.sqrt(a))
        return r * c
    }

    fun sendNotificationToMultipleUsers(tokens: ArrayList<String>, data : Data) {
        for (token in tokens) {
            FirebaseNotificationSender(
                getString(R.string.Key),
                token,
                data.title,
                data.message,
                applicationContext,
                this
            ).sendNotifications()
        }
        Toast.makeText(this, "Notification Send Successfully", Toast.LENGTH_SHORT).show()
    }
}