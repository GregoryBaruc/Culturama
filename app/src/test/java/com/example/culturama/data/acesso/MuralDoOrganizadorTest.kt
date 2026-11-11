package com.example.culturama.data.acesso

import com.example.culturama.data.admin.lerInstante
import org.junit.Assert.assertEquals
import org.junit.Test

class MuralDoOrganizadorTest {

    @Test
    fun contagemUsaSomenteOsStatusReais() {
        val eventos = listOf(
            EventoDoMural("1", "Show", "publicado", null),
            EventoDoMural("2", "Roda", "publicado", null),
            EventoDoMural("3", "Batalha", "em_revisao", null),
            EventoDoMural("4", "Oficina", "rascunho", null),
            EventoDoMural("5", "Feira", "cancelado", null),
            EventoDoMural("6", "Sarau", null, null)
        )
        assertEquals(ContagemDoMural(2, 1, 1), eventos.contagem())
    }

    @Test
    fun rotulosNaoInventamStatus() {
        assertEquals("Publicado", rotuloDoStatus("publicado"))
        assertEquals("Em análise", rotuloDoStatus("em_revisao"))
        assertEquals("Rascunho", rotuloDoStatus("rascunho"))
        assertEquals("Sem status", rotuloDoStatus(null))
        assertEquals("agendado", rotuloDoStatus("agendado"))
    }

    @Test
    fun filtroFicaNosEventosDaListaENoNome() {
        val agora = lerInstante("2024-06-01T12:00:00Z")!!
        val eventos = listOf(
            EventoDoMural("1", "Roda de samba", "publicado", null, dataFim = "2024-07-01T12:00:00Z"),
            EventoDoMural("2", "Oficina", "rascunho", null),
            EventoDoMural("3", "Batalha", "em_revisao", null, dataFim = "2020-01-01T00:00:00Z")
        )
        assertEquals(listOf("1"), eventos.filtrar(FiltroDosEventos.PUBLICADOS, "", agora).map { it.id })
        assertEquals(listOf("2"), eventos.filtrar(FiltroDosEventos.RASCUNHOS, "ofi", agora).map { it.id })
        assertEquals(listOf("3"), eventos.filtrar(FiltroDosEventos.EM_ANALISE, "", agora).map { it.id })
        assertEquals(listOf("3"), eventos.filtrar(FiltroDosEventos.ENCERRADOS, "", agora).map { it.id })
        assertEquals(listOf("1"), eventos.filtrar(FiltroDosEventos.TODOS, "samba", agora).map { it.id })
    }
}
