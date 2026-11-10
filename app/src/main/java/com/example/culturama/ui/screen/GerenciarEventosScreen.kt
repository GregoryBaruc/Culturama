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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.culturama.data.admin.ACAO_INDISPONIVEL
import com.example.culturama.data.admin.AcaoDoEvento
import com.example.culturama.data.admin.EventoDoPainel
import com.example.culturama.data.admin.EventosDoPainelRepository
import com.example.culturama.data.admin.FiltroDeEventos
import com.example.culturama.data.admin.FonteDoEvento
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.StatusDoEvento
import com.example.culturama.data.admin.acoesDoMenu
import com.example.culturama.data.admin.aplicar
import com.example.culturama.data.admin.autorizar
import com.example.culturama.data.admin.autorizarCriacao
import com.example.culturama.data.admin.diaDoSeletor
import com.example.culturama.data.admin.rotulo
import com.example.culturama.data.admin.rotuloFonte
import com.example.culturama.data.admin.rotuloStatus
import com.example.culturama.data.admin.textoDaData
import com.example.culturama.data.admin.textoDoDia
import com.example.culturama.ui.component.CulturamaTextField
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

private sealed interface EstadoDaLista {
    data object Carregando : EstadoDaLista
    data class Pronto(val eventos: List<EventoDoPainel>) : EstadoDaLista
    data class Erro(val mensagem: String) : EstadoDaLista
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarEventosScreen(
    navController: NavController,
    papel: PapelNoPainel
) {
    var estado by remember { mutableStateOf<EstadoDaLista>(EstadoDaLista.Carregando) }
    var tentativa by remember { mutableIntStateOf(0) }
    var filtro by remember { mutableStateOf(FiltroDeEventos()) }
    var aberto by remember { mutableStateOf<EventoDoPainel?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var menuAberto by remember { mutableStateOf<String?>(null) }
    var seletor by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(tentativa) {
        estado = EstadoDaLista.Carregando
        estado = EventosDoPainelRepository.listar().fold(
            onSuccess = { EstadoDaLista.Pronto(it) },
            onFailure = { EstadoDaLista.Erro(it.message ?: "Não foi possível consultar os eventos.") }
        )
    }

    val eventos = (estado as? EstadoDaLista.Pronto)?.eventos.orEmpty()
    val visiveis = eventos.aplicar(filtro)
    val emDetalhe = aberto?.let { atual -> eventos.firstOrNull { it.id == atual.id } ?: atual }

    Column(modifier = Modifier.fillMaxSize()) {
        if (emDetalhe != null) {
            DetalheDoEvento(
                evento = emDetalhe,
                papel = papel,
                aviso = aviso,
                onAviso = { aviso = it },
                onVoltar = {
                    aviso = null
                    aberto = null
                },
                onAbrirPublico = {
                    navController.navigate("eventDetail?eventId=${emDetalhe.id}")
                }
            )
        } else {
            CabecalhoDaLista(
                total = if (estado is EstadoDaLista.Pronto) eventos.size else null,
                filtrados = if (filtro.ativo && estado is EstadoDaLista.Pronto) visiveis.size else null,
                onNovo = { aviso = autorizarCriacao(papel).motivo },
                onAtualizar = { tentativa++ }
            )
            if (aviso != null) {
                Aviso(
                    texto = aviso.orEmpty(),
                    onFechar = { aviso = null },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            when (val atual = estado) {
                EstadoDaLista.Carregando -> Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CulturamaOrange)
                }

                is EstadoDaLista.Erro -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Aviso(atual.mensagem)
                    OutlinedButton(onClick = { tentativa++ }) { Text("Tentar de novo") }
                }

                is EstadoDaLista.Pronto -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, bottom = 24.dp
                    )
                ) {
                    item {
                        CulturamaTextField(
                            value = filtro.busca,
                            onValueChange = { filtro = filtro.copy(busca = it) },
                            label = "Buscar",
                            placeholder = "Nome, endereço ou organizador"
                        )
                    }
                    item {
                        Filtros(
                            filtro = filtro,
                            eventos = eventos,
                            onFiltro = { filtro = it },
                            onEscolherDia = { seletor = it }
                        )
                    }
                    if (visiveis.isEmpty()) {
                        item {
                            Aviso(
                                if (eventos.isEmpty()) {
                                    "Nenhum evento foi devolvido para esta sessão. " +
                                        "A lista pública não entra aqui, porque ela não traz rascunho, revisão nem fonte."
                                } else {
                                    "Nenhum evento corresponde à pesquisa e aos filtros."
                                }
                            )
                        }
                    } else {
                        items(visiveis, key = { it.id }) { evento ->
                            CartaoDoEvento(
                                evento = evento,
                                papel = papel,
                                menuAberto = menuAberto == evento.id,
                                onMenu = { menuAberto = if (menuAberto == evento.id) null else evento.id },
                                onFecharMenu = { menuAberto = null },
                                onAbrir = { aberto = evento },
                                onAviso = { aviso = it }
                            )
                        }
                    }
                }
            }
        }
    }

    val escolhendoInicio = seletor
    if (escolhendoInicio != null) {
        val estadoData = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { seletor = null },
            confirmButton = {
                TextButton(onClick = {
                    estadoData.selectedDateMillis?.let { millis ->
                        val dia = diaDoSeletor(millis)
                        filtro = if (escolhendoInicio) filtro.copy(diaInicial = dia) else filtro.copy(diaFinal = dia)
                    }
                    seletor = null
                }) { Text("Ok") }
            },
            dismissButton = {
                TextButton(onClick = { seletor = null }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoData)
        }
    }
}

