package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val repository = mockk<EventRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()
    private val eventsFlow = MutableStateFlow<List<EventEntity>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.allEvents } returns eventsFlow
        viewModel = DashboardViewModel(repository, alarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() = runTest {
        viewModel.events.test {
            assertEquals(emptyList<EventEntity>(), awaitItem())
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
            eventsFlow.value = listOf(EventEntity("1", "Teste", "08:00"))
            
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addEvent should call repository`() = runTest {
        val label = "Teste"
        val time = "08:00"
        
        viewModel.addEvent(label, time, medications = listOf(Medication("Aspirina")))
        advanceUntilIdle()

        coVerify { repository.insertEvent(match { it.medications.size == 1 }) }
        coVerify { alarmScheduler.scheduleAlarm(any<EventEntity>(), any(), any()) }
    }

    @Test
    fun `toggleEventStatus should call repository update`() = runTest {
        val event = EventEntity("1", "Teste", "12:00", isTakenToday = false)
        
        viewModel.toggleEventStatus(event, true)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.isTakenToday }) }
    }
}
