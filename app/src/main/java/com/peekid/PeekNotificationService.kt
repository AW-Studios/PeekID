package com.peekid

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class PeekNotificationService : NotificationListenerService() {

    private val CHANNEL_ID = "peekid_channel"
    private val CHANNEL_NAME = "PeekID Notifications"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 1. If package is not whatsapp -> return (ignore)
        if (!NotificationHelper.isSupported(sbn.packageName)) return

        // 2. If it is a call, fullScreenIntent, ongoing, or transport -> return (ignore)
        if (sbn.isOngoing) return
        if (sbn.notification.category == Notification.CATEGORY_CALL) return
        if (sbn.notification.category == Notification.CATEGORY_TRANSPORT) return
        if (sbn.notification.fullScreenIntent != null) return

        // 3. If the notification is silent / group summary -> leave it completely alone in drawer
        val isGroupSummary = sbn.notification.flags and
            Notification.FLAG_GROUP_SUMMARY != 0
        if (isGroupSummary) {
            return
        }

        val extras = sbn.notification.extras

        // Extract sender name from EXTRA_TITLE
        val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return

        // 4. If it IS the heads-up popup notification -> cancel it and post ONE PeekID popup instead
        cancelNotification(sbn.key)

        val whatsappIcon = try {
            val pm = applicationContext.packageManager
            val drawable = pm.getApplicationIcon("com.whatsapp")
            (drawable as? BitmapDrawable)?.bitmap
                ?: drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(whatsappIcon)
            .setContentTitle(sender)
            .setContentText("sent you a message")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Forward click action so tapping opens the chat in WhatsApp
        sbn.notification.contentIntent?.let {
            builder.setContentIntent(it)
        }

        // Post exactly ONE PeekID popup
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 192
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 192
        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
