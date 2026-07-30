package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "events")
@TypeConverters(MedicationTypeConverter::class)
data class EventEntity(
    @PrimaryKey val id: String, // Changed to String to match RN UUIDs if needed, or just for flexibility
    val title: String,
    val time: String, // HH:MM
    val medications: List<Medication> = emptyList(),
    val isEnabled: Boolean = true,
    val isTakenToday: Boolean = false,
    val icon: String = "access_time" // Added icon to match RN
)
