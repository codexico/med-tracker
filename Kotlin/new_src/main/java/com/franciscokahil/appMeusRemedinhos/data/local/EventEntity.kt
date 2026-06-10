package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "events")
@TypeConverters(MedicationTypeConverter::class)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val time: String, // HH:MM
    val medications: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val isTakenToday: Boolean = false
)
