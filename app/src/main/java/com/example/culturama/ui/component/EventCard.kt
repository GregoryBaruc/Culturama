package com.example.culturama.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.model.EventModel
import com.example.culturama.ui.theme.*

/**
 * Card de evento reutilizável.
 * Exibe imagem de capa, selo de data, botão de favorito, categoria,
 * horário, nome do evento, local e preço.
 */
@Composable
fun EventCard(
    event:        EventModel,
    isFavorited:  Boolean,
    onFavorite:   () -> Unit,
    onClick:      () -> Unit,
    modifier:     Modifier = Modifier,
    imageContent: @Composable BoxScope.() -> Unit
) {
    var heartScale by remember { mutableFloatStateOf(1f) }
    val animatedHeartScale by animateFloatAsState(
        targetValue   = heartScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "heart_scale"
    )

    LaunchedEffect(heartScale) {
        if (heartScale != 1f) {
            kotlinx.coroutines.delay(200)
            heartScale = 1f
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClickLabel      = "Ver detalhes de ${event.title}"
            ) { onClick() },
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CulturamaCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // ── Imagem de capa ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                content = imageContent
            )

            // Gradiente sobre a imagem (parte inferior)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomStart)
                    .offset(y = 80.dp)   // Alinha com o final da imagem
                    // (gradiente real aplicado via background na Column abaixo)
            )

            // ── Selo de data ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD1C1C1E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = event.monthAbbr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = CulturamaOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 9.sp
                    )
                )
                Text(
                    text  = event.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize   = 18.sp
                    )
                )
            }

            // ── Botão favorito ────────────────────────────────────────────
            IconButton(
                onClick = {
                    heartScale = 1.35f
                    onFavorite()
                    // Retorna ao tamanho normal após breve delay
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xDD1C1C1E))
            ) {
                Icon(
                    imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorited) "Remover dos favoritos" else "Adicionar aos favoritos",
                    tint     = if (isFavorited) FavoriteActive else FavoriteInactive,
                    modifier = Modifier
                        .size(18.dp)
                        .scale(animatedHeartScale)
                )
            }
        }

        // ── Informações do evento ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Categoria + dia e horário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = event.category,
                    style    = MaterialTheme.typography.labelSmall.copy(
                        color      = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text  = "${event.dayOfWeek}, ${event.time}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }

            // Nome do evento
            Text(
                text     = event.title,
                style    = MaterialTheme.typography.titleMedium.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Local
            Text(
                text     = event.venueName,
                style    = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Preço
            Spacer(Modifier.height(2.dp))
            Surface(
                shape  = RoundedCornerShape(6.dp),
                color  = if (event.isFree) Color(0xFF1A3A1A) else Color(0xFF2A2A2A)
            ) {
                Text(
                    text  = if (event.isFree) "Gratuito" else event.price,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color      = if (event.isFree) Color(0xFF4CAF50) else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
