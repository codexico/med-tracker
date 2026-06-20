package com.franciscokahil.appMeusRemedinhos.data.repository

import android.content.Context
import android.util.Log
import com.franciscokahil.appMeusRemedinhos.data.local.EventDao
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.widget.MedicationWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface EventRepository {
    val allEvents: Flow<List<EventEntity>>
    suspend fun insertEvent(event: EventEntity)
    suspend fun updateEvent(event: EventEntity)
    suspend fun deleteEvent(event: EventEntity)
    suspend fun resetDailyStatus()
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

    override suspend fun resetDailyStatus() {
        eventDao.resetAllTakenStatus()
        updateWidgets()
    }

    private suspend fun updateWidgets() {
        try {
            // Using NonCancellable ensures that widget updates (which are side-effects)
            // don't throw CancellationException if the calling coroutine is cancelled.
            // This is common in tests where the scope is cancelled before the side-effect completes.
            withContext(NonCancellable) {
                try {
                    MedicationWidget().updateAll(context)
                } catch (e: Throwable) {
                    // Ignore errors during widget updates as they are non-critical side effects
                    Log.e("EventRepository", "Failed to update widgets", e)
                }
            }
        } catch (e: Throwable) {
            // Extra safety to prevent any exception from bubbling up to the caller
        }
    }
}
