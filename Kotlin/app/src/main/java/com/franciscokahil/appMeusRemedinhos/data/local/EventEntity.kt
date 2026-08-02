package com.franciscokahil.appMeusRemedinhos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.franciscokahil.appMeusRemedinhos.R

enum class EventType(val defaultTitleRes: Int, val defaultIcon: String) {
    WAKE_UP(R.string.wake_up, "🕐"),
    BREAKFAST(R.string.breakfast, "🍳"),
    MORNING(R.string.morning, "☀️"),
    LUNCH(R.string.lunch, "🍽️"),
    AFTERNOON(R.string.afternoon, "🌤️"),
    DINNER(R.string.dinner, "🍴"),
    SLEEP(R.string.sleep, "🌙"),
    OTHER(R.string.preset_other, "⏰")
}

class EventTypeConverter {
    @TypeConverter
    fun fromEventType(type: EventType): String = type.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)
}

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val time: String, // HH:MM
    val type: EventType,
    val createdAt: Long = System.currentTimeMillis(),
    val isEnabled: Boolean = true,
    val icon: String = "⏰"
)
