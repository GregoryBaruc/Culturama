package com.example.culturama.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.model.EventModel
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*

/**
 * Seção Hero da tela inicial.
 * Contém: gradiente de fundo, palavras animadas, imagens flutuantes e conteúdo central.
 */
@Composable
fun HeroSection(
    onOpenMap:      () -> Unit,
    events:         List<EventModel>,
    onEventClick:   (EventModel) -> Unit,
    onEventsScroll: () -> Unit,
    modifier:       Modifier = Modifier
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val heroHeight   = maxOf(screenHeight, 640.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight)
    ) {
        // ── Gradiente de fundo ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to HeroGradientTop,
                            0.35f to HeroGradientMid,
                            0.65f to Color(0xFF1A1215),
                            1.0f to HeroGradientBottom
                        )
                    )
                )
        )

        // ── Palavras animadas ao fundo ────────────────────────────────────
        AnimatedBackgroundWords(modifier = Modifier.fillMaxSize())

        // ── Imagens flutuantes nas laterais ───────────────────────────────
        FloatingImages(
            events       = events,
            onEventClick = onEventClick,
            modifier     = Modifier.fillMaxSize()
        )

        // ── Conteúdo central ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Identificador pequeno
            HeroIdentifier()

            // Título principal
            Text(
                text      = "Culturama",
                style     = MaterialTheme.typography.displayLarge.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize   = 56.sp
                ),
                textAlign = TextAlign.Center
            )

            // Subtítulo
            Text(
                text      = "Sua próxima experiência cultural começa aqui.",
                style     = MaterialTheme.typography.titleLarge.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )

            // Texto complementar
            Text(
                text      = "Shows, teatro e rua. Descubra a cultura que acontece perto de você.",
                style     = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Texto dinâmico de categoria
            DynamicCategoryText()

            Spacer(modifier = Modifier.height(8.dp))

            // ── Botão "Abrir o mapa" ──────────────────────────────────────
            Button(
                onClick   = onOpenMap,
                colors    = ButtonDefaults.buttonColors(
                    containerColor = CulturamaOrange,
                    contentColor   = Color.White
                ),
                shape     = RoundedCornerShape(28.dp),
                modifier  = Modifier
                    .height(52.dp)
                    .defaultMinSize(minWidth = 180.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text  = "Abrir o mapa",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // ── Gradiente de transição para seção de eventos ──────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xFFF5F0EB))
                    )
                )
        )
    }
}

// ─── Imagens Flutuantes ─────────────────────────────────────────────────────

private data class FloatSpec(
    val isLeft:   Boolean,
    val topFrac:  Float,         // Fração da altura do container
    val edgeDP:   Dp,            // Distância da borda
    val width:    Dp,
    val height:   Dp,
    val rotation: Float,
    val phase:    Float          // Fase da oscilação (0..1)
)

@Composable
fun FloatingImages(
    events:       List<EventModel>,
    onEventClick: (EventModel) -> Unit,
    modifier:     Modifier = Modifier
) {
    val specs = listOf(
        FloatSpec(isLeft = true,  topFrac = 0.10f, edgeDP = 8.dp,  width = 90.dp,  height = 115.dp, rotation = -8f,  phase = 0.0f),
        FloatSpec(isLeft = true,  topFrac = 0.38f, edgeDP = 4.dp,  width = 100.dp, height = 125.dp, rotation =  5f,  phase = 0.3f),
        FloatSpec(isLeft = true,  topFrac = 0.60f, edgeDP = 12.dp, width = 85.dp,  height = 105.dp, rotation = -5f,  phase = 0.6f),
        FloatSpec(isLeft = false, topFrac = 0.08f, edgeDP = 8.dp,  width = 85.dp,  height = 100.dp, rotation =  8f,  phase = 0.2f),
        FloatSpec(isLeft = false, topFrac = 0.36f, edgeDP = 4.dp,  width = 95.dp,  height = 120.dp, rotation = -6f,  phase = 0.5f),
        FloatSpec(isLeft = false, topFrac = 0.60f, edgeDP = 10.dp, width = 88.dp,  height = 110.dp, rotation =  4f,  phase = 0.8f),
    )

    BoxWithConstraints(modifier = modifier) {
        val containerHeight = maxHeight

        specs.forEachIndexed { index, spec ->
            val event    = events.getOrNull(index)
            val emoji    = heroEmoji(event?.category)
            val gradient = heroGradient(event?.category)

            val topOffset = containerHeight * spec.topFrac

            FloatingEventImage(
                imageContent = {
                    EventCover(
                        imageUrl = event?.imageUrl,
                        label    = event?.title?.take(14) ?: "Cultural",
                        emoji    = emoji,
                        gradient = gradient
                    )
                },
                rotation    = spec.rotation,
                width       = spec.width,
                height      = spec.height,
                phaseOffset = spec.phase,
                onClick     = event?.let { { onEventClick(it) } },
                modifier    = Modifier
                    .then(
                        if (spec.isLeft)
                            Modifier.align(Alignment.TopStart).padding(start = spec.edgeDP)
                        else
                            Modifier.align(Alignment.TopEnd).padding(end = spec.edgeDP)
                    )
                    .padding(top = topOffset)
            )
        }
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

private fun heroEmoji(category: String?): String = when {
    category == null                                     -> "🎵"
    category.contains("Hip-Hop",  ignoreCase = true)    -> "🎤"
    category.contains("Samba",    ignoreCase = true)    -> "🥁"
    category.contains("Capoeira", ignoreCase = true)    -> "🥋"
    category.contains("Sarau",    ignoreCase = true)    -> "📖"
    category.contains("Feira",    ignoreCase = true)    -> "🎪"
    category.contains("Música",   ignoreCase = true)    -> "🎸"
    else                                                 -> "🎭"
}

private fun heroGradient(category: String?): List<Color> = when {
    category == null                                     -> listOf(Color(0xFF2A2A3A), Color(0xFF1A1A2A))
    category.contains("Hip-Hop",  ignoreCase = true)    -> listOf(Color(0xFF2A1A2A), Color(0xFF1A0A1A))
    category.contains("Samba",    ignoreCase = true)    -> listOf(Color(0xFF3A2A0A), Color(0xFF2A1A00))
    category.contains("Capoeira", ignoreCase = true)    -> listOf(Color(0xFF0A2A2A), Color(0xFF001A1A))
    category.contains("Sarau",    ignoreCase = true)    -> listOf(Color(0xFF1A0A2A), Color(0xFF0A001A))
    category.contains("Feira",    ignoreCase = true)    -> listOf(Color(0xFF0A2A1A), Color(0xFF00180A))
    category.contains("Música",   ignoreCase = true)    -> listOf(Color(0xFF2A1A0A), Color(0xFF1A0A00))
    else                                                 -> listOf(Color(0xFF2A1A1A), Color(0xFF1A0A0A))
}
