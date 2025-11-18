package com.secure.privacyfirst.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.secure.privacyfirst.ui.screens.OnboardingScreen
import com.secure.privacyfirst.ui.screens.SplashScreen
import com.secure.privacyfirst.ui.screens.AuthScreen
import com.secure.privacyfirst.ui.screens.WebViewScreen
import com.secure.privacyfirst.ui.screens.PasswordManagerScreen
import com.secure.privacyfirst.ui.screens.SetupPinScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.WebView.route) {
            WebViewScreen()
        }

        composable(Screen.Auth.route) {
            // Pass the navController into AuthScreen so it can navigate directly on success.
            AuthScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            // Placeholder for home screen
        }
        
        composable(Screen.PasswordManager.route) {
            PasswordManagerScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.SetupPin.route) {
            SetupPinScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPinSet = {
                    navController.popBackStack()
                }
            )
        }
    }
}
