package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.NotificationHelper
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val medicationRepository: MedicationRepository,
    eventRepository: EventRepository,
    private val notificationHelper: NotificationHelper,
) : ViewModel() {

    val medications: StateFlow<List<MedicationStockUIModel>> = combine(
        medicationRepository.allMedications,
        eventRepository.allEvents
    ) { allMeds, allEvents ->
        allMeds.map { med ->
            // Calculate total daily consumption for this med
            var totalDailyDosage = 0f
            allEvents.forEach { eventWithMeds ->
                if (eventWithMeds.event.isEnabled) {
                    eventWithMeds.medications.forEach { medWithDosage ->
                        if (medWithDosage.medication.id == med.id) {
                            totalDailyDosage += medWithDosage.crossRef.dosageValue.toFloatOrNull() ?: 0f
                        }
                    }
                }
            }

            val days = if (totalDailyDosage > 0) {
                // Using floor to be safe on how many FULL days are left
                kotlin.math.floor(med.currentStock / totalDailyDosage).toInt()
            } else {
                null
            }

            MedicationStockUIModel(med, days, totalDailyDosage)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.updateMedication(medication)
            // Stock may have just been replenished; drop the stale low-stock alert for
            // THIS medication only. It's harmless to cancel when still low: the periodic
            // StockWorker check will re-post it if the medication remains under threshold.
            notificationHelper.cancelStockNotification(medication.id)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication)
            notificationHelper.cancelStockNotification(medication.id)
        }
    }
}

class InventoryViewModelFactory(
    private val medicationRepository: MedicationRepository,
    private val eventRepository: EventRepository,
    private val notificationHelper: NotificationHelper,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(medicationRepository, eventRepository, notificationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
