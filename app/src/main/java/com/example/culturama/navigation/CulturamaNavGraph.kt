package com.example.culturama.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.culturama.ui.screen.AccessScreen
import com.example.culturama.ui.screen.HomeScreen

@Composable
fun CulturamaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Access.route) {
            AccessScreen(navController = navController)
        }
    }
}
