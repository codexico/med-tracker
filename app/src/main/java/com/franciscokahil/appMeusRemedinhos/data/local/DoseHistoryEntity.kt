package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dose_history")
data class DoseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val medicationId: String,
    val timestamp: Long, // When the dose was scheduled for (the event's time on its day)
    val amountTaken: Float,
    val status: String, // "TAKEN", "SKIPPED"
    val recordedAt: Long = timestamp, // When the user actually marked it in the app
    val isOverdue: Boolean = false, // True when marked after its own day had already ended
)
