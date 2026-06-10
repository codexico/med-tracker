package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.widget.MedicationWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val allEvents: Flow<List<EventEntity>>
    suspend fun insertEvent(event: EventEntity)
    suspend fun updateEvent(event: EventEntity)
    suspend fun deleteEvent(event: EventEntity)
}

class EventRepositoryImpl(
    private val context: Context,
    private val eventDao: EventDao
) : EventRepository {
    override val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    override suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)
        updateWidgets()
    }

    override suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
        updateWidgets()
    }

    override suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
        updateWidgets()
    }

    private suspend fun updateWidgets() {
        try {
            MedicationWidget().updateAll(context)
        } catch (e: Exception) {
            // Log error or ignore if running in tests where Glance is not mocked
        }
    }
}
