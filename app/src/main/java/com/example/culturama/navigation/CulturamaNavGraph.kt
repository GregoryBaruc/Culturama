package com.example.culturama.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.ui.screen.AccessScreen
import com.example.culturama.ui.screen.HomeScreen
import com.example.culturama.ui.screen.LoginScreen
import com.example.culturama.ui.screen.OrganizerAccessScreen
import com.example.culturama.ui.screen.OrganizersScreen
import com.example.culturama.ui.screen.PreferencesScreen
import com.example.culturama.ui.screen.SignUpScreen

@Composable
fun CulturamaNavGraph(navController: NavHostController) {
    // Sessão encerrada pelo servidor: leva a pessoa para entrar de novo.
    LaunchedEffect(Unit) {
        ApiClient.sessaoEncerrada.collect {
            navController.navigate(Screen.Login.route) { launchSingleTop = true }
        }
    }

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
        composable(Screen.Organizers.route) {
            OrganizersScreen(navController = navController)
        }
        composable(Screen.OrganizerAccess.route) {
            OrganizerAccessScreen(navController = navController)
        }
        composable(
            route = "login?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            LoginScreen(navController = navController, initialEmail = email)
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        composable(Screen.Preferences.route) {
            PreferencesScreen(navController = navController)
        }
    }
}
