package com.franciscokahil.appMeusRemedinhos.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.franciscokahil.appMeusRemedinhos.MainActivity
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.Medication

open class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_DOSE = "dose_channel"
        const val CHANNEL_STOCK = "stock_channel"
        const val STOCK_NOTIFICATION_BASE_ID = 10000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val doseChannel = NotificationChannel(
                CHANNEL_DOSE,
                "Lembretes de Doses",
                NotificationManager.IMPORTANCE_HIGH
            )
            val stockChannel = NotificationChannel(
                CHANNEL_STOCK,
                "Alertas de Estoque",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(doseChannel)
            notificationManager.createNotificationChannel(stockChannel)
        }
    }

    open fun getStockNotificationId(medicationId: Long): Int {
        return (STOCK_NOTIFICATION_BASE_ID + medicationId).toInt()
    }

    open fun showLowStockNotification(medicationId: Long, medicationName: String, remainingQuantity: Int) {
        val notificationId = getStockNotificationId(medicationId)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "inventory")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STOCK)
            .setSmallIcon(R.drawable.ic_notification_pill)
            .setContentTitle("Estoque Baixo: $medicationName")
            .setContentText("Resta(m) apenas $remainingQuantity unidade(s).")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    open fun showLowStockNotification(medication: Medication) {
        showLowStockNotification(medication.id, medication.name, medication.quantity)
    }

    open fun cancelStockNotification(medicationId: Long) {
        val notificationId = getStockNotificationId(medicationId)
        notificationManager.cancel(notificationId)
    }
}
