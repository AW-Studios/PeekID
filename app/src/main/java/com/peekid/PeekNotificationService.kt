package com.peekid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class PeekNotificationService : NotificationListenerService() {

    private val CHANNEL_ID = "peekid_channel"
    private val CHANNEL_NAME = "PeekID Notifications"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Only intercept WhatsApp and WhatsApp Business
        if (!NotificationHelper.isSupported(sbn.packageName)) return

        val extras = sbn.notification.extras

        // Extract sender name from EXTRA_TITLE
        val sender = extras.getString(Notification.EXTRA_TITLE) ?: return

        // Skip group summary notifications (they have no real sender)
        val isGroupSummary = sbn.notification.flags and
            Notification.FLAG_GROUP_SUMMARY != 0
        if (isGroupSummary) return

        // Cancel the original notification
        cancelNotification(sbn.key)

        // Re-post sanitized version
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val sanitized = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(sender)
            .setContentText("sent you a message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(sbn.id, sanitized)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // No action needed
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Re-request binding on Android 7.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(
                ComponentName(this, PeekNotificationService::class.java)
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Privacy-filtered WhatsApp notifications"
                enableVibration(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
