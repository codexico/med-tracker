package com.franciscokahil.appMeusRemedinhos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.franciscokahil.appMeusRemedinhos.ui.dashboard.DashboardScreen
import com.franciscokahil.appMeusRemedinhos.ui.inventory.InventoryScreen

@Composable
fun MainNavigation(
    highlightedId: String? = null,
    onHighlightedConsumed: () -> Unit
) {
    val startDestination = "dashboard"
    val navController = rememberNavController()

    // Handle navigation to dashboard when a deep link is received (e.g. from widget)
    LaunchedEffect(highlightedId) {
        if (highlightedId != null) {
            navController.navigate("dashboard") {
                // Ensure we don't build up a backstack of dashboards
                popUpTo("dashboard") { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToInventory = { medId -> 
                    val route = if (medId != null) "inventory?medId=$medId" else "inventory"
                    navController.navigate(route) 
                },
                highlightedId = highlightedId,
                onHighlightedConsumed = onHighlightedConsumed
            )
        }
        composable(
            route = "inventory?medId={medId}",
            arguments = listOf(navArgument("medId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val medId = backStackEntry.arguments?.getString("medId")
            InventoryScreen(
                highlightedMedId = medId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