@Composable
private fun CabecalhoDaLista(
    total: Int?,
    filtrados: Int?,
    onNovo: () -> Unit,
    onAtualizar: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Gerenciar eventos",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = when {
                        total == null -> "Consultando eventos"
                        filtrados != null -> "$filtrados de $total ${if (total == 1) "evento" else "eventos"}"
                        total == 1 -> "1 evento"
                        else -> "$total eventos"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
            IconButton(onClick = onAtualizar) {
                Icon(Icons.Filled.Refresh, contentDescription = "Atualizar lista", tint = TextPrimary)
            }
        }
        Button(
            onClick = onNovo,
            colors = ButtonDefaults.buttonColors(
                containerColor = CulturamaOrange,
                contentColor = CulturamaBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Novo evento", modifier = Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun Filtros(
    filtro: FiltroDeEventos,
    eventos: List<EventoDoPainel>,
    onFiltro: (FiltroDeEventos) -> Unit,
    onEscolherDia: (Boolean) -> Unit
) {
    val statusExtras = eventos.map { it.status }.filter { it == StatusDoEvento.OUTRO }.distinct()
    val fontesExtras = eventos.map { it.fonte }.filter { it == FonteDoEvento.DESCONHECIDA }.distinct()
    val categorias = eventos.mapNotNull { it.categoria }.distinct().sorted()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Filtros",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
            )
            if (filtro.ativo) {
                TextButton(onClick = { onFiltro(FiltroDeEventos()) }) {
                    Text("Limpar filtros", color = CulturamaOrange)
                }
            }
        }
        GrupoDeChips("Status") {
            Chip("Todos", filtro.status == null) { onFiltro(filtro.copy(status = null)) }
            StatusDoEvento.entries.filter { it != StatusDoEvento.OUTRO }.forEach { status ->
                Chip(status.rotulo, filtro.status == status) {
                    onFiltro(filtro.copy(status = if (filtro.status == status) null else status))
                }
            }
            if (statusExtras.isNotEmpty()) {
                Chip("Outro", filtro.status == StatusDoEvento.OUTRO) {
                    onFiltro(filtro.copy(status = if (filtro.status == StatusDoEvento.OUTRO) null else StatusDoEvento.OUTRO))
                }
            }
        }
        GrupoDeChips("Fonte") {
            Chip("Todas", filtro.fonte == null) { onFiltro(filtro.copy(fonte = null)) }
            FonteDoEvento.entries.filter { it != FonteDoEvento.DESCONHECIDA }.forEach { fonte ->
                Chip(fonte.rotulo, filtro.fonte == fonte) {
                    onFiltro(filtro.copy(fonte = if (filtro.fonte == fonte) null else fonte))
                }
            }
            if (fontesExtras.isNotEmpty()) {
                Chip("Outra fonte", filtro.fonte == FonteDoEvento.DESCONHECIDA) {
                    onFiltro(
                        filtro.copy(
                            fonte = if (filtro.fonte == FonteDoEvento.DESCONHECIDA) null else FonteDoEvento.DESCONHECIDA
                        )
                    )
                }
            }
        }
        if (categorias.isNotEmpty()) {
            GrupoDeChips("Categoria") {
                Chip("Todas", filtro.categoria == null) { onFiltro(filtro.copy(categoria = null)) }
                categorias.forEach { nome ->
                    Chip(nome, filtro.categoria == nome) {
                        onFiltro(filtro.copy(categoria = if (filtro.categoria == nome) null else nome))
                    }
                }
            }
        }
        Text(
            text = "Intervalo de datas",
            style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEscolherDia(true) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = CulturamaOrange)
                Text(
                    "De ${textoDoDia(filtro.diaInicial)}",
                    modifier = Modifier.padding(start = 6.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(onClick = { onEscolherDia(false) }, modifier = Modifier.weight(1f)) {
                Text(
                    "Até ${textoDoDia(filtro.diaFinal)}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun GrupoDeChips(titulo: String, conteudo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = { conteudo() }
        )
    }
}

@Composable
private fun Chip(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selecionado) CulturamaOrange else CulturamaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            color = if (selecionado) CulturamaBackground else TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun CartaoDoEvento(
    evento: EventoDoPainel,
    papel: PapelNoPainel,
    menuAberto: Boolean,
    onMenu: () -> Unit,
    onFecharMenu: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .clickable(onClick = onAbrir)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = evento.categoria ?: "Sem categoria",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CulturamaOrange),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = textoDaData(evento.instante),
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                Text(
                    text = "${evento.rotuloFonte()} · ${evento.rotuloStatus()}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                if (evento.destaque) {
                    Text(
                        text = "Destaque",
                        style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
                    )
                }
                if (!evento.endereco.isNullOrBlank()) {
                    Text(
                        text = evento.endereco,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            MenuDeAcoes(
                evento = evento,
                papel = papel,
                aberto = menuAberto,
                onMenu = onMenu,
                onFechar = onFecharMenu,
                onAbrir = onAbrir,
                onAviso = onAviso
            )
        }
    }
}

