package com.franciscokahil.appMeusRemedinhos.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.widget.WidgetUpdateManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            val appContext = context.applicationContext
            scope.launch {
                val database = AppDatabase.getDatabase(appContext)
                val scheduler = AlarmSchedulerImpl(appContext)
                
                // Reschedule all enabled events
                val events = database.eventDao().getAllEventsWithMedicationsSnapshot()
                events.filter { it.event.isEnabled }.forEach { event ->
                    val parts = event.event.time.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull() ?: return@forEach
                        val minute = parts[1].toIntOrNull() ?: return@forEach
                        scheduler.scheduleAlarm(event, hour, minute)
                    }
                }
                
                // Reschedule midnight refresh
                scheduler.scheduleMidnightRefresh()
                
                // Refresh widget
                WidgetUpdateManager.updateWidgets(appContext)
            }
        }
    }
}
