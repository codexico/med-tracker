package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): Medication?

    @Query("SELECT * FROM medications WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getMedicationByName(name: String): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("UPDATE medications SET currentStock = currentStock - :amount WHERE id = :medicationId")
    suspend fun subtractFromStock(medicationId: String, amount: Float)

    @Query("UPDATE medications SET currentStock = :newStock WHERE id = :medicationId")
    suspend fun updateStock(medicationId: String, newStock: Float)

    @Query("DELETE FROM medications")
    suspend fun deleteAll()
}
