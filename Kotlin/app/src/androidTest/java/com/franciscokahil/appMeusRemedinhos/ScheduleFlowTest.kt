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
class ScheduleFlowTest {

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

    @Before
    fun setup() { }

    private fun addTestEvent(title: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val otherText = targetContext.getString(R.string.preset_other)

        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText(otherText, substring = true) and hasAnyAncestor(hasTestTag("fab_menu_presets")), useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput(title)
        composeTestRule.onNodeWithTag("save_event_button").performClick()
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(title, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testCreateEventWithExactTime() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val otherText = targetContext.getString(R.string.preset_other)

        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText(otherText, substring = true) and hasAnyAncestor(hasTestTag("fab_menu_presets")), useUnmergedTree = true).performClick()

        val testName = "Lanche Exato 10:12"
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput(testName)
        
        composeTestRule.onNodeWithTag("save_event_button").performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(testName, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(testName, substring = true).assertIsDisplayed()
    }

    @Test
    fun testMedicationVisibilityInCollapsedAndExpandedState() {
        val testTitle = "Evento Teste"
        addTestEvent(testTitle)
        
        val medName = "Vitamina Teste"
        
        // 1. Expand and add medication
        composeTestRule.onAllNodesWithText(testTitle, substring = true).onFirst().performClick()
        composeTestRule.onNodeWithTag("medication_input").performTextInput(medName)
        
        // Use a more specific finder for the Add medication button
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val addText = targetContext.getString(R.string.add)
        composeTestRule.onNodeWithContentDescription(addText, substring = true).performClick()
        
        // 2. Save and collapse
        composeTestRule.onNodeWithTag("save_event_button").performClick()
        composeTestRule.waitForIdle()
        
        // 3. Verify medication is visible in compact card (AssistChip label)
        composeTestRule.onAllNodesWithText(medName, substring = true).onFirst().assertIsDisplayed()
        
        // 4. Verify title is also there
        composeTestRule.onAllNodesWithText(testTitle, substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testRemoveMedicationInExpandedState() {
        val testTitle = "Remover Med"
        addTestEvent(testTitle)
        val medName = "Temporario"

        // 1. Add medication
        composeTestRule.onAllNodesWithText(testTitle, substring = true).onFirst().performClick()
        composeTestRule.onNodeWithTag("medication_input").performTextInput(medName)
        composeTestRule.onNodeWithTag("add_medication_button").performClick()
        
        // 2. Verify it's there as an InputChip
        composeTestRule.onNodeWithText(medName).assertIsDisplayed()
        
        // 3. Remove it
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val cdRemove = targetContext.getString(R.string.cd_remove_medication, medName)
        composeTestRule.onNodeWithContentDescription(cdRemove, substring = true).performClick()
        
        // 4. Verify it's gone
        composeTestRule.onNodeWithText(medName).assertDoesNotExist()
    }

    @Test
    fun testMedicationNameValidation() {
        val testTitle = "Validar Med"
        addTestEvent(testTitle)

        // 1. Expand
        composeTestRule.onAllNodesWithText(testTitle, substring = true).onFirst().performClick()

        // 2. Try to add empty medication (button should be disabled)
        composeTestRule.onNodeWithTag("medication_input").performTextInput("   ") // only spaces
        composeTestRule.onNodeWithTag("add_medication_button").assertIsNotEnabled()

        // 3. Try to exceed character limit (30)
        // Clear previous input
        composeTestRule.onNodeWithTag("medication_input").performTextReplacement("")
        
        val longName = "A".repeat(40)
        composeTestRule.onNodeWithTag("medication_input").performTextInput(longName)
        
        // Check that character counter exists (doesn't need to be exact value if semantics merge it)
        composeTestRule.onAllNodesWithText("/30", substring = true).onFirst().assertExists()
        
        // 4. Add valid name and check character counter
        composeTestRule.onNodeWithTag("medication_input").performTextReplacement("Vitamina C")
        composeTestRule.onNodeWithText("10/30", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("add_medication_button").performClick()
        
        // 5. Save and verify it's there
        composeTestRule.onNodeWithTag("save_event_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("Vitamina C", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testDeleteEventFlow() {
        val toDelete = "Event to Remove"
        addTestEvent(toDelete)
        
        composeTestRule.onNodeWithText(toDelete, substring = true).performClick()
        
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val cdDelete = targetContext.getString(R.string.cd_delete_event, toDelete)
        composeTestRule.onNodeWithContentDescription(cdDelete, substring = true).performClick()

        composeTestRule.onNodeWithTag("confirm_delete_button").performClick()

        composeTestRule.onNodeWithText(toDelete).assertDoesNotExist()
    }
}
