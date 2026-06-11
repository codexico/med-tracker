package com.franciscokahil.appMeusRemedinhos.ui.dashboard

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
    private val repository: EventRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

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
                isEnabled = true,
                icon = getClockEmoji(time)
            )
            repository.insertEvent(newEvent)
            scheduleEventAlarm(newEvent)
        }
    }

    fun updateEvent(event: EventEntity, newTitle: String, newTime: String) {
        viewModelScope.launch {
            val updatedEvent = event.copy(
                title = newTitle,
                time = newTime,
                icon = getClockEmoji(newTime)
            )
            repository.updateEvent(updatedEvent)
            if (updatedEvent.isEnabled) {
                scheduleEventAlarm(updatedEvent)
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(event.id)
            repository.deleteEvent(event)
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

    private fun getClockEmoji(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return "💊"
        val hour = parts[0].toIntOrNull() ?: return "💊"
        val minute = parts[1].toIntOrNull() ?: return "💊"
        
        val h12 = if (hour % 12 == 0) 12 else hour % 12
        val isHalf = minute >= 15 && minute < 45

        return when (h12) {
            1 -> if (isHalf) "🕜" else "🕐"
            2 -> if (isHalf) "🕝" else "🕑"
            3 -> if (isHalf) "🕞" else "🕒"
            4 -> if (isHalf) "🕟" else "🕓"
            5 -> if (isHalf) "🕠" else "🕔"
            6 -> if (isHalf) "🕡" else "🕕"
            7 -> if (isHalf) "🕢" else "🕖"
            8 -> if (isHalf) "🕣" else "🕗"
            9 -> if (isHalf) "🕤" else "🕘"
            10 -> if (isHalf) "🕥" else "🕙"
            11 -> if (isHalf) "🕦" else "🕚"
            12 -> if (isHalf) "🕧" else "🕛"
            else -> "💊"
        }
    }
}

class DashboardViewModelFactory(
    private val repository: EventRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
