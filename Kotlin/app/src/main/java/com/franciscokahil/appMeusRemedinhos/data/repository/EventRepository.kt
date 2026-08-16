package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventMedicationEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.widget.WidgetUpdateManager
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val allEvents: Flow<List<EventWithMedications>>
    suspend fun insertEvent(event: EventEntity, medications: List<EventMedicationEntity>)
    suspend fun updateEvent(event: EventEntity, medications: List<EventMedicationEntity>)
    suspend fun deleteEvent(event: EventEntity)
}

class EventRepositoryImpl(
    private val context: Context,
    private val eventDao: EventDao,
) : EventRepository {
    override val allEvents: Flow<List<EventWithMedications>> = eventDao.getAllEventsWithMedications()

    override suspend fun insertEvent(event: EventEntity, medications: List<EventMedicationEntity>) {
        eventDao.updateEventWithMedications(event, medications)
        updateWidgets()
    }

    override suspend fun updateEvent(event: EventEntity, medications: List<EventMedicationEntity>) {
        eventDao.updateEventWithMedications(event, medications)
        updateWidgets()
    }

    override suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
        updateWidgets()
    }

    private fun updateWidgets() {
        WidgetUpdateManager.updateWidgets(context)
    }
}
