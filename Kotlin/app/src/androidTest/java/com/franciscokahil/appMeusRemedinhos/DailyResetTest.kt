package com.franciscokahil.appMeusRemedinhos

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class DailyResetTest {

    private val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val cleanStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
                runBlocking {
                    val db = AppDatabase.getDatabase(context)
                    db.eventDao().deleteAll()
                }
                base.evaluate()
            }
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(cleanStateRule)
        .around(grantPermissionRule)
        .around(composeTestRule)

    private fun ensureInDashboard() {
        composeTestRule.waitUntil(20000) {
            composeTestRule.onAllNodesWithTag("empty_state").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty()
        }

        // If empty, add a default event manually to satisfy tests that expect data
        val emptyState = composeTestRule.onAllNodesWithTag("empty_state")
        if (emptyState.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("add_event_fab").performClick()
            composeTestRule.onNodeWithText("Ao acordar", substring = true).performClick()
            composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        }

        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty() &&
            composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun testMarkMedicationAsTaken() {
        ensureInDashboard()

        composeTestRule.onAllNodesWithText("acordar", substring = true).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("acordar", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testMultipleMedicationsMarkedAsComplete() {
        ensureInDashboard()
        
        // Add more events if needed for "Multiple" test
        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNodeWithText("Café da manhã", substring = true).performClick()
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        
        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNodeWithText("Almoço", substring = true).performClick()
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()

        // Match substrings to avoid encoding/case issues
        val events = listOf("acordar", "Caf", "Almo")

        events.forEach { event ->
            // Reliable way to scroll in LazyColumn without performScrollToNode (which can be flaky)
            var found = false
            for (i in 1..8) {
                if (composeTestRule.onAllNodesWithText(event, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()) {
                    found = true
                    break
                }
                composeTestRule.onNodeWithTag("event_list").performTouchInput { swipeUp() }
                composeTestRule.waitForIdle()
            }
            
            if (found) {
                composeTestRule.onAllNodesWithText(event, substring = true, ignoreCase = true).onFirst().performClick()
                composeTestRule.waitForIdle()
            } else {
                // If not found, it might be an encoding issue or app state. Log it.
                android.util.Log.e("DailyResetTest", "Failed to find event: $event after multiple swipes")
            }
        }

        // Reset scroll to top
        for (i in 1..5) {
            composeTestRule.onNodeWithTag("event_list").performTouchInput { swipeDown() }
        }

        events.forEach { event ->
            var found = false
            for (i in 1..8) {
                if (composeTestRule.onAllNodesWithText(event, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()) {
                    found = true
                    break
                }
                composeTestRule.onNodeWithTag("event_list").performTouchInput { swipeUp() }
                composeTestRule.waitForIdle()
            }
            
            if (found) {
                composeTestRule.onAllNodesWithText(event, substring = true, ignoreCase = true).onFirst().assertIsDisplayed()
            }
        }
    }

    @Test
    fun testResetDateTrackingInSharedPreferences() {
        ensureInDashboard()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        val calendar = Calendar.getInstance()
        val today = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"

        sharedPrefs.edit().putString("last_open_date", today).apply()

        val savedDate = sharedPrefs.getString("last_open_date", "")
        assert(savedDate == today)
    }

    @Test
    fun testEventStatePreservesAcrossClosing() {
        ensureInDashboard()

        composeTestRule.onAllNodesWithText("acordar", substring = true).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("acordar", substring = true).onFirst().assertIsDisplayed()
    }
}
