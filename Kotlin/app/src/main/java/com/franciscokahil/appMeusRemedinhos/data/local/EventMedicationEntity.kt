package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_medications",
    primaryKeys = ["eventId", "medicationId"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId"), Index("medicationId")]
)
data class EventMedicationEntity(
    val eventId: String,
    val medicationId: String,
    val dosageValue: String = "",
    val dosageUnit: String = ""
)
