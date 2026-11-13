package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CartaoHubDePlanos
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

private val CorEmpresa = Color(0xFF8EADD4)

/**
 * Hub de planos comerciais (demonstrativo). Duas entradas: organizadores e empresas.
 * Não substitui o login; autenticação continua nos fluxos já existentes.
 */
@Composable
fun PlanosScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            Text(
                text = "Planos",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Escolha o caminho que combina com o seu projeto cultural ou com a sua marca.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            Text(
                text = "Os planos pagos ainda são uma proposta comercial demonstrativa. Nada é cobrado neste aplicativo.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            CartaoHubDePlanos(
                titulo = "Para organizadores",
                resumo = "Três planos para divulgar eventos: Culturama Livre, Plus e Max.",
                icone = CulturamaIcons.PublishEvent,
                cor = CulturamaOrange,
                rotuloBotao = "Ver planos de organizadores",
                onClick = {
                    navController.navigate(Screen.PlanosOrganizador.route) { launchSingleTop = true }
                }
            )

            CartaoHubDePlanos(
                titulo = "Para empresas",
                resumo = "Três modelos: Empresa Local, Conexão e Impacto — com Insights e patrocínio.",
                icone = CulturamaIcons.VerifiedBadge,
                cor = CorEmpresa,
                rotuloBotao = "Ver planos empresariais",
                onClick = {
                    navController.navigate(Screen.PlanosEmpresa.route) { launchSingleTop = true }
                }
            )
        }
    }
}
