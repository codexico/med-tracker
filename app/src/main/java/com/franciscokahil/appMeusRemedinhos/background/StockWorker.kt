package com.franciscokahil.appMeusRemedinhos.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository

class StockWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MedicationRepository(database.medicationDao())
        val notificationHelper = NotificationHelper(applicationContext)

        val medications = repository.getAllMedicationsSync()
        for (medication in medications) {
            if (medication.quantity <= medication.minQuantity) {
                notificationHelper.showLowStockNotification(medication.id, medication.name, medication.quantity)
            } else {
                notificationHelper.cancelStockNotification(medication.id)
            }
        }
        return Result.success()
    }
}
