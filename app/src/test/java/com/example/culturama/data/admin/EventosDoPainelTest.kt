package com.example.culturama.data.admin

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventosDoPainelTest {

    private val gson = Gson()

    @Test
    fun linhaPreservaIdStatusEFonte() {
        val json = """
            {
              "id": "abc",
              "titulo": "Roda",
              "status": "em_revisao",
              "fonte": "mapas_culturais",
              "endereco": "Praça",
              "data_hora": "2026-11-10T12:00:00.000Z",
              "destaque": true,
              "categoria": {"nome": "Samba"},
              "organizer": {"user": {"nome": "Ana"}}
            }
        """.trimIndent()
        val evento = gson.fromJson(json, LinhaDeEvento::class.java).paraEvento()
        assertEquals("abc", evento?.id)
        assertEquals(StatusDoEvento.EM_REVISAO, evento?.status)
        assertEquals(FonteDoEvento.MAPAS_CULTURAIS, evento?.fonte)
        assertEquals("Samba", evento?.categoria)
        assertEquals("Ana", evento?.organizador)
        assertTrue(evento?.destaque == true)
        assertEquals("10 nov 2026 · 9h", evento?.instante?.let { textoDaData(it) })
    }

    @Test
    fun statusEFonteDesconhecidosNaoSaoTrocados() {
        val json = """{"id":"abc","titulo":"Roda","status":"suspenso","fonte":"sympla"}"""
        val evento = gson.fromJson(json, LinhaDeEvento::class.java).paraEvento()!!
        assertEquals(StatusDoEvento.OUTRO, evento.status)
        assertEquals("suspenso", evento.rotuloStatus())
        assertEquals("sympla", evento.rotuloFonte())
    }

    @Test
    fun linhaSemIdNaoViraEvento() {
        val json = """{"titulo":"Roda","status":"publicado","fonte":"interno"}"""
        assertNull(gson.fromJson(json, LinhaDeEvento::class.java).paraEvento())
    }

    @Test
    fun filtrosSeCombinam() {
        val samba = exemplo(id = "1", titulo = "Roda de samba", endereco = "Centro", organizador = "Ana")
        val teatro = exemplo(
            id = "2",
            titulo = "Peça",
            categoria = "Teatro",
            fonte = FonteDoEvento.TICKETMASTER,
            status = StatusDoEvento.RASCUNHO,
            instante = lerInstante("2026-12-01T15:00:00Z")
        )
        val lista = listOf(samba, teatro)
        val porNome = lista.aplicar(FiltroDeEventos(busca = "samba"))
        assertEquals(listOf("1"), porNome.map { it.id })

        val porOrganizador = lista.aplicar(FiltroDeEventos(busca = "ana"))
        assertEquals(listOf("1"), porOrganizador.map { it.id })

        val combinado = lista.aplicar(
            FiltroDeEventos(
                busca = "peça",
                status = StatusDoEvento.RASCUNHO,
                fonte = FonteDoEvento.TICKETMASTER,
                categoria = "Teatro",
                diaInicial = "2026-12-01",
                diaFinal = "2026-12-01"
            )
        )
        assertEquals(listOf("2"), combinado.map { it.id })

        val foraDoDia = lista.aplicar(FiltroDeEventos(diaFinal = "2026-11-09"))
        assertTrue(foraDoDia.isEmpty())
    }

    @Test
    fun buscaIgnoraAcento() {
        val lista = listOf(exemplo(titulo = "São Paulo"))
        assertEquals(1, lista.aplicar(FiltroDeEventos(busca = "sao paulo")).size)
    }

    @Test
    fun soAdminEModeradorConsultam() {
        assertTrue(podeConsultarEventos(PapelNoPainel.ADMINISTRADOR))
        assertTrue(podeConsultarEventos(PapelNoPainel.MODERADOR))
        assertFalse(podeConsultarEventos(PapelNoPainel.NENHUM))
    }

    @Test
    fun visualizarELiberadaEEscritaNao() {
        val interno = exemplo(fonte = FonteDoEvento.INTERNO, status = StatusDoEvento.RASCUNHO)
        val externo = exemplo(fonte = FonteDoEvento.TICKETMASTER, status = StatusDoEvento.PUBLICADO)
        assertTrue(autorizar(PapelNoPainel.ADMINISTRADOR, interno, AcaoDoEvento.VISUALIZAR).permitida)
        assertTrue(autorizar(PapelNoPainel.MODERADOR, externo, AcaoDoEvento.VISUALIZAR).permitida)

        listOf(PapelNoPainel.ADMINISTRADOR, PapelNoPainel.MODERADOR).forEach { papel ->
            listOf(interno, externo).forEach { evento ->
                listOf(
                    AcaoDoEvento.EDITAR,
                    AcaoDoEvento.PUBLICAR,
                    AcaoDoEvento.DESPUBLICAR,
                    AcaoDoEvento.DESTACAR,
                    AcaoDoEvento.EXCLUIR
                ).forEach { acao ->
                    val decisao = autorizar(papel, evento, acao)
                    assertFalse(decisao.permitida)
                    assertEquals(ACAO_INDISPONIVEL, decisao.motivo)
                }
            }
        }
        assertFalse(autorizarCriacao(PapelNoPainel.ADMINISTRADOR).permitida)
        assertFalse(autorizar(PapelNoPainel.NENHUM, interno, AcaoDoEvento.VISUALIZAR).permitida)
        assertTrue(AcaoDoEvento.EXCLUIR.exigeConfirmacao())
        assertFalse(AcaoDoEvento.PUBLICAR.exigeConfirmacao())
    }

    private fun exemplo(
        id: String = "1",
        titulo: String = "Evento",
        categoria: String? = "Música",
        endereco: String? = "Rua 1",
        organizador: String? = null,
        fonte: FonteDoEvento = FonteDoEvento.INTERNO,
        status: StatusDoEvento = StatusDoEvento.PUBLICADO,
        instante: Long? = lerInstante("2026-11-10T12:00:00.000Z")
    ) = EventoDoPainel(
        id = id,
        titulo = titulo,
        categoria = categoria,
        endereco = endereco,
        organizador = organizador,
        instante = instante,
        fim = null,
        descricao = null,
        status = status,
        statusBruto = status.id,
        fonte = fonte,
        fonteBruta = fonte.id,
        destaque = false,
        gratuito = true,
        idExterno = null,
        imagemCapa = null
    )
}
