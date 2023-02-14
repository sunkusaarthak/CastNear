package com.example.castnear

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
        val location : Location? = intent.getParcelableExtra<Location>("location")
        dbRef = FirebaseDatabase.getInstance().getReference("NearLoc")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for(ds in dataSnapshot.children) {
                    //userList.add(ds.child("id").toString())
                    val lat = ds.child("latitude").toString().toDouble()
                    val lon = ds.child("longitude").toString().toDouble()

                    val currLat = location?.latitude.toString().toDouble()
                    val currLon = location?.longitude.toString().toDouble()
                    val disKm = distance(currLat, currLon, lat, lon)
                    if(disKm <= 3) {
                        userList.add(ds.child("id").toString())
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
    }
}