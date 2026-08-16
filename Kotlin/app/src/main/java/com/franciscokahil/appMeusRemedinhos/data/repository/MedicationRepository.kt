package com.franciscokahil.appMeusRemedinhos.data.repository

import com.franciscokahil.appMeusRemedinhos.data.local.DoseHistoryDao
import com.franciscokahil.appMeusRemedinhos.data.local.DoseHistoryEntity
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface MedicationRepository {
    val allMedications: Flow<List<Medication>>
    val allHistory: Flow<List<DoseHistoryEntity>>
    
    suspend fun insertMedication(medication: Medication): String
    suspend fun updateMedication(medication: Medication)
    suspend fun deleteMedication(medication: Medication)
    suspend fun markAsTaken(eventId: String, medicationId: String, amount: Float, timestamp: Long)
    suspend fun markAsSkipped(eventId: String, medicationId: String, timestamp: Long)
    suspend fun unmarkAsTaken(eventId: String, startOfDay: Long)
    fun getDosesForEventToday(eventId: String, startOfDay: Long): Flow<List<DoseHistoryEntity>>
}

class MedicationRepositoryImpl(
    private val medicationDao: MedicationDao,
    private val doseHistoryDao: DoseHistoryDao,
) : MedicationRepository {
    private val mutex = Mutex()

    override val allMedications: Flow<List<Medication>> = medicationDao.getAllMedications()
    override val allHistory: Flow<List<DoseHistoryEntity>> = doseHistoryDao.getAllHistory()

    override suspend fun insertMedication(medication: Medication): String {
        return mutex.withLock {
            val existing = medicationDao.getMedicationByName(medication.name)
            if (existing != null) {
                // If it exists, we update the existing one with any new info but keep its ID
                // This ensures we link to the same entity
                medicationDao.updateMedication(medication.copy(id = existing.id))
                existing.id
            } else {
                medicationDao.insertMedication(medication)
                medication.id
            }
        }
    }

    override suspend fun updateMedication(medication: Medication) {
        medicationDao.updateMedication(medication)
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.deleteMedication(medication)
    }

    override suspend fun markAsTaken(eventId: String, medicationId: String, amount: Float, timestamp: Long) {
        mutex.withLock {
            val dose = DoseHistoryEntity(
                eventId = eventId,
                medicationId = medicationId,
                timestamp = timestamp,
                amountTaken = amount,
                status = "TAKEN"
            )
            doseHistoryDao.insertDose(dose)
            medicationDao.subtractFromStock(medicationId, amount)
        }
    }

    override suspend fun markAsSkipped(eventId: String, medicationId: String, timestamp: Long) {
        val dose = DoseHistoryEntity(
            eventId = eventId,
            medicationId = medicationId,
            timestamp = timestamp,
            amountTaken = 0f,
            status = "SKIPPED"
        )
        doseHistoryDao.insertDose(dose)
    }

    override suspend fun unmarkAsTaken(eventId: String, startOfDay: Long) {
        mutex.withLock {
            val dosesToday = doseHistoryDao.getDosesForEventTodaySync(eventId, startOfDay)
            dosesToday.forEach { dose ->
                if (dose.status == "TAKEN") {
                    medicationDao.updateStock(
                        dose.medicationId,
                        (medicationDao.getMedicationById(dose.medicationId)?.currentStock ?: 0f) + dose.amountTaken
                    )
                }
            }
            doseHistoryDao.deleteDosesForEventToday(eventId, startOfDay)
        }
    }

    override fun getDosesForEventToday(eventId: String, startOfDay: Long): Flow<List<DoseHistoryEntity>> {
        return doseHistoryDao.getDosesForEventToday(eventId, startOfDay)
    }
}
