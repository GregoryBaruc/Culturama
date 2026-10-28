package com.example.culturama.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.component.OrganizerActionButton
import com.example.culturama.ui.theme.*

/**
 * Destino Provisório de Acesso para Organizadores (Etapa 4).
 *
 * Rota exclusiva e separada do fluxo de usuários comuns.
 * Informa com transparência que os formulários definitivos de cadastro e login de organizadores
 * serão implementados na próxima etapa do projeto, garantindo conformidade com os requisitos.
 */
@Composable
fun OrganizerAccessScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    fun handleBack() {
        if (!navController.popBackStack()) {
            navController.navigate(Screen.Organizers.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = CulturamaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to HeroGradientTop.copy(alpha = 0.35f),
                            0.3f to HeroGradientMid.copy(alpha = 0.25f),
                            0.7f to Color(0xFF141013),
                            1.0f to CulturamaBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Barra Superior com Botão Voltar ──────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { handleBack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar para apresentação de organizadores",
                            tint = TextPrimary
                        )
                    }

                    // Logo com ponto laranja
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

                    // Espaçador para balanceamento
                    Spacer(Modifier.size(40.dp))
                }

                Spacer(Modifier.height(8.dp))

                // ── Badge de status da área ──────────────────────────────────
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF221714),
                    border = BorderStroke(1.dp, CulturamaOrange.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CulturamaOrange)
                        )
                        Text(
                            text = "ÁREA EXCLUSIVA DE ORGANIZADORES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CulturamaOrange,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ── Título Principal ─────────────────────────────────────────
                Text(
                    text = "Acesso de Organizadores",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Ambiente dedicado a produtores culturais, coletivos, artistas independentes, casas de shows e empresas.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ── Card Informativo de Status Provisório ─────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF19171C),
                    border = BorderStroke(1.dp, Color(0xFF2E2B38)),
                    shadowElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF281C18)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CulturamaIcons.OrganizerBadge,
                                    contentDescription = null,
                                    tint = CulturamaOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Cadastro em Preparação",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Próxima etapa do TCC",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CulturamaOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFF2B2836),
                            thickness = 1.dp
                        )

                        Text(
                            text = "Para manter a integridade da plataforma e garantir que manifestações culturais independentes tenham visibilidade de confiança, a área de organizadores contará com fluxo próprio:",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            )
                        )

                        OrganizerFeatureBullet(
                            icon = CulturamaIcons.VerifiedBadge,
                            title = "Validação Documental",
                            description = "Envio de CPF ou CNPJ com selo de verificado para afastar golpes."
                        )

                        OrganizerFeatureBullet(
                            icon = CulturamaIcons.PublishEvent,
                            title = "Publicador de Atrações",
                            description = "Cadastro de data, local, capa, ingressos e rotas de transporte."
                        )

                        OrganizerFeatureBullet(
                            icon = CulturamaIcons.Groups,
                            title = "Múltiplos Eventos",
                            description = "Gestão de eventos recorrentes por um único perfil de coletivo."
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Aviso de separação de fluxos
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = "ℹ️ Este fluxo é exclusivo para organizadores e não se mistura com o cadastro do público geral do Culturama.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextTertiary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Ações de Navegação ───────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OrganizerActionButton(
                        text = "Voltar para apresentação",
                        showArrow = false,
                        isPrimary = true,
                        onClick = { handleBack() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OrganizerActionButton(
                        text = "Ir para a tela inicial",
                        showArrow = false,
                        isPrimary = false,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OrganizerFeatureBullet(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF221714)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CulturamaOrange,
                modifier = Modifier.size(13.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            )
        }
    }
}
