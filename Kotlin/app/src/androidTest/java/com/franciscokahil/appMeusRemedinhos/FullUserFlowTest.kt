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

@RunWith(AndroidJUnit4::class)
class FullUserFlowTest {

    private val permissionRule = GrantPermissionRule.grant(
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
        .around(permissionRule)
        .around(composeTestRule)

    @Test
    fun fullAppFlow() {
        // 1. Dashboard State
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("empty_state").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Click FAB
        composeTestRule.onNodeWithTag("add_event_fab").performClick()

        // 3. Select "Other" preset (PT: Outro, EN: Other)
        composeTestRule.onNode(hasText("Outro", substring = true) or hasText("Other", substring = true)).performClick()
        
        // 4. Verify New Reminder header (PT: Novo Horário, EN: New Reminder)
        composeTestRule.onNode(hasText("Novo Horário", substring = true) or hasText("New Reminder", substring = true), useUnmergedTree = true).assertIsDisplayed()
        
        val testLabel = "Teste Automatizado"
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput(testLabel)
        
        // Add a medication to ensure status can be toggled
        composeTestRule.onNodeWithTag("medication_input").performTextInput("Med Teste")
        composeTestRule.onNodeWithTag("add_medication_button").performClick()

        composeTestRule.onNodeWithTag("confirm_add_event").performClick()

        // 5. Verify added event
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(testLabel, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(testLabel, substring = true).assertIsDisplayed()
        
        // 6. Toggle status
        composeTestRule.onAllNodesWithTag("event_checkbox").onFirst().performClick()
        
        // 7. Verify taken status in content description (PT: Concluído, EN: Done)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodes(hasContentDescription("Concluído", substring = true) or hasContentDescription("Done", substring = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodes(hasContentDescription("Concluído", substring = true) or hasContentDescription("Done", substring = true), useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
