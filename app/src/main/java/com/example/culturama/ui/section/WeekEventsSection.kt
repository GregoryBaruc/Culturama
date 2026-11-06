package com.example.culturama.ui.section

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.culturama.model.EventModel
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*

/**
 * Seção "Acontecendo esta semana" — grade de cards de eventos.
 * Layout responsivo: 2 colunas em celular, 3 em tablet.
 * Os cards aparecem com animação fade+slide ao entrar na tela.
 */
@Composable
fun WeekEventsSection(
    events:      List<EventModel>,
    favoritedIds: Set<String>,
    onFavorite:   (String) -> Unit,
    onEventClick: (EventModel) -> Unit,
    onSeeAll:     () -> Unit,
    isLoading:    Boolean = false,
    errorMessage: String? = null,
    onRetry:      () -> Unit = {},
    modifier:     Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ── Cabeçalho da seção ────────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "Acontecendo esta semana",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = Color(0xFF333333),
                    fontWeight = FontWeight.Bold
                )
            )
            TextButton(onClick = onSeeAll) {
                Text(
                    text  = "Ver todos",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CulturamaOrange
                    )
                )
            }
        }

        // ── Carregando, erro ou lista vazia ───────────────────────────────
        when {
            isLoading -> Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CulturamaOrange)
            }
            errorMessage != null -> Column(
                modifier            = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF333333))
                )
                TextButton(onClick = onRetry) {
                    Text(
                        text  = "Tentar de novo",
                        style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
                    )
                }
            }
            events.isEmpty() -> Text(
                text     = "Nenhum evento nos próximos 7 dias.",
                style    = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF333333)),
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        // ── Grade de eventos ──────────────────────────────────────────────
        // Usamos LazyVerticalGrid mas dentro de uma Column com altura fixa
        // para funcionar dentro de um LazyColumn pai via items()
        events.forEachIndexed { index, event ->
            if (index % 2 == 0) {
                // Linha com dois cards (ou um se for o último ímpar)
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedEventCard(
                        event        = events[index],
                        isFavorited  = event.id in favoritedIds,
                        onFavorite   = { onFavorite(events[index].id) },
                        onClick      = { onEventClick(events[index]) },
                        delayIndex   = index,
                        modifier     = Modifier.weight(1f)
                    )
                    if (index + 1 < events.size) {
                        AnimatedEventCard(
                            event       = events[index + 1],
                            isFavorited = events[index + 1].id in favoritedIds,
                            onFavorite  = { onFavorite(events[index + 1].id) },
                            onClick     = { onEventClick(events[index + 1]) },
                            delayIndex  = index + 1,
                            modifier    = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedEventCard(
    event:       EventModel,
    isFavorited: Boolean,
    onFavorite:  () -> Unit,
    onClick:     () -> Unit,
    delayIndex:  Int,
    modifier:    Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayIndex * 80L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec  = tween(400, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        EventCard(
            event       = event,
            isFavorited = isFavorited,
            onFavorite  = onFavorite,
            onClick     = onClick,
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

private fun eventEmoji(category: String): String = when {
    category.contains("Hip-Hop", ignoreCase = true)   -> "🎤"
    category.contains("Samba",   ignoreCase = true)   -> "🥁"
    category.contains("Capoeira",ignoreCase = true)   -> "🥋"
    category.contains("Sarau",   ignoreCase = true)   -> "📖"
    category.contains("Feira",   ignoreCase = true)   -> "🎪"
    category.contains("Música",  ignoreCase = true)   -> "🎸"
    else                                               -> "🎭"
}

private fun eventGradient(category: String): List<Color> = when {
    category.contains("Hip-Hop", ignoreCase = true)   -> PlaceholderHipHop
    category.contains("Samba",   ignoreCase = true)   -> PlaceholderSamba
    category.contains("Capoeira",ignoreCase = true)   -> PlaceholderCapoeira
    category.contains("Sarau",   ignoreCase = true)   -> PlaceholderSarau
    category.contains("Feira",   ignoreCase = true)   -> PlaceholderFeira
    category.contains("Música",  ignoreCase = true)   -> PlaceholderFestival
    else                                               -> PlaceholderBatalha
}
