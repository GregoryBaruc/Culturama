package com.example.culturama.ui.section

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.culturama.model.EventModel
import com.example.culturama.ui.component.EventImagePlaceholder
import com.example.culturama.ui.component.PlaceholderBatalha
import com.example.culturama.ui.component.PlaceholderCapoeira
import com.example.culturama.ui.component.PlaceholderFeira
import com.example.culturama.ui.component.PlaceholderFestival
import com.example.culturama.ui.component.PlaceholderHipHop
import com.example.culturama.ui.component.PlaceholderSamba
import com.example.culturama.ui.component.PlaceholderSarau
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.FavoriteActive

private val CreamCard = Color(0xFFFFFBF7)
private val CreamInk = Color(0xFF2C1F1A)
private val CreamMuted = Color(0xFF6B5A52)
private val CreamImagePad = Color(0xFFEDE4DA)

/**
 * Seção "Acontecendo esta semana" — carrossel horizontal mobile-first.
 */
@Composable
fun WeekEventsSection(
    events: List<EventModel>,
    favoritedIds: Set<String>,
    onFavorite: (String) -> Unit,
    onEventClick: (EventModel) -> Unit,
    onSeeAll: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = (screenWidth * 0.78f).coerceIn(260.dp, 340.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Acontecendo esta semana",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = CreamInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(
                onClick = onSeeAll,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Ver todos",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CulturamaOrange)
            }
            errorMessage != null -> Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(color = CreamInk)
                )
                TextButton(onClick = onRetry) {
                    Text(
                        text = "Tentar de novo",
                        style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
                    )
                }
            }
            events.isEmpty() -> Text(
                text = "Nenhum evento nos próximos 7 dias.",
                style = MaterialTheme.typography.bodyMedium.copy(color = CreamMuted),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            )
            else -> LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(events, key = { _, e -> e.id }) { _, event ->
                    WeekEventCarouselCard(
                        event = event,
                        isFavorited = event.id in favoritedIds,
                        onFavorite = { onFavorite(event.id) },
                        onClick = { onEventClick(event) },
                        modifier = Modifier.width(cardWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekEventCarouselCard(
    event: EventModel,
    isFavorited: Boolean,
    onFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var heartScale by remember { mutableFloatStateOf(1f) }
    val animatedHeartScale by animateFloatAsState(
        targetValue = heartScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "week_heart"
    )
    LaunchedEffect(heartScale) {
        if (heartScale != 1f) {
            kotlinx.coroutines.delay(200)
            heartScale = 1f
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CreamCard)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(CreamImagePad)
        ) {
            EventPosterImage(
                imageUrl = event.imageUrl,
                title = event.title,
                category = event.category,
                modifier = Modifier.fillMaxSize()
            )

            // Selo de data
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE61C1C1E))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = event.monthAbbr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = event.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                )
            }

            IconButton(
                onClick = {
                    heartScale = 1.35f
                    onFavorite()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xE61C1C1E))
            ) {
                Icon(
                    imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorited) "Remover dos favoritos" else "Favoritar",
                    tint = if (isFavorited) FavoriteActive else Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .scale(animatedHeartScale)
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = CreamInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.category,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = CulturamaOrange,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.venueName,
                style = MaterialTheme.typography.bodySmall.copy(color = CreamMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${event.dayOfWeek} · ${event.time}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CreamMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Text(
                    text = when {
                        event.isFree -> "Gratuito"
                        event.price.isNotBlank() -> event.price
                        else -> "Pago"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (event.isFree) Color(0xFF2E7D32) else CreamInk,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

/**
 * Cartaz: Fit para não cortar informação; fundo complementar preenche laterais.
 */
@Composable
private fun EventPosterImage(
    imageUrl: String?,
    title: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val emoji = eventEmoji(category)
    val gradient = eventGradient(category)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        EventImagePlaceholder(
            label = title.take(18),
            emoji = emoji,
            gradient = gradient,
            modifier = Modifier.fillMaxSize()
        )
        if (!imageUrl.isNullOrBlank()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(CreamImagePad, Color(0xFFE2D6C8))
                        )
                    )
            )
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun eventEmoji(category: String): String = when {
    category.contains("Hip-Hop", ignoreCase = true) -> "🎤"
    category.contains("Samba", ignoreCase = true) -> "🥁"
    category.contains("Capoeira", ignoreCase = true) -> "🥋"
    category.contains("Sarau", ignoreCase = true) -> "📖"
    category.contains("Feira", ignoreCase = true) -> "🎪"
    category.contains("Música", ignoreCase = true) -> "🎸"
    else -> "🎭"
}

private fun eventGradient(category: String): List<Color> = when {
    category.contains("Hip-Hop", ignoreCase = true) -> PlaceholderHipHop
    category.contains("Samba", ignoreCase = true) -> PlaceholderSamba
    category.contains("Capoeira", ignoreCase = true) -> PlaceholderCapoeira
    category.contains("Sarau", ignoreCase = true) -> PlaceholderSarau
    category.contains("Feira", ignoreCase = true) -> PlaceholderFeira
    category.contains("Música", ignoreCase = true) -> PlaceholderFestival
    else -> PlaceholderBatalha
}
