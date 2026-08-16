package com.franciscokahil.appMeusRemedinhos

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.DoseHistoryEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class WidgetNavigationTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val cleanStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                runBlocking {
                    val db = AppDatabase.getDatabase(context)
                    db.clearAllTables()
                }
                base.evaluate()
            }
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(cleanStateRule)
        .around(composeTestRule)

    @Test
    fun testWidgetDeepLinkNavigatesFromInventoryToDashboard() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(targetContext)
        
        val eventId = "widget_event_123"
        val eventTitle = "Evento via Widget"
        
        runBlocking {
            database.eventDao().insertEvent(
                EventEntity(
                    id = eventId,
                    title = eventTitle,
                    time = "09:00",
                    type = EventType.OTHER,
                )
            )
        }

        // 1. Initial State: Dashboard
        composeTestRule.onNodeWithText(eventTitle, substring = true).assertIsDisplayed()

        // 2. Navigate to Inventory
        val inventoryCd = "Stock"
        composeTestRule.onNodeWithContentDescription(inventoryCd, substring = true).performClick()
        
        val inventoryTitle = targetContext.getString(R.string.stock_management_title)
        composeTestRule.onNodeWithText(inventoryTitle).assertIsDisplayed()

        // 3. Simulate clicking the Widget (Deep Link Intent)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("meusremedinhos://event/$eventId")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage(targetContext.packageName)
        }
        targetContext.startActivity(intent)

        // 4. Verify it returns to Dashboard and shows the event title
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(eventTitle, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(eventTitle, substring = true).assertIsDisplayed()
        
        // 5. Verify Inventory title is no longer present
        composeTestRule.onNodeWithText(inventoryTitle).assertDoesNotExist()
    }

    @Test
    fun testWidgetSnapshotConsistency() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(targetContext)
        
        val pastEventId = "past_event_id"
        // Force a time that is definitely in the past (00:01)
        val pastTime = "00:01" 
        
        runBlocking {
            database.eventDao().insertEvent(
                EventEntity(
                    id = pastEventId,
                    title = "Past Event",
                    time = pastTime,
                    type = EventType.OTHER,
                    isEnabled = true,
                )
            )
            
            // This mirrors the logic in MedicationWidget.provideGlance
            val snapshot = database.eventDao().getAllEventsWithMedicationsSnapshot()
                .filter { it.event.isEnabled }
            
            val found = snapshot.any { it.event.id == pastEventId }
            assertTrue("Enabled past event must be present in widget data snapshot", found)
            
            // Verify disabling it removes it from widget view
            database.eventDao().updateEvent(
                EventEntity(
                    id = pastEventId,
                    title = "Past Event",
                    time = pastTime,
                    type = EventType.OTHER,
                    isEnabled = false,
                )
            )
            
            val snapshotAfterDisable = database.eventDao().getAllEventsWithMedicationsSnapshot()
                .filter { it.event.isEnabled }
                
            val foundAfterDisable = snapshotAfterDisable.any { it.event.id == pastEventId }
            assertTrue("Disabled event must be excluded from widget view", !foundAfterDisable)
        }
    }

    @Test
    fun testWidgetHistoryInclusion() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(targetContext)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val eventId = "taken_event_id"
        
        runBlocking {
            database.eventDao().insertEvent(
                EventEntity(
                    id = eventId,
                    title = "Taken Event",
                    time = "08:00",
                    type = EventType.OTHER,
                )
            )
            
            // Mark as taken
            database.doseHistoryDao().insertDose(
                DoseHistoryEntity(
                    eventId = eventId,
                    medicationId = "some_med",
                    timestamp = System.currentTimeMillis(),
                    amountTaken = 1f,
                    status = "TAKEN",
                )
            )
            
            val takenIds = database.doseHistoryDao().getTakenEventIdsToday(todayStart).toSet()
            assertTrue("Taken event must be in the history set for today", takenIds.contains(eventId))
        }
    }
}
