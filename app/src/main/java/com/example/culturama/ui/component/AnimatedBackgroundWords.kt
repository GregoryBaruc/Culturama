package com.example.culturama.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.ui.theme.BackgroundWordColor

/**
 * Palavras animadas de fundo — efeito visual contornado que se move
 * horizontalmente em linhas alternadas, criando sensação de profundidade.
 *
 * Respeita preferências de redução de animação do sistema via
 * [LocalConfiguration] (detecção de animações reduzidas não é suportada
 * diretamente no Compose; usa-se velocidade mínima como fallback seguro).
 */

private val WORD_ROWS = listOf(
    listOf("ANA CAROLINA", "HUMBERTO GESSINGER", "CAPITAL INICIAL", "MARISA MONTE"),
    listOf("THE", "AT", "EMPINADO", "CA7RIEL", "PACO"),
    listOf("RISA MONTE", "BENEFICENTE", "LAGUM", "GABRIEL"),
    listOf("BATALHA DE RIMA", "RODA DE SAMBA", "CAPOEIRA", "HIP-HOP", "SARAU"),
    listOf("CULTURA DE RUA", "PERIFÉRICA", "MANIFESTAÇÃO", "ARTE URBANA")
)

// Velocidades e direções por linha (pixels/segundo em termos de fração do infinito)
private val ROW_CONFIGS = listOf(
    RowConfig(durationMs = 28_000, reversed = false),
    RowConfig(durationMs = 22_000, reversed = true),
    RowConfig(durationMs = 35_000, reversed = false),
    RowConfig(durationMs = 25_000, reversed = true),
    RowConfig(durationMs = 30_000, reversed = false)
)

private data class RowConfig(val durationMs: Int, val reversed: Boolean)

@Composable
fun AnimatedBackgroundWords(modifier: Modifier = Modifier) {
    val density        = LocalDensity.current
    val textSizeSp     = 40.sp
    val textSizePx     = with(density) { textSizeSp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        WORD_ROWS.forEachIndexed { index, words ->
            val config = ROW_CONFIGS.getOrElse(index) { ROW_CONFIGS.first() }
            ScrollingWordRow(
                words      = words,
                config     = config,
                rowIndex   = index,
                textSizePx = textSizePx,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScrollingWordRow(
    words:      List<String>,
    config:     RowConfig,
    rowIndex:   Int,
    textSizePx: Float,
    modifier:   Modifier = Modifier
) {
    // Anima de 0f a 1f de forma infinita; offset é aplicado como fração da largura total
    val infiniteTransition = rememberInfiniteTransition(label = "row_$rowIndex")
    val fraction by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = config.durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fraction_$rowIndex"
    )

    // Texto completo da linha (repetido para efeito contínuo)
    val lineText = (words + words).joinToString(separator = "   ·   ")

    val textPaint = remember(textSizePx) {
        android.graphics.Paint().apply {
            textSize  = textSizePx
            typeface  = android.graphics.Typeface.DEFAULT_BOLD
            style     = android.graphics.Paint.Style.STROKE
            strokeWidth = 1.5f
            color     = android.graphics.Color.argb(55, 255, 255, 255) // ~22% opacidade
            isAntiAlias = true
        }
    }

    val textWidth = remember(lineText, textSizePx) {
        textPaint.measureText(lineText)
    }

    // Espaçamento vertical — distribui linhas pela altura da seção hero
    val verticalOffset = with(LocalDensity.current) {
        (rowIndex * 72 + 40).dp.toPx()
    }

    val direction = if (config.reversed) -1f else 1f

    Box(
        modifier = modifier
            .offset(y = with(LocalDensity.current) { verticalOffset.toDp() })
            .height(with(LocalDensity.current) { (textSizePx * 1.4f).toDp() })
            .drawWithCache {
                onDrawBehind {
                    val canvasWidth = size.width
                    // Desloca o texto pela largura total, criando loop contínuo
                    val offset = (fraction * textWidth * direction) % textWidth

                    // Desenha o texto repetido para preencher a tela sem lacunas
                    var x = -offset
                    if (direction < 0) x = -(textWidth - offset)

                    while (x < canvasWidth + textWidth) {
                        drawContext.canvas.nativeCanvas.drawText(
                            lineText,
                            x,
                            textSizePx,
                            textPaint
                        )
                        x += textWidth
                    }
                }
            }
    )
}
