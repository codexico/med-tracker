package com.franciscokahil.appMeusRemedinhos

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullUserFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun fullAppFlow() {
        // Wait for onboarding to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Bem-vindo ao Meus Remedinhos")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Onboarding
        composeTestRule.onNodeWithText("Bem-vindo ao Meus Remedinhos").assertIsDisplayed()
        
        // Handle permission dialog if it blocks
        // In instrumented tests, sometimes the OS dialog pops up and steals focus.
        // We can't easily click it via Compose rule, but for this test we focus on app logic.
        
        composeTestRule.onNodeWithText("Começar Agora").performClick()

        // 2. Dashboard
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Meus Remedinhos")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Ao acordar").assertIsDisplayed()

        // 3. Add Custom Event
        composeTestRule.onNodeWithContentDescription("Adicionar Novo Horário").performClick()
        
        composeTestRule.onNodeWithText("Novo Horário").assertIsDisplayed()
        
        val testLabel = "Teste Automatizado"
        // Use placeholder or sibling relation if needed, but text input usually finds by placeholder
        composeTestRule.onNodeWithText("Ex: Lanche da Tarde").performTextInput(testLabel)
        composeTestRule.onNodeWithText("Criar").performClick()

        // 4. Verify added event
        composeTestRule.onNodeWithText(testLabel).assertIsDisplayed()
    }
}
