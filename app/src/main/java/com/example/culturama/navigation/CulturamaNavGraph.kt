package com.example.culturama.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.ui.screen.AccessScreen
import com.example.culturama.ui.screen.CompleteProfileScreen
import com.example.culturama.ui.screen.HomeScreen
import com.example.culturama.ui.screen.LoginScreen
import com.example.culturama.ui.screen.OrganizerAccessScreen
import com.example.culturama.ui.screen.OrganizersScreen
import com.example.culturama.ui.screen.PreferencesScreen
import com.example.culturama.ui.screen.EventsScreen
import com.example.culturama.ui.screen.EventDetailScreen
import com.example.culturama.ui.screen.SignUpScreen

@Composable
fun CulturamaNavGraph(navController: NavHostController) {
    // SessÃ£o encerrada pelo servidor: leva a pessoa para entrar de novo.
    LaunchedEffect(Unit) {
        ApiClient.sessaoEncerrada.collect {
            navController.navigate(Screen.Login.route) { launchSingleTop = true }
        }
    }

    // Conta sem data de nascimento ou aceite sÃ³ segue depois de completar o cadastro.
    // Confere a cada troca de tela atÃ© o perfil estar completo: login e abertura do app passam por aqui.
    val authService = remember { SupabaseAuthService.instance }
    val isLoggedIn by authService.isLoggedIn.collectAsState(initial = false)
    val rotaAtual = navController.currentBackStackEntryAsState().value?.destination?.route
    var perfilConfirmado by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, rotaAtual) {
        if (!isLoggedIn) {
            perfilConfirmado = false
        } else if (!perfilConfirmado && rotaAtual != null && rotaAtual != Screen.CompleteProfile.route) {
            when (authService.isProfileComplete()) {
                true -> perfilConfirmado = true
                false -> navController.navigate(Screen.CompleteProfile.route) { launchSingleTop = true }
                null -> Unit
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Events.route) {
            EventsScreen(navController = navController)
        }
        composable(
            route = "eventDetail?eventId={eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(navController = navController, eventId = eventId)
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
        composable(Screen.CompleteProfile.route) {
            CompleteProfileScreen(navController = navController)
        }
    }
}
