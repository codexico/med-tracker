package com.franciscokahil.appMeusRemedinhos

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.EventType
import com.franciscokahil.appMeusRemedinhos.data.local.EventEntity
import com.franciscokahil.appMeusRemedinhos.widget.MedicationWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstEventWidgetSyncTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Grant permissions via shell for API 33+ to avoid system dialog interruption
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "pm grant ${targetContext.packageName} android.permission.POST_NOTIFICATIONS"
            )
        }
        
        runBlocking {
            val db = AppDatabase.getDatabase(targetContext)
            db.eventDao().deleteAll()
            db.medicationDao().deleteAll()
            db.doseHistoryDao().deleteAll()
        }
    }

    @Test
    fun testFirstEventCreationSyncsWithWidgetSnapshot() {
        // 1. Initial State: Empty
        composeTestRule.onNodeWithTag("empty_state").assertIsDisplayed()
        
        // 2. Click Add to expand FAB
        composeTestRule.onNodeWithTag("onboarding_add_button").performClick()
        
        // 3. Choose a preset (e.g., Breakfast)
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val breakfastLabel = targetContext.getString(R.string.breakfast)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithText(breakfastLabel, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(breakfastLabel, substring = true).performClick()
        
        // 4. In EventCard (expanded), save it via tag.
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("confirm_add_event").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        
        // 5. Handle Permission Explanation Dialog
        val confirmPermissionTag = "permission_confirm_button"
        if (composeTestRule.onAllNodesWithTag(confirmPermissionTag).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag(confirmPermissionTag).performClick()
        }
        
        // 6. Wait for event to appear in DB
        val database = AppDatabase.getDatabase(targetContext)
        composeTestRule.waitUntil(15000) {
            runBlocking {
                database.eventDao().getEventCount() > 0
            }
        }
        
        // 7. Verify the widget snapshot logic
        runBlocking {
            val snapshot = database.eventDao().getAllEventsWithMedicationsSnapshot()
            assertTrue("Widget snapshot should contain the newly created event", snapshot.isNotEmpty())
            
            val event = snapshot.first().event
            assertTrue("Event should be enabled", event.isEnabled)
        }
        
        // 8. Final check: Dashboard no longer empty
        composeTestRule.onNodeWithTag("empty_state").assertDoesNotExist()
        composeTestRule.onNodeWithTag("event_list").assertIsDisplayed()
    }

    @Test
    fun testManualWidgetUpdateTriggersDataLoad() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val database = AppDatabase.getDatabase(targetContext)
        
        runBlocking {
            // 1. Insert event directly
            val eventId = "manual_test_id"
            database.eventDao().insertEvent(
                EventEntity(
                    id = eventId,
                    title = "Manual Event",
                    time = "10:00",
                    type = EventType.MORNING
                )
            )
            
            // 2. Trigger widget update manually
            MedicationWidget().updateAll(targetContext)
            
            // 3. Verify snapshot contains it
            val snapshot = database.eventDao().getAllEventsWithMedicationsSnapshot()
            assertTrue("Snapshot should contain manual event", snapshot.any { it.event.id == eventId })
        }
    }
}
