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
        com.franciscokahil.appMeusRemedinhos.background.StockWorker.schedule(this)
        handleIntent(intent)

        setContent {
            MeusRemedinhosTheme {
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
