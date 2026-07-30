package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MedicationTypeConverter {
    @TypeConverter
    fun fromString(value: String): List<Medication> {
        if (value.isBlank()) return emptyList()
        return try {
            Json.decodeFromString<List<Medication>>(value)
        } catch (e: Exception) {
            // Fallback for legacy data if needed, or just return empty
            // In a real migration we might want to handle the transition from comma-separated strings
            value.split(",").filter { it.isNotBlank() }.map { Medication(it.trim()) }
        }
    }

    @TypeConverter
    fun fromList(list: List<Medication>): String {
        return Json.encodeToString(list)
    }
}
