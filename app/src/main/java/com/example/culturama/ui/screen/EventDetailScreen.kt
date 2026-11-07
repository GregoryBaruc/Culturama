package com.example.culturama.ui.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.EventCover
import com.example.culturama.ui.theme.*

/**
 * Tela de detalhes de um evento cultural.
 * Carrega os detalhes completos do evento a partir do [EventRepository] pelo [eventId].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String
) {
    val context = LocalContext.current
    val authService = remember { SupabaseAuthService.instance }
    val favoritos = remember { RemoteFavoriteRepository.instance }
    val isLoggedIn by authService.isLoggedIn.collectAsState(initial = false)
    val versaoFavoritos by favoritos.alteracoes.collectAsState()
    val favorited = remember(isLoggedIn, versaoFavoritos, eventId) {
        val userId = if (isLoggedIn) idDoUsuarioAutenticado() else null
        userId != null && eventId in favoritos.ids(userId)
    }
    var event by remember { mutableStateOf<EventModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(eventId, reloadKey) {
        isLoading = true
        errorMessage = null
        EventRepository.getEventDetails(eventId)
            .onSuccess {
                event = it
                isLoading = false
            }
            .onFailure { error ->
                errorMessage = error.message ?: "Erro ao carregar detalhes do evento."
                isLoading = false
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CulturamaBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = CulturamaOrange)
                Text(
                    text = "Carregando evento...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        }
        return
    }

    if (errorMessage != null || event == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CulturamaBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "⚠️", fontSize = 42.sp)
                Text(
                    text = errorMessage ?: "Evento não encontrado.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { navController.popBackStack() }) {
                        Text("Voltar", color = TextSecondary)
                    }
                    Button(
                        onClick = { reloadKey++ },
                        colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
                    ) {
                        Text("Tentar novamente", color = Color.White)
                    }
                }
            }
        }
        return
    }

    val currentEvent = event!!
    val emoji = eventsScreenEmoji(currentEvent.category)
    val gradient = eventsScreenGradient(currentEvent.category)

    fun shareEvent() {
        val text = buildString {
            append("🎉 ${currentEvent.title}\n")
            append("📅 ${currentEvent.dayOfWeek}, ${currentEvent.dayOfMonth} de ${currentEvent.monthAbbr} às ${currentEvent.time}\n")
            append("📍 ${currentEvent.venueName}\n")
            if (currentEvent.isFree) append("✅ Entrada gratuita\n") else append("🎫 ${currentEvent.price}\n")
            if (currentEvent.description.isNotBlank()) append("\n${currentEvent.description}\n")
            if (!currentEvent.siteUrl.isNullOrBlank()) append("\nMais informações: ${currentEvent.siteUrl}\n")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar evento"))
    }

    Scaffold(
        containerColor = CulturamaBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val userId = idDoUsuarioAutenticado()
                        if (!isLoggedIn || userId == null) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            favoritos.setFavorite(userId, eventId, !favorited)
                        }
                    }) {
                        Icon(
                            imageVector = if (favorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (favorited) "Remover dos favoritos" else "Adicionar aos favoritos",
                            tint = if (favorited) FavoriteActive else TextPrimary
                        )
                    }
                    IconButton(onClick = { shareEvent() }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartilhar",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CulturamaSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Imagem de capa ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                EventCover(
                    imageUrl = currentEvent.imageUrl,
                    label = currentEvent.title,
                    emoji = emoji,
                    gradient = gradient,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Conteúdo principal ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Categoria
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CulturamaOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = currentEvent.category,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CulturamaOrange,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Título
                Text(
                    text = currentEvent.title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    )
                )

                HorizontalDivider(color = Color(0xFF2A2A2E), thickness = 0.5.dp)

                // ── Data e horário ────────────────────────────────────────────
                DetailRow(
                    emoji = "📅",
                    label = "Data e horário",
                    value = "${currentEvent.dayOfWeek}, ${currentEvent.dayOfMonth} de ${currentEvent.monthAbbr} às ${currentEvent.time}"
                )

                // ── Local ─────────────────────────────────────────────────────
                DetailRow(
                    emoji = "📍",
                    label = "Local",
                    value = currentEvent.venueName.ifBlank { "Local a confirmar" }
                )

                // ── Gratuito / Pago ───────────────────────────────────────────
                DetailRow(
                    emoji = if (currentEvent.isFree) "✅" else "🎫",
                    label = "Entrada",
                    value = if (currentEvent.isFree) "Gratuita" else currentEvent.price.ifBlank { "Pago" }
                )

                // ── Organizador / Fonte (se disponível) ────────────────────────
                val orgOuFonte = currentEvent.organizerName.ifBlank { currentEvent.sourceName.orEmpty() }
                if (orgOuFonte.isNotBlank()) {
                    DetailRow(
                        emoji = "🧑‍💼",
                        label = "Organização / Fonte",
                        value = orgOuFonte
                    )
                }

                // ── Acessibilidade ────────────────────────────────────────────
                val acessibilidadeTexto = when {
                    currentEvent.accessibilityInfo.isNotBlank() -> currentEvent.accessibilityInfo
                    currentEvent.isAccessible -> "Espaço com acessibilidade garantida."
                    else -> "Consulte o organizador para informações de acessibilidade."
                }
                DetailRow(
                    emoji = "♿",
                    label = "Acessibilidade",
                    value = acessibilidadeTexto
                )

                HorizontalDivider(color = Color(0xFF2A2A2E), thickness = 0.5.dp)

                // ── Descrição ─────────────────────────────────────────────────
                Text(
                    text = "Sobre o evento",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = currentEvent.description.ifBlank {
                        "Venha participar de mais um evento incrível da cena cultural independente! " +
                        "Uma oportunidade única de vivenciar a cultura local em toda a sua diversidade."
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                )

                Spacer(Modifier.height(24.dp))

                // ── Botão de compartilhar ─────────────────────────────────────
                Button(
                    onClick = { shareEvent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CulturamaOrange,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Compartilhar evento",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(
    emoji: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF252528)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextTertiary,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
