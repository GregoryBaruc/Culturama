package com.example.culturama.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.ui.screen.AdminPainelScreen
import com.example.culturama.ui.screen.AccessScreen
import com.example.culturama.ui.screen.AcessosScreen
import com.example.culturama.ui.screen.AreaDaContaScreen
import com.example.culturama.ui.screen.CompleteProfileScreen
import com.example.culturama.ui.screen.HomeScreen
import com.example.culturama.ui.screen.EditProfileScreen
import com.example.culturama.ui.screen.FavoritesScreen
import com.example.culturama.ui.screen.LoginScreen
import com.example.culturama.ui.screen.MeusInteressesScreen
import com.example.culturama.ui.screen.ModeracaoScreen
import com.example.culturama.ui.screen.OrganizerAccessScreen
import com.example.culturama.ui.screen.OrganizersScreen
import com.example.culturama.ui.screen.ParaEmpresasScreen
import com.example.culturama.ui.screen.PlanosEmpresaScreen
import com.example.culturama.ui.screen.PlanosOrganizadorScreen
import com.example.culturama.ui.screen.PlanosScreen
import com.example.culturama.ui.screen.PreferencesScreen
import com.example.culturama.ui.screen.EventsScreen
import com.example.culturama.ui.screen.EventDetailScreen
import com.example.culturama.ui.screen.SettingsScreen
import com.example.culturama.ui.screen.SignUpScreen
import com.example.culturama.ui.screen.SolicitacaoDeAcessoScreen
import com.example.culturama.ui.screen.UserMenuScreen
import com.example.culturama.ui.screen.UserSectionScreen

@Composable
fun CulturamaNavGraph(navController: NavHostController) {
    // Sessão encerrada pelo servidor: leva a pessoa para entrar de novo.
    LaunchedEffect(Unit) {
        ApiClient.sessaoEncerrada.collect {
            navController.navigate(Screen.Login.route) { launchSingleTop = true }
        }
    }

    // Favorito que o servidor recusou: a tela já desfez o coração, falta dizer o motivo.
    val contexto = LocalContext.current
    LaunchedEffect(Unit) {
        RemoteFavoriteRepository.instance.avisos.collect { aviso ->
            Toast.makeText(contexto, aviso, Toast.LENGTH_SHORT).show()
        }
    }

    // Conta sem data de nascimento ou aceite só segue depois de completar o cadastro.
    // Confere a cada troca de tela até o perfil estar completo: login e abertura do app passam por aqui.
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
        composable(
            route = Screen.Events.route,
            arguments = listOf(
                navArgument("mapa") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entrada ->
            EventsScreen(
                navController = navController,
                abrirNoMapa = entrada.arguments?.getBoolean("mapa") == true
            )
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
        composable(Screen.ParaEmpresas.route) {
            ParaEmpresasScreen(navController = navController)
        }
        composable(Screen.Planos.route) {
            PlanosScreen(navController = navController)
        }
        composable(Screen.PlanosOrganizador.route) {
            PlanosOrganizadorScreen(navController = navController)
        }
        composable(Screen.PlanosEmpresa.route) {
            PlanosEmpresaScreen(navController = navController)
        }
        composable(Screen.OrganizerAccess.route) {
            OrganizerAccessScreen(navController = navController)
        }
        composable(
            route = "acessos?foco={foco}",
            arguments = listOf(
                navArgument("foco") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { entrada ->
            AcessosScreen(
                navController = navController,
                foco = entrada.arguments?.getString("foco").orEmpty()
            )
        }
        composable(
            route = Screen.AreaDaConta.route,
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) { entrada ->
            AreaDaContaScreen(
                navController = navController,
                tipo = entrada.arguments?.getString("tipo").orEmpty()
            )
        }
        composable(
            route = Screen.SolicitacaoDeAcesso.route,
            arguments = listOf(navArgument("tipo") { type = NavType.StringType })
        ) { entrada ->
            SolicitacaoDeAcessoScreen(
                navController = navController,
                tipo = entrada.arguments?.getString("tipo").orEmpty()
            )
        }
        composable(
            route = "login?email={email}&destino={destino}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("destino") {
                    type = NavType.StringType
                    defaultValue = "descoberta"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email").orEmpty()
            val destino = backStackEntry.arguments?.getString("destino").orEmpty()
            LoginScreen(
                navController = navController,
                initialEmail = email,
                destino = destino
            )
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
        composable(Screen.Profile.route) {
            UserMenuScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = Screen.UserSection.route,
            arguments = listOf(navArgument("section") { type = NavType.StringType })
        ) { backStackEntry ->
            val section = backStackEntry.arguments?.getString("section").orEmpty()
            when (section) {
                SecaoDoUsuario.FAVORITOS.id -> FavoritesScreen(navController = navController)
                SecaoDoUsuario.EDITAR_PERFIL.id -> EditProfileScreen(navController = navController)
                SecaoDoUsuario.INTERESSES.id -> MeusInteressesScreen(navController = navController)
                else -> UserSectionScreen(navController = navController, sectionId = section)
            }
        }
        composable(
            route = Screen.AdminPainel.route,
            arguments = listOf(navArgument("secao") { type = NavType.StringType })
        ) { backStackEntry ->
            AdminPainelScreen(
                navController = navController,
                secaoId = backStackEntry.arguments?.getString("secao").orEmpty()
            )
        }
        composable(Screen.Moderacao.route) {
            ModeracaoScreen(navController = navController)
        }
    }
}
