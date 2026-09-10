package com.franciscokahil.appMeusRemedinhos.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.NotificationHelper
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: MedicationRepository,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    val medications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateStock(medicationId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateStock(medicationId, newQuantity)
            notificationHelper?.cancelStockNotification(medicationId)
        }
    }

    fun addStock(medicationId: Long, amountToAdd: Int) {
        viewModelScope.launch {
            repository.addStock(medicationId, amountToAdd)
            notificationHelper?.cancelStockNotification(medicationId)
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            repository.updateMedication(medication)
            notificationHelper?.cancelStockNotification(medication.id)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            repository.deleteMedication(medication)
            notificationHelper?.cancelStockNotification(medication.id)
        }
    }
}
