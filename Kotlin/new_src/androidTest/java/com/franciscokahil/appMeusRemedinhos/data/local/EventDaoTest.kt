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
        // Usa um banco de dados em memória para que os testes não modifiquem o app real
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
            title = "Aspirina",
            time = "10:00",
            medications = listOf("Aspirina 100mg"),
            isTakenToday = false
        )
        dao.insertEvent(event)

        val allEvents = dao.getAllEvents().first()
        assertEquals(1, allEvents.size)
        assertEquals("Aspirina", allEvents[0].title)
    }

    @Test
    fun resetAllTakenStatus() = runBlocking {
        val event1 = EventEntity(title = "Remedio 1", time = "08:00", isTakenToday = true)
        val event2 = EventEntity(title = "Remedio 2", time = "14:00", isTakenToday = true)
        dao.insertEvent(event1)
        dao.insertEvent(event2)

        dao.resetAllTakenStatus()

        val allEvents = dao.getAllEvents().first()
        allEvents.forEach {
            assertTrue(!it.isTakenToday)
        }
    }
}
