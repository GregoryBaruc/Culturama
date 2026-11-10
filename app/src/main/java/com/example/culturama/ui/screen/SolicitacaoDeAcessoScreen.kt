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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

/**
 * Interface do pedido. Não grava papel, não aprova empresa e não verifica organizador.
 * Pendência: a API do site não publica rota para esse cadastro.
 */
@Composable
fun SolicitacaoDeAcessoScreen(
    navController: NavController,
    tipo: String,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val intencao = IntencaoDeAcesso.porId(tipo).let {
        if (it == IntencaoDeAcesso.EMPRESA) it else IntencaoDeAcesso.ORGANIZADOR
    }
    val logado by authService.isLoggedIn.collectAsState(initial = false)
    val empresa = intencao == IntencaoDeAcesso.EMPRESA
    val titulo = if (empresa) "Cadastro empresarial" else "Cadastro de organizador"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (empresa) {
                    "O Culturama Insights abre quando o servidor marca a empresa como aprovada. Este pedido não faz essa aprovação."
                } else {
                    "A área de publicação abre quando o servidor marca o organizador como autorizado. Este pedido não cria esse selo."
                },
                style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
            )
            Text(
                text = if (empresa) {
                    "Não há, no aplicativo, rota para enviar documentos, CNPJ ou pedido de análise. Criar a conta cultural também não transforma a pessoa em empresa."
                } else {
                    "No site, o cadastro de organizador pede CPF ou CNPJ, o nome do coletivo ou da empresa e um documento. CNPJ ativo pode ser aprovado na hora; CPF passa pela equipe. O aplicativo ainda não envia esses dados e não conclui o cadastro."
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            Text(
                text = "Administrador e moderador não têm cadastro público. Esses papéis continuam só os que o servidor já reconhece.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            if (!logado) {
                Button(
                    onClick = { navController.navigate(Screen.SignUp.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
                ) { Text("Criar conta de usuário") }
            }
            OutlinedButton(
                onClick = {
                    if (logado) navController.navigate(Screen.AreaDaConta.createRoute(intencao.id))
                    else navController.navigate(Screen.Login.createRoute(destino = intencao.id))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (logado) "Conferir esta conta" else "Entrar e conferir")
            }
        }
    }
}