@Composable
private fun MenuDeAcoes(
    evento: EventoDoPainel,
    papel: PapelNoPainel,
    aberto: Boolean,
    onMenu: () -> Unit,
    onFechar: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit
) {
    Box {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Ações do evento", tint = TextPrimary)
        }
        DropdownMenu(expanded = aberto, onDismissRequest = onFechar) {
            acoesDoMenu(evento).forEach { acao ->
                val decisao = autorizar(papel, evento, acao)
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(acao.rotulo(evento))
                            if (!decisao.permitida) {
                                Text(
                                    text = "Em desenvolvimento",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                        }
                    },
                    enabled = decisao.permitida,
                    onClick = {
                        onFechar()
                        if (acao == AcaoDoEvento.VISUALIZAR && decisao.permitida) {
                            onAbrir()
                        } else {
                            onAviso(decisao.motivo.ifBlank { ACAO_INDISPONIVEL })
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalheDoEvento(
    evento: EventoDoPainel,
    papel: PapelNoPainel,
    aviso: String?,
    onAviso: (String) -> Unit,
    onVoltar: () -> Unit,
    onAbrirPublico: () -> Unit
) {
    var menuAberto by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVoltar) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar à lista",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Detalhe",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            MenuDeAcoes(
                evento = evento,
                papel = papel,
                aberto = menuAberto,
                onMenu = { menuAberto = true },
                onFechar = { menuAberto = false },
                onAbrir = {},
                onAviso = onAviso
            )
        }
        if (!evento.imagemCapa.isNullOrBlank()) {
            AsyncImage(
                model = evento.imagemCapa,
                contentDescription = evento.titulo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Text(
            text = evento.titulo,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Linha("Categoria", evento.categoria ?: "Sem categoria")
        Linha("Data", textoDaData(evento.instante))
        if (evento.fim != null) Linha("Término", textoDaData(evento.fim))
        Linha("Fonte", evento.rotuloFonte())
        Linha("Status", evento.rotuloStatus())
        Linha("Destaque", if (evento.destaque) "Sim" else "Não")
        if (evento.gratuito != null) Linha("Entrada", if (evento.gratuito) "Gratuita" else "Paga")
        if (!evento.endereco.isNullOrBlank()) Linha("Local", evento.endereco)
        if (!evento.organizador.isNullOrBlank()) Linha("Organizador", evento.organizador)
        if (!evento.idExterno.isNullOrBlank()) Linha("Id na origem", evento.idExterno)
        if (!evento.descricao.isNullOrBlank()) {
            Text(
                text = evento.descricao,
                style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
            )
        }
        if (evento.status == StatusDoEvento.PUBLICADO) {
            OutlinedButton(onClick = onAbrirPublico) {
                Text("Abrir na área pública")
            }
        }
        if (aviso != null) Aviso(aviso)
        Text(
            text = ACAO_INDISPONIVEL,
            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
        )
    }
}

@Composable
private fun Linha(rotulo: String, valor: String) {
    Column {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge.copy(color = TextTertiary)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
        )
    }
}

@Composable
private fun Aviso(
    texto: String,
    onFechar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        if (onFechar != null) {
            TextButton(onClick = onFechar) { Text("Fechar", color = CulturamaOrange) }
        }
    }
}
