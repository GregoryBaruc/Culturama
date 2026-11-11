package com.example.culturama.ui.map

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.culturama.data.map.Coordenada
import com.example.culturama.model.EventModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

private const val MARCADOR_USUARIO = "usuario"
private const val PREFIXO_EVENTO = "evt:"
private const val LIMITE_DENSO = 90
private const val CARGA_NORMAL = 42
private const val CARGA_DENSA = 18

private class PonteDoMapa {
    var onCamera: (GeoPoint, Double) -> Unit = { _, _ -> }
    var onEventSelected: (EventModel) -> Unit = {}
    var onToqueNoMapa: () -> Unit = {}
    var onAncora: (PosicaoDoMarcador?) -> Unit = {}
    var eventos: List<EventModel> = emptyList()
    var selecionadoId: String? = null
    var localUsuario: Coordenada? = null
    var ignorarToqueMapaAte: Long = 0
}

@Composable
fun CulturamaMapView(
    events: List<EventModel>,
    userLocation: Coordenada?,
    selectedEventId: String?,
    foco: Coordenada?,
    pedidoCentralizarUsuario: Int,
    centroInicial: GeoPoint,
    zoomInicial: Double,
    onCamera: (GeoPoint, Double) -> Unit,
    onEventSelected: (EventModel) -> Unit,
    onToqueNoMapa: () -> Unit,
    onAncora: (PosicaoDoMarcador?) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val ponte = remember { PonteDoMapa() }
    ponte.onCamera = onCamera
    ponte.onEventSelected = onEventSelected
    ponte.onToqueNoMapa = onToqueNoMapa
    ponte.onAncora = onAncora
    ponte.eventos = events
    ponte.selecionadoId = selectedEventId
    ponte.localUsuario = userLocation

    val cache = remember { FilaDeMiniaturas(contexto) }
    val assinaturas = remember { HashMap<String, String>() }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val density = contexto.resources.displayMetrics.density
    val mapa = remember {
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 5.0
            maxZoomLevel = 19.0
            controller.setZoom(zoomInicial)
            controller.setCenter(centroInicial)
            isTilesScaledToDpi = true
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            overlays.add(
                MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        if (android.os.SystemClock.uptimeMillis() < ponte.ignorarToqueMapaAte) return true
                        ponte.onToqueNoMapa()
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                })
            )
        }
    }
    val nucleo = remember(mapa) {
        MarcadoresNoMapa(mapa, ponte, cache, assinaturas, handler, density)
    }

    DisposableEffect(Unit) {
        val ouvinte = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                ponte.onCamera(GeoPoint(mapa.mapCenter), mapa.zoomLevelDouble)
                nucleo.publicarAncora()
                nucleo.agendarCarga()
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                ponte.onCamera(GeoPoint(mapa.mapCenter), mapa.zoomLevelDouble)
                nucleo.publicarAncora()
                nucleo.agendarCarga()
                return false
            }
        }
        val layout = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            nucleo.publicarAncora()
            nucleo.agendarCarga()
        }
        mapa.addMapListener(ouvinte)
        mapa.addOnLayoutChangeListener(layout)
        mapa.onResume()
        onDispose {
            handler.removeCallbacksAndMessages(null)
            cache.encerrar()
            ponte.onAncora = {}
            ponte.onToqueNoMapa = {}
            ponte.onCamera = { _, _ -> }
            ponte.onEventSelected = {}
            mapa.removeMapListener(ouvinte)
            mapa.removeOnLayoutChangeListener(layout)
            mapa.onPause()
            mapa.onDetach()
        }
    }

    LaunchedEffect(foco) {
        foco?.let {
            mapa.controller.animateTo(GeoPoint(it.latitude, it.longitude), 15.0, 500L)
        }
    }

    LaunchedEffect(pedidoCentralizarUsuario) {
        if (pedidoCentralizarUsuario > 0) {
            userLocation?.let {
                mapa.controller.animateTo(GeoPoint(it.latitude, it.longitude), 14.0, 450L)
            }
        }
    }

    LaunchedEffect(events, selectedEventId) {
        nucleo.sincronizar()
    }

    LaunchedEffect(userLocation) {
        nucleo.atualizarUsuario()
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier.fillMaxSize(),
        update = { view -> view.invalidate() }
    )
}

