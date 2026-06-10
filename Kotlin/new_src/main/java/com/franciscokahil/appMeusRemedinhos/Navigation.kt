package com.franciscokahil.appMeusRemedinhos

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.franciscokahil.appMeusRemedinhos.ui.dashboard.DashboardScreen

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.franciscokahil.appMeusRemedinhos.ui.onboarding.OnboardingScreen

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
  val hasSeenOnboarding = sharedPrefs.getBoolean("has_seen_onboarding", false)

  val initialRoute = if (hasSeenOnboarding) Main else OnboardingRoute
  val backStack = rememberNavBackStack(initialRoute)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          DashboardScreen()
        }
        entry<OnboardingRoute> {
          OnboardingScreen(onFinish = {
              sharedPrefs.edit().putBoolean("has_seen_onboarding", true).apply()
              backStack.clear()
              backStack.add(Main)
          })
        }
      },
  )
}
