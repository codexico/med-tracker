package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(dose: DoseHistoryEntity)

    @Query("SELECT * FROM dose_history WHERE eventId = :eventId AND timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getDosesForEventToday(eventId: String, startOfDay: Long): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<DoseHistoryEntity>>

    @Query("SELECT * FROM dose_history WHERE eventId = :eventId AND timestamp >= :startOfDay")
    suspend fun getDosesForEventTodaySync(eventId: String, startOfDay: Long): List<DoseHistoryEntity>

    @Query("DELETE FROM dose_history WHERE id = :id")
    suspend fun deleteDose(id: Long)

    @Query("DELETE FROM dose_history WHERE eventId = :eventId AND timestamp >= :startOfDay")
    suspend fun deleteDosesForEventToday(eventId: String, startOfDay: Long)

    @Query("DELETE FROM dose_history")
    suspend fun deleteAll()
}
