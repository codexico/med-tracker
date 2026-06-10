package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.widget.MedicationWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.Flow

class EventRepository(private val context: Context, private val eventDao: EventDao) {
    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: EventEntity) {
        eventDao.insertEvent(event)
        MedicationWidget().updateAll(context)
    }

    suspend fun updateEvent(event: EventEntity) {
        eventDao.updateEvent(event)
        MedicationWidget().updateAll(context)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventDao.deleteEvent(event)
        MedicationWidget().updateAll(context)
    }

    suspend fun resetAllTakenStatus() {
        eventDao.resetAllTakenStatus()
    }
}
