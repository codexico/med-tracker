package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class DashboardViewModel(
    private val eventRepository: EventRepository,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val ticker: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1.minutes) // Refresh every minute
        }
    }

    val events: StateFlow<List<DashboardEventUIModel>> = combine(
        eventRepository.allEvents,
        medicationRepository.allHistory,
        ticker,
    ) { allEvents, history, currentTime ->
        val todayStart = getStartOfDay(currentTime)

        allEvents.map { eventWithMeds ->
            val isTaken = history.any { 
                (it.eventId == eventWithMeds.event.id) && 
                (it.timestamp >= todayStart) && 
                (it.status == "TAKEN") 
            }
            DashboardEventUIModel(eventWithMeds, isTaken)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val pendingEvents: StateFlow<List<EventWithMedications>> = combine(
        eventRepository.allEvents,
        medicationRepository.allHistory,
        ticker
    ) { allEvents, history, currentTime ->
        val todayStart = getStartOfDay(currentTime)
        val yesterdayStart = getStartOfDay(currentTime - (24 * 60 * 60 * 1000))

        allEvents.filter { eventWithMeds ->
            val event = eventWithMeds.event
            // Event created before today
            val isOldEvent = event.createdAt < todayStart
            
            // Check if it was NOT handled (TAKEN or SKIPPED) since yesterday
            val isHandled = history.any { 
                it.eventId == event.id && it.timestamp >= yesterdayStart 
            }
            
            isOldEvent && !isHandled && event.isEnabled
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shouldShowOnboarding: StateFlow<Boolean> = events
        .map { it.isEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true,
        )

    val allMedications: StateFlow<List<Medication>> = medicationRepository.allMedications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getStartOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun toggleEventStatus(eventWithMeds: EventWithMedications, isTaken: Boolean) {
        viewModelScope.launch {
            if (isTaken) {
                if (eventWithMeds.medications.isEmpty()) {
                    // Even if there are no medications, we record the event as taken
                    medicationRepository.markAsTaken(
                        eventWithMeds.event.id,
                        "", // No specific medication
                        0f,
                        System.currentTimeMillis()
                    )
                } else {
                    eventWithMeds.medications.forEach { medWithDosage ->
                        medicationRepository.markAsTaken(
                            eventWithMeds.event.id,
                            medWithDosage.medication.id,
                            medWithDosage.crossRef.dosageValue.toFloatOrNull() ?: 0f,
                            System.currentTimeMillis()
                        )
                    }
                }
            } else {
                medicationRepository.unmarkAsTaken(eventWithMeds.event.id, getStartOfDay(System.currentTimeMillis()))
            }
        }
    }

    fun markAsTakenRetrospectively(eventWithMeds: EventWithMedications) {
        viewModelScope.launch {
            if (eventWithMeds.medications.isEmpty()) {
                medicationRepository.markAsTaken(
                    eventWithMeds.event.id,
                    "",
                    0f,
                    System.currentTimeMillis()
                )
            } else {
                eventWithMeds.medications.forEach { medWithDosage ->
                    medicationRepository.markAsTaken(
                        eventWithMeds.event.id,
                        medWithDosage.medication.id,
                        medWithDosage.crossRef.dosageValue.toFloatOrNull() ?: 0f,
                        System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun markAsSkippedRetrospectively(eventWithMeds: EventWithMedications) {
        viewModelScope.launch {
            eventWithMeds.medications.forEach { medWithDosage ->
                medicationRepository.markAsSkipped(
                    eventWithMeds.event.id,
                    medWithDosage.medication.id,
                    System.currentTimeMillis()
                )
            }
        }
    }

    fun addEvent(label: String, time: String, icon: String? = null, medications: List<Medication> = emptyList(), type: EventType = EventType.OTHER) {
        viewModelScope.launch {
            safeLogD("DashboardViewModel", "addEvent: title=$label, time=$time")
            val finalIcon = if (icon == "⏰") getClockEmoji(time) else icon ?: getClockEmoji(time)
            val eventId = UUID.randomUUID().toString()
            val newEvent = EventEntity(
                id = eventId,
                title = label,
                time = time,
                type = type,
                isEnabled = true,
                icon = finalIcon
            )
            
            val medicationLinks = medications.map { med ->
                val effectiveId = medicationRepository.insertMedication(med)
                EventMedicationEntity(
                    eventId = eventId,
                    medicationId = effectiveId,
                    dosageValue = med.dosageValue,
                    dosageUnit = med.dosageUnit
                )
            }
            
            eventRepository.insertEvent(newEvent, medicationLinks)
            
            val fullEvent = EventWithMedications(
                event = newEvent,
                medications = medications.mapIndexed { index, med ->
                    MedicationWithDosage(
                        crossRef = medicationLinks[index],
                        medication = med.copy(id = medicationLinks[index].medicationId)
                    )
                }
            )
            scheduleEventAlarm(fullEvent)
        }
    }

    fun updateEvent(event: EventEntity, newTitle: String, newTime: String, medications: List<Medication>? = null, type: EventType? = null) {
        viewModelScope.launch {
            val updatedEvent = event.copy(
                title = newTitle,
                time = newTime,
                type = type ?: event.type,
                icon = if (event.icon.startsWith("🕒") || event.icon.startsWith("🕐") || event.icon == "⏰") getClockEmoji(newTime) else event.icon
            )
            
            val medicationLinks = medications?.map { med ->
                val effectiveId = medicationRepository.insertMedication(med)
                EventMedicationEntity(
                    eventId = event.id,
                    medicationId = effectiveId,
                    dosageValue = med.dosageValue,
                    dosageUnit = med.dosageUnit
                )
            } ?: emptyList()
            
            eventRepository.updateEvent(updatedEvent, medicationLinks)
            
            // Reschedule alarm
            val medicationsList = medications ?: emptyList()
            val fullEvent = EventWithMedications(
                event = updatedEvent,
                medications = medicationsList.mapIndexed { index, med ->
                    MedicationWithDosage(
                        crossRef = medicationLinks[index],
                        medication = med.copy(id = medicationLinks[index].medicationId)
                    )
                }
            )
            scheduleEventAlarm(fullEvent)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
            alarmScheduler.cancelAlarm(event.id)
        }
    }

    fun scheduleEventAlarm(eventWithMeds: EventWithMedications) {
        if (!eventWithMeds.event.isEnabled) return
        
        val parts = eventWithMeds.event.time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            alarmScheduler.scheduleAlarm(eventWithMeds, hour, minute)
        }
    }

    fun getClockEmoji(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return "\uD83D\uDC8A"
        var hour = parts[0].toIntOrNull() ?: return "\uD83D\uDC8A"
        val minute = parts[1].toIntOrNull() ?: return "\uD83D\uDC8A"
        
        var isHalf = false
        if ((minute in 15..44)) {
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

    private fun safeLogD(tag: String, msg: String) {
        try {
            if (System.getProperty("java.runtime.name")?.contains("Android", ignoreCase = true) == true) {
                Log.d(tag, msg)
            } else {
                println("[$tag] $msg")
            }
        } catch (_: Exception) {
            println("[$tag] $msg")
        }
    }
}

class DashboardViewModelFactory(
    private val eventRepository: EventRepository,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(eventRepository, medicationRepository, alarmScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
