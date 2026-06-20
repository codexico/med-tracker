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
        val emptyTitle = composeTestRule.onAllNodesWithText("Nunca esqueça seus remédios", substring = true)
        if (emptyTitle.fetchSemanticsNodes().isNotEmpty()) {
            emptyTitle[0].assertIsDisplayed()
        }

        // 2. Click FAB to open menu
        composeTestRule.onNodeWithContentDescription("Adicionar Novo Horário").performClick()

        // 3. Select "Outro" preset
        composeTestRule.onNodeWithText("Outro").performClick()
        
        // 4. Fill and Create
        composeTestRule.onNodeWithText("Novo Horário").assertIsDisplayed()
        
        val testLabel = "Teste Automatizado"
        composeTestRule.onNodeWithTag("event_title_input").performTextInput(testLabel)
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
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithContentDescription("Tomado", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithContentDescription("Tomado", substring = true).onFirst().assertIsDisplayed()
    }
}
