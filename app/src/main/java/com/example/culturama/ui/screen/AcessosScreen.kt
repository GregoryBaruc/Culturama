package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
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
import androidx.navigation.NavController
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

private val CorEmpresa = Color(0xFF8EADD4)

/**
 * Três entradas na mesma sessão. Nenhuma delas cria administrador, moderador,
 * organizador verificado ou empresa aprovada.
 */
@Composable
fun AcessosScreen(
    navController: NavController,
    foco: String = "",
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val logado by authService.isLoggedIn.collectAsState(initial = false)
    val cartoes = listOf(
        CartaoDeAcesso(
            id = IntencaoDeAcesso.DESCOBERTA.id,
            titulo = "Usuário",
            resumo = "Descoberta cultural: eventos, mapa, favoritos e perfil.",
            cor = CulturamaOrange,
            primario = if (logado) "Abrir início" else "Entrar",
            secundario = if (logado) "Abrir perfil" else "Criar conta",
            terciario = if (logado) null else "Acessar perfil"
        ),
        CartaoDeAcesso(
            id = IntencaoDeAcesso.ORGANIZADOR.id,
            titulo = "Organizador",
            resumo = "Publicação de eventos. A área abre só se o servidor já autorizou esta conta.",
            cor = CulturamaOrange,
            primario = if (logado) "Continuar com esta conta" else "Entrar como organizador",
            secundario = "Cadastrar-se como organizador",
            terciario = "Acessar minha área"
        ),
        CartaoDeAcesso(
            id = IntencaoDeAcesso.EMPRESA.id,
            titulo = "Empresa",
            resumo = "Patrocínio e Culturama Insights. O painel abre só com cadastro aprovado.",
            cor = CorEmpresa,
            primario = if (logado) "Continuar com esta conta" else "Entrar como empresa",
            secundario = "Solicitar cadastro empresarial",
            terciario = "Acessar o Insights"
        )
    ).sortedByDescending { it.id == foco }

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
                text = "Acessos",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
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
                text = "A mesma conta pode ter mais de um papel. Entrar por um caminho não concede outro.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            cartoes.forEach { cartao ->
                CartaoDeEntrada(cartao, navController, logado)
            }
        }
    }
}

private data class CartaoDeAcesso(
    val id: String,
    val titulo: String,
    val resumo: String,
    val cor: Color,
    val primario: String,
    val secundario: String,
    val terciario: String?
)

@Composable
private fun CartaoDeEntrada(cartao: CartaoDeAcesso, navController: NavController, logado: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CulturamaCard
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                cartao.titulo,
                style = MaterialTheme.typography.titleLarge.copy(color = cartao.cor, fontWeight = FontWeight.Bold)
            )
            Text(cartao.resumo, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            Button(
                onClick = { acaoPrimaria(cartao.id, logado, navController) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = cartao.cor, contentColor = Color.White)
            ) { Text(cartao.primario) }
            OutlinedButton(
                onClick = { acaoSecundaria(cartao.id, logado, navController) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text(cartao.secundario) }
            cartao.terciario?.let { rotulo ->
                OutlinedButton(
                    onClick = { acaoTerciaria(cartao.id, logado, navController) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(rotulo) }
            }
            Spacer(Modifier.height(0.dp))
        }
    }
}

private fun acaoPrimaria(id: String, logado: Boolean, navController: NavController) {
    when (id) {
        IntencaoDeAcesso.DESCOBERTA.id -> {
            if (logado) navController.navigate(Screen.Home.route) { launchSingleTop = true }
            else navController.navigate(Screen.Login.createRoute())
        }
        IntencaoDeAcesso.ORGANIZADOR.id -> seguir(logado, IntencaoDeAcesso.ORGANIZADOR, navController)
        else -> seguir(logado, IntencaoDeAcesso.EMPRESA, navController)
    }
}

private fun acaoSecundaria(id: String, logado: Boolean, navController: NavController) {
    when (id) {
        IntencaoDeAcesso.DESCOBERTA.id -> {
            if (logado) navController.navigate(Screen.Profile.route)
            else navController.navigate(Screen.SignUp.route)
        }
        else -> navController.navigate(Screen.SolicitacaoDeAcesso.createRoute(id))
    }
}

private fun acaoTerciaria(id: String, logado: Boolean, navController: NavController) {
    when (id) {
        IntencaoDeAcesso.DESCOBERTA.id -> seguir(logado, IntencaoDeAcesso.PERFIL, navController)
        IntencaoDeAcesso.ORGANIZADOR.id -> seguir(logado, IntencaoDeAcesso.ORGANIZADOR, navController)
        else -> seguir(logado, IntencaoDeAcesso.EMPRESA, navController)
    }
}

private fun seguir(logado: Boolean, intencao: IntencaoDeAcesso, navController: NavController) {
    if (!logado) {
        navController.navigate(Screen.Login.createRoute(destino = intencao.id))
        return
    }
    val rota = when (intencao) {
        IntencaoDeAcesso.PERFIL, IntencaoDeAcesso.DESCOBERTA -> Screen.Profile.route
        else -> Screen.AreaDaConta.createRoute(intencao.id)
    }
    navController.navigate(rota) { launchSingleTop = true }
}
