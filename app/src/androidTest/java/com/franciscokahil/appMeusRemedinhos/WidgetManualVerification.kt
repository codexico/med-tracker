package com.franciscokahil.appMeusRemedinhos

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.background.AlarmReceiver
import com.franciscokahil.appMeusRemedinhos.background.AlarmSchedulerImpl
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.widget.WidgetUpdateManager
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * These tests require manual observation of the Home Screen widget.
 * They are designed to be run one by one.
 */
@RunWith(AndroidJUnit4::class)
class WidgetManualVerification {

    @Test
    fun manual_verifyAlarmSync() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 1. Setup: Clear and add an event for 1 minute from now
        runBlocking {
            val db = AppDatabase.getDatabase(context)
            db.clearAllTables()
            
            val calendar = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.MINUTE, 1)
            }
            val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = calendar.get(java.util.Calendar.MINUTE)
            val timeString = String.format("%02d:%02d", hour, minute)
            
            db.eventDao().insertEvent(EventEntity("m1", "Manual Sync Test", timeString, EventType.OTHER))
            
            // Trigger initial update
            WidgetUpdateManager.updateWidgets(context)
        }

        println("--- MANUAL TEST: Alarm Sync ---")
        println("1. Go to Home Screen and ensure the widget is visible.")
        println("2. You should see 'Manual Sync Test' at its scheduled time.")
        println("3. Wait ~60 seconds for the notification to fire.")
        println("4. OBSERVE: The widget should immediately show a ⚠️ icon without you opening the app.")
        
        // Wait 90 seconds to allow time for the alarm and observation
        Thread.sleep(90000)
    }

    @Test
    fun manual_verifyMidnightReset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        runBlocking {
            val db = AppDatabase.getDatabase(context)
            db.clearAllTables()
            db.eventDao().insertEvent(EventEntity("m2", "Midnight Reset Test", "08:00", EventType.OTHER))
            
            // Trigger update
            WidgetUpdateManager.updateWidgets(context)
        }

        println("--- MANUAL TEST: Midnight Reset ---")
        println("1. Ensure 'Midnight Reset Test' is visible on the widget.")
        println("2. Open the app and mark it as TAKEN (strikethrough should appear on widget).")
        println("3. Go to System Settings -> Date & Time.")
        println("4. Disable 'Set time automatically' and change time to 23:59.")
        println("5. Wait for the clock to hit 00:00.")
        println("6. OBSERVE: The widget should clear the strikethrough automatically within a few seconds of midnight.")
        
        // Wait 3 minutes for user to perform the manual time change and observe
        Thread.sleep(180000)
    }

    @Test
    fun manual_triggerBootBroadcast() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        
        println("--- MANUAL TEST: Boot Recovery ---")
        println("1. This test simulates a reboot by sending the BOOT_COMPLETED broadcast via ADB.")
        println("2. Run this command in your terminal:")
        println("   adb shell am broadcast -a android.intent.action.BOOT_COMPLETED -p ${context.packageName}")
        println("3. OBSERVE: Check Logcat for 'WidgetUpdateManager' triggering an update.")
        
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED).apply {
            setPackage(context.packageName)
        }
        // We can't easily trigger the real system broadcast from here, 
        // but we can manually invoke our receiver to test logic.
        context.sendBroadcast(intent)
        
        Thread.sleep(10000)
    }
}
