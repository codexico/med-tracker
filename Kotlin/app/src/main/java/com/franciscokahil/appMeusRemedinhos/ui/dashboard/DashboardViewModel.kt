package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class DashboardViewModel(
    application: Application,
    private val repository: EventRepository
) : AndroidViewModel(application) {

    private val alarmScheduler = AlarmScheduler(application)

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

    fun toggleEventEnabled(event: EventEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            val updatedEvent = event.copy(isEnabled = isEnabled)
            repository.updateEvent(updatedEvent)
            if (isEnabled) {
                scheduleEventAlarm(updatedEvent)
            } else {
                alarmScheduler.cancelAlarm(updatedEvent.id)
            }
        }
    }

    fun addEvent(label: String, time: String) {
        viewModelScope.launch {
            val newEvent = EventEntity(
                id = UUID.randomUUID().toString(),
                title = label,
                time = time,
                isEnabled = true
            )
            repository.insertEvent(newEvent)
            scheduleEventAlarm(newEvent)
        }
    }

    fun addMedication(eventId: String, medicationName: String) {
        viewModelScope.launch {
            val event = events.value.find { it.id == eventId }
            event?.let {
                val updatedMeds = it.medications.toMutableList().apply { add(medicationName) }
                val updatedEvent = it.copy(medications = updatedMeds)
                repository.updateEvent(updatedEvent)
                if (updatedEvent.isEnabled) {
                    scheduleEventAlarm(updatedEvent)
                }
            }
        }
    }

    fun removeMedication(eventId: String, index: Int) {
        viewModelScope.launch {
            val event = events.value.find { it.id == eventId }
            event?.let {
                val updatedMeds = it.medications.toMutableList().apply { 
                    if (index in indices) removeAt(index) 
                }
                val updatedEvent = it.copy(medications = updatedMeds)
                repository.updateEvent(updatedEvent)
                if (updatedEvent.isEnabled) {
                    scheduleEventAlarm(updatedEvent)
                }
            }
        }
    }

    private fun scheduleEventAlarm(event: EventEntity) {
        val parts = event.time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return
            val message = if (event.medications.isEmpty()) {
                "Não se esqueça da sua medicação!"
            } else {
                "Remédios: ${event.medications.joinToString(", ")}"
            }
            alarmScheduler.scheduleAlarm(
                event.id,
                event.title,
                message,
                hour,
                minute
            )
        }
    }
}

class DashboardViewModelFactory(
    private val application: Application,
    private val repository: EventRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
