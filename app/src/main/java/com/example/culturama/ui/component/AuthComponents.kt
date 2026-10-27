package com.example.culturama.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.ui.theme.*

/**
 * Banner de apresentação do Culturama inspirado na seção lateral da referência:
 * "Seu próximo rolê começa aqui"
 * - Lista de recursos com ícones
 * - Miniaturas estilizadas de eventos culturais
 */
@Composable
fun AuthHeroBanner(
    modifier: Modifier = Modifier,
    isSignUp: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Título estilizado: "Seu próximo rolê começa aqui"
        val annotatedTitle = buildAnnotatedString {
            append("Seu próximo ")
            withStyle(
                SpanStyle(
                    color = CulturamaOrange,
                    fontWeight = FontWeight.Black
                )
            ) {
                append("rolê")
            }
            append("\ncomeça aqui")
        }

        Text(
            text = annotatedTitle,
            style = MaterialTheme.typography.headlineLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp
            )
        )

        Spacer(Modifier.height(14.dp))

        // Lista de benefícios com ícones da identidade Culturama
        FeatureBenefitItem(
            icon = CulturamaIcons.HeartOutline,
            text = "Salve eventos nos favoritos"
        )
        Spacer(Modifier.height(8.dp))
        FeatureBenefitItem(
            icon = CulturamaIcons.DirectionsCar,
            text = "Veja como chegar de carro, metrô ou a pé"
        )
        Spacer(Modifier.height(8.dp))
        FeatureBenefitItem(
            icon = CulturamaIcons.CalendarMonth,
            text = "Publique seus próprios eventos"
        )

        Spacer(Modifier.height(16.dp))

        // Mini cards sobrepostos inspirados na imagem de referência
        Row(
            modifier = Modifier.padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy((-10).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniCultureCard(
                label = "Música",
                rotation = -6f,
                gradient = listOf(Color(0xFF381E15), Color(0xFF1F110B)),
                emoji = "🎸"
            )
            MiniCultureCard(
                label = "Artes",
                rotation = 2f,
                gradient = listOf(Color(0xFF281C30), Color(0xFF140D1A)),
                emoji = "🎨"
            )
            MiniCultureCard(
                label = "Teatro & Rua",
                rotation = 7f,
                gradient = listOf(Color(0xFF1B2B28), Color(0xFF0D1715)),
                emoji = "🎭"
            )
        }
    }
}

@Composable
private fun FeatureBenefitItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CulturamaOrange,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                fontSize = 13.5.sp
            )
        )
    }
}

@Composable
private fun MiniCultureCard(
    label: String,
    rotation: Float,
    gradient: List<Color>,
    emoji: String
) {
    Surface(
        modifier = Modifier
            .size(width = 68.dp, height = 82.dp)
            .rotate(rotation),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
                .padding(6.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Botão "Continuar com Google" conforme a imagem de referência.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E23),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF33333E))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // "G" estilizado do Google
            GoogleLogoBadge()

            Spacer(Modifier.width(12.dp))

            Text(
                text = "Continuar com Google",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun GoogleLogoBadge() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color(0xFF4285F4),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        )
    }
}

/**
 * Divisor visual "ou com e-mail" conforme a imagem de referência.
 */
@Composable
fun AuthOrDivider(
    text: String = "ou com e-mail",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF2C2C34),
            thickness = 1.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextTertiary,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color(0xFF2C2C34),
            thickness = 1.dp
        )
    }
}

/**
 * Botão principal de ação (ex: "Criar conta", "Entrar") com visual Culturama.
 */
@Composable
fun CulturamaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CulturamaOrange,
            contentColor = Color.White,
            disabledContainerColor = CulturamaOrange.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(26.dp), // Estilo pílula da referência
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
