package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM events ORDER BY time ASC")
    fun getAllEventsWithMedications(): Flow<List<EventWithMedications>>

    @Transaction
    @Query("SELECT * FROM events ORDER BY time ASC")
    suspend fun getAllEventsWithMedicationsSnapshot(): List<EventWithMedications>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventMedications(links: List<EventMedicationEntity>)

    @Query("DELETE FROM event_medications WHERE eventId = :eventId")
    suspend fun deleteMedicationLinksForEvent(eventId: String)

    @Transaction
    suspend fun updateEventWithMedications(event: EventEntity, medications: List<EventMedicationEntity>) {
        insertEvent(event)
        deleteMedicationLinksForEvent(event.id)
        insertEventMedications(medications)
    }

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT COUNT(*) FROM events")
    suspend fun getEventCount(): Int

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
