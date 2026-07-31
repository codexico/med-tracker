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
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val medications = database.medicationDao().getAllMedications().first()
        
        val lowStockMeds = medications.filter { 
            it.currentStock <= it.lowStockThreshold && it.lowStockThreshold > 0 
        }

        if (lowStockMeds.isNotEmpty()) {
            val notificationHelper = NotificationHelper(applicationContext)
            val title = applicationContext.getString(R.string.stock_banner_title)
            val message = if (lowStockMeds.size == 1) {
                applicationContext.getString(R.string.stock_notification_single, lowStockMeds[0].name)
            } else {
                applicationContext.getString(R.string.stock_notification_multiple, lowStockMeds.size)
            }
            notificationHelper.showNotification(title, message)
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
