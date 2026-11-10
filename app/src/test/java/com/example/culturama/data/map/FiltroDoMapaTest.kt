package com.example.culturama.data.map

import com.example.culturama.model.EventModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltroDoMapaTest {

    private val musicaGratis = evento("1", "Roda", "Música", gratis = true, dia = 10, semana = "Sáb")
    private val musicaPaga = evento("2", "Show", "Música", gratis = false, dia = 11, semana = "Dom")
    private val teatro = evento("3", "Peça", "Artes Cênicas", gratis = true, dia = 10, semana = "Ter")
    private val semCoordenada = evento("4", "Feira", "Música", gratis = true, dia = 10, semana = "Sáb")
        .copy(latitude = null, longitude = null)

    private val todos = listOf(musicaGratis, musicaPaga, teatro, semCoordenada)

    @Test
    fun semFiltroMostraSomenteQuemTemCoordenada() {
        val ids = filtrarParaOMapa(todos, FiltroDoMapa(), hojeDia = 10, hojeMes = "NOV").map { it.id }
        assertEquals(listOf("1", "2", "3"), ids)
    }

    @Test
    fun categoriaMusica() {
        val filtro = FiltroDoMapa(categoria = "Música")
        val ids = filtrarParaOMapa(todos, filtro, 10, "NOV").map { it.id }
        assertEquals(listOf("1", "2"), ids)
    }

    @Test
    fun gratisEMusicaSaoAIntersecao() {
        val filtro = FiltroDoMapa(rapido = "Grátis", categoria = "Música")
        val ids = filtrarParaOMapa(todos, filtro, 10, "NOV").map { it.id }
        assertEquals(listOf("1"), ids)
    }

    @Test
    fun hojeUsaDiaEMes() {
        val filtro = FiltroDoMapa(rapido = "Hoje")
        val ids = filtrarParaOMapa(todos, filtro, hojeDia = 10, hojeMes = "NOV").map { it.id }
        assertEquals(listOf("1", "3"), ids)
        assertTrue(filtrarParaOMapa(todos, filtro, hojeDia = 10, hojeMes = "DEZ").isEmpty())
    }

    @Test
    fun todosLimpaSoOFiltroRapido() {
        val aplicado = FiltroDoMapa(rapido = "Grátis", categoria = "Música", busca = "Roda")
        val limpo = aplicado.semFiltroRapido()
        assertEquals("Todos", limpo.rapido)
        assertEquals("Música", limpo.categoria)
        assertEquals("Roda", limpo.busca)
    }

    @Test
    fun buscaPorLocal() {
        val filtro = FiltroDoMapa(busca = "largo")
        val ids = filtrarParaOMapa(todos, filtro, 10, "NOV").map { it.id }
        assertEquals(listOf("3"), ids)
    }

    private fun evento(
        id: String,
        titulo: String,
        categoria: String,
        gratis: Boolean,
        dia: Int,
        semana: String
    ) = EventModel(
        id = id,
        title = titulo,
        category = categoria,
        dayOfMonth = dia,
        monthAbbr = "NOV",
        dayOfWeek = semana,
        time = "20:00",
        venueName = if (id == "3") "Teatro do Largo, Centro" else "Casa, Santo André",
        isFree = gratis,
        latitude = -23.6,
        longitude = -46.5
    )
}
