package com.franciscokahil.appMeusRemedinhos.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class StockWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val medications = database.medicationDao().getAllMedications().first()
        val notificationHelper = NotificationHelper(applicationContext)
        val title = applicationContext.getString(R.string.stock_banner_title)

        medications.forEach { medication ->
            val isLowStock = medication.lowStockThreshold > 0 &&
                medication.currentStock <= medication.lowStockThreshold
            if (isLowStock) {
                val message = applicationContext.getString(
                    R.string.stock_notification_single,
                    medication.name,
                )
                notificationHelper.showNotification(
                    title,
                    message,
                    NotificationHelper.NotificationType.STOCK,
                    notificationHelper.getStockNotificationId(medication.id),
                )
            } else {
                // Stock was replenished (or the alert was disabled) since the last check,
                // so any stale low-stock notification for this medication must go away.
                notificationHelper.cancelStockNotification(medication.id)
            }
        }

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "StockCheckWork"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StockWorker>(1, TimeUnit.DAYS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
