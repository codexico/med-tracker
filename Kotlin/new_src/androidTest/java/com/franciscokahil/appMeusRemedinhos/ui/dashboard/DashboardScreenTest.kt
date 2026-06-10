package com.franciscokahil.appMeusRemedinhos.ui.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun eventCardDisplaysCorrectly() {
        composeTestRule.setContent {
            EventCard(
                time = "15:00",
                title = "Lanche",
                medications = listOf("Paracetamol 750mg"),
                isTaken = false,
                onCheckedChange = {}
            )
        }

        // Verifica se os textos passados para o componente aparecem corretamente
        composeTestRule.onNodeWithText("15:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lanche").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paracetamol 750mg").assertIsDisplayed()
    }
}
