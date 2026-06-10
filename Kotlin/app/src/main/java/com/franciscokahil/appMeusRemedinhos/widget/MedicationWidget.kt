package com.franciscokahil.appMeusRemedinhos.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import androidx.glance.text.FontWeight
import androidx.glance.background
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.updateAll
import androidx.glance.color.ColorProvider
import androidx.glance.GlanceTheme
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import kotlinx.coroutines.flow.first

class MedicationWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val events = database.eventDao().getAllEvents().first()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Meus Remedinhos",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    if (events.isEmpty()) {
                        Text(text = "Sem remédios hoje")
                    } else {
                        LazyColumn {
                            items(events) { event ->
                                WidgetEventItem(event)
                            }
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetEventItem(event: EventEntity) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "${event.time} - ${event.title}",
                style = TextStyle(color = GlanceTheme.colors.onBackground)
            )
        }
    }
}
