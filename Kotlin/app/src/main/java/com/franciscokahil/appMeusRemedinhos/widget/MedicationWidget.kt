package com.franciscokahil.appMeusRemedinhos.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background as composeBackground
import androidx.compose.foundation.layout.Column as ComposeColumn
import androidx.compose.foundation.layout.Row as ComposeRow
import androidx.compose.foundation.layout.Box as ComposeBox
import androidx.compose.foundation.layout.Spacer as ComposeSpacer
import androidx.compose.foundation.layout.fillMaxSize as composeFillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth as composeFillMaxWidth
import androidx.compose.foundation.layout.padding as composePadding
import androidx.compose.foundation.layout.height as composeHeight
import androidx.compose.foundation.layout.width as composeWidth
import androidx.compose.foundation.layout.size as composeSize
import androidx.compose.foundation.lazy.LazyColumn as ComposeLazyColumn
import androidx.compose.foundation.lazy.items as composeItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text as ComposeText
import androidx.compose.ui.Modifier as ComposeModifier
import androidx.compose.ui.text.TextStyle as ComposeTextStyle
import androidx.compose.ui.text.font.FontWeight as ComposeFontWeight
import androidx.compose.ui.text.style.TextDecoration as ComposeTextDecoration
import androidx.compose.ui.Alignment as ComposeAlignment
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.material3.ColorProviders
import com.franciscokahil.appMeusRemedinhos.MainActivity
import com.franciscokahil.appMeusRemedinhos.R
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventWithMedications
import com.franciscokahil.appMeusRemedinhos.ui.theme.*
import kotlinx.coroutines.flow.first
import java.util.Calendar

class MedicationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val events = try {
            database.eventDao().getAllEventsWithMedications().first()
        } catch (_: Exception) {
            emptyList()
        }

        val takenEventIds = try {
            database.doseHistoryDao().getTakenEventIdsToday(todayStart).toSet()
        } catch (_: Exception) {
            emptySet()
        }

        provideContent {
            val colors = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                GlanceTheme.colors
            } else {
                MedicationWidgetTheme.colors
            }

            GlanceTheme(colors = colors) {
                MedicationWidgetContent(
                    context = context,
                    events = events,
                    takenEventIds = takenEventIds,
                )
            }
        }
    }
}

/**
 * Custom Theme for the Widget to avoid manual ColorProvider calls.
 * This maps our brand colors to the Glance theme system.
 */
object MedicationWidgetTheme {
    private val lightColors = androidx.compose.material3.lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        background = Color(0xFFF0D4BD), // Brand background for the widget
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
    )

    private val darkColors = androidx.compose.material3.darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        background = Color(0xFF2D241B), // Darker brand background
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    )

    val colors = ColorProviders(
        light = lightColors,
        dark = darkColors,
    )
}

// Preview/Brand Colors
private val colorPrimary = Color(0xFF8B6F47)
private val colorBackground = Color(0xFFF0D4BD)
private val colorSurface = Color(0xFFFFFFFF)
private val colorTextPrimary = Color(0xFF2D241B)
private val colorTextSecondary = Color(0xFF6D5D4B)

@Composable
fun MedicationWidgetContent(
    context: Context,
    events: List<EventWithMedications>,
    takenEventIds: Set<String>,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.background)
            .padding(12.dp),
    ) {
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.primary,
                fontSize = 16.sp,
            ),
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = context.getString(R.string.widget_empty_text),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                )
            }
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(events) { event ->
                    WidgetEventItem(
                        context = context,
                        eventWithMeds = event,
                        isTakenToday = takenEventIds.contains(event.event.id),
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetEventItem(
    context: Context,
    eventWithMeds: EventWithMedications,
    isTakenToday: Boolean,
) {
    val event = eventWithMeds.event
    // Explicit intent for Deep Link
    val intent = Intent(Intent.ACTION_VIEW, "meusremedinhos://event/${event.id}".toUri()).apply {
        setClass(context, MainActivity::class.java)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val action = actionStartActivity(intent)

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(GlanceTheme.colors.surface)
            .cornerRadius(12.dp)
            .padding(12.dp)
            .clickable(action),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(GlanceTheme.colors.primaryContainer)
                    .cornerRadius(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = event.icon, style = TextStyle(fontSize = 16.sp))
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = event.title,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        color = if (isTakenToday) GlanceTheme.colors.onSurfaceVariant else GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                        textDecoration = if (isTakenToday) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                )
                Text(
                    text = event.time,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        textDecoration = if (isTakenToday) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                )
            }
        }
    }
}

// GLANCE PREVIEW
@Preview(showBackground = true)
@Composable
fun MedicationWidgetPreview() {
    val mockEvents = listOf(
        EventWithMedications(
            event = EventEntity("1", "Café da manhã", "08:00", EventType.BREAKFAST, icon = "🍳"),
            medications = emptyList(),
        ),
        EventWithMedications(
            event = EventEntity("2", "Almoço", "12:00", EventType.LUNCH, icon = "🍽️"),
            medications = emptyList(),
        ),
        EventWithMedications(
            event = EventEntity("3", "Remédio Noite", "22:00", EventType.SLEEP, icon = "🌙"),
            medications = emptyList(),
        ),
    )
    
    ComposeColumn(
        modifier = ComposeModifier
            .composeFillMaxSize()
            .composeBackground(colorBackground)
            .composePadding(12.dp),
    ) {
        ComposeText(
            text = "Meus Remedinhos",
            style = ComposeTextStyle(
                fontWeight = ComposeFontWeight.Bold,
                color = colorPrimary,
                fontSize = 16.sp,
            ),
        )
        
        ComposeSpacer(modifier = ComposeModifier.composeHeight(8.dp))
        
        ComposeLazyColumn(modifier = ComposeModifier.composeFillMaxSize()) {
            composeItems(mockEvents) { event ->
                WidgetEventItemPreview(event.event, isTakenToday = event.event.id == "1")
            }
        }
    }
}

@Composable
private fun WidgetEventItemPreview(event: EventEntity, isTakenToday: Boolean) {
    ComposeColumn(
        modifier = ComposeModifier
            .composeFillMaxWidth()
            .composePadding(vertical = 4.dp)
            .composeBackground(colorSurface, shape = RoundedCornerShape(12.dp))
            .composePadding(12.dp),
    ) {
        ComposeRow(
            modifier = ComposeModifier.composeFillMaxWidth(),
            verticalAlignment = ComposeAlignment.CenterVertically,
        ) {
            ComposeBox(
                modifier = ComposeModifier
                    .composeSize(32.dp)
                    .composeBackground(colorPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)),
                contentAlignment = ComposeAlignment.Center,
            ) {
                ComposeText(text = event.icon, style = ComposeTextStyle(fontSize = 16.sp))
            }

            ComposeSpacer(modifier = ComposeModifier.composeWidth(12.dp))

            ComposeColumn(modifier = ComposeModifier.weight(1f)) {
                ComposeText(
                    text = event.title,
                    style = ComposeTextStyle(
                        fontWeight = ComposeFontWeight.Bold,
                        color = if (isTakenToday) colorTextSecondary else colorTextPrimary,
                        fontSize = 14.sp,
                        textDecoration = if (isTakenToday) ComposeTextDecoration.LineThrough else ComposeTextDecoration.None,
                    ),
                )
                ComposeText(
                    text = event.time,
                    style = ComposeTextStyle(
                        color = colorTextSecondary,
                        fontSize = 12.sp,
                        textDecoration = if (isTakenToday) ComposeTextDecoration.LineThrough else ComposeTextDecoration.None,
                    ),
                )
            }
        }
    }
}
