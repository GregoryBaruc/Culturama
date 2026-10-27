package com.example.culturama.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.ui.theme.*

/**
 * Campo de texto personalizado com a identidade visual do Culturama.
 *
 * Características:
 * - Fundo escuro com bordas sutis.
 * - Realce com a cor CulturamaOrange quando focado.
 * - Borda e mensagem de erro em vermelho/coral quando inválido.
 * - Rótulo posicionado acima do campo de digitação, conforme referência.
 * - Suporte a texto de ajuda (ex: "Mínimo de 8 caracteres.").
 * - Controle integrado de exibição/ocultação de senha.
 * - Altura de toque confortável para acessibilidade móvel (52dp).
 */
@Composable
fun CulturamaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorMessage: String? = null,
    helperText: String? = null,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val isError = !errorMessage.isNullOrBlank()

    val borderColor = when {
        isError -> Color(0xFFE55353)
        isFocused -> CulturamaOrange
        else -> Color(0xFF2C2C34)
    }

    val backgroundColor = Color(0xFF131316)

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Rótulo do campo ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isError) Color(0xFFFF7A7A) else if (isFocused) CulturamaOrange else TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }

        // ── Caixa de entrada ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isFocused || isError) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícone à esquerda opcional
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) Color(0xFFE55353) else if (isFocused) CulturamaOrange else TextTertiary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 4.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }

                // Campo de digitação
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextTertiary.copy(alpha = 0.7f),
                                fontSize = 15.sp
                            )
                        )
                    }

                    val visualTransformation = if (isPassword && !isPasswordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        singleLine = singleLine,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = TextPrimary,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(CulturamaOrange),
                        visualTransformation = visualTransformation,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        interactionSource = interactionSource
                    )
                }

                // Botão de alternar visibilidade de senha
                if (isPassword && onPasswordVisibilityToggle != null) {
                    IconButton(
                        onClick = onPasswordVisibilityToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) CulturamaIcons.VisibilityOff else CulturamaIcons.Visibility,
                            contentDescription = if (isPasswordVisible) "Ocultar senha" else "Visualizar senha",
                            tint = if (isPasswordVisible) CulturamaOrange else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Texto de erro ou de ajuda ────────────────────────────────────
        AnimatedVisibility(
            visible = isError,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        if (!isError && !helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextTertiary,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
