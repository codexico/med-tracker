package com.franciscokahil.appMeusRemedinhos.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.franciscokahil.appMeusRemedinhos.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val EVENTS_CHANNEL_ID = "medication_events_channel"
        const val STOCK_CHANNEL_ID = "medication_stock_channel"
    }

    enum class NotificationType(
        val channelId: String,
        val importance: Int,
        val priority: Int,
        val nameRes: Int,
        val descriptionRes: Int,
    ) {
        EVENTS(
            EVENTS_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH,
            NotificationCompat.PRIORITY_HIGH,
            R.string.notification_events_channel_name,
            R.string.notification_events_channel_desc,
        ),
        STOCK(
            STOCK_CHANNEL_ID,
            NotificationManager.IMPORTANCE_LOW,
            NotificationCompat.PRIORITY_LOW,
            R.string.notification_stock_channel_name,
            R.string.notification_stock_channel_desc,
        ),
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            NotificationType.entries.forEach { type ->
                val channel = NotificationChannel(
                    type.channelId,
                    context.getString(type.nameRes),
                    type.importance,
                ).apply {
                    description = context.getString(type.descriptionRes)
                    enableLights(true)
                    enableVibration(type == NotificationType.EVENTS)
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    // Kept for callers that only need to initialize notifications.
    fun createNotificationChannel() {
        createNotificationChannels()
    }

    fun showNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.EVENTS,
    ) {
        createNotificationChannels()

        // The channel controls importance on Android 8+; priority covers older versions.
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, type.channelId)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(type.priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
