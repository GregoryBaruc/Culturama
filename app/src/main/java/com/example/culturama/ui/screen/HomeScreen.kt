package com.example.culturama.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.DemoEventRepository
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
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
    val density        = LocalDensity.current

    // Estado: eventos e favoritos
    val events       = remember { DemoEventRepository.getWeekEvents() }
    var favoritedIds by remember { mutableStateOf(setOf<String>()) }

    // Estado: evento selecionado para o bottom sheet
    var selectedEvent by remember { mutableStateOf<EventModel?>(null) }

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
                    navController  = navController,
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
            // SEÇÃO 2 — Eventos da semana
            // ═══════════════════════════════════════════════════════════════
            Spacer(Modifier.height(0.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F0EB))
                    .padding(top = 32.dp, bottom = 32.dp)
            ) {
                WeekEventsSection(
                    events        = events,
                    favoritedIds  = favoritedIds,
                    onFavorite    = { id ->
                        favoritedIds = if (id in favoritedIds)
                            favoritedIds - id else favoritedIds + id
                    },
                    onEventClick  = { event -> selectedEvent = event },
                    onSeeAll      = { /* TODO: Navegar para lista completa na próxima etapa */ }
                )
            }
        }

        // ── Cabeçalho fixo sobreposto ──────────────────────────────────────
        CulturamaTopBar(
            onEventsClick     = {
                coroutineScope.launch {
                    scrollState.animateScrollTo(
                        value         = heroHeightPx,
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                }
            },
            onMapClick        = { navController.navigate(Screen.Access.route) },
            onOrganizersClick = { /* TODO: próxima etapa */ },
            onCompaniesClick  = { /* TODO: próxima etapa */ },
            onLoginClick      = { navController.navigate(Screen.Login.route) },
            onSignUpClick     = { navController.navigate(Screen.SignUp.route) },
            onPreferencesClick= { navController.navigate(Screen.Preferences.route) },
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
                EventImagePlaceholder(
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
