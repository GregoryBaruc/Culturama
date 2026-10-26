package com.example.culturama.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.model.EventModel
import com.example.culturama.ui.theme.*

/**
 * Texto dinâmico que alterna entre categorias culturais com crossfade.
 * Exibe: "O que está rolando em [Categoria]"
 */
@Composable
fun DynamicCategoryText(modifier: Modifier = Modifier) {
    val categories = remember {
        listOf("Hip-Hop", "Roda de Samba", "Capoeira", "Sarau", "Batalha de Rima", "Música ao Vivo")
    }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            currentIndex = (currentIndex + 1) % categories.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "category_alpha")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.7f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier            = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Text(
            text  = "O que está rolando em ",
            style = MaterialTheme.typography.bodyLarge.copy(
                color      = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text  = categories[currentIndex],
            style = MaterialTheme.typography.bodyLarge.copy(
                color      = CulturamaOrange.copy(alpha = alpha),
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * Identificador pequeno no topo do conteúdo central.
 */
@Composable
fun HeroIdentifier(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(20.dp),
        color    = Color.White.copy(alpha = 0.08f)
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CulturamaOrange)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "CULTURAMA · MAPA E LISTA",
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = TextSecondary,
                    letterSpacing = 1.5.sp
                )
            )
        }
    }
}
