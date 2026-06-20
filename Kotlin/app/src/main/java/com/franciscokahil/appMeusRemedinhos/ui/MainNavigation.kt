package com.franciscokahil.appMeusRemedinhos.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.franciscokahil.appMeusRemedinhos.ui.dashboard.DashboardScreen

@Composable
fun MainNavigation(
    highlightedId: String? = null,
    onHighlightedConsumed: () -> Unit
) {
    // Start destination is always dashboard now as per UX 2.1
    val startDestination = "dashboard"
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("dashboard") {
            DashboardScreen(
                highlightedId = highlightedId,
                onHighlightedConsumed = onHighlightedConsumed
            )
        }
    }
}
