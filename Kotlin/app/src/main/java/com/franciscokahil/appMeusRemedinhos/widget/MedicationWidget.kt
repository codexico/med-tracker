package com.franciscokahil.appMeusRemedinhos.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.text.TextDecoration
import com.franciscokahil.appMeusRemedinhos.MainActivity
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import kotlinx.coroutines.flow.first

class MedicationWidget : GlanceAppWidget() {
    
    // Hex colors from theme.ts
    private val colorPrimary = Color(0xFF8B6F47)
    private val colorBackground = Color(0xFFFDFBF7)
    private val colorSurface = Color(0xFFFFFFFF)
    private val colorTextPrimary = Color(0xFF2D241B)
    private val colorTextSecondary = Color(0xFF6D5D4B)
    private val colorPrimaryContainer = Color(0xFFF0D4BD)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val events = try {
            database.eventDao().getAllEvents().first()
        } catch (e: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(ColorProvider(colorBackground))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Meus Remedinhos",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(colorPrimary),
                            fontSize = 16.sp
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    
                    if (events.isEmpty()) {
                        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sem remédios hoje",
                                style = TextStyle(color = ColorProvider(colorTextSecondary))
                            )
                        }
                    } else {
                        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                            items(events) { event ->
                                WidgetEventItem(event)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetEventItem(event: EventEntity) {
        val action = actionStartActivity<MainActivity>() 

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(ColorProvider(colorSurface))
                .cornerRadius(8.dp)
                .padding(8.dp)
                .clickable(action)
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(32.dp)
                        .background(ColorProvider(colorPrimaryContainer))
                        .cornerRadius(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = event.icon, style = TextStyle(fontSize = 16.sp))
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = event.time,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(colorPrimary),
                        textDecoration = if (event.isTakenToday) TextDecoration.LineThrough else TextDecoration.None
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = event.title,
                    style = TextStyle(
                        color = ColorProvider(colorTextPrimary),
                        textDecoration = if (event.isTakenToday) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }
}
