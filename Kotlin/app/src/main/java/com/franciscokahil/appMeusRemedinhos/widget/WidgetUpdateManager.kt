package com.franciscokahil.appMeusRemedinhos.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Singleton manager to coordinate widget updates from different parts of the app.
 * Added robustness for initial installations and race conditions.
 */
object WidgetUpdateManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updateWidgets(context: Context) {
        val appContext = context.applicationContext
        
        // Use a background scope to avoid blocking the caller
        scope.launch {
            try {
                // Ensure we don't hit a race condition with DB transactions
                // by waiting a short time and doing a double update for reliability.
                withContext(NonCancellable) {
                    safeLogD("WidgetUpdateManager", "Triggering first update...")
                    try {
                        MedicationWidget().updateAll(appContext)
                    } catch (e: Exception) { 
                        safeLogE("WidgetUpdateManager", "First update failed", e)
                    }
                    
                    delay(500.milliseconds) // Slightly longer delay for stability
                    
                    safeLogD("WidgetUpdateManager", "Triggering second update (safety)...")
                    try {
                        MedicationWidget().updateAll(appContext)
                    } catch (e: Exception) { 
                        safeLogE("WidgetUpdateManager", "Second update failed", e)
                    }
                }
            } catch (e: Exception) {
                safeLogE("WidgetUpdateManager", "Failed to update widgets", e)
            }
        }
    }

    private fun safeLogD(tag: String, msg: String) {
        try {
            // Check if we are in a unit test environment where Log is not mocked
            if (System.getProperty("java.runtime.name")?.contains("Android", ignoreCase = true) == true) {
                Log.d(tag, msg)
            } else {
                println("[$tag] $msg")
            }
        } catch (_: Exception) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogE(tag: String, msg: String, e: Throwable) {
        try {
            if (System.getProperty("java.runtime.name")?.contains("Android", ignoreCase = true) == true) {
                Log.e(tag, msg, e)
            } else {
                println("ERROR [$tag] $msg: ${e.message}")
            }
        } catch (_: Exception) {
            println("ERROR [$tag] $msg: ${e.message}")
        }
    }
}
