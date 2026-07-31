package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class PendingEventsTest {

    private lateinit var viewModel: DashboardViewModel
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()
    private val eventsFlow = MutableStateFlow<List<EventWithMedications>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { eventRepository.allEvents } returns eventsFlow
        every { medicationRepository.allHistory } returns flowOf(emptyList())
        viewModel = DashboardViewModel(eventRepository, medicationRepository, alarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `newly created event should not be in pending list`() = runTest {
        // Created exactly at "now" (which is today)
        val today = System.currentTimeMillis()
        val event = EventWithMedications(
            event = EventEntity(id = "1", title = "New Event", time = "08:00", createdAt = today),
            medications = emptyList()
        )
        
        eventsFlow.value = listOf(event)

        viewModel.pendingEvents.test {
            val item = awaitItem()
            assertEquals("Event created today should NOT be pending", 0, item.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `event created yesterday without history should be in pending list`() = runTest {
        val calendar = Calendar.getInstance()
        
        // Start of today according to ViewModel logic
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayMidnight = calendar.timeInMillis

        // Created well before today (1 hour before midnight)
        val createdYesterday = todayMidnight - (1000 * 60 * 60)

        val event = EventWithMedications(
            event = EventEntity(id = "2", title = "Old Event", time = "08:00", createdAt = createdYesterday),
            medications = emptyList()
        )
        
        eventsFlow.value = listOf(event)

        viewModel.pendingEvents.test {
            // First item might be the empty state if combine emits quickly
            var item = awaitItem()
            if (item.isEmpty()) {
                item = awaitItem()
            }
            assertEquals("Event created yesterday should be pending", 1, item.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
