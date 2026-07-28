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
class AccessibilityTest {

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
    fun setup() {
        // Dashboard should be visible
    }

    private fun addTestEvent(title: String) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val otherText = targetContext.getString(R.string.preset_other)

        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        // Use unmerged tree to find the preset item inside the merged menu
        composeTestRule.onNode(hasText(otherText, substring = true) and hasAnyAncestor(hasTestTag("fab_menu_presets")), useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithTag("event_title_input").performTextInput(title)
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText(title, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testEventCardHasSemanticDescriptions() {
        val testTitle = "Teste Acessibilidade"
        addTestEvent(testTitle)

        // Check if card has grouped content descriptions
        composeTestRule.onNode(
            hasContentDescription(testTitle, substring = true) and
            hasContentDescription("12:00", substring = true),
            useUnmergedTree = false
        ).assertExists()

        // Check icon description
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val cdIcon = targetContext.getString(R.string.cd_event_icon)
        composeTestRule.onAllNodesWithContentDescription(cdIcon, useUnmergedTree = true).onFirst().assertExists()

        // Check edit button description
        composeTestRule.onAllNodesWithTag("edit_event_button").onFirst().assertIsDisplayed()

        // Check checkbox description
        composeTestRule.onAllNodesWithTag("event_checkbox").onFirst().assertIsDisplayed()
    }

    @Test
    fun testExpandedCardAccessibility() {
        val testTitle = "Teste Expansão"
        addTestEvent(testTitle)

        composeTestRule.onNodeWithText(testTitle, substring = true).performClick()
        composeTestRule.waitForIdle()

        // Verify expanded state in semantics
        composeTestRule.onNode(hasStateDescription("Expandido") or hasStateDescription("Expanded")).assertExists()

        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Verify "Novo medicamento" description exists
        val medNamePlaceholder = targetContext.getString(R.string.med_name_placeholder)
        composeTestRule.onNodeWithText(medNamePlaceholder, useUnmergedTree = true, substring = true).assertIsDisplayed()
        
        // Verify delete event description
        val cdDeleteEvent = targetContext.getString(R.string.cd_delete_event, testTitle)
        composeTestRule.onAllNodesWithContentDescription(cdDeleteEvent, substring = true, useUnmergedTree = true).onFirst().assertExists()
    }
}
