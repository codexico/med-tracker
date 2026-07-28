package com.franciscokahil.appMeusRemedinhos

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class DashboardRefinementTest {

    private val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val cleanStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
                
                val db = AppDatabase.getDatabase(context)
                db.clearAllTables()
                base.evaluate()
            }
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(cleanStateRule)
        .around(grantPermissionRule)
        .around(composeTestRule)

    @Before
    fun setup() { }

    private fun addTestEvent(title: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val otherText = targetContext.getString(R.string.preset_other)

        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText(otherText, substring = true) and hasAnyAncestor(hasTestTag("fab_menu_presets")), useUnmergedTree = true).performClick()
        
        // Use the new test tag
        composeTestRule.onNodeWithTag("event_title_input").performTextInput(title)
        
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(title, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testOnboardingTooltipsVisibleOnlyWhenEmpty() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val startTooltip = targetContext.getString(R.string.onboarding_fab_tooltip)
        val chooseTimeTooltip = targetContext.getString(R.string.onboarding_fab_menu_tooltip)
        val otherText = targetContext.getString(R.string.preset_other)

        // 1. Initially empty, tooltip should be visible
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText(startTooltip, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(startTooltip, substring = true).assertIsDisplayed()

        // 2. Open FAB Menu
        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        
        // 3. Menu tooltip should be visible
        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithText(chooseTimeTooltip, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(chooseTimeTooltip, substring = true).assertIsDisplayed()

        // 4. Add an event
        composeTestRule.onNodeWithText(otherText, substring = true).performClick()
        composeTestRule.onNodeWithTag("event_title_input").performTextInput("Remedio de Teste")
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        
        // Wait for the event to appear, which signals that tooltips SHOULD be gone
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Remedio de Teste", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle()

        // 5. Tooltip should DISAPPEAR as we now have an event
        Thread.sleep(1000)

        composeTestRule.onNodeWithText(startTooltip, substring = true).assertIsNotDisplayed()
        composeTestRule.onNodeWithText(chooseTimeTooltip, substring = true).assertIsNotDisplayed()
    }

    @Test
    fun testFabHidesOnExpansion() {
        val testTitle = "Teste FAB"
        addTestEvent(testTitle)
        
        // FAB should be visible
        composeTestRule.onNodeWithTag("add_event_fab").assertIsDisplayed()
        
        // Expand card
        composeTestRule.onNodeWithText(testTitle, substring = true).performClick()
        composeTestRule.waitForIdle()
        
        // FAB should be hidden
        composeTestRule.onNodeWithTag("add_event_fab").assertDoesNotExist()
        
        // Collapse
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val cancelText = targetContext.getString(R.string.cancel)
        composeTestRule.onNodeWithText(cancelText, substring = true).performClick()
        composeTestRule.waitForIdle()
        
        // FAB should return
        composeTestRule.onNodeWithTag("add_event_fab").assertIsDisplayed()
    }

    @Test
    fun testMedicationAutoSaveOnSaveClick() {
        val testTitle = "Teste Medicamento"
        addTestEvent(testTitle)
        
        val medName = "Remedio Esquecido"
        
        // 1. Expand
        composeTestRule.onNodeWithText(testTitle, substring = true).performClick()
        composeTestRule.waitForIdle()
        
        // 2. Type med name but DON'T click the '+' button
        composeTestRule.onNodeWithTag("medication_input").performTextInput(medName)
        
        // 3. Click "Salvar"
        composeTestRule.onNodeWithTag("save_event_button").performClick()
        composeTestRule.waitForIdle()
        
        // 4. Verify it was saved and is visible in the compact card
        composeTestRule.onNodeWithText(medName).assertIsDisplayed()
    }

    @Test
    fun testExpandedCardFillsScreenAndHidesOthers() {
        addTestEvent("Evento 1")
        addTestEvent("Evento 2")
        
        // Expand "Evento 1"
        composeTestRule.onNodeWithText("Evento 1", substring = true).performClick()
        composeTestRule.waitForIdle()
        
        // "Evento 2" should be hidden from the view entirely (filtered out in LazyColumn)
        composeTestRule.onNodeWithText("Evento 2", substring = true).assertDoesNotExist()
        
        // Verify we are indeed expanded
        composeTestRule.onNode(hasStateDescription("Expandido") or hasStateDescription("Expanded")).assertIsDisplayed()
    }
}
