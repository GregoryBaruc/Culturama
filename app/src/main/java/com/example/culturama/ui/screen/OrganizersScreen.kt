package com.example.culturama.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*

/**
 * Tela "Para Organizadores" do Culturama.
 *
 * Reorganiza o conteúdo das três referências de desktop para uma experiência mobile moderna:
 * - Imagem 2: Hero com título "Sua cena no mapa da cidade", badge "PARA QUEM ORGANIZA",
 *   CTAs "Criar conta grátis" / "Já tenho conta" e nuvem de tags culturais interativas.
 * - Imagem 1: Seção "O que você ganha" com 4 benefícios estruturados em cards + Seção "Como funciona" com 3 passos.
 * - Imagem 3: Seção "Coletivo, assessoria ou clube com vários eventos?" com CTA final "Criar conta grátis".
 *
 * Todos os botões "Criar conta grátis" direcionam para a rota exclusiva de organizadores (Screen.OrganizerAccess).
 */
@Composable
fun OrganizersScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    // Animação de entrada suave da página
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    fun navigateToOrganizerAccess() {
        navController.navigate(Screen.OrganizerAccess.route)
    }

    fun handleBack() {
        if (!navController.popBackStack()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = CulturamaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            // Cabeçalho fixo no topo com retorno e acesso rápido
            OrganizerTopBar(
                onBackClick = { handleBack() },
                onPrimaryCtaClick = { navigateToOrganizerAccess() },
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF5101014),
                            Color(0xCC101014),
                            Color.Transparent
                        )
                    )
                )
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                initialOffsetY = { 40 },
                animationSpec = tween(500)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0xFF141012),
                                0.2f to CulturamaBackground,
                                0.8f to CulturamaBackground,
                                1.0f to Color(0xFF140F11)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(36.dp)
                ) {
                    // ═══════════════════════════════════════════════════════
                    // 1. SEÇÃO PRINCIPAL — APRESENTAÇÃO HERO (IMAGEM 2)
                    // ═══════════════════════════════════════════════════════
                    OrganizerHeroSection(
                        onPrimaryCtaClick = { navigateToOrganizerAccess() },
                        onSecondaryCtaClick = { navigateToOrganizerAccess() }
                    )

                    // ═══════════════════════════════════════════════════════
                    // 2. BENEFÍCIOS: "O QUE VOCÊ GANHA" (IMAGEM 1)
                    // ═══════════════════════════════════════════════════════
                    OrganizerBenefitsSection()

                    // ═══════════════════════════════════════════════════════
                    // 3. PASSO A PASSO: "COMO FUNCIONA" (IMAGEM 1)
                    // ═══════════════════════════════════════════════════════
                    OrganizerHowItWorksSection()

                    // ═══════════════════════════════════════════════════════
                    // 4. CHAMADA FINAL PARA AÇÃO & COLETIVOS (IMAGEM 3)
                    // ═══════════════════════════════════════════════════════
                    OrganizerCollectiveCtaSection(
                        onPrimaryCtaClick = { navigateToOrganizerAccess() }
                    )

                    // ═══════════════════════════════════════════════════════
                    // 5. RODAPÉ INSTITUCIONAL
                    // ═══════════════════════════════════════════════════════
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Culturama · A plataforma da cena independente",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextTertiary,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Trabalho de Conclusão de Curso · ADS",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextTertiary.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
