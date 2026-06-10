package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(private val repository: EventRepository) : ViewModel() {
    val events: StateFlow<List<EventEntity>> = repository.allEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleEventStatus(event: EventEntity, isTaken: Boolean) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isTakenToday = isTaken))
        }
    }

    fun addEvent(label: String, time: String) {
        viewModelScope.launch {
            val newEvent = EventEntity(
                id = UUID.randomUUID().toString(),
                title = label,
                time = time
            )
            repository.insertEvent(newEvent)
        }
    }

    fun addMedication(eventId: String, medicationName: String) {
        viewModelScope.launch {
            val currentEvents = events.value
            val event = currentEvents.find { it.id == eventId }
            event?.let {
                val updatedMeds = it.medications.toMutableList().apply { add(medicationName) }
                repository.updateEvent(it.copy(medications = updatedMeds))
            }
        }
    }

    fun removeMedication(eventId: String, index: Int) {
        viewModelScope.launch {
            val currentEvents = events.value
            val event = currentEvents.find { it.id == eventId }
            event?.let {
                val updatedMeds = it.medications.toMutableList().apply { 
                    if (index in indices) removeAt(index) 
                }
                repository.updateEvent(it.copy(medications = updatedMeds))
            }
        }
    }
}

class DashboardViewModelFactory(private val repository: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
