package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.TypeConverter

class MedicationTypeConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}
