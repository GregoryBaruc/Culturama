package com.example.culturama.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
import com.example.culturama.navigation.SecaoDoUsuario
import com.example.culturama.ui.component.*
import com.example.culturama.ui.section.WeekEventsSection
import com.example.culturama.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Tela principal do Culturama.
 * Contém a seção Hero com animações e a seção de eventos da semana.
 * Rolagem vertical contínua — as duas seções fazem parte da mesma tela.
 */
@Composable
fun HomeScreen(navController: NavController) {
    val scrollState    = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val authService    = remember { SupabaseAuthService.instance }
    val favoritos      = remember { RemoteFavoriteRepository.instance }
    val isLoggedIn by authService.isLoggedIn.collectAsState(initial = false)
    val versaoFavoritos by favoritos.alteracoes.collectAsState()

    // Estado: eventos e favoritos locais desta instalação
    var events       by remember { mutableStateOf(emptyList<EventModel>()) }
    var isLoading    by remember { mutableStateOf(true) }
    var eventsError  by remember { mutableStateOf<String?>(null) }
    var reloadKey    by remember { mutableIntStateOf(0) }
    var favoritedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        eventsError = null
        EventRepository.getWeekEvents()
            .onSuccess { events = it }
            .onFailure { eventsError = it.message }
        isLoading = false
    }

    LaunchedEffect(isLoggedIn, versaoFavoritos) {
        val userId = if (isLoggedIn) idDoUsuarioAutenticado() else null
        favoritedIds = userId?.let { favoritos.ids(it).toSet() } ?: emptySet()
    }

    // Estado: evento selecionado para o bottom sheet
    var selectedEvent by remember { mutableStateOf<EventModel?>(null) }

    LaunchedEffect(selectedEvent?.id) {
        val event = selectedEvent ?: return@LaunchedEffect
        if (!event.detailsLoaded) {
            // Sem o detalhe, o sheet segue com os dados do card e para de carregar.
            selectedEvent = EventRepository.getEventDetails(event.id)
                .getOrDefault(event.copy(detailsLoaded = true))
        }
    }

    // Altura da seção Hero em pixels (capturada via onGloballyPositioned)
    var heroHeightPx by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(CulturamaBackground)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO 1 — Hero
            // ═══════════════════════════════════════════════════════════════
            Box(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    heroHeightPx = coordinates.size.height
                }
            ) {
                HeroSection(
                    onOpenMap      = {
                        navController.navigate(
                            if (isLoggedIn) Screen.Events.createRoute(mapa = true)
                            else Screen.Access.route
                        )
                    },
                    events         = events,
                    onEventClick   = { event -> selectedEvent = event },
                    onEventsScroll = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(
                                value         = heroHeightPx,
                                animationSpec = tween(600, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                )
            }

            // ═══════════════════════════════════════════════════════════════
            // SEÇÃO 2 — Eventos da semana (transição limpa + carrossel)
            // ═══════════════════════════════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFFF5F0EB))
                    .padding(top = 20.dp, bottom = 28.dp)
            ) {
                WeekEventsSection(
                    events        = events,
                    favoritedIds  = favoritedIds,
                    onFavorite    = { id ->
                        val userId = idDoUsuarioAutenticado()
                        if (!isLoggedIn || userId == null) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            favoritedIds = favoritos.setFavorite(userId, id, id !in favoritedIds).toSet()
                        }
                    },
                    onEventClick  = { event -> selectedEvent = event },
                    onSeeAll      = { navController.navigate(Screen.Events.createRoute()) },
                    isLoading     = isLoading,
                    errorMessage  = eventsError,
                    onRetry       = { reloadKey++ }
                )
            }
        }

        // ── Cabeçalho fixo sobreposto ──────────────────────────────────────
        CulturamaTopBar(
            onEventsClick     = { navController.navigate(Screen.Events.createRoute()) },
            onMapClick        = {
                navController.navigate(
                    if (isLoggedIn) Screen.Events.createRoute(mapa = true)
                    else Screen.Access.route
                )
            },
            onOrganizersClick = { navController.navigate(Screen.PlanosOrganizador.route) },
            onCompaniesClick  = { navController.navigate(Screen.PlanosEmpresa.route) },
            onLoginClick      = { navController.navigate(Screen.Acessos.route) },
            onSignUpClick     = { navController.navigate(Screen.SignUp.route) },
            onPreferencesClick= {
                navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.INTERESSES.id))
            },
            onPlanosClick     = { navController.navigate(Screen.Planos.route) },

            isLoggedIn        = isLoggedIn,
            onLogoutClick     = { coroutineScope.launch { authService.logout() } },
            onProfileClick    = { navController.navigate(Screen.Profile.route) },
            modifier          = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xE0161618),
                            Color(0x80161618),
                            Color.Transparent
                        )
                    )
                )
        )
    }

    // ── Bottom sheet de detalhes do evento ─────────────────────────────
    selectedEvent?.let { event ->
        EventDetailBottomSheet(
            event        = event,
            onDismiss    = { selectedEvent = null },
            imageContent = {
                EventCover(
                    imageUrl = event.imageUrl,
                    label    = event.title,
                    emoji    = eventEmoji(event.category),
                    gradient = eventGradient(event.category)
                )
            }
        )
    }
}

// ─── Helpers de emoji/gradiente ─────────────────────────────────────────────

private fun eventEmoji(category: String): String = when {
    category.contains("Hip-Hop",  ignoreCase = true) -> "🎤"
    category.contains("Samba",    ignoreCase = true) -> "🥁"
    category.contains("Capoeira", ignoreCase = true) -> "🥋"
    category.contains("Sarau",    ignoreCase = true) -> "📖"
    category.contains("Feira",    ignoreCase = true) -> "🎪"
    category.contains("Música",   ignoreCase = true) -> "🎸"
    else                                              -> "🎭"
}

private fun eventGradient(category: String) = when {
    category.contains("Hip-Hop",  ignoreCase = true) -> PlaceholderHipHop
    category.contains("Samba",    ignoreCase = true) -> PlaceholderSamba
    category.contains("Capoeira", ignoreCase = true) -> PlaceholderCapoeira
    category.contains("Sarau",    ignoreCase = true) -> PlaceholderSarau
    category.contains("Feira",    ignoreCase = true) -> PlaceholderFeira
    category.contains("Música",   ignoreCase = true) -> PlaceholderFestival
    else                                              -> PlaceholderBatalha
}
