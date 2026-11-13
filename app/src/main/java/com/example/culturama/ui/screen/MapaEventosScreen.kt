package com.example.culturama.ui.screen

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Place
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
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
import com.example.culturama.model.EventModel
import com.example.culturama.ui.component.CulturamaLogo
import com.example.culturama.ui.component.EventCover
import com.example.culturama.ui.map.CulturamaMapView
import com.example.culturama.ui.map.corCategoriaMapa
import com.example.culturama.ui.map.corDoSimbolo
import com.example.culturama.ui.map.letraDoMarcador
import kotlin.math.roundToInt
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.CulturamaSurface
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.Calendar
import java.util.TimeZone

private val raiosKm = listOf(
    null to "Qualquer distância",
    5 to "Até 5 km",
    10 to "Até 10 km",
    25 to "Até 25 km",
    50 to "Até 50 km"
)
private val filtrosRapidos = listOf("Todos", "Hoje", "Fim de semana", "Grátis")
private val categoriasDoMapa = listOf(
    "Música", "Samba", "Esportes", "Geek e Games", "Dança", "Artes Cênicas",
    "Arte e Exposição", "Feiras e Gastronomia", "Cultura Popular", "Oficinas", "Outros"
)
private val mesesMapa = listOf(
    "JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"
)

private class AncoraDoMarcador {
    var ponto by mutableStateOf<Offset?>(null)
    var raioPx by mutableFloatStateOf(36f)
}

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
    val ancora = remember { AncoraDoMarcador() }
    var alturaCabecalho by remember { mutableIntStateOf(0) }
    val densidade = LocalDensity.current
    val view = LocalView.current
    // Compose e a janela podem divergir no primeiro frame. O maior dos dois cobre a faixa real.
    var plataformaTopo by remember { mutableIntStateOf(0) }
    var plataformaFundo by remember { mutableIntStateOf(0) }
    DisposableEffect(view) {
        val janela = (view.context as? Activity)?.window
        val controle = janela?.let { WindowCompat.getInsetsController(it, view) }
        val statusAntes = controle?.isAppearanceLightStatusBars
        val navAntes = controle?.isAppearanceLightNavigationBars
        controle?.isAppearanceLightStatusBars = false
        controle?.isAppearanceLightNavigationBars = false

        fun lerInsets() {
            val raiz = ViewCompat.getRootWindowInsets(view) ?: return
            plataformaTopo = raiz.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout()
            ).top
            plataformaFundo = raiz.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout()
            ).bottom
        }

        val aoLayout = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> lerInsets() }
        view.addOnLayoutChangeListener(aoLayout)
        view.post { lerInsets() }
        onDispose {
            view.removeOnLayoutChangeListener(aoLayout)
            if (statusAntes != null) controle.isAppearanceLightStatusBars = statusAntes
            if (navAntes != null) controle.isAppearanceLightNavigationBars = navAntes
        }
    }
    val topoPx = maxOf(
        WindowInsets.statusBars.union(WindowInsets.displayCutout).getTop(densidade),
        plataformaTopo
    )
    val fundoPx = maxOf(
        WindowInsets.navigationBars.union(WindowInsets.displayCutout).getBottom(densidade),
        plataformaFundo
    )
    val limiteTopoPx = if (alturaCabecalho > 0) {
        alturaCabecalho + with(densidade) { 8.dp.roundToPx() }
    } else {
        topoPx + with(densidade) { 218.dp.roundToPx() }
    }
    val usuario = usuarioLat.toDoubleOrNull()?.let { lat ->
        usuarioLon.toDoubleOrNull()?.let { lon -> Coordenada(lat, lon) }
    }

    val logado by SupabaseAuthService.instance.isLoggedIn.collectAsState(initial = false)
    val favoritosRepo = remember { RemoteFavoriteRepository.instance }
    val tickFavoritos by favoritosRepo.alteracoes.collectAsState()
    val userId = remember(logado, tickFavoritos) { idDoUsuarioAutenticado() }

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
            foco = null
            ancora.ponto = null
        }
    }

    fun selecionarEvento(evento: EventModel) {
        selecionado = evento
        eventoCoordenada(evento.latitude, evento.longitude)?.let { foco = it }
    }

    Box(modifier.fillMaxSize().background(CulturamaBackground)) {
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
            onEventSelected = { selecionarEvento(it) },
            onToqueNoMapa = {
                if (selecionado != null) {
                    selecionado = null
                    foco = null
                    ancora.ponto = null
                }
            },
            onAncora = { posicao ->
                ancora.ponto = posicao?.let { Offset(it.x, it.y) }
                if (posicao != null) ancora.raioPx = posicao.raioPx
            },
            modifier = Modifier.fillMaxSize()
        )

        // O mapa preenche a janela. O fundo do cabeçalho começa em y=0 e só o conteúdo
        // recua pelo inset, senão os tiles aparecem atrás da barra de status.
        Column(Modifier.fillMaxSize().zIndex(1f)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { alturaCabecalho = it.height }
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(with(densidade) { topoPx.toDp() })
                        .background(CulturamaBackground)
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0f to CulturamaBackground,
                                0.42f to CulturamaBackground.copy(alpha = 0.97f),
                                0.72f to CulturamaBackground.copy(alpha = 0.78f),
                                1f to Color.Transparent
                            )
                        )
                ) {
                    CabecalhoDoMapa(
                        onMostrarLista = onMostrarLista,
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
                        }
                    )
                    avisoLocal?.let {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        bottom = if (selecionado != null) with(densidade) { fundoPx.toDp() } else 0.dp
                    )
            ) {
                Text(
                    text = "© OpenStreetMap contributors",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 10.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary.copy(alpha = 0.8f),
                        fontSize = 9.sp
                    )
                )
                FloatingActionButton(
                    onClick = { pertoDeMim() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp),
                    containerColor = CulturamaCard,
                    contentColor = CulturamaOrange,
                    shape = CircleShape
                ) {
                    if (buscandoLocal) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = CulturamaOrange,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Place, contentDescription = "Perto de mim")
                    }
                }
            }

            AnimatedVisibility(
                visible = selecionado == null,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + slideOutVertically { it / 4 }
            ) {
                PainelProximos(
                    itens = comMapa,
                    temLocalizacao = usuario != null,
                    podeAumentarRaio = usuario != null && raioKm != null && comMapa.isEmpty(),
                    reservaInferior = with(densidade) { fundoPx.toDp() },
                    onAumentarRaio = { aumentarRaio() },
                    onSelecionar = { selecionarEvento(it) }
                )
            }
        }

        selecionado?.let { eventoSel ->
            CardFlutuanteDoMapa(
                evento = eventoSel,
                ancora = ancora,
                limiteTopoPx = limiteTopoPx,
                limiteFundoPx = fundoPx,
                distanciaKm = comMapa.firstOrNull { it.first.id == eventoSel.id }?.second,
                favorito = userId?.let { favoritosRepo.ids(it).contains(eventoSel.id) } == true,
                podeFavoritar = userId != null,
                onFavorito = {
                    val uid = userId ?: return@CardFlutuanteDoMapa
                    val atual = favoritosRepo.ids(uid).contains(eventoSel.id)
                    favoritosRepo.setFavorite(uid, eventoSel.id, !atual)
                },
                onFechar = {
                    selecionado = null
                    foco = null
                    ancora.ponto = null
                },
                onVer = { navController.navigate("eventDetail?eventId=${eventoSel.id}") },
                onRota = {
                    eventoCoordenada(eventoSel.latitude, eventoSel.longitude)?.let { coord ->
                        abrirRota(contexto, coord.latitude, coord.longitude, eventoSel.title)
                    }
                }
            )
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
    }
}

