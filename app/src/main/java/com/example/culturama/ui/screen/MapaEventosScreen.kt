package com.example.culturama.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.culturama.data.map.Coordenada
import com.example.culturama.data.map.FiltroDoMapa
import com.example.culturama.data.map.LocalizacaoAtual
import com.example.culturama.data.map.abrirRota
import com.example.culturama.data.map.bairroOuCidade
import com.example.culturama.data.map.cabeNoRaio
import com.example.culturama.data.map.distanciaEmKm
import com.example.culturama.data.map.eventoCoordenada
import com.example.culturama.data.map.filtrarParaOMapa
import com.example.culturama.data.map.textoDistancia
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.example.culturama.model.EventModel
import com.example.culturama.ui.component.EventCover
import com.example.culturama.ui.map.CulturamaMapView
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.CulturamaSurface
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary
import java.util.Calendar
import java.util.TimeZone

private val raiosKm = listOf(null to "Qualquer distância", 5 to "Até 5 km", 10 to "Até 10 km", 25 to "Até 25 km", 50 to "Até 50 km")
private val filtrosRapidos = listOf("Todos", "Hoje", "Fim de semana", "Grátis")
private val categoriasDoMapa = listOf(
    "Música", "Samba", "Esportes", "Geek e Games", "Dança", "Artes Cênicas",
    "Arte e Exposição", "Feiras e Gastronomia", "Cultura Popular", "Oficinas", "Outros"
)
private val mesesMapa = listOf("JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")

data class MapaUiState(
    val userLocation: Coordenada? = null,
    val maxDistanceKm: Int? = null,
    val nearMeAtivo: Boolean = false,
    val avisoLocal: String? = null
)

