package com.franciscokahil.appMeusRemedinhos

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.franciscokahil.appMeusRemedinhos.background.NotificationHelper
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.repository.EventRepositoryImpl
import com.franciscokahil.appMeusRemedinhos.ui.MainNavigation
import com.franciscokahil.appMeusRemedinhos.ui.theme.MeusRemedinhosTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : ComponentActivity() {
    
    private val highlightedEventId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        NotificationHelper(this).createNotificationChannel()
        handleIntent(intent)
        checkDailyReset()

        lifecycleScope.launch {
            AppDatabase.getDatabase(applicationContext).ensureSeeded()
        }

        setContent {
            MeusRemedinhosTheme {
                // Notification permission request removed from onCreate for UX 2.0.
                // It is now requested Just-in-Time in DashboardScreen when creating an event.

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        highlightedId = highlightedEventId.value,
                        onHighlightedConsumed = { highlightedEventId.value = null }
                    )
                }
            }
        }
    }

    private fun checkDailyReset() {
        val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastOpenDate = sharedPrefs.getString("last_open_date", "")
        
        val calendar = Calendar.getInstance()
        val today = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        if (lastOpenDate != today) {
            lifecycleScope.launch {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = EventRepositoryImpl(applicationContext, database.eventDao())
                repository.resetDailyStatus()
                sharedPrefs.edit {
                    putString("last_open_date", today)
                }
            }
        }
    }

    // Made public for testing
    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "meusremedinhos" && uri.host == "event") {
                val eventId = uri.lastPathSegment
                if (!eventId.isNullOrEmpty()) {
                    highlightedEventId.value = eventId
                    // Log for debugging
                    android.util.Log.d("MainActivity", "Deep-link received for event: $eventId")
                }
            }
        }
    }
}
