package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.admin.AcessoAdministrativo
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

private val AREAS = listOf(
    "Revisão de eventos",
    "Eventos, conforme a autorização da conta",
    "Comentários",
    "Denúncias"
)

/**
 * Área do moderador. Não carrega o painel administrativo nem executa ações.
 * A autorização é conferida de novo aqui.
 */
@Composable
fun ModeracaoScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val logado by authService.isLoggedIn.collectAsState(initial = authService.contaAtual() != null)
    var papel by remember { mutableStateOf<PapelNoPainel?>(null) }
    var erro by remember { mutableStateOf<String?>(null) }
    var tentativa by remember { mutableIntStateOf(0) }

    LaunchedEffect(tentativa, logado) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        if (!logado) return@LaunchedEffect
        papel = null
        erro = null
        AcessoAdministrativo.verificar().fold(
            onSuccess = { papel = it },
            onFailure = { erro = it.message ?: "Não foi possível confirmar a permissão." }
        )
    }

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
                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar ao perfil",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Moderação",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        when {
            erro != null -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Cartao(
                    titulo = "Não foi possível confirmar",
                    texto = erro.orEmpty()
                )
                OutlinedButton(
                    onClick = { tentativa++ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Tentar de novo")
                }
            }

            papel == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CulturamaOrange)
            }

            papel == PapelNoPainel.NENHUM -> Cartao(
                titulo = "Sem permissão",
                texto = "Esta área só abre quando o servidor marca a conta como moderadora ou administradora."
            )

            else -> Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Cartao(
                    titulo = "Em desenvolvimento",
                    texto = "O painel de moderação ainda não executa ações. Quando existir, a conta moderadora trabalha nestas filas:",
                    modifier = Modifier
                )
                AREAS.forEach { area ->
                    Text(
                        text = area,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CulturamaCard)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                    )
                }
                Text(
                    text = "Usuários, papéis, empresas e configurações gerais ficam fora desta área.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun Cartao(
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
        )
    }
}
