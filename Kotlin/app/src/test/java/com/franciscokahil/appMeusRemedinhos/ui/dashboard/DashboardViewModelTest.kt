package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var viewModel: DashboardViewModel
    private val repository = mockk<EventRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.allEvents } returns flowOf(emptyList())
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
    fun `addEvent should call repository`() = runTest {
        val label = "Teste"
        val time = "08:00"
        
        viewModel.addEvent(label, time)
        advanceUntilIdle()

        coVerify { repository.insertEvent(any()) }
        coVerify { alarmScheduler.scheduleAlarm(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `toggleEventStatus should call repository update`() = runTest {
        val event = EventEntity("1", "Teste", "12:00", isTakenToday = false)
        
        viewModel.toggleEventStatus(event, true)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.isTakenToday }) }
    }
}
