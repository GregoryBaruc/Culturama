package com.example.culturama.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Imagem placeholder visualmente rica.
 * Substitui imagens reais enquanto a integração com API não é feita.
 * Cada placeholder tem gradiente e emoji/texto únicos para diferenciar eventos.
 */
@Composable
fun EventImagePlaceholder(
    label:    String,
    emoji:    String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier        = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text     = emoji,
                fontSize = 36.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

/**
 * Capa do evento. O placeholder fica por baixo enquanto a imagem carrega
 * e quando o evento não tem capa ou ela falha.
 */
@Composable
fun EventCover(
    imageUrl: String?,
    label:    String,
    emoji:    String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        EventImagePlaceholder(label = label, emoji = emoji, gradient = gradient)
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model              = imageUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        }
    }
}

// ─── Paletas de placeholder por tipo de evento ─────────────────────────────

val PlaceholderBatalha = listOf(Color(0xFF1A0A0A), Color(0xFF4A1A1A))
val PlaceholderSamba   = listOf(Color(0xFF1A1200), Color(0xFF5C3D00))
val PlaceholderCapoeira = listOf(Color(0xFF001A1A), Color(0xFF004444))
val PlaceholderSarau   = listOf(Color(0xFF12001A), Color(0xFF3D0060))
val PlaceholderHipHop  = listOf(Color(0xFF0A0A1A), Color(0xFF1A1A4A))
val PlaceholderFeira   = listOf(Color(0xFF001A08), Color(0xFF00502A))
val PlaceholderFestival = listOf(Color(0xFF1A0A00), Color(0xFF5C2E00))
