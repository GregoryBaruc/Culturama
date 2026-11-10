package com.example.culturama.ui.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.culturama.data.admin.ACAO_INDISPONIVEL
import com.example.culturama.data.admin.AcaoDoAnuncio
import com.example.culturama.data.admin.AnuncioDoPainel
import com.example.culturama.data.admin.AnunciosDoPainelRepository
import com.example.culturama.data.admin.FiltroDeAnuncios
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.ResumoDeInteracoes
import com.example.culturama.data.admin.SituacaoDoAnuncio
import com.example.culturama.data.admin.TipoDeAnuncio
import com.example.culturama.data.admin.acoesDoAnuncio
import com.example.culturama.data.admin.aplicar
import com.example.culturama.data.admin.autorizarAnuncio
import com.example.culturama.data.admin.rotulo
import com.example.culturama.data.admin.rotuloSituacao
import com.example.culturama.data.admin.rotuloTipo
import com.example.culturama.data.admin.textoDeQuando
import com.example.culturama.ui.component.CulturamaTextField
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

private sealed interface EstadoDosAnuncios {
    data object Carregando : EstadoDosAnuncios
    data class Pronto(val itens: List<AnuncioDoPainel>) : EstadoDosAnuncios
    data class Erro(val mensagem: String) : EstadoDosAnuncios
}

private sealed interface EstadoDoResumo {
    data object Carregando : EstadoDoResumo
    data class Pronto(val resumo: ResumoDeInteracoes) : EstadoDoResumo
    data class Erro(val mensagem: String) : EstadoDoResumo
}

