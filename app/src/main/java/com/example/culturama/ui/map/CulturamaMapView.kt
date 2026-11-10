package com.example.culturama.ui.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
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
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val MARCADOR_USUARIO = "usuario"

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
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val mapa = remember {
        MapView(contexto).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 5.0
            maxZoomLevel = 19.0
            controller.setZoom(zoomInicial)
            controller.setCenter(centroInicial)
            isTilesScaledToDpi = true
            // Esconde os botões +/- nativos (faixa preta na lateral).
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        }
    }

    DisposableEffect(Unit) {
        val ouvinte = object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                onCamera(GeoPoint(mapa.mapCenter), mapa.zoomLevelDouble)
                return false
            }
            override fun onZoom(event: ZoomEvent?): Boolean {
                onCamera(GeoPoint(mapa.mapCenter), mapa.zoomLevelDouble)
                return false
            }
        }
        mapa.addMapListener(ouvinte)
        mapa.onResume()
        onDispose {
            mapa.removeMapListener(ouvinte)
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

    LaunchedEffect(events, selectedEventId, userLocation) {
        val comCoordenada = events.filter { it.temCoordenadas }
        mapa.overlays.removeAll { it is Marker && it.relatedObject != MARCADOR_USUARIO }
        comCoordenada.forEach { evento ->
            val destaque = evento.id == selectedEventId
            val marcador = Marker(mapa).apply {
                position = GeoPoint(evento.latitude!!, evento.longitude!!)
                title = evento.title
                snippet = evento.venueName
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = pinCategoria(contexto.resources.displayMetrics.density, evento.category, destaque)
                setOnMarkerClickListener { _, _ ->
                    onEventSelected(evento)
                    true
                }
            }
            mapa.overlays.add(marcador)
        }
        marcarUsuario(mapa, userLocation)
        mapa.invalidate()
    }

    AndroidView(
        factory = { mapa },
        modifier = modifier.fillMaxSize(),
        update = { view -> view.invalidate() }
    )
}

private fun marcarUsuario(mapa: MapView, local: Coordenada?) {
    val existente = mapa.overlays.filterIsInstance<Marker>().find { it.relatedObject == MARCADOR_USUARIO }
    if (local == null) {
        if (existente != null) mapa.overlays.remove(existente)
        return
    }
    val ponto = GeoPoint(local.latitude, local.longitude)
    if (existente == null) {
        mapa.overlays.add(
            Marker(mapa).apply {
                relatedObject = MARCADOR_USUARIO
                position = ponto
                title = "Você"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = pontoUsuario()
                setOnMarkerClickListener { _, _ -> true }
            }
        )
    } else {
        existente.position = ponto
    }
}

private fun pontoUsuario(): Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFFFFFFFF.toInt())
        setSize(28, 28)
        setStroke(6, 0xFFE8835A.toInt())
    }
}

/** Cores dos pins por categoria real do app (referência visual do mockup). */
internal fun corCategoriaMapa(categoria: String): Int {
    val c = categoria.lowercase()
    return when {
        "música" in c || "musica" in c || "samba" in c || "hip" in c || "rap" in c || "sarau" in c -> 0xFFE8835A.toInt()
        "geek" in c || "game" in c || "jog" in c -> 0xFF9B6BFF.toInt()
        "feira" in c || "gastro" in c || "comida" in c -> 0xFFF0C040.toInt()
        "arte" in c || "teatro" in c || "cênic" in c || "cenic" in c || "cinema" in c -> 0xFF4C8DFF.toInt()
        "grafite" in c || "oficina" in c || "exposição" in c || "exposicao" in c -> 0xFFFF5CA8.toInt()
        "esporte" in c || "skate" in c || "capoeira" in c || "dança" in c || "danca" in c -> 0xFF3DCC7A.toInt()
        "cultura" in c || "popular" in c -> 0xFFE85A5A.toInt()
        else -> 0xFFE8835A.toInt()
    }
}

private fun simboloCategoria(categoria: String): String {
    val c = categoria.lowercase()
    return when {
        "música" in c || "musica" in c || "samba" in c || "hip" in c || "rap" in c || "sarau" in c -> "♪"
        "geek" in c || "game" in c -> "G"
        "feira" in c || "gastro" in c -> "F"
        "arte" in c || "teatro" in c || "cênic" in c || "cenic" in c || "cinema" in c -> "A"
        "grafite" in c || "oficina" in c || "exposição" in c || "exposicao" in c -> "✦"
        "esporte" in c || "skate" in c || "capoeira" in c -> "E"
        "dança" in c || "danca" in c -> "D"
        "cultura" in c || "popular" in c -> "C"
        else -> "★"
    }
}

private fun pinCategoria(density: Float, categoria: String, destaque: Boolean): Drawable {
    val escala = if (destaque) 1.22f else 1f
    val w = (36f * density * escala).toInt().coerceAtLeast(36)
    val h = (46f * density * escala).toInt().coerceAtLeast(46)
    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val cor = corCategoriaMapa(categoria)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cor; style = Paint.Style.FILL }
    val sombra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x66000000
        style = Paint.Style.FILL
    }
    val cx = w / 2f
    val cy = h * 0.38f
    val raio = w * 0.32f
    // sombra suave
    canvas.drawOval(RectF(cx - raio * 0.7f, h - raio * 0.55f, cx + raio * 0.7f, h.toFloat()), sombra)
    // gota
    val path = Path().apply {
        moveTo(cx, h - density * 2f)
        quadTo(cx + raio * 1.15f, cy + raio * 0.55f, cx + raio, cy)
        arcTo(RectF(cx - raio, cy - raio, cx + raio, cy + raio), 0f, -180f, false)
        quadTo(cx - raio * 1.15f, cy + raio * 0.55f, cx, h - density * 2f)
        close()
    }
    canvas.drawPath(path, paint)
    if (destaque) {
        val borda = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
        canvas.drawPath(path, borda)
    }
    val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textAlign = Paint.Align.CENTER
        textSize = raio * 0.95f
        typeface = Typeface.DEFAULT_BOLD
    }
    val simbolo = simboloCategoria(categoria)
    val textY = cy - (texto.descent() + texto.ascent()) / 2f
    canvas.drawText(simbolo, cx, textY, texto)
    return BitmapDrawable(null, bitmap).apply {
        setBounds(0, 0, w, h)
    }
}
