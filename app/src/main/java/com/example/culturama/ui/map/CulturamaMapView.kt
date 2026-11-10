package com.example.culturama.ui.map

import android.graphics.drawable.GradientDrawable
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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

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
            val marcador = Marker(mapa).apply {
                position = GeoPoint(evento.latitude!!, evento.longitude!!)
                title = evento.title
                snippet = evento.venueName
                icon = pin(
                    contexto,
                    destaque = evento.id == selectedEventId
                )
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
        modifier = modifier,
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

private fun pontoUsuario(): android.graphics.drawable.Drawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(0xFFFFFFFF.toInt())
        setSize(28, 28)
        setStroke(6, 0xFFE8835A.toInt())
    }
}

private fun pin(contexto: android.content.Context, destaque: Boolean): android.graphics.drawable.Drawable {
    val tamanho = if (destaque) 44 else 34
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(if (destaque) 0xFFE8835A.toInt() else 0xFF6E6E78.toInt())
        setSize(tamanho, tamanho)
        setStroke(if (destaque) 4 else 2, 0xFFFFFFFF.toInt())
    }
}
