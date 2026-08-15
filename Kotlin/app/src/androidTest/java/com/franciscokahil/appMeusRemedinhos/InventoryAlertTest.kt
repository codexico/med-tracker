package com.franciscokahil.appMeusRemedinhos

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.franciscokahil.appMeusRemedinhos.data.local.AppDatabase
import com.franciscokahil.appMeusRemedinhos.data.local.Medication
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class InventoryAlertTest {

    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val cleanStateRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                runBlocking<Unit> {
                    val db = AppDatabase.getDatabase(context)
                    db.clearAllTables()
                    
                    // Insert a medication with low stock
                    db.medicationDao().insertMedication(
                        Medication(
                            id = "low_stock_med",
                            name = "Remedio Alerta",
                            currentStock = 2f,
                            lowStockThreshold = 5f
                        )
                    )
                    // Insert a medication with low stock and unit
                    db.medicationDao().insertMedication(
                        Medication(
                            id = "low_stock_unit_med",
                            name = "Remedio com Unidade",
                            currentStock = 3f,
                            lowStockThreshold = 10f,
                            dosageUnit = "comprimidos"
                        )
                    )
                    // Insert a medication with decimal stock
                    db.medicationDao().insertMedication(
                        Medication(
                            id = "decimal_stock_med",
                            name = "Remedio Decimal",
                            currentStock = 4.5f,
                            lowStockThreshold = 0f
                        )
                    )
                }
                base.evaluate()
            }
        }
    }

    @get:Rule
    val ruleChain: RuleChain = RuleChain
        .outerRule(cleanStateRule)
        .around(composeTestRule)

    @Test
    fun testStockFormatting() {
        val inventoryCd = "Stock"

        // 1. Navigate to Inventory
        composeTestRule.onNodeWithContentDescription(inventoryCd, substring = true).performClick()

        // 2. Verify "Remedio Alerta" shows "2" not "2.0"
        composeTestRule.onNodeWithText("2 ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("2.0 ", substring = true).assertDoesNotExist()

        // 3. Verify "Remedio Decimal" shows "4.5"
        composeTestRule.onNodeWithText("4.5 ", substring = true).assertIsDisplayed()
    }

    @Test
    fun testLowStockAlertAndUnitVisibilityInInventory() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inventoryCd = "Stock" // From DashboardScreen.kt actions
        val stockRemainingLabel = targetContext.getString(R.string.stock_remaining_label, "2")

        // 1. Navigate to Inventory
        composeTestRule.onNodeWithContentDescription(inventoryCd, substring = true).performClick()

        // 2. Verify "Remedio Alerta" (empty unit) shows formatted stock
        composeTestRule.onNodeWithText("Remedio Alerta", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText(stockRemainingLabel, substring = true).assertIsDisplayed()

        // 3. Verify "Remedio com Unidade" shows custom unit
        composeTestRule.onNodeWithText("Remedio com Unidade", substring = true).assertIsDisplayed()
        val stockWithUnitLabel = targetContext.getString(R.string.stock_remaining_with_unit_label, "3", "comprimidos")
        composeTestRule.onNodeWithText(stockWithUnitLabel, substring = true).assertIsDisplayed()

        // 4. Verify the alert emoji is visible via tag
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("low_stock_emoji", useUnmergedTree = true).fetchSemanticsNodes().size == 2
        }
        composeTestRule.onAllNodesWithTag("low_stock_emoji", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
