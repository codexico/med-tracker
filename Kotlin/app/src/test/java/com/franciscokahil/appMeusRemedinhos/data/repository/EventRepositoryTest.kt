package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventRepositoryTest {

    private lateinit var repository: EventRepository
    private val eventDao = mockk<EventDao>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        repository = EventRepositoryImpl(context, eventDao)
    }

    @Test
    fun `insertEvent should call DAO`() = runTest {
        val event = EventEntity("1", "Teste", "12:00")
        
        repository.insertEvent(event)

        coVerify { eventDao.insertEvent(event) }
    }

    @Test
    fun `deleteEvent should call DAO`() = runTest {
        val event = EventEntity("1", "Teste", "12:00")
        
        repository.deleteEvent(event)

        coVerify { eventDao.deleteEvent(event) }
    }
}
