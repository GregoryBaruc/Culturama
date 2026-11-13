package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.planos.AcaoDoPlano
import com.example.culturama.data.planos.PlanosCatalogo
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CartaoDePlano
import com.example.culturama.ui.component.CulturamaLogo
import com.example.culturama.ui.component.SecaoComparacaoDePlanos
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

/**
 * Apresentação dos planos Livre, Plus e Max para organizadores.
 * CTAs reutilizam cadastro/login/área já existentes; sem pagamento.
 */
@Composable
fun PlanosOrganizadorScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val logado by authService.isLoggedIn.collectAsState(initial = false)
    val planos = PlanosCatalogo.organizadores

    fun cadastrar() {
        navController.navigate(Screen.SolicitacaoDeAcesso.createRoute(IntencaoDeAcesso.ORGANIZADOR.id))
    }

    fun entrar() {
        if (logado) {
            navController.navigate(Screen.AreaDaConta.createRoute(IntencaoDeAcesso.ORGANIZADOR.id))
        } else {
            navController.navigate(Screen.Login.createRoute(destino = IntencaoDeAcesso.ORGANIZADOR.id))
        }
    }

    fun acaoDoPlano(acao: AcaoDoPlano) {
        when (acao) {
            AcaoDoPlano.CADASTRO_ORGANIZADOR -> cadastrar()
            AcaoDoPlano.EM_BREVE -> Unit
            else -> Unit
        }
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
                    navController.navigate(Screen.Organizers.route) {
                        popUpTo(Screen.PlanosOrganizador.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            CulturamaLogo(height = 24.dp)
            Text(
                text = "  Planos organizadores",
                style = MaterialTheme.typography.titleMedium.copy(
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
                text = "Divulgue sua cena no Culturama",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Três planos para quem organiza cultura independente. Os recursos pagos aparecem como Em breve e não são cobrados neste app.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
            )

            planos.forEach { plano ->
                CartaoDePlano(
                    plano = plano,
                    onAcao = { acaoDoPlano(plano.acao) }
                )
            }

            SecaoComparacaoDePlanos(
                titulo = "Qual plano combina com você?",
                nomes = planos.map { it.nome.removePrefix("Culturama ").trim() },
                linhas = PlanosCatalogo.comparacaoOrganizadores,
                rodape = "* Sujeito às regras da plataforma. ** Quando os indicadores estiverem disponíveis."
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Já faz parte da plataforma?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Button(
                    onClick = ::cadastrar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange, contentColor = Color.White)
                ) {
                    Text("Cadastrar como organizador", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = ::entrar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(if (logado) "Acessar meu mural" else "Já sou organizador")
                }
                OutlinedButton(
                    onClick = {
                        if (!navController.popBackStack(Screen.Organizers.route, inclusive = false)) {
                            navController.navigate(Screen.Organizers.route) {
                                popUpTo(Screen.PlanosOrganizador.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Conhecer a página para organizadores")
                }
            }
        }
    }
}
