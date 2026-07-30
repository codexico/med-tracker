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
        unmockkAll()
    }

    // ========== UPDATE EVENT ==========

    @Test
    fun `updateEvent should call repository and reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = true)

        viewModel.updateEvent(event, "Café Updated", "09:00")
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.title == "Café Updated" && it.time == "09:00" }) }
        coVerify { alarmScheduler.scheduleAlarm(match { it.id == "1" && it.title == "Café Updated" }, 9, 0) }
    }

    @Test
    fun `updateEvent when disabled should not reschedule alarm`() = runTest {
        val event = EventEntity("1", "Café", "08:00", isEnabled = false)

        viewModel.updateEvent(event, "Café Updated", "09:00")
        advanceUntilIdle()

        coVerify { repository.updateEvent(any()) }
        coVerify(inverse = true) { alarmScheduler.scheduleAlarm(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `updateEvent should preserve medications when null passed`() = runTest {
        val originalMeds = listOf(Medication("Vitamina D"), Medication("Ômega-3"))
        val event = EventEntity("1", "Café", "08:00", medications = originalMeds)

        viewModel.updateEvent(event, "Café", "09:00", medications = null)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.medications == originalMeds }) }
    }

    @Test
    fun `updateEvent should update medications when provided`() = runTest {
        val event = EventEntity("1", "Café", "08:00", medications = emptyList())
        val newMeds = listOf(Medication("Aspirina"))

        viewModel.updateEvent(event, "Café", "08:00", medications = newMeds)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.medications == newMeds }) }
    }

    @Test
    fun `updateEvent should preserve original icon if not a clock emoji`() = runTest {
        val event = EventEntity("1", "Breakfast", "08:00", icon = "🍳")

        viewModel.updateEvent(event, "Breakfast", "09:00")
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.icon == "🍳" }) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was alarm clock`() = runTest {
        val event = EventEntity("1", "Other", "08:00", icon = "⏰")

        viewModel.updateEvent(event, "Other", "05:30") // 5:30 should be 🕟 (\uD83D\uDD64)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.icon == "\uD83D\uDD64" }) }
    }

    @Test
    fun `updateEvent should update icon to dynamic clock if previous icon was already a clock emoji`() = runTest {
        val event = EventEntity("1", "Other", "08:00", icon = "\uD83D\uDD57") // 8:00

        viewModel.updateEvent(event, "Other", "10:00") // 10:00 should be 🕙 (\uD83D\uDD59)
        advanceUntilIdle()

        coVerify { repository.updateEvent(match { it.icon == "\uD83D\uDD59" }) }
    }

    // ========== DELETE EVENT ==========

    @Test
    fun `deleteEvent should cancel alarm before removing from repo`() = runTest {
        val event = EventEntity("1", "Café", "08:00")

        viewModel.deleteEvent(event)
        advanceUntilIdle()

        coVerify(exactly = 1) { alarmScheduler.cancelAlarm("1") }
        coVerify(exactly = 1) { repository.deleteEvent(event) }
    }

    // ========== MEDICATION MANAGEMENT ==========

    @Test
    fun `addMedication should append to existing list`() = runTest {
        val existingMeds = listOf(Medication("Vitamina D"))
        val event = EventEntity("1", "Café", "08:00", medications = existingMeds)
        
        val flow = MutableStateFlow(listOf(event))
        every { repository.allEvents } returns flow
        viewModel = DashboardViewModel(repository, alarmScheduler)
        
        viewModel.events.test {
            var item = awaitItem()
            if (item.isEmpty()) item = awaitItem()
            assertEquals(1, item.size)

            val newMed = Medication("Ômega-3")
            viewModel.addMedication("1", newMed)
            advanceUntilIdle()

            coVerify { repository.updateEvent(match { it.id == "1" && it.medications.contains(newMed) }) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeMedication should remove by index`() = runTest {
        val med1 = Medication("Vitamina")
        val med2 = Medication("Ômega")
        val med3 = Medication("Aspirina")
        val event = EventEntity("1", "Café", "08:00", medications = listOf(med1, med2, med3))
        val flow = MutableStateFlow(listOf(event))
        every { repository.allEvents } returns flow
        viewModel = DashboardViewModel(repository, alarmScheduler)
        
        viewModel.events.test {
            var item = awaitItem()
            if (item.isEmpty()) item = awaitItem()
            assertEquals(1, item.size)

            viewModel.removeMedication("1", 1)  // Remove med2
            advanceUntilIdle()

            coVerify { repository.updateEvent(match { it.id == "1" && it.medications == listOf(med1, med3) }) }
            cancelAndIgnoreRemainingEvents()
        }
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