@Composable
private fun CabecalhoDoMapa(
    onMostrarLista: () -> Unit,
    busca: String,
    onBusca: (String) -> Unit,
    rapido: String,
    onRapido: (String) -> Unit,
    avancadoAtivo: Boolean,
    onMaisFiltros: () -> Unit,
    quantidade: Int,
    semResultado: Boolean,
    onLimpar: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CulturamaLogo(height = 28.dp)
            Text(
                text = "Lista",
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CulturamaCard.copy(alpha = 0.92f))
                    .clickable(onClick = onMostrarLista)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CulturamaCard.copy(alpha = 0.94f))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (busca.isEmpty()) {
                        Text(
                            "Buscar eventos, culturas ou bairros...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextTertiary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
            IconButton(
                onClick = onMaisFiltros,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(
                        1.5.dp,
                        if (avancadoAtivo) CulturamaOrange else CulturamaOrange.copy(alpha = 0.55f),
                        RoundedCornerShape(14.dp)
                    )
                    .background(CulturamaCard.copy(alpha = 0.94f))
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Mais filtros",
                    tint = if (avancadoAtivo) CulturamaOrange else TextSecondary
                )
            }
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            Text(
                if (quantidade == 1) "1 evento no mapa" else "$quantidade eventos no mapa",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}

@Composable
private fun CardFlutuanteDoMapa(
    evento: EventModel,
    ancora: AncoraDoMarcador,
    limiteTopoPx: Int,
    limiteFundoPx: Int,
    distanciaKm: Double?,
    favorito: Boolean,
    podeFavoritar: Boolean,
    onFavorito: () -> Unit,
    onFechar: () -> Unit,
    onVer: () -> Unit,
    onRota: () -> Unit
) {
    val ponto = ancora.ponto ?: return
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val margemPx = with(density) { 12.dp.roundToPx() }
        val disponivel = maxWidth - 24.dp
        val largura = when {
            disponivel <= 0.dp -> 220.dp
            disponivel < 300.dp -> disponivel
            else -> 300.dp
        }
        val larguraPx = with(density) { largura.roundToPx() }
        val basePx = constraints.maxHeight - limiteFundoPx - margemPx
        var alturaPx by remember(evento.id) { mutableIntStateOf(0) }
        val folga = ancora.raioPx + with(density) { 8.dp.toPx() }
        val acima = alturaPx == 0 || ponto.y - alturaPx - folga >= limiteTopoPx
        val foraDaTela = ponto.x < -72f || ponto.y < -72f ||
            ponto.x > constraints.maxWidth + 72f || ponto.y > constraints.maxHeight + 72f
        if (foraDaTela) return@BoxWithConstraints

        val maxX = (constraints.maxWidth - larguraPx - margemPx).coerceAtLeast(margemPx)
        val x = (ponto.x - larguraPx / 2f).roundToInt().coerceIn(margemPx, maxX)
        val y = if (alturaPx == 0) {
            -8000
        } else {
            val bruto = if (acima) {
                (ponto.y - alturaPx - folga).roundToInt()
            } else {
                (ponto.y + folga).roundToInt()
            }
            val maxY = (basePx - alturaPx).coerceAtLeast(limiteTopoPx)
            bruto.coerceIn(limiteTopoPx, maxY)
        }
        val limiteSeta = (larguraPx / 2f - with(density) { 18.dp.toPx() }).coerceAtLeast(0f)
        val deslocamento = (ponto.x - (x + larguraPx / 2f)).coerceIn(-limiteSeta, limiteSeta)

        Column(
            Modifier
                .offset { IntOffset(x, y) }
                .width(largura)
                .onSizeChanged { alturaPx = it.height }
        ) {
            if (!acima) SetaDoCard(deslocamento, paraCima = true)
            ConteudoDoCardFlutuante(
                evento = evento,
                distanciaKm = distanciaKm,
                favorito = favorito,
                podeFavoritar = podeFavoritar,
                onFavorito = onFavorito,
                onFechar = onFechar,
                onVer = onVer,
                onRota = onRota
            )
            if (acima) SetaDoCard(deslocamento, paraCima = false)
        }
    }
}

