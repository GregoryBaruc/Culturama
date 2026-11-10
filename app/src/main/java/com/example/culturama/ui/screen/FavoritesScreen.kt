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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.FavoriteRepository
import com.example.culturama.data.favorite.RemoteFavoriteRepository
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
 * Eventos salvos pela conta que está autenticada, os mesmos do site.
 */
@Composable
fun FavoritesScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance },
    favoritos: FavoriteRepository = remember { RemoteFavoriteRepository.instance }
) {
    val versao by favoritos.alteracoes.collectAsState()
    var userId by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(true) }
    var falhou by remember { mutableStateOf(false) }
    var eventos by remember { mutableStateOf<List<Pair<String, EventModel?>>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        idDoUsuarioAutenticado()?.let { favoritos.atualizar(it) }
    }

    LaunchedEffect(versao) {
        val id = idDoUsuarioAutenticado()
        userId = id
        if (id == null) {
            carregando = false
            return@LaunchedEffect
        }
        val ids = favoritos.ids(id)
        falhou = favoritos.falhouAoCarregar(id)
        if (falhou) {
            carregando = false
            return@LaunchedEffect
        }
        carregando = true
        if (!favoritos.carregado(id)) return@LaunchedEffect
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

        when {
            carregando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CulturamaOrange)
            }
            falhou -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Não foi possível carregar os seus favoritos.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                )
                CulturamaPrimaryButton(
                    text = "Tentar de novo",
                    onClick = { userId?.let { favoritos.atualizar(it) } },
                    modifier = Modifier.padding(top = 20.dp)
                )
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
                    onClick = { navController.navigate(Screen.Events.createRoute()) },
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
