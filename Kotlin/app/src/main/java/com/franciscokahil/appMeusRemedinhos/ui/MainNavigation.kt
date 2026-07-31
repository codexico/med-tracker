package com.franciscokahil.appMeusRemedinhos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.franciscokahil.appMeusRemedinhos.ui.dashboard.DashboardScreen
import com.franciscokahil.appMeusRemedinhos.ui.inventory.InventoryScreen

@Composable
fun MainNavigation(
    highlightedId: String? = null,
    onHighlightedConsumed: () -> Unit
) {
    val startDestination = "dashboard"
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToInventory = { navController.navigate("inventory") },
                highlightedId = highlightedId,
                onHighlightedConsumed = onHighlightedConsumed
            )
        }
        composable("inventory") {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
