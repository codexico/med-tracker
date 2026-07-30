package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: EventDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.eventDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetEvents() = runBlocking {
        val event = EventEntity(
            id = "test-1",
            title = "Aspirina",
            time = "10:00",
            medications = listOf(Medication("Aspirina", "100", "mg")),
            isTakenToday = false
        )
        dao.insertEvent(event)

        val allEvents = dao.getAllEvents().first()
        assertEquals(1, allEvents.size)
        assertEquals("Aspirina", allEvents[0].title)
        assertEquals(listOf(Medication("Aspirina", "100", "mg")), allEvents[0].medications)
    }

    @Test
    fun updateEvent() = runBlocking {
        val event = EventEntity(id = "test-2", title = "Remedio", time = "08:00", isTakenToday = false)
        dao.insertEvent(event)
        
        dao.updateEvent(event.copy(isTakenToday = true))

        val allEvents = dao.getAllEvents().first()
        assertTrue(allEvents[0].isTakenToday)
    }

    @Test
    fun resetAllTakenStatus() = runBlocking {
        val event1 = EventEntity(id = "test-3", title = "Remedio 1", time = "08:00", isTakenToday = true)
        val event2 = EventEntity(id = "test-4", title = "Remedio 2", time = "14:00", isTakenToday = true)
        dao.insertEvent(event1)
        dao.insertEvent(event2)

        dao.resetAllTakenStatus()

        val allEvents = dao.getAllEvents().first()
        allEvents.forEach {
            assertTrue(!it.isTakenToday)
        }
    }
}
