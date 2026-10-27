package com.example.culturama.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.model.CategoryPreference
import com.example.culturama.ui.theme.*

/**
 * Bolinha interativa de categoria cultural inspirada na referência do Culturama.
 *
 * Características:
 * - Forma circular com tamanhos orgânicos variados.
 * - Animação de escala elástica com efeito spring ao selecionar ou desmarcar.
 * - Transição suave de cor: grafite escuro -> gradiente terracota/laranja com brilho.
 * - Selo circular branco com checkmark laranja (✓) na extremidade superior direita quando selecionado.
 * - Rótulo central com tipografia legível e ícone temático alinhado.
 * - Acessibilidade completa com semântica de estado.
 */
@Composable
fun CategoryBubble(
    category: CategoryPreference,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // ── Animação de escala com amortecimento elástico (spring) ─────────────
    val targetScale = if (isSelected) 1.08f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bubble_scale_${category.id}"
    )

    // ── Animação suave de cores e brilho ──────────────────────────────────
    val unselectedColor = Color(0xFF1E1E24)
    val selectedGradientStart = CulturamaOrange
    val selectedGradientEnd = CulturamaOrangeDeep

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFFFB18F) else Color(0xFF33333E),
        animationSpec = tween(300),
        label = "border_color_${category.id}"
    )

    val sizeDp = category.sizeType.baseDp.dp
    val textSp = category.sizeType.textSp.sp
    val iconDp = category.sizeType.iconDp.dp

    val accessibilityDesc = if (isSelected) {
        "${category.name.replace("\n", " ")}, selecionado. Toque para desmarcar."
    } else {
        "${category.name.replace("\n", " ")}, não selecionado. Toque para selecionar."
    }

    Box(
        modifier = modifier
            .size(sizeDp + 16.dp) // Espaço extra para o selo e a escala sem clipping
            .semantics {
                contentDescription = accessibilityDesc
            },
        contentAlignment = Alignment.Center
    ) {
        // ── Bolinha Principal ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(sizeDp)
                .scale(scale)
                .shadow(
                    elevation = if (isSelected) 14.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (isSelected) CulturamaOrange.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.4f),
                    ambientColor = if (isSelected) CulturamaOrange.copy(alpha = 0.4f) else Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    if (isSelected) {
                        Brush.verticalGradient(
                            listOf(selectedGradientStart, selectedGradientEnd)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(unselectedColor, Color(0xFF141418))
                        )
                    }
                )
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Checkbox,
                    onClick = onToggle
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Ícone temático da categoria
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(iconDp)
                )

                Spacer(Modifier.height(3.dp))

                // Nome da categoria centralizado
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = textSp,
                        lineHeight = (textSp.value + 3).sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )
            }
        }

        // ── Selo Circular Branco com Checkmark Laranja (✓) ────────────────────
        // Inspirado diretamente na referência visual anexada
        val badgeScale by animateFloatAsState(
            targetValue = if (isSelected) 1.0f else 0.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "badge_scale_${category.id}"
        )

        if (badgeScale > 0.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = if (category.sizeType.baseDp > 110) 4.dp else 2.dp,
                        end = if (category.sizeType.baseDp > 110) 6.dp else 4.dp
                    )
                    .scale(badgeScale)
                    .size(24.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CulturamaIcons.Checkmark,
                    contentDescription = null,
                    tint = CulturamaOrange,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
