package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val time: String, // HH:MM
    val createdAt: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true,
    val icon: String = "access_time"
)
