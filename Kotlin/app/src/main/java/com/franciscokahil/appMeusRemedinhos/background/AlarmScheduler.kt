package com.franciscokahil.appMeusRemedinhos.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.franciscokahil.appMeusRemedinhos.R
import java.util.Calendar

interface AlarmScheduler {
    fun scheduleAlarm(id: String, title: String, message: String, hour: Int, minute: Int)
    fun scheduleAlarm(event: com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications, hour: Int, minute: Int)
    fun cancelAlarm(id: String)
    fun getContext(): Context
}

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    private val alarmManager by lazy {
        try {
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        } catch (_: Exception) {
            null
        }
    }

    override fun scheduleAlarm(id: String, title: String, message: String, hour: Int, minute: Int) {
        val manager = alarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
        }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (!manager.canScheduleExactAlarms())) {
            manager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent,
            )
        } else {
            manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent,
            )
        }
    }

    override fun scheduleAlarm(
        event: com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications,
        hour: Int,
        minute: Int
    ) {
        val message = if (event.medications.isEmpty()) {
            context.getString(R.string.notification_default_msg)
        } else {
            val prefixRes = if (event.medications.size == 1) R.string.notification_med_prefix else R.string.notification_meds_prefix
            val prefix = context.getString(prefixRes)
            "$prefix: ${event.medications.joinToString(", ") { it.displayName }}"
        }
        scheduleAlarm(event.event.id, event.event.title, message, hour, minute)
    }

    override fun cancelAlarm(id: String) {
        val manager = alarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        manager.cancel(pendingIntent)
    }

    override fun getContext(): Context = context
}
