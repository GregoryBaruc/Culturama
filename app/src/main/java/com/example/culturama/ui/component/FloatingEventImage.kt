package com.example.culturama.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.culturama.ui.theme.CulturamaCard

/**
 * Imagem flutuante com oscilação vertical suave.
 * Pode exibir um drawable local (placeholder) ou futuramente uma URL.
 * Clicável quando associada a um evento.
 */
@Composable
fun FloatingEventImage(
    imageContent:   @Composable BoxScope.() -> Unit,
    rotation:       Float = 0f,
    width:          Dp    = 100.dp,
    height:         Dp    = 120.dp,
    phaseOffset:    Float = 0f,   // 0..1 — distribui a fase da oscilação entre imagens
    onClick:        (() -> Unit)? = null,
    modifier:       Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float_img")

    val floatY by infiniteTransition.animateFloat(
        initialValue  = -6f,
        targetValue   = 6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(
                durationMillis = 3200,
                easing         = FastOutSlowInEasing,
                delayMillis    = (phaseOffset * 1200).toInt()
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .offset(y = floatY.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(12.dp))
            .background(CulturamaCard)
            .then(
                if (onClick != null)
                    Modifier.clickable(onClickLabel = "Ver evento") { onClick() }
                else
                    Modifier
            ),
        content = imageContent
    )
}
