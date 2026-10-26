package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.ui.theme.*

/**
 * Tela provisória de acesso ao Culturama.
 * Apresenta as opções "Entrar" e "Criar conta".
 * A implementação definitiva de autenticação será feita na próxima etapa.
 *
 * TODO: Implementar autenticação real (Firebase Auth ou outro provider).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to HeroGradientTop,
                        0.4f to HeroGradientMid,
                        1.0f to CulturamaBackground
                    )
                )
            )
    ) {
        // ── Botão voltar ─────────────────────────────────────────────────
        IconButton(
            onClick  = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint               = TextPrimary
            )
        }

        // ── Conteúdo central ─────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Text(
                text  = "Culturama",
                style = MaterialTheme.typography.displayMedium.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize   = 44.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text      = "Acesse sua conta para descobrir e favoritar eventos culturais.",
                style     = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // ── Botão Criar conta ─────────────────────────────────────────
            Button(
                onClick  = { /* TODO: Navegar para tela de cadastro definitiva */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = CulturamaOrange,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text  = "Criar conta",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // ── Botão Entrar ─────────────────────────────────────────────
            OutlinedButton(
                onClick  = { /* TODO: Navegar para tela de login definitiva */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        listOf(CulturamaOrange.copy(alpha = 0.6f), CulturamaOrange)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text  = "Entrar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            // Nota sobre a etapa atual
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Text(
                    text      = "🔧  Autenticação será implementada na próxima etapa do desenvolvimento.",
                    style     = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(16.dp)
                )
            }
        }

        // ── Rodapé ────────────────────────────────────────────────────────
        Text(
            text      = "Culturama · Cultura perto de você",
            style     = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
            textAlign = TextAlign.Center,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        )
    }
}
