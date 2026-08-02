package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
            type = EventType.OTHER
        )
        dao.insertEvent(event)

        val allEvents = dao.getAllEventsWithMedications().first()
        assertEquals(1, allEvents.size)
        assertEquals("Aspirina", allEvents[0].event.title)
    }

    @Test
    fun updateEvent() = runBlocking {
        val event = EventEntity(id = "test-2", title = "Remedio", time = "08:00", type = EventType.OTHER)
        dao.insertEvent(event)
        
        dao.updateEvent(event.copy(title = "Remedio Atualizado"))

        val allEvents = dao.getAllEventsWithMedications().first()
        assertEquals("Remedio Atualizado", allEvents[0].event.title)
    }

    @Test
    fun deleteEvent() = runBlocking {
        val event = EventEntity(id = "test-3", title = "Remedio 1", time = "08:00", type = EventType.OTHER)
        dao.insertEvent(event)

        dao.deleteEvent(event)

        val allEvents = dao.getAllEventsWithMedications().first()
        assertEquals(0, allEvents.size)
    }
}
