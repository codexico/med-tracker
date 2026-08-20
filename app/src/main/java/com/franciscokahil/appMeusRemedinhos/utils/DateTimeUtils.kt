package com.franciscokahil.appMeusRemedinhos.utils

import java.util.Calendar

object DateTimeUtils {
    /**
     * Checks if the given time (HH:mm) has already passed today.
     */
    fun isTimePassed(eventTime: String, now: Calendar = Calendar.getInstance()): Boolean {
        val parts = eventTime.split(":")
        if (parts.size != 2) return false
        val hour = parts[0].toIntOrNull() ?: return false
        val minute = parts[1].toIntOrNull() ?: return false

        val eventCal = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return now.after(eventCal)
    }
}
