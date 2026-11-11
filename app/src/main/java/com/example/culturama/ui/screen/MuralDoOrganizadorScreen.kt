package com.example.culturama.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.CargaDoMural
import com.example.culturama.data.acesso.EventoDoMural
import com.example.culturama.data.acesso.MuralDoOrganizador
import com.example.culturama.data.acesso.PerfilDoMural
import com.example.culturama.data.acesso.ResumoDeOrganizacao
import com.example.culturama.data.acesso.SituacaoCadastral
import com.example.culturama.data.acesso.contagem
import com.example.culturama.data.acesso.situacaoCadastral
import com.example.culturama.data.admin.lerInstante
import com.example.culturama.data.admin.textoDaData
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary

private enum class SecaoDoMural { INICIO, EVENTOS, COLETIVO, VERIFICACAO, COLABORADORES }

/**
 * Espaço do organizador já autorizado. Não é o painel administrativo.
 * A lista e o formulário de eventos ficam em Meus eventos. O servidor ainda não grava essa edição.
 */
@Composable
fun MuralDoOrganizadorScreen(
    navController: NavController,
    nomeDaSessao: String?,
    onVoltar: () -> Unit,
    comoAdministrador: Boolean = false
) {
    var carga by remember { mutableStateOf<CargaDoMural?>(null) }
    var recarga by remember { mutableIntStateOf(0) }
    var secao by rememberSaveable { mutableStateOf(SecaoDoMural.INICIO.name) }
    var criarAberto by rememberSaveable { mutableStateOf(false) }
    var avisoConvite by rememberSaveable { mutableStateOf(false) }
    var organizacaoId by rememberSaveable { mutableStateOf<String?>(null) }
    var trocando by rememberSaveable { mutableStateOf(false) }
    var organizacoes by remember { mutableStateOf<List<ResumoDeOrganizacao>?>(null) }
    var avisoLista by remember { mutableStateOf<String?>(null) }
    val atual = SecaoDoMural.entries.firstOrNull { it.name == secao } ?: SecaoDoMural.INICIO

    LaunchedEffect(recarga, organizacaoId) {
        carga = if (organizacaoId.isNullOrBlank()) {
            MuralDoOrganizador.carregar()
        } else {
            MuralDoOrganizador.carregarComoAdministrador(organizacaoId!!)
        }
    }
    LaunchedEffect(comoAdministrador) {
        if (!comoAdministrador) return@LaunchedEffect
        MuralDoOrganizador.listarParaAdministrador()
            .onSuccess { organizacoes = it }
            .onFailure { avisoLista = it.message }
    }

    val pronta = carga as? CargaDoMural.Pronta
    if (atual == SecaoDoMural.EVENTOS && pronta != null) {
        MeusEventosDoOrganizador(
            eventos = pronta.eventos,
            abrirFormulario = criarAberto,
            onFormularioAberto = { criarAberto = false },
            onVoltar = { secao = SecaoDoMural.INICIO.name },
            onAbrirDetalhe = { id ->
                navController.navigate("eventDetail?eventId=${Uri.encode(id)}")
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
                if (atual != SecaoDoMural.INICIO) secao = SecaoDoMural.INICIO.name else onVoltar()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            Text(
                text = "Meu espaço cultural",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        }
        if (comoAdministrador) {
            val rotulo = organizacoes?.firstOrNull { it.id == (pronta?.perfil?.id ?: organizacaoId) }?.rotulo
                ?: pronta?.perfil?.nomeDoResponsavel
                ?: "Nenhuma organização selecionada"
            FaixaAdministrador(rotulo) {
                trocando = true
                secao = SecaoDoMural.INICIO.name
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (comoAdministrador && trocando) {
                SeletorDeOrganizacoes(
                    itens = organizacoes,
                    aviso = avisoLista,
                    onEscolher = { id ->
                        organizacaoId = id
                        trocando = false
                        secao = SecaoDoMural.INICIO.name
                    }
                )
            } else when (val estado = carga) {
                null -> {
                    CircularProgressIndicator(color = CulturamaOrange)
                    Text("Carregando o espaço cultural…", color = TextSecondary)
                }
                CargaDoMural.SemCadastro -> {
                    if (comoAdministrador && organizacaoId == null) {
                        Text(
                            "Esta conta não tem organização própria. Escolha uma organização que o servidor autorizar. Nenhuma foi selecionada automaticamente.",
                            color = TextPrimary
                        )
                        SeletorDeOrganizacoes(
                            itens = organizacoes,
                            aviso = avisoLista,
                            onEscolher = { id ->
                                organizacaoId = id
                                secao = SecaoDoMural.INICIO.name
                            }
                        )
                    } else {
                        Text(
                            "Esta conta não tem uma organização vinculada. O login não cria esse vínculo.",
                            color = TextPrimary
                        )
                    }
                }
                is CargaDoMural.Falhou -> Text(estado.mensagem, color = TextPrimary)
                is CargaDoMural.Pronta -> when (atual) {
                    SecaoDoMural.INICIO -> Inicio(
                        perfil = estado.perfil,
                        nomeDaSessao = nomeDaSessao,
                        eventos = estado.eventos,
                        onSecao = { secao = it.name },
                        onCriar = {
                            secao = SecaoDoMural.EVENTOS.name
                            criarAberto = true
                        },
                        onDescoberta = {
                            navController.navigate(Screen.Home.route) { launchSingleTop = true }
                        }
                    )
                    SecaoDoMural.EVENTOS -> Text(
                        "Abra Meus eventos quando o espaço cultural terminar de carregar.",
                        color = TextSecondary
                    )
                    SecaoDoMural.COLETIVO -> Coletivo(
                        perfil = estado.perfil,
                        nomeDaSessao = nomeDaSessao
                    )
                    SecaoDoMural.VERIFICACAO -> Verificacao(
                        perfil = estado.perfil,
                        nomeDaSessao = nomeDaSessao,
                        onAtualizar = { recarga++ }
                    )
                    SecaoDoMural.COLABORADORES -> Colaboradores(
                        perfil = estado.perfil,
                        nomeDaSessao = nomeDaSessao,
                        avisoConvite = avisoConvite,
                        onConvidar = { avisoConvite = true }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Inicio(
    perfil: PerfilDoMural,
    nomeDaSessao: String?,
    eventos: List<EventoDoMural>?,
    onSecao: (SecaoDoMural) -> Unit,
    onCriar: () -> Unit,
    onDescoberta: () -> Unit
) {
    val responsavel = perfil.nomeDoResponsavel ?: nomeDaSessao?.trim()?.takeIf { it.isNotEmpty() }
    CabecalhoDaOrganizacao(responsavel, situacaoCadastral(perfil.verificado, perfil.status))
    Text("Visão geral", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
    if (eventos == null) {
        Cartao {
            Text(
                "Não foi possível consultar os eventos desta organização. Nenhum número foi estimado.",
                color = TextSecondary
            )
        }
    } else {
        val numeros = eventos.contagem()
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Indicador("Publicados", numeros.publicados, Modifier.weight(1f))
            Indicador("Em análise", numeros.emAnalise, Modifier.weight(1f))
        }
        Indicador("Rascunhos", numeros.rascunhos, Modifier.fillMaxWidth())
        Text(
            "Os números são os eventos que esta conta pode ler. A lista traz no máximo 100.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
    }
    Text("Áreas", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
    Atalho(Icons.Filled.DateRange, "Meus eventos", "Eventos ligados a esta organização", { onSecao(SecaoDoMural.EVENTOS) })
    Atalho(CulturamaIcons.Groups, "Meu coletivo", "Dados que o servidor já envia", { onSecao(SecaoDoMural.COLETIVO) })
    Atalho(CulturamaIcons.VerifiedBadge, "Verificação", "Situação cadastral desta conta", { onSecao(SecaoDoMural.VERIFICACAO) })
    Atalho(CulturamaIcons.Person, "Colaboradores", "Vínculos da organização, quando existirem", { onSecao(SecaoDoMural.COLABORADORES) })
    Button(
        onClick = onCriar,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
    ) { Text("Criar novo evento", fontWeight = FontWeight.Bold) }
    OutlinedButton(
        onClick = onDescoberta,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) { Text("Ir para a descoberta cultural") }
}

@Composable
private fun CabecalhoDaOrganizacao(responsavel: String?, situacao: SituacaoCadastral) {
    Cartao {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CulturamaOrange.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    CulturamaIcons.Groups,
                    contentDescription = "Organização sem imagem",
                    tint = CulturamaOrange,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(
                    "Meu espaço cultural",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    "O servidor não envia o nome nem a imagem do coletivo.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
        if (responsavel != null) {
            Text("Responsável: $responsavel", color = TextPrimary)
        } else {
            Text("O servidor não enviou o nome do responsável.", color = TextSecondary)
        }
        Text(
            "Situação cadastral: ${situacao.titulo}",
            color = CulturamaOrange,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun Coletivo(perfil: PerfilDoMural, nomeDaSessao: String?) {
    val responsavel = perfil.nomeDoResponsavel ?: nomeDaSessao?.trim()?.takeIf { it.isNotEmpty() }
    Text("Meu coletivo", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
    Dado("Nome", "O servidor não envia o nome do coletivo.")
    Dado("Responsável", responsavel ?: "O servidor não enviou o nome do responsável.")
    Dado("Descrição", "O servidor não envia a descrição.")
    Dado("Tipo de manifestação", "O servidor não envia o tipo de manifestação cultural.")
    Dado("Tipo de documento", perfil.tipoDocumento?.let(::apresentarDocumento) ?: "O servidor não enviou o tipo de documento.")
    Dado("Localização", "O servidor não envia a localização.")
    Dado("Contatos públicos", "O servidor não envia contatos públicos.")
    Dado("Imagem", "O servidor não envia logotipo nem imagem do coletivo.")
    Text(
        "O número do documento não é exibido. A edição destes campos será disponibilizada quando o servidor autorizar a gravação. Esta tela não altera o cadastro.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun Verificacao(
    perfil: PerfilDoMural,
    nomeDaSessao: String?,
    onAtualizar: () -> Unit
) {
    val situacao = situacaoCadastral(perfil.verificado, perfil.status)
    val responsavel = perfil.nomeDoResponsavel ?: nomeDaSessao?.trim()?.takeIf { it.isNotEmpty() }
    val pedidoEm = lerInstante(perfil.criadoEm)?.let { textoDaData(it) }
    Text(
        "Verificação do meu coletivo",
        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
    )
    Cartao {
        Text(situacao.titulo, color = CulturamaOrange, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(situacao.detalhe, color = TextSecondary)
    }
    Dado("Nome da organização", "O servidor não envia o nome do coletivo.")
    Dado("Responsável", responsavel ?: "O servidor não enviou o nome do responsável.")
    Dado(
        "Data de solicitação",
        pedidoEm ?: "O servidor não envia a data de solicitação."
    )
    Dado("Data de aprovação", "O servidor não envia a data de aprovação.")
    Dado(
        "Observações",
        perfil.observacao ?: "Nenhuma observação veio nesta consulta."
    )
    Text("Documentação", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
    Dado(
        "Situação da documentação",
        "O servidor não envia arquivos. Só o tipo informado no cadastro está disponível: " +
            (perfil.tipoDocumento?.let(::apresentarDocumento) ?: "não informado") + "."
    )
    Dado(
        "Solicitação de correção",
        perfil.observacao ?: "Nenhuma solicitação de correção veio nesta consulta."
    )
    Text(
        "Nenhum documento pessoal ou empresarial é baixado, copiado ou exibido. O aplicativo não altera a verificação.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
    OutlinedButton(
        onClick = onAtualizar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) { Text("Atualizar consulta") }
}

@Composable
private fun Colaboradores(
    perfil: PerfilDoMural,
    nomeDaSessao: String?,
    avisoConvite: Boolean,
    onConvidar: () -> Unit
) {
    val responsavel = perfil.nomeDoResponsavel ?: nomeDaSessao?.trim()?.takeIf { it.isNotEmpty() }
    Text("Colaboradores", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
    Dado("Organização", "O servidor não envia o nome do coletivo.")
    Dado("Responsável nesta conta", responsavel ?: "O servidor não enviou o nome do responsável.")
    Cartao {
        Text("Nenhum colaborador vinculado", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(
            "O servidor não tem uma lista de pessoas ligadas a esta organização. Nenhum vínculo foi inventado.",
            color = TextSecondary
        )
    }
    OutlinedButton(
        onClick = onConvidar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) { Text("Convidar colaborador") }
    if (avisoConvite) {
        Cartao {
            Text(
                "Em desenvolvimento. O convite não é enviado e nenhum papel é concedido.",
                color = TextSecondary
            )
        }
    }
    Text(
        "Colaborador não é administrador nem moderador da plataforma. Esta tela não altera permissões.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun FaixaAdministrador(entidade: String, onTrocar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Modo administrador", color = CulturamaOrange, fontWeight = FontWeight.SemiBold)
        Text("Organização: $entidade", color = TextPrimary)
        OutlinedButton(onClick = onTrocar, shape = RoundedCornerShape(14.dp)) {
            Text("Trocar organização")
        }
    }
}

@Composable
private fun SeletorDeOrganizacoes(
    itens: List<ResumoDeOrganizacao>?,
    aviso: String?,
    onEscolher: (String) -> Unit
) {
    when {
        itens == null && aviso == null -> Text("Consultando organizações…", color = TextSecondary)
        aviso != null && itens.isNullOrEmpty() -> Text(aviso, color = TextPrimary)
        itens.isNullOrEmpty() -> Text(
            "O servidor não devolveu outras organizações para esta sessão. Nenhuma organização foi criada.",
            color = TextSecondary
        )
        else -> itens.forEach { item ->
            Cartao(onClick = { onEscolher(item.id) }) {
                Text(item.rotulo, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(item.situacao, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Indicador(rotulo: String, valor: Int, modifier: Modifier = Modifier) {
    Cartao(modifier) {
        Text(valor.toString(), color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Text(rotulo, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Atalho(icone: ImageVector, titulo: String, detalhe: String, onClick: () -> Unit) {
    Cartao(onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icone, contentDescription = null, tint = CulturamaOrange, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(detalhe, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Dado(rotulo: String, valor: String) {
    Cartao {
        Text(rotulo, color = TextSecondary, style = MaterialTheme.typography.labelLarge)
        Text(valor, color = TextPrimary)
    }
}

@Composable
private fun Cartao(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    conteudo: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CulturamaCard)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) { conteudo() }
}

private fun apresentarDocumento(tipo: String): String = when (tipo.trim().lowercase()) {
    "cpf" -> "CPF"
    "cnpj" -> "CNPJ"
    else -> tipo.trim()
}
