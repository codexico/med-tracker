package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventRepositoryExtendedTest {

    private lateinit var repository: EventRepository
    private val eventDao = mockk<EventDao>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        repository = EventRepositoryImpl(context, eventDao)
    }

    @Test
    fun `insertEvent should call DAO`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00", type = EventType.OTHER)
        
        repository.insertEvent(event, emptyList())

        coVerify { eventDao.updateEventWithMedications(event, any()) }
    }

    @Test
    fun `updateEvent should call DAO`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00", type = EventType.OTHER)
        
        repository.updateEvent(event, emptyList())

        coVerify { eventDao.updateEventWithMedications(event, any()) }
    }

    @Test
    fun `deleteEvent should call DAO`() = runTest {
        val event = EventEntity("1", "Test Event", "12:00", type = EventType.OTHER)
        
        repository.deleteEvent(event)

        coVerify { eventDao.deleteEvent(event) }
    }
}
