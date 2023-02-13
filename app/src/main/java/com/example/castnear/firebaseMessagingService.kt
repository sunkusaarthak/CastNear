package com.example.castnear

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

@Suppress("DEPRECATION")
class firebaseMessagingService: FirebaseMessagingService() {
    private lateinit var notificationManager: NotificationManager
    val CHANNEL_ID : String = "PUSH_NOTIFICATION"
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        pushNotification(message.notification?.title, message.notification?.body)
    }

    private fun pushNotification(title: String?, body: String?) {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, AfterNotifyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this,
            0, intent, PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Custom Channel"
            val descriptionText = "Channel for Push Notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1, builder)
        }
        else {
            val builder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1, builder)
        }

    }


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("Token", token)
    }
}