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
import org.junit.Before
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

    @Before
    fun setup() {
        // Activity is launched after cleanStateRule
    }

    @Test
    fun fullAppFlow() {
        // 1. Dashboard State - can be empty OR seeded
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("empty_state").fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty()
        }

        // If it's empty, we might see the onboarding title
        val emptyTitle = composeTestRule.onAllNodesWithText("esqueça seus remédios", substring = true)
        if (emptyTitle.fetchSemanticsNodes().isNotEmpty()) {
            emptyTitle[0].assertIsDisplayed()
        }

        // 2. Click FAB to open menu
        composeTestRule.onNodeWithTag("add_event_fab").performClick()

        // 3. Select "Outro" preset (Other in English)
        composeTestRule.onNode(hasText("Outro", substring = true) or hasText("Other", substring = true)).performClick()
        
        // 4. Fill and Create
        composeTestRule.onNode(hasText("Novo Horário", substring = true) or hasText("New Reminder", substring = true)).assertIsDisplayed()
        
        val testLabel = "Teste Automatizado"
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput(testLabel)
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()

        // 5. Verify added event on Dashboard
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(testLabel, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(testLabel, substring = true).assertIsDisplayed()
        
        // 6. Toggle status
        // Use the test tag and onFirst to handle potential seeded events
        composeTestRule.onAllNodesWithTag("event_checkbox").onFirst().performClick()
        
        // Verify taken status (semantics usually merge text into content description)
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val takenStatus = targetContext.getString(R.string.status_taken)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithContentDescription(takenStatus, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithContentDescription(takenStatus, substring = true).onFirst().assertIsDisplayed()
    }
}
