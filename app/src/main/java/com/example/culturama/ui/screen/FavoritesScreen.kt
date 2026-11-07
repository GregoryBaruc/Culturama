package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.FavoriteRepository
import com.example.culturama.data.favorite.LocalFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaPrimaryButton
import com.example.culturama.ui.component.EventCard
import com.example.culturama.ui.component.EventCover
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

/**
 * Lista local dos eventos salvos nesta instalação, da conta que está autenticada.
 */
@Composable
fun FavoritesScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance },
    favoritos: FavoriteRepository = LocalFavoriteRepository.from(LocalContext.current)
) {
    val versao by favoritos.alteracoes.collectAsState()
    var userId by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var eventos by remember { mutableStateOf<List<Pair<String, EventModel?>>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(versao) {
        val id = idDoUsuarioAutenticado()
        userId = id
        if (id == null) {
            carregando = false
            return@LaunchedEffect
        }
        carregando = true
        val ids = favoritos.ids(id)
        eventos = ids.map { eventoId -> eventoId to EventRepository.getEventDetails(eventoId).getOrNull() }
        carregando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Meus favoritos",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Text(
            text = "Salvos neste aparelho.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        when {
            carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CulturamaOrange)
            }
            eventos.isEmpty() -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Você ainda não salvou nenhum evento.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
                CulturamaPrimaryButton(
                    text = "Explorar eventos",
                    onClick = { navController.navigate(Screen.Events.route) },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)
            ) {
                items(eventos, key = { it.first }) { (eventoId, evento) ->
                    if (evento == null) {
                        EventoIndisponivel(
                            onRemover = {
                                userId?.let { favoritos.setFavorite(it, eventoId, false) }
                            }
                        )
                    } else {
                        EventCard(
                            event = evento,
                            isFavorited = true,
                            onFavorite = {
                                userId?.let { favoritos.setFavorite(it, evento.id, false) }
                            },
                            onClick = { navController.navigate("eventDetail?eventId=${evento.id}") },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            imageContent = {
                                EventCover(
                                    imageUrl = evento.imageUrl,
                                    label = evento.title,
                                    emoji = eventsScreenEmoji(evento.category),
                                    gradient = eventsScreenGradient(evento.category)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventoIndisponivel(onRemover: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Este evento não está mais disponível.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
        )
        androidx.compose.material3.TextButton(onClick = onRemover) {
            Text("Remover", color = CulturamaOrange)
        }
    }
}
