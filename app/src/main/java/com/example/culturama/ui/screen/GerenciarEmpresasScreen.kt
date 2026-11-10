package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.culturama.data.admin.ACAO_INDISPONIVEL
import com.example.culturama.data.admin.AcaoDaEmpresa
import com.example.culturama.data.admin.EmpresaDoPainel
import com.example.culturama.data.admin.EmpresasDoPainelRepository
import com.example.culturama.data.admin.FiltroDeEmpresas
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.SituacaoDaEmpresa
import com.example.culturama.data.admin.aplicar
import com.example.culturama.data.admin.autorizarEmpresa
import com.example.culturama.data.admin.rotulo
import com.example.culturama.data.admin.rotuloSituacao
import com.example.culturama.data.admin.textoDeQuando
import com.example.culturama.ui.component.CulturamaTextField
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

private sealed interface EstadoDasEmpresas {
    data object Carregando : EstadoDasEmpresas
    data class Pronto(val itens: List<EmpresaDoPainel>) : EstadoDasEmpresas
    data class Erro(val mensagem: String) : EstadoDasEmpresas
}

@Composable
fun GerenciarEmpresasScreen(papel: PapelNoPainel) {
    var estado by remember { mutableStateOf<EstadoDasEmpresas>(EstadoDasEmpresas.Carregando) }
    var tentativa by remember { mutableIntStateOf(0) }
    var filtro by remember { mutableStateOf(FiltroDeEmpresas()) }
    var aberta by remember { mutableStateOf<EmpresaDoPainel?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var menuAberto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tentativa) {
        estado = EstadoDasEmpresas.Carregando
        estado = EmpresasDoPainelRepository.listar().fold(
            onSuccess = { EstadoDasEmpresas.Pronto(it) },
            onFailure = { EstadoDasEmpresas.Erro(it.message ?: "Não foi possível consultar as empresas.") }
        )
    }

    val itens = (estado as? EstadoDasEmpresas.Pronto)?.itens.orEmpty()
    val visiveis = itens.aplicar(filtro)
    val detalhe = aberta?.let { atual -> itens.firstOrNull { it.id == atual.id } ?: atual }

    Column(Modifier.fillMaxSize()) {
        if (detalhe != null) {
            DetalheDaEmpresa(detalhe, papel, aviso, { aviso = it }, { aberta = null; aviso = null })
        } else {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Empresas",
                        style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        when {
                            estado !is EstadoDasEmpresas.Pronto -> "Consultando empresas"
                            filtro.ativo -> "${visiveis.size} de ${itens.size}"
                            itens.size == 1 -> "1 empresa"
                            else -> "${itens.size} empresas"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
                IconButton(onClick = { tentativa++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Atualizar lista", tint = TextPrimary)
                }
            }
            if (aviso != null) {
                AvisoCurto(aviso.orEmpty(), { aviso = null }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
            when (val atual = estado) {
                EstadoDasEmpresas.Carregando -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CulturamaOrange)
                }
                is EstadoDasEmpresas.Erro -> Column(
                    Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvisoCurto(atual.mensagem)
                    OutlinedButton(onClick = { tentativa++ }) { Text("Tentar de novo") }
                }
                is EstadoDasEmpresas.Pronto -> LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 0.dp, 16.dp, 24.dp)
                ) {
                    item {
                        CulturamaTextField(
                            value = filtro.busca,
                            onValueChange = { filtro = filtro.copy(busca = it) },
                            label = "Buscar",
                            placeholder = "Nome, responsável ou e-mail"
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Situação",
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
                                )
                                if (filtro.ativo) {
                                    TextButton(onClick = { filtro = FiltroDeEmpresas() }) {
                                        Text("Limpar filtros", color = CulturamaOrange)
                                    }
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ChipDoPainel("Todas", filtro.situacao == null) { filtro = filtro.copy(situacao = null) }
                                SituacaoDaEmpresa.entries.filter { it != SituacaoDaEmpresa.OUTRA }.forEach { situacao ->
                                    ChipDoPainel(situacao.rotulo, filtro.situacao == situacao) {
                                        filtro = filtro.copy(situacao = if (filtro.situacao == situacao) null else situacao)
                                    }
                                }
                            }
                            Text(
                                "O cadastro não marca recusa automática. Rejeitado é o status que o servidor grava.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                            )
                        }
                    }
                    if (visiveis.isEmpty()) {
                        item {
                            AvisoCurto(
                                if (itens.isEmpty()) "Nenhuma empresa foi devolvida para esta sessão."
                                else "Nenhuma empresa corresponde à pesquisa e ao filtro."
                            )
                        }
                    } else {
                        items(visiveis, key = { it.id }) { empresa ->
                            CartaoDaEmpresa(
                                empresa = empresa,
                                papel = papel,
                                menuAberto = menuAberto == empresa.id,
                                onMenu = { menuAberto = if (menuAberto == empresa.id) null else empresa.id },
                                onFecharMenu = { menuAberto = null },
                                onAbrir = { aberta = empresa },
                                onAviso = { aviso = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoDaEmpresa(
    empresa: EmpresaDoPainel,
    papel: PapelNoPainel,
    menuAberto: Boolean,
    onMenu: () -> Unit,
    onFecharMenu: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CulturamaCard).clickable(onClick = onAbrir).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    empresa.razaoSocial,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(empresa.rotuloSituacao(), style = MaterialTheme.typography.bodyMedium.copy(color = CulturamaOrange))
                if (!empresa.responsavel.isNullOrBlank()) {
                    Text(empresa.responsavel, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!empresa.email.isNullOrBlank()) {
                    Text(empresa.email, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!empresa.cnpjMascarado.isNullOrBlank()) {
                    Text(empresa.cnpjMascarado, style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary))
                }
                Text(textoDeQuando(empresa.criadoEm), style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary))
            }
                            MenuDaEmpresa(papel, menuAberto, onMenu, onFecharMenu, onAbrir, onAviso)
        }
        if (!empresa.observacao.isNullOrBlank()) {
            Text(empresa.observacao, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MenuDaEmpresa(
    papel: PapelNoPainel,
    aberto: Boolean,
    onMenu: () -> Unit,
    onFechar: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit
) {
    Box {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Ações da empresa", tint = TextPrimary)
        }
        DropdownMenu(expanded = aberto, onDismissRequest = onFechar) {
            listOf(AcaoDaEmpresa.VISUALIZAR, AcaoDaEmpresa.APROVAR, AcaoDaEmpresa.RECUSAR).forEach { acao ->
                val decisao = autorizarEmpresa(papel, acao)
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(acao.rotulo())
                            if (!decisao.permitida) {
                                Text(
                                    "Em desenvolvimento",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                        }
                    },
                    enabled = decisao.permitida,
                    onClick = {
                        onFechar()
                        if (acao == AcaoDaEmpresa.VISUALIZAR && decisao.permitida) onAbrir()
                        else onAviso(decisao.motivo.ifBlank { ACAO_INDISPONIVEL })
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalheDaEmpresa(
    empresa: EmpresaDoPainel,
    papel: PapelNoPainel,
    aviso: String?,
    onAviso: (String) -> Unit,
    onVoltar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar à lista", tint = TextPrimary)
            }
            Text(
                "Detalhe",
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
            MenuDaEmpresa(papel, menu, { menu = true }, { menu = false }, {}, onAviso)
        }
        Text(empresa.razaoSocial, style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Campo("Situação cadastral", empresa.rotuloSituacao())
        if (!empresa.responsavel.isNullOrBlank()) Campo("Responsável", empresa.responsavel)
        if (!empresa.email.isNullOrBlank()) Campo("E-mail", empresa.email)
        if (!empresa.cnpjMascarado.isNullOrBlank()) Campo("CNPJ", empresa.cnpjMascarado)
        Campo("Cadastro", textoDeQuando(empresa.criadoEm))
        if (!empresa.observacao.isNullOrBlank()) Campo("Observações", empresa.observacao)
        if (aviso != null) AvisoCurto(aviso)
        Text(ACAO_INDISPONIVEL, style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary))
    }
}

@Composable
private fun Campo(rotulo: String, valor: String) {
    Column {
        Text(rotulo, style = MaterialTheme.typography.labelLarge.copy(color = TextTertiary))
        Text(valor, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary))
    }
}