@Composable
fun MapaEventosScreen(
    modifier: Modifier = Modifier,
    events: List<EventModel>,
    navController: NavController,
    onMostrarLista: () -> Unit
) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    var selecionado by remember { mutableStateOf<EventModel?>(null) }
    var foco by remember { mutableStateOf<Coordenada?>(null) }
    var proximosExpandido by remember { mutableStateOf(false) }
    var painelAberto by rememberSaveable { mutableStateOf(false) }
    var busca by rememberSaveable { mutableStateOf("") }
    var rapido by rememberSaveable { mutableStateOf("Todos") }
    var categoria by rememberSaveable { mutableStateOf<String?>(null) }
    var subcategoria by rememberSaveable { mutableStateOf<String?>(null) }
    var diaInicial by rememberSaveable { mutableStateOf<Int?>(null) }
    var diaFinal by rememberSaveable { mutableStateOf<Int?>(null) }
    var entrada by rememberSaveable { mutableStateOf("Todos") }
    var somenteAcessivel by rememberSaveable { mutableStateOf(false) }
    var raioKm by rememberSaveable { mutableStateOf<Int?>(null) }
    var usuarioLat by rememberSaveable { mutableStateOf("") }
    var usuarioLon by rememberSaveable { mutableStateOf("") }
    var cameraLat by rememberSaveable { mutableStateOf("-23.6620") }
    var cameraLon by rememberSaveable { mutableStateOf("-46.5322") }
    var cameraZoom by rememberSaveable { mutableStateOf("13.0") }
    var pedidoCentro by remember { mutableIntStateOf(0) }
    var buscandoLocal by remember { mutableStateOf(false) }
    var avisoLocal by remember { mutableStateOf<String?>(null) }
    val usuario = usuarioLat.toDoubleOrNull()?.let { lat ->
        usuarioLon.toDoubleOrNull()?.let { lon -> Coordenada(lat, lon) }
    }

    fun guardarLocal(ponto: Coordenada) {
        usuarioLat = ponto.latitude.toString()
        usuarioLon = ponto.longitude.toString()
        pedidoCentro += 1
        avisoLocal = null
    }

    fun lerLocal() {
        if (!LocalizacaoAtual.servicosLigados(contexto)) {
            avisoLocal = "A localização do aparelho está desativada. O mapa continua disponível."
            return
        }
        buscandoLocal = true
        avisoLocal = "Buscando sua localização…"
        escopo.launch {
            val aqui = LocalizacaoAtual.obter(contexto)
            buscandoLocal = false
            if (aqui == null) {
                avisoLocal = "Não foi possível obter a localização agora. O mapa continua disponível."
            } else {
                guardarLocal(aqui)
            }
        }
    }

    val pedidoPermissao = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        val concedida = resultado[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            resultado[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedida) lerLocal()
        else avisoLocal = "Sem permissão de localização. Você ainda pode navegar pelo mapa."
    }

    fun pertoDeMim() {
        val fina = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
        val aproximada = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fina == PackageManager.PERMISSION_GRANTED || aproximada == PackageManager.PERMISSION_GRANTED) {
            lerLocal()
        } else {
            pedidoPermissao.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun aumentarRaio() {
        raioKm = when (raioKm) {
            null, 50 -> null
            5 -> 10
            10 -> 25
            25 -> 50
            else -> null
        }
    }

    val filtro = FiltroDoMapa(
        busca = busca,
        rapido = rapido,
        categoria = categoria,
        subcategoria = subcategoria,
        diaInicial = diaInicial,
        diaFinal = diaFinal,
        entrada = entrada,
        somenteAcessivel = somenteAcessivel,
        raioKm = raioKm
    )
    val hoje = remember {
        Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"))
    }
    val hojeDia = hoje.get(Calendar.DAY_OF_MONTH)
    val hojeMes = mesesMapa[hoje.get(Calendar.MONTH)]

    val comMapa = remember(events, filtro, hojeDia, hojeMes, usuario) {
        filtrarParaOMapa(events, filtro, hojeDia, hojeMes).mapNotNull { evento ->
            val coord = eventoCoordenada(evento.latitude, evento.longitude) ?: return@mapNotNull null
            val distancia = usuario?.let { distanciaEmKm(it, coord) }
            if (!cabeNoRaio(distancia, if (usuario == null) null else filtro.raioKm)) return@mapNotNull null
            evento to distancia
        }.sortedBy { it.second ?: Double.MAX_VALUE }
    }

    LaunchedEffect(comMapa) {
        if (selecionado != null && comMapa.none { it.first.id == selecionado?.id }) {
            selecionado = null
        }
    }

    Box(modifier.fillMaxSize()) {
        CulturamaMapView(
            events = comMapa.map { it.first },
            userLocation = usuario,
            selectedEventId = selecionado?.id,
            foco = foco,
            pedidoCentralizarUsuario = pedidoCentro,
            centroInicial = GeoPoint(
                cameraLat.toDoubleOrNull() ?: -23.6620,
                cameraLon.toDoubleOrNull() ?: -46.5322
            ),
            zoomInicial = cameraZoom.toDoubleOrNull() ?: 13.0,
            onCamera = { centro, zoom ->
                cameraLat = centro.latitude.toString()
                cameraLon = centro.longitude.toString()
                cameraZoom = zoom.toString()
            },
            onEventSelected = { selecionado = it },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(CulturamaBackground.copy(alpha = 0.94f), Color.Transparent)
                    )
                )
        ) {
            BarraFiltrosDoMapa(
                busca = busca,
                onBusca = { busca = it },
                rapido = rapido,
                onRapido = { escolhido ->
                    rapido = if (escolhido == "Todos") "Todos" else escolhido
                },
                avancadoAtivo = filtro.avancadoAtivo,
                onMaisFiltros = { painelAberto = true },
                quantidade = comMapa.size,
                semResultado = filtro.temAlgum && comMapa.isEmpty(),
                onLimpar = {
                    busca = ""
                    rapido = "Todos"
                    categoria = null
                    subcategoria = null
                    diaInicial = null
                    diaFinal = null
                    entrada = "Todos"
                    somenteAcessivel = false
                    raioKm = null
                },
                onMostrarLista = onMostrarLista
            )
            avisoLocal?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        if (painelAberto) {
            PainelFiltrosDoMapa(
                filtro = filtro,
                subcategorias = events.map { it.category }.filter { it.isNotBlank() }.distinct().sorted(),
                onFechar = { painelAberto = false },
                temLocalizacao = usuario != null,
                onAplicar = { novo ->
                    categoria = novo.categoria
                    subcategoria = novo.subcategoria
                    diaInicial = novo.diaInicial
                    diaFinal = novo.diaFinal
                    entrada = novo.entrada
                    somenteAcessivel = novo.somenteAcessivel
                    raioKm = novo.raioKm
                    painelAberto = false
                    if (novo.raioKm != null && usuario == null) pertoDeMim()
                },
                onLimpar = {
                    categoria = null
                    subcategoria = null
                    diaInicial = null
                    diaFinal = null
                    entrada = "Todos"
                    somenteAcessivel = false
                    raioKm = null
                    painelAberto = false
                }
            )
        }

        FloatingActionButton(
            onClick = { pertoDeMim() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 188.dp)
                .navigationBarsPadding(),
            containerColor = CulturamaOrange,
            contentColor = CulturamaBackground,
            shape = CircleShape
        ) {
            if (buscandoLocal) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = CulturamaBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Filled.Place, contentDescription = "Perto de mim")
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .animateContentSize()
        ) {
            selecionado?.let { evento ->
                CardMarcador(
                    evento = evento,
                    distanciaKm = comMapa.firstOrNull { it.first.id == evento.id }?.second,
                    onFechar = { selecionado = null },
                    onVer = { navController.navigate("eventDetail?eventId=${evento.id}") },
                    onRota = {
                        eventoCoordenada(evento.latitude, evento.longitude)?.let { coord ->
                            abrirRota(contexto, coord.latitude, coord.longitude, evento.title)
                        }
                    }
                )
            }

            PainelProximos(
                expandido = proximosExpandido,
                onExpandir = { proximosExpandido = !proximosExpandido },
                itens = comMapa,
                temLocalizacao = usuario != null,
                podeAumentarRaio = usuario != null && raioKm != null && comMapa.isEmpty(),
                onAumentarRaio = { aumentarRaio() },
                onSelecionar = { evento ->
                    selecionado = evento
                    eventoCoordenada(evento.latitude, evento.longitude)?.let { foco = it }
                },
                onVerDetalhe = { evento ->
                    navController.navigate("eventDetail?eventId=${evento.id}")
                }
            )
        }
    }
}

