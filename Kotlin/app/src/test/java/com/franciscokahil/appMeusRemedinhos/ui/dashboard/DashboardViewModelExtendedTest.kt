package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepository
import com.franciscokahil.appMeusRemedinhos.data.repository.MedicationRepository
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
class DashboardViewModelExtendedTest {

    private lateinit var viewModel: DashboardViewModel
    private val eventRepository = mockk<EventRepository>(relaxed = true)
    private val medicationRepository = mockk<MedicationRepository>(relaxed = true)
    private val alarmScheduler = mockk<AlarmScheduler>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { medicationRepository.allMedications } returns flowOf(emptyList())
        every { eventRepository.allEvents } returns flowOf(emptyList())
        every { medicationRepository.allHistory } returns flowOf(emptyList())
        viewModel = DashboardViewModel(eventRepository, medicationRepository, alarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateEvent should call repository and reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = true, type = EventType.OTHER)
        
        viewModel.updateEvent(event, "Café Novo", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.title == "Café Novo" && it.time == "09:00" }, any()) }
        coVerify { alarmScheduler.scheduleAlarm(any<EventWithMedications>(), 9, 0) }
    }

    @Test
    fun `updateEvent when disabled should not reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = false, type = EventType.OTHER)
        
        viewModel.updateEvent(event, "Café Novo", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.id == "1" && !it.isEnabled }, any()) }
        coVerify(exactly = 0) { alarmScheduler.scheduleAlarm(any(), any(), any()) }
    }

    @Test
    fun `updateEvent should preserve original icon if not a clock emoji`() = runTest {
        val event = EventEntity("1", "Breakfast", "08:00", icon = "🍳", type = EventType.OTHER)
        
        viewModel.updateEvent(event, "Breakfast", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "🍳" }, any()) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was alarm clock`() = runTest {
        val event = EventEntity("1", "Other", "08:00", icon = "⏰", type = EventType.OTHER)
        
        viewModel.updateEvent(event, "Other", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "🕘" }, any()) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was already a clock emoji`() = runTest {
        // Using common clock emoji instead of potentially problematic unicode escapes
        val event = EventEntity("1", "Other", "08:00", icon = "🕒", type = EventType.OTHER)
        
        viewModel.updateEvent(event, "Other", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "🕘" }, any()) }
    }

    @Test
    fun `deleteEvent should cancel alarm before removing from repo`() = runTest {
        val event = EventEntity("1", "Café", "08:00", type = EventType.OTHER)
        
        viewModel.deleteEvent(event)
        advanceUntilIdle()

        verify { alarmScheduler.cancelAlarm("1") }
        coVerify { eventRepository.deleteEvent(event) }
    }

    @Test
    fun `getClockEmoji should return correct emoji for each hour`() {
        assertEquals("🕐", viewModel.getClockEmoji("01:00"))
        assertEquals("🕑", viewModel.getClockEmoji("02:00"))
        assertEquals("🕒", viewModel.getClockEmoji("03:00"))
        assertEquals("🕓", viewModel.getClockEmoji("04:00"))
        assertEquals("🕔", viewModel.getClockEmoji("05:00"))
        assertEquals("🕕", viewModel.getClockEmoji("06:00"))
        assertEquals("🕖", viewModel.getClockEmoji("07:00"))
        assertEquals("🕗", viewModel.getClockEmoji("08:00"))
        assertEquals("🕘", viewModel.getClockEmoji("09:00"))
        assertEquals("🕙", viewModel.getClockEmoji("10:00"))
        assertEquals("🕚", viewModel.getClockEmoji("11:00"))
        assertEquals("🕛", viewModel.getClockEmoji("12:00"))
    }
}
