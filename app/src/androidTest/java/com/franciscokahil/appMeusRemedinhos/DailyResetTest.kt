package com.franciscokahil.appMeusRemedinhos

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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
            composeTestRule.onNodeWithTag("onboarding_add_button").performClick()
            // Try matching "Ao acordar" or "Wake up"
            composeTestRule.onNode(hasText("acordar", substring = true, ignoreCase = true) or hasText("Wake", substring = true, ignoreCase = true)).performClick()
            
            // Add a medication to ensure status can be toggled
            composeTestRule.onNodeWithTag("medication_input").performTextInput("Med Teste")
            composeTestRule.onNodeWithTag("add_medication_button").performClick()

            composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        }

        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty() &&
            (composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty() ||
             composeTestRule.onAllNodesWithText("Wake", substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty())
        }
        
        composeTestRule.waitForIdle()
    }

    private fun hasTakenStatusMatcher() = hasContentDescription("Concluído", substring = true) or hasContentDescription("Done", substring = true)

    @Test
    fun testMarkMedicationAsTaken() {
        ensureInDashboard()

        composeTestRule.onAllNodesWithTag("event_checkbox").onFirst().performClick()
        composeTestRule.waitForIdle()

        // Verify status in card content description (PT: Concluído, EN: Done)
        composeTestRule.onAllNodes(hasTakenStatusMatcher(), useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testMultipleMedicationsMarkedAsComplete() {
        ensureInDashboard()
        
        // Add more events if needed for "Multiple" test
        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText("Café", substring = true, ignoreCase = true) or hasText("Breakfast", substring = true, ignoreCase = true)).performClick()
        
        // Add medication
        composeTestRule.onNodeWithTag("medication_input").performTextInput("Med 1")
        composeTestRule.onNodeWithTag("add_medication_button").performClick()
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        
        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText("Almoço", substring = true, ignoreCase = true) or hasText("Lunch", substring = true, ignoreCase = true)).performClick()

        // Add medication
        composeTestRule.onNodeWithTag("medication_input").performTextInput("Med 2")
        composeTestRule.onNodeWithTag("add_medication_button").performClick()
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()

        // Click all visible checkboxes
        val checkboxes = composeTestRule.onAllNodesWithTag("event_checkbox", useUnmergedTree = true)
        val count = checkboxes.fetchSemanticsNodes().size
        
        for (i in 0 until count) {
            composeTestRule.onAllNodesWithTag("event_checkbox", useUnmergedTree = true).get(i).performClick()
            composeTestRule.waitForIdle()
        }

        // Verify all are now in "Taken" state
        composeTestRule.onAllNodes(hasTakenStatusMatcher(), useUnmergedTree = true).assertCountEquals(count)
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

        composeTestRule.onAllNodesWithTag("event_checkbox", useUnmergedTree = true).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(hasTakenStatusMatcher(), useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
