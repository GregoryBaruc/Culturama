package com.example.culturama.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.preference.UserPreferencesRepository
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaPrimaryButton
import com.example.culturama.ui.component.OrganicBubbleCloud
import com.example.culturama.ui.theme.*

/**
 * Tela de Personalização de Interesses Culturais do Usuário (Etapa 3).
 *
 * Reproduz fielmente o visual da imagem de referência:
 * - Título com destaque: "Do que você curte?"
 * - Subtítulo: "Escolha pelo menos 3 pra gente montar seu rolê"
 * - Composição orgânica de bolinhas interativas de diferentes tamanhos
 * - Animações fluidas de escala elástica e selo de checkmark ao selecionar
 * - Indicador dinâmico de interesses escolhidos
 * - Botão "Continuar" e opção "Pular por enquanto" com retorno à Home existente
 */
@Composable
fun PreferencesScreen(
    navController: NavController,
    preferencesRepository: UserPreferencesRepository = UserPreferencesRepository.getInstance(LocalContext.current)
) {
    val scrollState = rememberScrollState()

    // ── Estado das categorias e seleção ─────────────────────────────────
    val availableCategories = remember { preferencesRepository.getAvailableCategories() }
    var selectedIds by remember { mutableStateOf(preferencesRepository.getSelectedCategoryIds()) }

    // ── Ação de salvar e concluir ───────────────────────────────────────
    fun handleContinue() {
        preferencesRepository.savePreferences(selectedIds)
        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
        if (!popped) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun handleSkip() {
        preferencesRepository.setPreferencesConfigured(true)
        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
        if (!popped) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = CulturamaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // ── Barra inferior com botão "Continuar" e "Pular por enquanto" ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF131317),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22222A)),
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CulturamaPrimaryButton(
                        text = if (selectedIds.isNotEmpty()) "Continuar (${selectedIds.size})" else "Continuar",
                        onClick = { handleContinue() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    TextButton(
                        onClick = { handleSkip() },
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Pular por enquanto",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.5.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to HeroGradientTop.copy(alpha = 0.45f),
                            0.3f to HeroGradientMid.copy(alpha = 0.35f),
                            0.7f to Color(0xFF151013),
                            1.0f to CulturamaBackground
                        )
                    )
                )
        ) {
            // ── Brilho radial acolhedor atrás das bolinhas ────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CulturamaOrange.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = 700f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Barra Superior com Botão Voltar ──────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            if (!navController.popBackStack()) {
                                handleSkip()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }

                    // Logo com o ponto laranja
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Culturama",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 3.dp, top = 2.dp)
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(CulturamaOrange)
                        )
                    }

                    // Ação rápida Pular
                    TextButton(onClick = { handleSkip() }) {
                        Text(
                            text = "Pular",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Cabeçalho Principal (Fiel à Imagem de Referência) ────────
                val annotatedHeading = buildAnnotatedString {
                    append("Do que você ")
                    withStyle(
                        SpanStyle(
                            color = CulturamaOrange,
                            fontWeight = FontWeight.Black
                        )
                    ) {
                        append("curte?")
                    }
                }

                Text(
                    text = annotatedHeading,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Subtítulo da referência
                Text(
                    text = "Escolha pelo menos 3 pra gente montar seu rolê",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.5.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(Modifier.height(12.dp))

                // ── Badge de contador de seleção ─────────────────────────────
                val counterText = when {
                    selectedIds.isEmpty() -> "Toque nas bolinhas para escolher seus gostos"
                    selectedIds.size == 1 -> "1 interesse selecionado (escolha mais 2)"
                    selectedIds.size == 2 -> "2 interesses selecionados (escolha mais 1)"
                    else -> "${selectedIds.size} interesses selecionados ✨"
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectedIds.size >= 3) CulturamaOrange.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.07f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedIds.size >= 3) CulturamaOrange.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = counterText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selectedIds.size >= 3) CulturamaOrange else TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                // ── Nuvem Orgânica de Bolinhas (Composição Central) ──────────
                OrganicBubbleCloud(
                    categories = availableCategories,
                    selectedIds = selectedIds,
                    onToggleCategory = { id ->
                        selectedIds = preferencesRepository.toggleCategory(id)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
