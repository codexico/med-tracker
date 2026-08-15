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
    fun testLowStockAlertVisibilityInInventory() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val lowStockLabel = targetContext.getString(R.string.low_stock_label)
        val inventoryCd = "Stock" // From DashboardScreen.kt actions

        // 1. Navigate to Inventory
        composeTestRule.onNodeWithContentDescription(inventoryCd, substring = true).performClick()

        // 2. Verify the medication is listed
        composeTestRule.onNodeWithText("Remedio Alerta", substring = true).assertIsDisplayed()

        // 3. Verify the "LOW STOCK" alert is visible
        composeTestRule.onNodeWithText(lowStockLabel, substring = true, ignoreCase = true).assertIsDisplayed()
    }
}
