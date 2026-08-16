package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class DashboardViewModel(
    private val eventRepository: EventRepository,
    private val medicationRepository: MedicationRepository,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {

    // Ticker that emits every minute to ensure todayStart is updated even if the app is left open
    private val ticker = flow {
        while (true) {
            delay(1.minutes)
            emit(System.currentTimeMillis())
        }
    }.onStart { emit(System.currentTimeMillis()) }

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
        ticker,
    ) { allEvents, history, currentTime ->
        val todayStart = getStartOfDay(currentTime)
        val yesterdayStart = getStartOfDay(currentTime - (24 * 60 * 60 * 1000))

        allEvents.filter { eventWithMeds ->
            val wasCreatedBeforeToday = eventWithMeds.event.createdAt < todayStart

            val hasEntryYesterday = history.any { 
                it.eventId == eventWithMeds.event.id && 
                it.timestamp >= yesterdayStart && 
                it.timestamp < todayStart 
            }
            
            wasCreatedBeforeToday && !hasEntryYesterday && eventWithMeds.event.isEnabled
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val shouldShowOnboarding: StateFlow<Boolean> = events.map { it.isEmpty() }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true,
    )

    val allMedications: StateFlow<List<Medication>> = medicationRepository.allMedications.stateIn(
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

    fun toggleEventStatus(event: EventWithMedications, isTaken: Boolean) {
        viewModelScope.launch {
            if (isTaken) {
                val timestamp = System.currentTimeMillis()
                event.medications.forEach { medWithDosage ->
                    val amount = medWithDosage.crossRef.dosageValue.toFloatOrNull() ?: 0f
                    medicationRepository.markAsTaken(
                        eventId = event.event.id,
                        medicationId = medWithDosage.medication.id,
                        amount = amount,
                        timestamp = timestamp
                    )
                }
            } else {
                val todayStart = getStartOfDay(System.currentTimeMillis())
                medicationRepository.unmarkAsTaken(event.event.id, todayStart)
            }
        }
    }

    fun markAsTakenRetrospectively(event: EventWithMedications) {
        viewModelScope.launch {
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            event.medications.forEach { medWithDosage ->
                val amount = medWithDosage.crossRef.dosageValue.toFloatOrNull() ?: 0f
                medicationRepository.markAsTaken(
                    eventId = event.event.id,
                    medicationId = medWithDosage.medication.id,
                    amount = amount,
                    timestamp = yesterday
                )
            }
        }
    }

    fun markAsSkippedRetrospectively(event: EventWithMedications) {
        viewModelScope.launch {
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
            event.medications.forEach { medWithDosage ->
                medicationRepository.markAsSkipped(
                    eventId = event.event.id,
                    medicationId = medWithDosage.medication.id,
                    timestamp = yesterday
                )
            }
        }
    }

    fun addEvent(label: String, time: String, icon: String? = null, medications: List<Medication> = emptyList(), type: EventType = EventType.OTHER) {
        viewModelScope.launch {
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
            val clockEmojis = listOf(
                "🕛", "🕧", "🕐", "🕜", "🕑", "🕝", "🕒", "🕞", "🕓", "🕟", "🕔", "🕠",
                "🕕", "🕡", "🕖", "🕢", "🕗", "🕣", "🕘", "🕤", "🕙", "🕥", "🕚", "🕦", "⏰"
            )
            
            val finalIcon = if (event.icon in clockEmojis) getClockEmoji(newTime) else event.icon
            val updatedEvent = event.copy(
                title = newTitle,
                time = newTime,
                icon = finalIcon,
                type = type ?: event.type
            )
            
            val medicationLinks = (medications ?: emptyList()).map { med ->
                val effectiveId = medicationRepository.insertMedication(med)
                EventMedicationEntity(
                    eventId = event.id,
                    medicationId = effectiveId,
                    dosageValue = med.dosageValue,
                    dosageUnit = med.dosageUnit
                )
            }
            
            eventRepository.updateEvent(updatedEvent, medicationLinks)
            if (updatedEvent.isEnabled) {
                val fullEvent = EventWithMedications(
                    event = updatedEvent,
                    medications = (medications ?: emptyList()).mapIndexed { index, med ->
                        MedicationWithDosage(
                            crossRef = medicationLinks[index],
                            medication = med.copy(id = medicationLinks[index].medicationId)
                        )
                    }
                )
                scheduleEventAlarm(fullEvent)
            }
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            alarmScheduler.cancelAlarm(event.id)
            eventRepository.deleteEvent(event)
        }
    }

    private fun scheduleEventAlarm(event: EventWithMedications) {
        val parts = event.event.time.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: return
            val minute = parts[1].toIntOrNull() ?: return
            
            alarmScheduler.scheduleAlarm(event, hour, minute)
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