@Composable
private fun SetaDoCard(deslocamentoPx: Float, paraCima: Boolean) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(9.dp)
            .offset(y = if (paraCima) 1.dp else (-1).dp)
    ) {
        val ponta = (size.width / 2f + deslocamentoPx).coerceIn(16.dp.toPx(), size.width - 16.dp.toPx())
        val base = 8.dp.toPx()
        val caminho = Path().apply {
            if (paraCima) {
                moveTo(ponta, 0f)
                lineTo(ponta - base, size.height)
                lineTo(ponta + base, size.height)
            } else {
                moveTo(ponta - base, 0f)
                lineTo(ponta + base, 0f)
                lineTo(ponta, size.height)
            }
            close()
        }
        drawPath(caminho, CulturamaCard)
    }
}

@Composable
private fun ConteudoDoCardFlutuante(
    evento: EventModel,
    distanciaKm: Double?,
    favorito: Boolean,
    podeFavoritar: Boolean,
    onFavorito: () -> Unit,
    onFechar: () -> Unit,
    onVer: () -> Unit,
    onRota: () -> Unit
) {
    val local = bairroOuCidade(evento.venueName) ?: evento.venueName.takeIf { it.isNotBlank() }
    val entrada = when {
        evento.isFree -> "Gratuito"
        evento.price.isNotBlank() -> evento.price
        else -> "Pago"
    }
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .border(1.5.dp, CulturamaOrange.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .clickable(onClick = onVer)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(corCategoriaMapa(evento.category))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    letraDoMarcador(evento.category),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color(corDoSimbolo(evento.category)),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            if (!evento.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = evento.imageUrl,
                    contentDescription = evento.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )
            IconButton(
                onClick = onFechar,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Fechar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (podeFavoritar) {
                IconButton(
                    onClick = onFavorito,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (favorito) "Remover dos favoritos" else "Favoritar",
                        tint = if (favorito) CulturamaOrange else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                evento.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    evento.category.ifBlank { "Cultura" },
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(999.dp))
                        .border(1.dp, CulturamaOrange.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    entrada,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (evento.isFree) Color(0xFF1A3A28) else CulturamaOrange.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (evento.isFree) Color(0xFF8FE0B0) else CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                Text(
                    "${evento.dayOfWeek.take(3)}, ${evento.dayOfMonth} de ${evento.monthAbbr.lowercase()} · ${evento.time}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (local != null || distanciaKm != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (local != null) {
                        Icon(Icons.Outlined.Place, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                        Text(
                            local,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    distanciaKm?.let {
                        Text(
                            textoDistancia(it),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CulturamaBackground)
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = CulturamaOrange)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onVer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CulturamaOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("Ver evento", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onRota,
                    enabled = evento.temCoordenadas,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, CulturamaOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CulturamaOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text("Como chegar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun PainelProximos(
    itens: List<Pair<EventModel, Double?>>,
    temLocalizacao: Boolean,
    podeAumentarRaio: Boolean,
    reservaInferior: Dp,
    onAumentarRaio: () -> Unit,
    onSelecionar: (EventModel) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(CulturamaCard)
            .padding(top = 8.dp, bottom = 14.dp + reservaInferior)
    ) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TextTertiary.copy(alpha = 0.5f))
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Outlined.Place, null, tint = CulturamaOrange, modifier = Modifier.size(18.dp))
                Text(
                    text = if (temLocalizacao) "Eventos perto de você" else "Explore eventos",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
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
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itens, key = { it.first.id }) { (evento, distancia) ->
                    Column(
                        Modifier
                            .width(148.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CulturamaBackground)
                            .clickable { onSelecionar(evento) }
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(96.dp)
                        ) {
                            EventCover(
                                imageUrl = evento.imageUrl,
                                label = evento.title,
                                emoji = eventsScreenEmoji(evento.category),
                                gradient = eventsScreenGradient(evento.category),
                                modifier = Modifier.fillMaxSize()
                            )
                            distancia?.let {
                                Text(
                                    textoDistancia(it),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color.Black.copy(alpha = 0.62f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
                                )
                            }
                        }
                        Column(Modifier.padding(10.dp)) {
                            Text(
                                evento.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                evento.category,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
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
            color = if (selecionado) Color.White else TextSecondary,
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
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
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
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge.copy(
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    )
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
