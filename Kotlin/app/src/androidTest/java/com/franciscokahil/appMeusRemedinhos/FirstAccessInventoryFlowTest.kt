package com.franciscokahil.appMeusRemedinhos

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class FirstAccessInventoryFlowTest {

    private val permissionRule = androidx.test.rule.GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Rule to clean database and preferences before each test
    private val cleanStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
                
                runBlocking<Unit> {
                    val db = AppDatabase.getDatabase(context)
                    db.eventDao().deleteAll()
                    db.medicationDao().deleteAll()
                    db.doseHistoryDao().deleteAll()
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
    fun testFirstAccessManageStockFlow() {
        // 1. Initial Dashboard (Empty State)
        composeTestRule.onNodeWithTag("empty_state").assertIsDisplayed()

        // 2. Click "Add new time"
        composeTestRule.onNodeWithTag("onboarding_add_button").performClick()

        // 3. Select "Other" preset (PT: Outro, EN: Other)
        composeTestRule.onNode(hasText("Outro", substring = true, ignoreCase = true) or 
                           hasText("Other", substring = true, ignoreCase = true)).performClick()

        // 4. Fill event title and medication name
        val testEventLabel = "Evento Teste"
        val testMedName = "Med de Teste"
        
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput(testEventLabel)
        composeTestRule.onNodeWithTag("medication_input").performTextInput(testMedName)

        // 5. Click "Manage Stock"
        composeTestRule.onNodeWithTag("manage_stock_button").performClick()

        // 6. Verify we are in Inventory screen and the med is there
        // With permission granted, it should navigate immediately
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(testMedName, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText(testMedName, substring = true).onFirst().assertIsDisplayed()

        // 8. Go back
        composeTestRule.onNode(hasContentDescription("Back", substring = true, ignoreCase = true) or 
                           hasContentDescription("Voltar", substring = true, ignoreCase = true)).performClick()

        // 9. Verify event is on Dashboard
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(testEventLabel, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(testEventLabel, substring = true).assertIsDisplayed()
    }
}
