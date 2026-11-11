package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.AcessoDaConta
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.acesso.ResultadoDeAcesso
import com.example.culturama.data.acesso.comPrivilegioDeAdministrador
import com.example.culturama.data.admin.AcessoAdministrativo
import com.example.culturama.data.admin.EmpresaDoPainel
import com.example.culturama.data.admin.EmpresasDoPainelRepository
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.rotuloSituacao
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

/**
 * Área pedida depois do login. Não é o painel completo.
 * Sem autorização do servidor, mostra o motivo e as saídas possíveis.
 * Administrador do painel entra nas áreas de organizador e empresa.
 */
@Composable
fun AreaDaContaScreen(
    navController: NavController,
    tipo: String,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val intencao = IntencaoDeAcesso.porId(tipo)
    var resultado by remember { mutableStateOf<ResultadoDeAcesso?>(null) }
    var comoAdministrador by remember { mutableStateOf(false) }

    LaunchedEffect(tipo) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.createRoute(destino = intencao.id)) {
                popUpTo(Screen.AreaDaConta.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        val daConta = when (intencao) {
            IntencaoDeAcesso.ORGANIZADOR -> AcessoDaConta.situacaoOrganizador()
            IntencaoDeAcesso.EMPRESA -> AcessoDaConta.situacaoEmpresa()
            else -> ResultadoDeAcesso.AUSENTE
        }
        val admin = AcessoAdministrativo.verificar()
            .getOrDefault(PapelNoPainel.NENHUM) == PapelNoPainel.ADMINISTRADOR
        comoAdministrador = admin && daConta != ResultadoDeAcesso.AUTORIZADO
        resultado = comPrivilegioDeAdministrador(daConta, administrador = admin)
    }

    val titulo = if (intencao == IntencaoDeAcesso.EMPRESA) "Culturama Insights" else "Área do organizador"
    val autorizado = resultado == ResultadoDeAcesso.AUTORIZADO

    if (intencao == IntencaoDeAcesso.ORGANIZADOR && autorizado) {
        MuralDoOrganizadorScreen(
            navController = navController,
            nomeDaSessao = authService.contaAtual()?.nome,
            comoAdministrador = comoAdministrador,
            onVoltar = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Organizers.route) { launchSingleTop = true }
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Acessos.route) { launchSingleTop = true }
                }
            }) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (resultado) {
                null -> {
                    CircularProgressIndicator(color = CulturamaOrange)
                    Text("Conferindo a permissão desta conta…", color = TextSecondary)
                }
                ResultadoDeAcesso.AUTORIZADO -> {
                    if (comoAdministrador && intencao == IntencaoDeAcesso.EMPRESA) {
                        EmpresasDoAdministrador()
                    } else {
                        TextoAutorizado(intencao, comoAdministrador)
                    }
                }
                else -> Text(
                    textoDoBloqueio(intencao, resultado!!),
                    style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                )
            }
            if (resultado != null && !autorizado && intencao != IntencaoDeAcesso.DESCOBERTA) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.SolicitacaoDeAcesso.createRoute(intencao.id)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (intencao == IntencaoDeAcesso.EMPRESA) "Ver solicitação empresarial" else "Ver cadastro de organizador")
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
            ) { Text("Ir para a descoberta cultural") }
            OutlinedButton(
                onClick = { navController.navigate(Screen.Profile.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Abrir perfil") }
        }
    }
}

@Composable
private fun EmpresasDoAdministrador() {
    var empresas by remember { mutableStateOf<List<EmpresaDoPainel>?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var escolhida by remember { mutableStateOf<String?>(null) }
    var trocando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        EmpresasDoPainelRepository.listar()
            .onSuccess {
                empresas = it
                trocando = true
            }
            .onFailure { aviso = it.message }
    }

    Text("Modo administrador", color = CulturamaOrange, fontWeight = FontWeight.SemiBold)
    val atual = empresas?.firstOrNull { it.id == escolhida }
    Text(
        "Empresa: ${atual?.razaoSocial ?: "Nenhuma empresa selecionada"}",
        color = TextPrimary
    )
    if (!trocando && atual != null) {
        Text(
            "Culturama Insights desta empresa. O painel de patrocínio ainda não carrega indicadores próprios. Situação: ${atual.rotuloSituacao()}.",
            color = TextPrimary
        )
        OutlinedButton(
            onClick = { trocando = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) { Text("Trocar empresa") }
    } else {
        Text(
            "Escolha a empresa que o servidor devolver para esta sessão. Nenhuma foi selecionada automaticamente.",
            color = TextSecondary
        )
        when {
            empresas == null && aviso == null -> Text("Consultando empresas…", color = TextSecondary)
            aviso != null && empresas.isNullOrEmpty() -> Text(aviso!!, color = TextPrimary)
            empresas.isNullOrEmpty() -> Text(
                "O servidor não devolveu empresas para esta sessão. Nenhuma empresa foi criada.",
                color = TextSecondary
            )
            else -> empresas?.forEach { empresa ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CulturamaCard)
                        .clickable {
                            escolhida = empresa.id
                            trocando = false
                        }
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(empresa.razaoSocial, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(empresa.rotuloSituacao(), color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun TextoAutorizado(intencao: IntencaoDeAcesso, comoAdministrador: Boolean) {
    val texto = when {
        comoAdministrador && intencao == IntencaoDeAcesso.EMPRESA ->
            "Você entrou como administrador. Pode usar a área empresarial desta conta."
        comoAdministrador ->
            "Você entrou como administrador. Pode usar a área de organizador desta conta."
        intencao == IntencaoDeAcesso.EMPRESA ->
            "Esta conta está aprovada como empresa. O painel de patrocínio e inteligência cultural entra numa próxima etapa."
        else ->
            "Esta conta está autorizada como organizador. O painel de publicação de eventos entra numa próxima etapa."
    }
    Text(texto, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary))
    Text(
        if (comoAdministrador) {
            "O acesso vem do papel de administrador, não de um cadastro próprio neste caminho."
        } else {
            "Nenhum dado de gestão é carregado aqui."
        },
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
    )
}

private fun textoDoBloqueio(intencao: IntencaoDeAcesso, resultado: ResultadoDeAcesso): String {
    val papel = if (intencao == IntencaoDeAcesso.EMPRESA) "empresa" else "organizador"
    return when (resultado) {
        ResultadoDeAcesso.PENDENTE ->
            "O cadastro de $papel desta conta ainda não foi aprovado. Entrar por este caminho não adianta a aprovação."
        ResultadoDeAcesso.RECUSADO ->
            "O cadastro de $papel desta conta não foi aprovado."
        ResultadoDeAcesso.AUSENTE ->
            "Esta conta não tem cadastro de $papel. O login não cria esse papel."
        ResultadoDeAcesso.INDETERMINADO ->
            if (intencao == IntencaoDeAcesso.ORGANIZADOR) {
                "Há um cadastro de organizador ligado a esta conta, mas o servidor não informa se ele está aprovado. O mural permanece fechado até essa confirmação."
            } else {
                "Há um cadastro ligado a esta conta, mas o servidor não informou se ele está aprovado. O acesso permanece bloqueado."
            }
        ResultadoDeAcesso.FALHA ->
            "Não foi possível confirmar a autorização. O acesso permanece bloqueado."
        ResultadoDeAcesso.AUTORIZADO -> ""
    }
}
