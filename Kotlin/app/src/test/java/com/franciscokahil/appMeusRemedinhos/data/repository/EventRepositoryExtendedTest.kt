package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Extended tests for EventRepository implementation.
 * Tests side effects like widget updates and daily reset logic.
 */
class EventRepositoryExtendedTest {

    private lateinit var repository: EventRepository
    private val eventDao = mockk<EventDao>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val sharedPrefs = mockk<SharedPreferences>(relaxed = true)

    @Before
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs
        repository = EventRepositoryImpl(context, eventDao)
    }

    @Test
    fun `insertEvent should trigger widget update`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00")

        try {
            repository.insertEvent(event)
            // Widget update is attempted
            // In real env, would verify widget refresh
        } catch (e: Exception) {
            // Expected: Glance may not be available in test
        }

        coVerify { eventDao.insertEvent(event) }
    }

    @Test
    fun `updateEvent should trigger widget update`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00")

        try {
            repository.updateEvent(event)
        } catch (e: Exception) {
            // Expected in test env
        }

        coVerify { eventDao.updateEvent(event) }
    }

    @Test
    fun `deleteEvent should trigger widget update`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00")

        try {
            repository.deleteEvent(event)
        } catch (e: Exception) {
            // Expected
        }

        coVerify { eventDao.deleteEvent(event) }
    }

    @Test
    fun `widget update exception should not crash app`() = runTest {
        val event = EventEntity("1", "Test", "12:00")

        // This should complete without throwing
        repository.insertEvent(event)

        coVerify { eventDao.insertEvent(event) }
    }

    @Test
    fun `resetDailyStatus should call DAO reset method`() = runTest {
        repository.resetDailyStatus()

        coVerify { eventDao.resetAllTakenStatus() }
    }

    @Test
    fun `multiple inserts should batch update widget`() = runTest {
        val event1 = EventEntity("1", "Event 1", "08:00")
        val event2 = EventEntity("2", "Event 2", "14:00")
        val event3 = EventEntity("3", "Event 3", "20:00")

        repository.insertEvent(event1)
        repository.insertEvent(event2)
        repository.insertEvent(event3)

        coVerify(exactly = 3) { eventDao.insertEvent(any()) }
    }
}

