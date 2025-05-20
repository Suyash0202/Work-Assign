package com.example.workassign.Api

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.workassign.MainActivity
import com.example.workassign.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NotificationService : FirebaseMessagingService() {
    private val channelId = "work_assign_channel"
    private val channelName = "Work Assign Notifications"

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "From: ${message.from}")
        Log.d("FCM", "Message Data: ${message.data}")
        Log.d("FCM", "Message Notification Body: ${message.notification?.body}")

        // If notification payload is present and app is in background, system handles it automatically
        // So, show notification only if app is in foreground
        if (message.notification != null) {
            if (isAppInForeground()) {
                showNotification(message)
            } else {
                Log.d("NotificationService", "App in background - system will show notification automatically")
            }
        } else if (message.data.isNotEmpty()) {
            // Data-only message, show notification manually
            showNotification(message)
        }
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Notification"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a new message"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "Permission POST_NOTIFICATIONS not granted. Cannot show notification.")
                return
            }
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)


        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notifi)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(null)

        // Use unique notification ID so notifications don’t override each other
        val notificationId = System.currentTimeMillis().toInt()
        NotificationManagerCompat.from(this).notify(notificationId, builder.build())
    }

    private fun createNotificationChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for work assignments"
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
}
