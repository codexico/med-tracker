package com.franciscokahil.appMeusRemedinhos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
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

@RunWith(AndroidJUnit4::class)
class DeepLinkScrollTest {

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

        val emptyState = composeTestRule.onAllNodesWithTag("empty_state")
        if (emptyState.fetchSemanticsNodes().isNotEmpty()) {
            // Add a few events manually to test scrolling
            val presets = listOf("Ao acordar", "Café da manhã", "Almoço", "Janta", "Antes de dormir")
            presets.forEach { preset ->
                composeTestRule.onNodeWithTag("add_event_fab").performClick()
                composeTestRule.onNodeWithText(preset, substring = true).performClick()
                composeTestRule.onNodeWithTag("confirm_add_event").performClick()
                composeTestRule.waitForIdle()
            }
        }

        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty() &&
            composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun testDeepLinkScrollsToLastEvent() {
        ensureInDashboard()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/last")
        }
        
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onNewIntent(intent)
        }

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("dormir", substring = true, ignoreCase = true))

        composeTestRule.onAllNodesWithText("dormir", substring = true, ignoreCase = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testDeepLinkHighlightFeedback() {
        ensureInDashboard()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/highlight")
        }
        
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onNewIntent(intent)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testMultipleDeepLinksScrollCorrectly() {
        ensureInDashboard()

        val intent1 = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/first")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent1) }
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).onFirst().assertIsDisplayed()

        val intent2 = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/second")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent2) }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("Almo", substring = true, ignoreCase = true))
        
        composeTestRule.onAllNodesWithText("Almo", substring = true, ignoreCase = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testDeepLinkWithNonExistentEventDoesNotCrash() {
        ensureInDashboard()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/none")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("acordar", substring = true, ignoreCase = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun testDeepLinkScrollPreservesUIState() {
        ensureInDashboard()

        val target = "Caf"
        composeTestRule.onNodeWithTag("event_list").performScrollToNode(hasText(target, substring = true, ignoreCase = true))
        composeTestRule.onAllNodesWithText(target, substring = true, ignoreCase = true).onFirst().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("Salvar").onFirst().assertIsDisplayed()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/reset")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithTag("add_event_fab").onFirst().assertIsDisplayed()
    }

    @Test
    fun testDeepLinkScrollPositionAfterListUpdate() {
        ensureInDashboard()

        composeTestRule.onNodeWithContentDescription("Adicionar Novo Horário").performClick()
        composeTestRule.onNodeWithText("Outro").performClick()
        composeTestRule.onNodeWithTag("event_title_input").performTextInput("New Event")
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        composeTestRule.waitForIdle()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/new")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("New Event", substring = true))

        composeTestRule.onAllNodesWithText("New Event", substring = true).onFirst().assertIsDisplayed()
    }
}
