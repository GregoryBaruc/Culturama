package com.example.culturama.data.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalizacaoUtilTest {

    @Test
    fun distanciaEntrePontosConhecidos() {
        val centro = Coordenada(-23.5505, -46.6333)
        val perto = Coordenada(-23.5629, -46.6544)
        val km = distanciaEmKm(centro, perto)
        assertTrue(km in 2.0..4.0)
    }

    @Test
    fun textoDistanciaFormataMetrosEKm() {
        assertEquals("450 m", textoDistancia(0.45))
        assertTrue(textoDistancia(3.2).endsWith("km"))
        assertEquals("12 km", textoDistancia(12.4))
    }

    @Test
    fun bairroSaiDoEndereco() {
        val texto = "Teatro, Santa Terezinha, Santo André - SP"
        assertEquals("Santa Terezinha", bairroOuCidade(texto))
    }

    @Test
    fun raioUsaADistanciaGeodesica() {
        val centro = Coordenada(-23.5505, -46.6333)
        val perto = Coordenada(-23.5629, -46.6544)
        val km = distanciaEmKm(centro, perto)
        assertTrue(cabeNoRaio(km, 5))
        assertFalse(cabeNoRaio(km, 1))
        assertTrue(cabeNoRaio(km, null))
        assertFalse(cabeNoRaio(null, 10))
    }

    @Test
    fun coordenadaInvalidaNaoPassa() {
        assertNull(eventoCoordenada(null, -46.0))
        assertNull(eventoCoordenada(95.0, 10.0))
    }
}