@Composable
private fun CardMarcador(
    evento: EventModel,
    distanciaKm: Double?,
    onFechar: () -> Unit,
    onVer: () -> Unit,
    onRota: () -> Unit
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            if (!evento.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = evento.imageUrl,
                    contentDescription = evento.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                EventCover(
                    imageUrl = null,
                    label = evento.title,
                    emoji = eventsScreenEmoji(evento.category),
                    gradient = eventsScreenGradient(evento.category),
                    modifier = Modifier.size(72.dp)
                )
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(
                    evento.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${evento.category} · ${evento.dayOfMonth} ${evento.monthAbbr} · ${evento.time}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Text(
                    evento.venueName,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                distanciaKm?.let {
                    Text(
                        textoDistancia(it),
                        style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
                    )
                }
            }
            IconButton(onClick = onFechar, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = TextSecondary)
            }
        }
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onVer,
                colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Ver evento") }
            OutlinedButton(
                onClick = onRota,
                enabled = evento.temCoordenadas,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) { Text("Como chegar") }
        }
    }
}

@Composable
private fun PainelProximos(
    expandido: Boolean,
    onExpandir: () -> Unit,
    itens: List<Pair<EventModel, Double?>>,
    temLocalizacao: Boolean,
    podeAumentarRaio: Boolean,
    onAumentarRaio: () -> Unit,
    onSelecionar: (EventModel) -> Unit,
    onVerDetalhe: (EventModel) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(CulturamaCard)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TextTertiary.copy(alpha = 0.5f))
                .clickable(onClick = onExpandir)
        )
        Text(
            text = "Eventos perto de você",
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onExpandir),
            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
        )
        if (itens.isEmpty()) {
            Text(
                text = when {
                    podeAumentarRaio -> "Nenhum evento neste raio."
                    temLocalizacao -> "Nenhum evento perto de você com esses filtros."
                    else -> "Toque em Perto de mim para ver a distância dos eventos."
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
            if (podeAumentarRaio) {
                Text(
                    text = "Aumentar raio",
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAumentarRaio)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange, fontWeight = FontWeight.SemiBold)
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(if (expandido) 248.dp else 196.dp)
            ) {
                items(itens, key = { it.first.id }) { (evento, distancia) ->
                    Column(
                        Modifier
                            .width(168.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CulturamaBackground)
                            .clickable { onSelecionar(evento) }
                            .padding(10.dp)
                    ) {
                        EventCover(
                            imageUrl = evento.imageUrl,
                            label = evento.title,
                            emoji = eventsScreenEmoji(evento.category),
                            gradient = eventsScreenGradient(evento.category),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        )
                        Text(
                            evento.title,
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            evento.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                        Text(
                            "${evento.dayOfMonth} ${evento.monthAbbr} · ${evento.time}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1
                        )
                        Text(
                            bairroOuCidade(evento.venueName) ?: evento.venueName,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary),
                            maxLines = 1
                        )
                        distancia?.let {
                            Text(
                                textoDistancia(it),
                                style = MaterialTheme.typography.labelSmall.copy(color = CulturamaOrange)
                            )
                        }
                        Text(
                            "Ver detalhes",
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { onVerDetalhe(evento) },
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = CulturamaOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraFiltrosDoMapa(
    busca: String,
    onBusca: (String) -> Unit,
    rapido: String,
    onRapido: (String) -> Unit,
    avancadoAtivo: Boolean,
    onMaisFiltros: () -> Unit,
    quantidade: Int,
    semResultado: Boolean,
    onLimpar: () -> Unit,
    onMostrarLista: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CulturamaCard)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    if (busca.isEmpty()) {
                        Text(
                            "Nome, categoria ou local",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary),
                            maxLines = 1
                        )
                    }
                    BasicTextField(
                        value = busca,
                        onValueChange = onBusca,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                        cursorBrush = SolidColor(CulturamaOrange),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (busca.isNotEmpty()) {
                    IconButton(onClick = { onBusca("") }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Clear, contentDescription = "Limpar pesquisa", tint = TextTertiary)
                    }
                }
            }
            Text(
                text = "Lista",
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(CulturamaCard)
                    .clickable(onClick = onMostrarLista)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtrosRapidos.forEach { rotulo ->
                ChipDoMapa(rotulo, selecionado = rapido == rotulo, onClick = { onRapido(rotulo) })
            }
            ChipDoMapa("Mais filtros", selecionado = avancadoAtivo, onClick = onMaisFiltros)
        }
        if (semResultado) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Nenhum evento encontrado com esses filtros.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Limpar filtros",
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onLimpar)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange, fontWeight = FontWeight.SemiBold)
                )
            }
        } else {
            Text(
                if (quantidade == 1) "1 evento encontrado no mapa" else "$quantidade eventos encontrados no mapa",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun ChipDoMapa(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    Text(
        text = rotulo,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selecionado) CulturamaOrange else CulturamaCard.copy(alpha = 0.92f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            color = if (selecionado) CulturamaBackground else TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PainelFiltrosDoMapa(
    filtro: FiltroDoMapa,
    subcategorias: List<String>,
    temLocalizacao: Boolean,
    onFechar: () -> Unit,
    onAplicar: (FiltroDoMapa) -> Unit,
    onLimpar: () -> Unit
) {
    var categoria by remember(filtro) { mutableStateOf(filtro.categoria) }
    var subcategoria by remember(filtro) { mutableStateOf(filtro.subcategoria) }
    var diaInicial by remember(filtro) { mutableStateOf(filtro.diaInicial?.toString().orEmpty()) }
    var diaFinal by remember(filtro) { mutableStateOf(filtro.diaFinal?.toString().orEmpty()) }
    var entrada by remember(filtro) { mutableStateOf(filtro.entrada) }
    var acessivel by remember(filtro) { mutableStateOf(filtro.somenteAcessivel) }
    var raio by remember(filtro) { mutableStateOf(filtro.raioKm) }

    ModalBottomSheet(
        onDismissRequest = onFechar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CulturamaSurface,
        contentColor = TextPrimary
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Mais filtros",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
            TextoSecao("Categoria")
            LinhaDeOpcoes(categoriasDoMapa, categoria) { categoria = if (categoria == it) null else it }
            if (subcategorias.isNotEmpty()) {
                TextoSecao("Subcategoria")
                LinhaDeOpcoes(subcategorias, subcategoria) {
                    subcategoria = if (subcategoria == it) null else it
                }
            }
            TextoSecao("Data inicial e data final")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoDia("Inicial", diaInicial, { diaInicial = it }, Modifier.weight(1f))
                CampoDia("Final", diaFinal, { diaFinal = it }, Modifier.weight(1f))
            }
            TextoSecao("Distância")
            if (!temLocalizacao) {
                Text(
                    "O raio usa a sua localização. Se ela ainda não foi autorizada, vamos pedir ao aplicar.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                )
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                raiosKm.forEach { (km, rotulo) ->
                    ChipDoMapa(rotulo, selecionado = raio == km, onClick = { raio = km })
                }
            }
            TextoSecao("Entrada")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Gratuitos", "Pagos").forEach { opcao ->
                    ChipDoMapa(opcao, selecionado = entrada == opcao, onClick = { entrada = opcao })
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CulturamaCard)
                    .clickable { acessivel = !acessivel }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Somente acessíveis",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )
                Switch(
                    checked = acessivel,
                    onCheckedChange = { acessivel = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CulturamaOrange
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onLimpar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Limpar filtros") }
                Button(
                    onClick = {
                        onAplicar(
                            filtro.copy(
                                categoria = categoria,
                                subcategoria = subcategoria,
                                diaInicial = diaInicial.toIntOrNull()?.takeIf { it in 1..31 },
                                diaFinal = diaFinal.toIntOrNull()?.takeIf { it in 1..31 },
                                entrada = entrada,
                                somenteAcessivel = acessivel,
                                raioKm = raio
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
                ) { Text("Aplicar filtros") }
            }
        }
    }
}

@Composable
private fun TextoSecao(texto: String) {
    Text(texto, style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold))
}

@Composable
private fun LinhaDeOpcoes(opcoes: List<String>, selecionada: String?, onEscolher: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opcoes.forEach { opcao ->
            val ativa = opcao == selecionada
            Text(
                text = opcao,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (ativa) CulturamaOrange.copy(alpha = 0.2f) else CulturamaCard)
                    .border(1.dp, if (ativa) CulturamaOrange else Color(0xFF333338), RoundedCornerShape(12.dp))
                    .clickable { onEscolher(opcao) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (ativa) CulturamaOrange else TextSecondary,
                    fontWeight = if (ativa) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun CampoDia(rotulo: String, valor: String, onValor: (String) -> Unit, modifier: Modifier) {
    Box(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CulturamaCard)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (valor.isEmpty()) {
            Text(rotulo, style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, fontSize = 12.sp))
        }
        BasicTextField(
            value = valor,
            onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) onValor(it) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            cursorBrush = SolidColor(CulturamaOrange),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
