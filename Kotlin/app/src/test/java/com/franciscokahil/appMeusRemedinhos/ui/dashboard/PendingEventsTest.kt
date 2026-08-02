package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        every { medicationRepository.allMedications } returns flowOf(emptyList())
        viewModel = DashboardViewModel(eventRepository, medicationRepository, alarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `newly created event should not be in pending list`() = runTest {
        val today = System.currentTimeMillis()
        
        val event = EventWithMedications(
            event = EventEntity(id = "1", title = "New Event", time = "08:00", createdAt = today, type = EventType.OTHER),
            medications = emptyList()
        )
        
        eventsFlow.value = listOf(event)

        viewModel.pendingEvents.test {
            val items = awaitItem()
            assertTrue(items.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `event created yesterday without history should be in pending list`() = runTest {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val createdYesterday = calendar.timeInMillis
        
        val event = EventWithMedications(
            event = EventEntity(id = "2", title = "Old Event", time = "08:00", createdAt = createdYesterday, type = EventType.OTHER),
            medications = emptyList()
        )
        
        eventsFlow.value = listOf(event)

        // Mock current time to be today
        viewModel.pendingEvents.test {
            // First emission might be empty, wait for the one with the event
            var items = awaitItem()
            if (items.isEmpty()) items = awaitItem()
            
            assertEquals(1, items.size)
            assertEquals("2", items[0].event.id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
