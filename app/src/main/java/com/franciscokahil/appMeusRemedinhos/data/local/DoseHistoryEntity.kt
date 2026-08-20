package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_history")
data class DoseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val medicationId: String,
    val timestamp: Long,
    val amountTaken: Float,
    val status: String // "TAKEN", "SKIPPED"
)
