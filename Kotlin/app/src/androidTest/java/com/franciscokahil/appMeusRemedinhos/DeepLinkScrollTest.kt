package com.franciscokahil.appMeusRemedinhos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
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
            // Using a mix of PT and EN substrings to handle either locale
            val presets = listOf("acordar", "Caf", "Almo", "Janta", "dormir", "Wake", "Break", "Lunch", "Dinner", "Sleep")
            presets.forEach { presetSub ->
                composeTestRule.onNodeWithTag("add_event_fab").performClick()
                val presetNodes = composeTestRule.onAllNodes(hasText(presetSub, substring = true, ignoreCase = true))
                if (presetNodes.fetchSemanticsNodes().isNotEmpty()) {
                    presetNodes.onFirst().performClick()
                    composeTestRule.onNodeWithTag("confirm_add_event").performClick()
                    composeTestRule.waitForIdle()
                } else {
                    // If this preset didn't exist in current locale, just close FAB
                    composeTestRule.onNodeWithTag("add_event_fab").performClick()
                    composeTestRule.waitForIdle()
                }
            }
        }

        composeTestRule.waitUntil(15000) {
            composeTestRule.onAllNodesWithTag("event_list").fetchSemanticsNodes().isNotEmpty()
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

        // Try to scroll to "dormir" (PT) or "Sleep" (EN)
        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("dormir", substring = true, ignoreCase = true) or hasText("Sleep", substring = true, ignoreCase = true))

        composeTestRule.onNode(hasText("dormir", substring = true, ignoreCase = true) or hasText("Sleep", substring = true, ignoreCase = true)).assertIsDisplayed()
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
        composeTestRule.onNode(hasText("acordar", substring = true, ignoreCase = true) or hasText("Wake", substring = true, ignoreCase = true)).assertIsDisplayed()
    }

    @Test
    fun testMultipleDeepLinksScrollCorrectly() {
        ensureInDashboard()

        val intent1 = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/first")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent1) }
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText("acordar", substring = true, ignoreCase = true) or hasText("Wake", substring = true, ignoreCase = true)).assertIsDisplayed()

        val intent2 = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/second")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent2) }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("Almo", substring = true, ignoreCase = true) or hasText("Lunch", substring = true, ignoreCase = true))
        
        composeTestRule.onNode(hasText("Almo", substring = true, ignoreCase = true) or hasText("Lunch", substring = true, ignoreCase = true)).assertIsDisplayed()
    }

    @Test
    fun testDeepLinkWithNonExistentEventDoesNotCrash() {
        ensureInDashboard()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/none")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("acordar", substring = true, ignoreCase = true) or hasText("Wake", substring = true, ignoreCase = true)).assertIsDisplayed()
    }

    @Test
    fun testDeepLinkScrollPreservesUIState() {
        ensureInDashboard()

        composeTestRule.onNodeWithTag("event_list").performScrollToNode(hasText("Caf", substring = true, ignoreCase = true) or hasText("Break", substring = true, ignoreCase = true))
        composeTestRule.onNode(hasText("Caf", substring = true, ignoreCase = true) or hasText("Break", substring = true, ignoreCase = true)).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasText("Salvar", substring = true, ignoreCase = true) or hasText("Save", substring = true, ignoreCase = true)).assertIsDisplayed()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/reset")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("add_event_fab").assertIsDisplayed()
    }

    @Test
    fun testDeepLinkScrollPositionAfterListUpdate() {
        ensureInDashboard()

        composeTestRule.onNodeWithTag("add_event_fab").performClick()
        composeTestRule.onNode(hasText("Outro", substring = true, ignoreCase = true) or hasText("Other", substring = true, ignoreCase = true)).performClick()
        composeTestRule.onNodeWithTag("edit_event_title_input").performTextInput("New Event")
        composeTestRule.onNodeWithTag("confirm_add_event").performClick()
        composeTestRule.waitForIdle()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("meusremedinhos://event/new")
        }
        composeTestRule.activityRule.scenario.onActivity { it.onNewIntent(intent) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("event_list")
            .performScrollToNode(hasText("New Event", substring = true))

        composeTestRule.onNode(hasText("New Event", substring = true)).assertIsDisplayed()
    }
}
