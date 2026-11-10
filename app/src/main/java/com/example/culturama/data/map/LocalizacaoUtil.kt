package com.example.culturama.data.map

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordenada(val latitude: Double, val longitude: Double)

private const val RAIO_TERRA_KM = 6371.0

/**
 * Distância geodésica entre dois pontos, no mesmo critério de
 * Location.distanceBetween. Não é tempo nem trajeto por ruas.
 */
fun distanciaEmKm(origem: Coordenada, destino: Coordenada): Double {
    val dLat = Math.toRadians(destino.latitude - origem.latitude)
    val dLon = Math.toRadians(destino.longitude - origem.longitude)
    val lat1 = Math.toRadians(origem.latitude)
    val lat2 = Math.toRadians(destino.latitude)
    val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
  return RAIO_TERRA_KM * 2 * atan2(sqrt(a), sqrt(1 - a))
}

fun textoDistancia(km: Double): String = when {
    km < 1 -> "${(km * 1000).toInt()} m"
    km < 10 -> String.format("%.1f km", km)
    else -> "${km.toInt()} km"
}

/** Tenta extrair bairro ou cidade a partir do endereço textual. */
fun bairroOuCidade(endereco: String): String? {
    val limpo = endereco.trim()
    if (limpo.isEmpty()) return null
  val partes = limpo.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (partes.size >= 2) {
        val penultima = partes[partes.lastIndex - 1]
        if (penultima.length in 2..48) return penultima
    }
    return partes.firstOrNull()?.takeIf { it.length in 2..48 }
}

/** `raioKm` nulo significa qualquer distância. Sem distância calculada, o raio não entra. */
fun cabeNoRaio(distanciaKm: Double?, raioKm: Int?): Boolean {
    if (raioKm == null) return true
    if (distanciaKm == null) return false
    return distanciaKm <= raioKm
}

fun eventoCoordenada(latitude: Double?, longitude: Double?): Coordenada? {
    if (latitude == null || longitude == null) return null
    if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
    return Coordenada(latitude, longitude)
}
