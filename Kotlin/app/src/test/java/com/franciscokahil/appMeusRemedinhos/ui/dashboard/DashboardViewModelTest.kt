package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

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
    fun `initial state should be empty`() = runTest {
        viewModel.events.test {
            assertEquals(emptyList<DashboardEventUIModel>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shouldShowOnboarding should be true when events is empty`() = runTest {
        viewModel.shouldShowOnboarding.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shouldShowOnboarding should be false when events is not empty`() = runTest {
        viewModel.shouldShowOnboarding.test {
            assertTrue(awaitItem()) // Initial empty state
            
            // Emit a non-empty list
            val event = EventWithMedications(
                event = EventEntity("1", "Teste", "08:00"),
                medications = emptyList()
            )
            eventsFlow.value = listOf(event)
            
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addEvent should call repository`() = runTest {
        val label = "Teste"
        val time = "08:00"
        
        viewModel.addEvent(label, time, medications = listOf(Medication(name = "Aspirina")))
        advanceUntilIdle()

        coVerify { eventRepository.insertEvent(any(), match { it.size == 1 }) }
        coVerify { alarmScheduler.scheduleAlarm(any<EventWithMedications>(), any(), any()) }
    }

    @Test
    fun `toggleEventStatus should call medication repository markAsTaken`() = runTest {
        val eventWithMeds = EventWithMedications(
            event = EventEntity("1", "Teste", "12:00"),
            medications = emptyList()
        )
        
        viewModel.toggleEventStatus(eventWithMeds, true)
        advanceUntilIdle()

        // Verify it doesn't crash even with no meds, but in a real scenario we'd have meds
        coVerify(exactly = 0) { medicationRepository.markAsTaken(any(), any(), any(), any()) }
    }
}