@Composable
fun GerenciarAnunciosScreen(papel: PapelNoPainel) {
    val contexto = LocalContext.current
    var estado by remember { mutableStateOf<EstadoDosAnuncios>(EstadoDosAnuncios.Carregando) }
    var resumo by remember { mutableStateOf<EstadoDoResumo>(EstadoDoResumo.Carregando) }
    var tentativa by remember { mutableIntStateOf(0) }
    var filtro by remember { mutableStateOf(FiltroDeAnuncios()) }
    var aberto by remember { mutableStateOf<AnuncioDoPainel?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var menuAberto by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tentativa) {
        estado = EstadoDosAnuncios.Carregando
        resumo = EstadoDoResumo.Carregando
        estado = AnunciosDoPainelRepository.listar().fold(
            onSuccess = { EstadoDosAnuncios.Pronto(it) },
            onFailure = { EstadoDosAnuncios.Erro(it.message ?: "Não foi possível consultar os anúncios.") }
        )
        resumo = AnunciosDoPainelRepository.resumo().fold(
            onSuccess = { EstadoDoResumo.Pronto(it) },
            onFailure = { EstadoDoResumo.Erro(it.message ?: "Não foi possível consultar os indicadores.") }
        )
    }

    val itens = (estado as? EstadoDosAnuncios.Pronto)?.itens.orEmpty()
    val visiveis = itens.aplicar(filtro)
    val detalhe = aberto?.let { atual -> itens.firstOrNull { it.id == atual.id } ?: atual }

    Column(Modifier.fillMaxSize()) {
        if (detalhe != null) {
            DetalheDoAnuncio(
                anuncio = detalhe,
                papel = papel,
                aviso = aviso,
                onAviso = { aviso = it },
                onVoltar = { aberto = null; aviso = null },
                onAbrirLink = { link ->
                    contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }
            )
        } else {
            Cabecalho(
                total = if (estado is EstadoDosAnuncios.Pronto) itens.size else null,
                filtrados = if (filtro.ativo && estado is EstadoDosAnuncios.Pronto) visiveis.size else null,
                onNovo = { aviso = autorizarAnuncio(papel, null, AcaoDoAnuncio.CRIAR).motivo },
                onAtualizar = { tentativa++ }
            )
            if (aviso != null) {
                AvisoCurto(aviso.orEmpty(), { aviso = null }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
            when (val atual = estado) {
                EstadoDosAnuncios.Carregando -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CulturamaOrange)
                }
                is EstadoDosAnuncios.Erro -> Column(
                    Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvisoCurto(atual.mensagem)
                    OutlinedButton(onClick = { tentativa++ }) { Text("Tentar de novo") }
                }
                is EstadoDosAnuncios.Pronto -> LazyColumn(
                    Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 0.dp, 16.dp, 24.dp)
                ) {
                    item {
                        CulturamaTextField(
                            value = filtro.busca,
                            onValueChange = { filtro = filtro.copy(busca = it) },
                            label = "Buscar",
                            placeholder = "Tipo ou link"
                        )
                    }
                    item { FiltrosDeAnuncio(filtro, itens) { filtro = it } }
                    item { CartaoDeInsights(resumo) }
                    if (visiveis.isEmpty()) {
                        item {
                            AvisoCurto(
                                if (itens.isEmpty()) {
                                    "Nenhum anúncio foi devolvido para esta sessão."
                                } else {
                                    "Nenhum anúncio corresponde à pesquisa e aos filtros."
                                }
                            )
                        }
                    } else {
                        items(visiveis, key = { it.id }) { anuncio ->
                            CartaoDeAnuncio(
                                anuncio = anuncio,
                                papel = papel,
                                menuAberto = menuAberto == anuncio.id,
                                onMenu = { menuAberto = if (menuAberto == anuncio.id) null else anuncio.id },
                                onFecharMenu = { menuAberto = null },
                                onAbrir = { aberto = anuncio },
                                onAviso = { aviso = it },
                                onAbrirLink = { link ->
                                    contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Cabecalho(total: Int?, filtrados: Int?, onNovo: () -> Unit, onAtualizar: () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Anúncios",
                    style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    when {
                        total == null -> "Consultando anúncios"
                        filtrados != null -> "$filtrados de $total"
                        total == 1 -> "1 anúncio"
                        else -> "$total anúncios"
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
            colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange, contentColor = CulturamaBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Novo anúncio", Modifier.padding(start = 6.dp))
        }
    }
}

@Composable
private fun FiltrosDeAnuncio(
    filtro: FiltroDeAnuncios,
    itens: List<AnuncioDoPainel>,
    onFiltro: (FiltroDeAnuncios) -> Unit
) {
    val extras = itens.map { it.tipo }.filter { it == TipoDeAnuncio.OUTRO }.distinct()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Filtros", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary))
            if (filtro.ativo) {
                TextButton(onClick = { onFiltro(FiltroDeAnuncios()) }) { Text("Limpar filtros", color = CulturamaOrange) }
            }
        }
        LinhaDeChips {
            ChipDoPainel("Todos", filtro.situacao == SituacaoDoAnuncio.TODOS) {
                onFiltro(filtro.copy(situacao = SituacaoDoAnuncio.TODOS))
            }
            ChipDoPainel("Ativos", filtro.situacao == SituacaoDoAnuncio.ATIVOS) {
                onFiltro(filtro.copy(situacao = SituacaoDoAnuncio.ATIVOS))
            }
            ChipDoPainel("Inativos", filtro.situacao == SituacaoDoAnuncio.INATIVOS) {
                onFiltro(filtro.copy(situacao = SituacaoDoAnuncio.INATIVOS))
            }
        }
        LinhaDeChips {
            ChipDoPainel("Todos os tipos", filtro.tipo == null) { onFiltro(filtro.copy(tipo = null)) }
            TipoDeAnuncio.entries.filter { it != TipoDeAnuncio.OUTRO }.forEach { tipo ->
                ChipDoPainel(tipo.rotulo, filtro.tipo == tipo) {
                    onFiltro(filtro.copy(tipo = if (filtro.tipo == tipo) null else tipo))
                }
            }
            if (extras.isNotEmpty()) {
                ChipDoPainel("Outro tipo", filtro.tipo == TipoDeAnuncio.OUTRO) {
                    onFiltro(filtro.copy(tipo = if (filtro.tipo == TipoDeAnuncio.OUTRO) null else TipoDeAnuncio.OUTRO))
                }
            }
        }
    }
}

@Composable
private fun LinhaDeChips(conteudo: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) { conteudo() }
}

@Composable
private fun CartaoDeInsights(estado: EstadoDoResumo) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CulturamaCard).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Culturama Insights",
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
        )
        Text(
            "Interações de eventos. Nenhum dado de pessoa entra aqui.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
        )
        when (estado) {
            EstadoDoResumo.Carregando -> CircularProgressIndicator(color = CulturamaOrange, modifier = Modifier.height(28.dp))
            is EstadoDoResumo.Erro -> Text(estado.mensagem, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
            is EstadoDoResumo.Pronto -> {
                Metrica("Visualizações", estado.resumo.visualizacoes)
                Metrica("Favoritos", estado.resumo.favoritos)
                Metrica("Cliques em ingressos", estado.resumo.ingressos)
            }
        }
    }
}

@Composable
private fun Metrica(rotulo: String, valor: Int?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(rotulo, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        Text(
            valor?.toString() ?: "—",
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (valor == null) TextTertiary else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun CartaoDeAnuncio(
    anuncio: AnuncioDoPainel,
    papel: PapelNoPainel,
    menuAberto: Boolean,
    onMenu: () -> Unit,
    onFecharMenu: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit,
    onAbrirLink: (String) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CulturamaCard).clickable(onClick = onAbrir).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    anuncio.rotuloTipo(),
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(anuncio.rotuloSituacao(), style = MaterialTheme.typography.bodyMedium.copy(color = CulturamaOrange))
                Text(
                    "Início ${textoDeQuando(anuncio.inicio)} · Fim ${textoDeQuando(anuncio.fim)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                if (anuncio.cliques != null) {
                    Text("${anuncio.cliques} cliques", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }
            }
            MenuDoAnuncio(anuncio, papel, menuAberto, onMenu, onFecharMenu, onAbrir, onAviso, onAbrirLink)
        }
        if (!anuncio.imagem.isNullOrBlank()) {
            AsyncImage(
                model = anuncio.imagem,
                contentDescription = anuncio.rotuloTipo(),
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun MenuDoAnuncio(
    anuncio: AnuncioDoPainel,
    papel: PapelNoPainel,
    aberto: Boolean,
    onMenu: () -> Unit,
    onFechar: () -> Unit,
    onAbrir: () -> Unit,
    onAviso: (String) -> Unit,
    onAbrirLink: (String) -> Unit
) {
    Box {
        IconButton(onClick = onMenu) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Ações do anúncio", tint = TextPrimary)
        }
        DropdownMenu(expanded = aberto, onDismissRequest = onFechar) {
            acoesDoAnuncio(anuncio).forEach { acao ->
                val decisao = autorizarAnuncio(papel, anuncio, acao)
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(acao.rotulo(anuncio))
                            if (!decisao.permitida) {
                                Text(
                                    if (acao == AcaoDoAnuncio.CONSULTAR_LINK) "Sem link" else "Em desenvolvimento",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                        }
                    },
                    enabled = decisao.permitida,
                    onClick = {
                        onFechar()
                        when {
                            acao == AcaoDoAnuncio.VISUALIZAR -> onAbrir()
                            acao == AcaoDoAnuncio.CONSULTAR_LINK && decisao.permitida -> onAbrirLink(anuncio.link.orEmpty())
                            else -> onAviso(decisao.motivo.ifBlank { ACAO_INDISPONIVEL })
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalheDoAnuncio(
    anuncio: AnuncioDoPainel,
    papel: PapelNoPainel,
    aviso: String?,
    onAviso: (String) -> Unit,
    onVoltar: () -> Unit,
    onAbrirLink: (String) -> Unit
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
            MenuDoAnuncio(anuncio, papel, menu, { menu = true }, { menu = false }, {}, onAviso, onAbrirLink)
        }
        if (!anuncio.imagem.isNullOrBlank()) {
            AsyncImage(
                model = anuncio.imagem,
                contentDescription = anuncio.rotuloTipo(),
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Text(anuncio.rotuloTipo(), style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
        Texto("Status", anuncio.rotuloSituacao())
        Texto("Início", textoDeQuando(anuncio.inicio))
        Texto("Término", textoDeQuando(anuncio.fim))
        if (anuncio.cliques != null) Texto("Cliques", anuncio.cliques.toString())
        if (!anuncio.link.isNullOrBlank()) Texto("Link", anuncio.link)
        val link = autorizarAnuncio(papel, anuncio, AcaoDoAnuncio.CONSULTAR_LINK)
        if (link.permitida) {
            OutlinedButton(onClick = { onAbrirLink(anuncio.link.orEmpty()) }) { Text("Consultar campanha") }
        }
        if (aviso != null) AvisoCurto(aviso)
        Text(ACAO_INDISPONIVEL, style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary))
    }
}

@Composable
private fun Texto(rotulo: String, valor: String) {
    Column {
        Text(rotulo, style = MaterialTheme.typography.labelLarge.copy(color = TextTertiary))
        Text(valor, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary))
    }
}
