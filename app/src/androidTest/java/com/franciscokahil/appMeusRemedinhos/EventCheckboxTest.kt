package com.franciscokahil.appMeusRemedinhos

import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCheckboxTest {

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
    fun testCheckboxToggleForEventWithoutMedications() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        
        // 1. Create an event without medications
        composeTestRule.onNodeWithTag("onboarding_add_button").performClick()
        
        val breakfastLabel = targetContext.getString(R.string.breakfast)
        composeTestRule.onNodeWithText(breakfastLabel, substring = true).performClick()
        
        // Ensure we are in the card
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("confirm_add_event").fetchSemanticsNodes().isNotEmpty()
        }
        
        // Save without adding meds
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        
        // Handle Permission Explanation Dialog if it appears
        val confirmPermissionTag = "permission_confirm_button"
        if (composeTestRule.onAllNodesWithTag(confirmPermissionTag).fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag(confirmPermissionTag).performClick()
        }

        // Wait for it to appear on dashboard and be collapsed
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("event_checkbox").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Click the checkbox
        composeTestRule.onNodeWithTag("event_checkbox").performClick()

        // 3. Verify it is checked 
        composeTestRule.onNodeWithTag("event_checkbox").assertIsOn()
    }
}
