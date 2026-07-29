package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

    val shouldShowOnboarding: StateFlow<Boolean> = events.map { it.isEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun toggleEventStatus(event: EventEntity, isTaken: Boolean) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(isTakenToday = isTaken))
        }
    }

    fun addEvent(label: String, time: String, icon: String? = null, medications: List<String> = emptyList()) {
        viewModelScope.launch {
            val newEvent = EventEntity(
                id = UUID.randomUUID().toString(),
                title = label,
                time = time,
                isEnabled = true,
                icon = icon ?: getClockEmoji(time),
                medications = medications
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

    fun getClockEmoji(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return "\uD83D\uDC8A"
        var hour = parts[0].toIntOrNull() ?: return "\uD83D\uDC8A"
        val minute = parts[1].toIntOrNull() ?: return "\uD83D\uDC8A"
        
        var isHalf = false
        if (minute in 15..44) {
            isHalf = true
        } else if (minute >= 45) {
            hour = (hour + 1) % 24
        }
        
        val h12 = if (hour % 12 == 0) 12 else hour % 12

        return when (h12) {
            1 -> if (isHalf) "\uD83D\uDD60" else "\uD83D\uDD50"
            2 -> if (isHalf) "\uD83D\uDD61" else "\uD83D\uDD51"
            3 -> if (isHalf) "\uD83D\uDD62" else "\uD83D\uDD52"
            4 -> if (isHalf) "\uD83D\uDD63" else "\uD83D\uDD53"
            5 -> if (isHalf) "\uD83D\uDD64" else "\uD83D\uDD54"
            6 -> if (isHalf) "\uD83D\uDD65" else "\uD83D\uDD55"
            7 -> if (isHalf) "\uD83D\uDD66" else "\uD83D\uDD56"
            8 -> if (isHalf) "\uD83D\uDD67" else "\uD83D\uDD57"
            9 -> if (isHalf) "\uD83D\uDD68" else "\uD83D\uDD58"
            10 -> if (isHalf) "\uD83D\uDD69" else "\uD83D\uDD59"
            11 -> if (isHalf) "\uD83D\uDD6A" else "\uD83D\uDD5A"
            12 -> if (isHalf) "\uD83D\uDD6B" else "\uD83D\uDD5B"
            else -> "\uD83D\uDC8A"
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