private class MarcadoresNoMapa(
    private val mapa: MapView,
    private val ponte: PonteDoMapa,
    private val cache: FilaDeMiniaturas,
    private val assinaturas: MutableMap<String, String>,
    private val handler: Handler,
    private val density: Float
) {
    private var tokenCarga = 0
    private var aguardandoLayout = false

    fun sincronizar() {
        val eventos = ponte.eventos.filter { it.temCoordenadas }
        val ids = eventos.map { it.id }.toSet()
        val denso = eventos.size > LIMITE_DENSO
        val atuais = HashMap<String, Marker>()
        mapa.overlays.filterIsInstance<Marker>().forEach { marker ->
            val bruto = marker.relatedObject as? String ?: return@forEach
            if (!bruto.startsWith(PREFIXO_EVENTO)) return@forEach
            val id = bruto.removePrefix(PREFIXO_EVENTO)
            if (id !in ids) {
                mapa.overlays.remove(marker)
                assinaturas.remove(id)
            } else {
                atuais[id] = marker
            }
        }
        eventos.forEach { evento ->
            val selecionado = evento.id == ponte.selecionadoId
            val url = evento.imageUrl?.takeIf { it.isNotBlank() }
            val foto = url?.let { cache.pronta(it) }
            val compacto = denso && !selecionado
            val assinatura = assinaturaDe(evento, selecionado, compacto, foto != null)
            val existente = atuais[evento.id]
            if (existente == null) {
                val novo = Marker(mapa).apply {
                    relatedObject = PREFIXO_EVENTO + evento.id
                    position = GeoPoint(evento.latitude!!, evento.longitude!!)
                    title = evento.title
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    infoWindow = null
                    icon = iconeMarcador(density, evento.category, selecionado, compacto, foto)
                    setOnMarkerClickListener { _, _ ->
                        ponte.ignorarToqueMapaAte = android.os.SystemClock.uptimeMillis() + 350
                        ponte.eventos.find { it.id == evento.id }?.let(ponte.onEventSelected)
                        true
                    }
                }
                mapa.overlays.add(novo)
                assinaturas[evento.id] = assinatura
            } else {
                existente.position = GeoPoint(evento.latitude!!, evento.longitude!!)
                existente.title = evento.title
                if (assinaturas[evento.id] != assinatura) {
                    existente.icon = iconeMarcador(density, evento.category, selecionado, compacto, foto)
                    assinaturas[evento.id] = assinatura
                }
            }
        }
        val selId = ponte.selecionadoId
        if (selId != null) {
            val sel = mapa.overlays.filterIsInstance<Marker>()
                .find { it.relatedObject == PREFIXO_EVENTO + selId }
            if (sel != null) {
                mapa.overlays.remove(sel)
                mapa.overlays.add(sel)
            }
        }
        desenharUsuario()
        mapa.invalidate()
        publicarAncora()
        agendarCarga()
    }

    fun atualizarUsuario() {
        desenharUsuario()
        mapa.invalidate()
    }

    fun aplicarFoto(eventoId: String, url: String, foto: Bitmap) {
        if (foto.isRecycled) return
        val evento = ponte.eventos.find { it.id == eventoId } ?: return
        if (evento.imageUrl != url) return
        val marker = mapa.overlays.filterIsInstance<Marker>()
            .find { it.relatedObject == PREFIXO_EVENTO + eventoId } ?: return
        val selecionado = eventoId == ponte.selecionadoId
        val denso = ponte.eventos.count { it.temCoordenadas } > LIMITE_DENSO
        val compacto = denso && !selecionado
        val assinatura = assinaturaDe(evento, selecionado, compacto, temFoto = true)
        if (assinaturas[eventoId] == assinatura) return
        marker.icon = iconeMarcador(density, evento.category, selecionado, compacto, foto)
        assinaturas[eventoId] = assinatura
        mapa.invalidate()
    }

    fun publicarAncora() {
        if (mapa.width == 0 || mapa.height == 0) {
            if (!aguardandoLayout) {
                aguardandoLayout = true
                mapa.post {
                    aguardandoLayout = false
                    if (mapa.width > 0 && mapa.height > 0) publicarAncora()
                }
            }
            return
        }
        val evento = ponte.selecionadoId?.let { id ->
            ponte.eventos.find { it.id == id && it.temCoordenadas }
        }
        if (evento == null) {
            ponte.onAncora(null)
            return
        }
        val ponto = Point()
        mapa.projection.toPixels(GeoPoint(evento.latitude!!, evento.longitude!!), ponto)
        ponte.onAncora(
            PosicaoDoMarcador(
                x = ponto.x.toFloat(),
                y = ponto.y.toFloat(),
                raioPx = raioDoMarcadorSelecionado(density)
            )
        )
    }

    fun agendarCarga() {
        val token = ++tokenCarga
        handler.postDelayed({
            if (token != tokenCarga) return@postDelayed
            carregarVisiveis()
        }, 220L)
    }

    private fun carregarVisiveis() {
        if (!mapa.isShown || mapa.width == 0) return
        val eventos = ponte.eventos.filter { it.temCoordenadas && !it.imageUrl.isNullOrBlank() }
        if (eventos.isEmpty()) return
        val box = runCatching { mapa.boundingBox }.getOrNull() ?: return
        val centro = mapa.mapCenter
        val visiveis = eventos.filter { box.contains(it.latitude!!, it.longitude!!) }
        val limite = if (visiveis.size > LIMITE_DENSO) CARGA_DENSA else CARGA_NORMAL
        val ordenados = visiveis.sortedBy { evento ->
            val dLat = evento.latitude!! - centro.latitude
            val dLon = evento.longitude!! - centro.longitude
            dLat * dLat + dLon * dLon
        }
        val escolhidos = ordenados.take(limite).toMutableList()
        val selecionado = ponte.selecionadoId?.let { id -> eventos.find { it.id == id } }
        if (selecionado != null && escolhidos.none { it.id == selecionado.id }) {
            escolhidos.add(0, selecionado)
        }
        cache.manterSomente(escolhidos.mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) }.toSet())
        escolhidos.forEach { evento ->
            val url = evento.imageUrl?.takeIf(String::isNotBlank) ?: return@forEach
            cache.pedir(evento.id, url) { bitmap ->
                if (bitmap != null) {
                    mapa.post {
                        if (mapa.isAttachedToWindow) aplicarFoto(evento.id, url, bitmap)
                    }
                }
            }
        }
    }

    private fun desenharUsuario() {
        marcarUsuario(mapa, ponte.localUsuario)
    }
}

private fun assinaturaDe(
    evento: EventModel,
    selecionado: Boolean,
    compacto: Boolean,
    temFoto: Boolean
): String = "${evento.category}|${evento.imageUrl}|$selecionado|$compacto|$temFoto"

private fun marcarUsuario(mapa: MapView, local: Coordenada?) {
    val existente = mapa.overlays.filterIsInstance<Marker>().find { it.relatedObject == MARCADOR_USUARIO }
    if (local == null) {
        if (existente != null) mapa.overlays.remove(existente)
        return
    }
    val ponto = GeoPoint(local.latitude, local.longitude)
    val marker = existente ?: Marker(mapa).apply {
        relatedObject = MARCADOR_USUARIO
        title = "Você"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        icon = pontoUsuario()
        setOnMarkerClickListener { _, _ -> true }
    }
    marker.position = ponto
    if (existente != null) mapa.overlays.remove(marker)
    mapa.overlays.add(marker)
}

private fun pontoUsuario(): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFFFFFFFF.toInt())
        setSize(28, 28)
        setStroke(6, 0xFFE8835A.toInt())
    }
}
