package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import app.cash.turbine.test
import com.franciscokahil.appMeusRemedinhos.background.AlarmScheduler
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import com.franciscokahil.appMeusRemedinhos.data.local.MedicationWithDosage
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
import org.junit.Before
import org.junit.Test

/**
 * Extended unit tests for DashboardViewModel.
 * Tests business logic, state management, and edge cases.
 */
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
        every { eventRepository.allEvents } returns flowOf(emptyList())
        every { medicationRepository.allHistory } returns flowOf(emptyList())
        viewModel = DashboardViewModel(eventRepository, medicationRepository, alarmScheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ========== UPDATE EVENT ==========

    @Test
    fun `updateEvent should call repository and reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = true)

        viewModel.updateEvent(event, "Café Updated", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.title == "Café Updated" && it.time == "09:00" }, any()) }
        coVerify { alarmScheduler.scheduleAlarm(match { it.event.id == "1" && it.event.title == "Café Updated" }, 9, 0) }
    }

    @Test
    fun `updateEvent when disabled should not reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = false)

        viewModel.updateEvent(event, "Café Updated", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(any(), any()) }
        coVerify(inverse = true) { alarmScheduler.scheduleAlarm(any(), any(), any()) }
    }

    @Test
    fun `updateEvent should preserve original icon if not a clock emoji`() = runTest {
        val event = EventEntity("1", "Breakfast", "08:00", icon = "🍳")

        viewModel.updateEvent(event, "Breakfast", "09:00", emptyList())
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "🍳" }, any()) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was alarm clock`() = runTest {
        val event = EventEntity("1", "Other", "08:00", icon = "⏰")

        viewModel.updateEvent(event, "Other", "05:30", emptyList()) // 5:30 should be 🕟 (\uD83D\uDD64)
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "\uD83D\uDD64" }, any()) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was already a clock emoji`() = runTest {
        val event = EventEntity("1", "Other", "08:00", icon = "\uD83D\uDD57") // 8:00

        viewModel.updateEvent(event, "Other", "10:00", emptyList()) // 10:00 should be 🕙 (\uD83D\uDD59)
        advanceUntilIdle()

        coVerify { eventRepository.updateEvent(match { it.icon == "\uD83D\uDD59" }, any()) }
    }

    // ========== DELETE EVENT ==========

    @Test
    fun `deleteEvent should cancel alarm before removing from repo`() = runTest {
        val event = EventEntity("1", "Café", "08:00")

        viewModel.deleteEvent(event)
        advanceUntilIdle()

        coVerify(exactly = 1) { alarmScheduler.cancelAlarm("1") }
        coVerify(exactly = 1) { eventRepository.deleteEvent(event) }
    }

    // ========== CLOCK EMOJI LOGIC ==========

    @Test
    fun `getClockEmoji should return correct emoji for each hour`() {
        val testCases = mapOf(
            "01:30" to "\uD83D\uDD60",  // 1:30 = half-past
            "01:00" to "\uD83D\uDD50",  // 1:00 = exact
            "12:00" to "\uD83D\uDD5B"
        )

        testCases.forEach { (time, expectedEmoji) ->
            val emoji = viewModel.getClockEmoji(time)
            assertEquals("Expected $expectedEmoji for $time, got $emoji", expectedEmoji, emoji)
        }
    }
}
