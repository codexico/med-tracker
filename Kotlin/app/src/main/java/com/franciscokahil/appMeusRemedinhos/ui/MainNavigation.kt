package com.franciscokahil.appMeusRemedinhos.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.franciscokahil.appMeusRemedinhos.ui.dashboard.DashboardScreen
import com.franciscokahil.appMeusRemedinhos.ui.onboarding.OnboardingScreen

@Composable
fun MainNavigation(highlightedId: String? = null) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val hasSeenOnboarding = sharedPrefs.getBoolean("has_seen_onboarding", false)
    
    val navController = rememberNavController()
    val startDestination = if (hasSeenOnboarding) "dashboard" else "onboarding"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(onFinish = {
                sharedPrefs.edit().putBoolean("has_seen_onboarding", true).apply()
                navController.navigate("dashboard") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("dashboard") {
            DashboardScreen(highlightedId = highlightedId)
        }
    }
}
