package com.example.culturama.data.admin

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnunciosEEmpresasTest {

    private val gson = Gson()

    @Test
    fun anuncioGuardaIdENaoInventaTitulo() {
        val json = """
            {"id":"slot-1","tipo":"banner_home","ativo":true,"data_inicio":"2026-11-01","data_fim":"2026-11-30","cliques":4,"link":"https://culturama.example/campanha","imagem":"https://cdn.example/a.jpg"}
        """.trimIndent()
        val anuncio = gson.fromJson(json, LinhaDeAnuncio::class.java).paraAnuncio()!!
        assertEquals("slot-1", anuncio.id)
        assertEquals(TipoDeAnuncio.BANNER_HOME, anuncio.tipo)
        assertEquals("Banner da home", anuncio.rotuloTipo())
        assertEquals("Ativo", anuncio.rotuloSituacao())
        assertEquals(4, anuncio.cliques)
        assertEquals("01/11/2026", textoDeQuando(anuncio.inicio))
    }

    @Test
    fun tipoDesconhecidoContinuaVisivel() {
        val json = """{"id":"slot-2","tipo":"banner_mapa","ativo":false}"""
        val anuncio = gson.fromJson(json, LinhaDeAnuncio::class.java).paraAnuncio()!!
        assertEquals(TipoDeAnuncio.OUTRO, anuncio.tipo)
        assertEquals("banner_mapa", anuncio.rotuloTipo())
        assertEquals("Inativo", anuncio.rotuloSituacao())
    }

    @Test
    fun filtrosDeAnuncioSeCombinam() {
        val home = anuncio(id = "1", tipo = TipoDeAnuncio.BANNER_HOME, ativo = true, link = "https://a.example")
        val categoria = anuncio(id = "2", tipo = TipoDeAnuncio.BANNER_CATEGORIA, ativo = false, link = "https://b.example")
        val lista = listOf(home, categoria)
        assertEquals(listOf("2"), lista.aplicar(FiltroDeAnuncios(situacao = SituacaoDoAnuncio.INATIVOS)).map { it.id })
        assertEquals(
            listOf("1"),
            lista.aplicar(FiltroDeAnuncios(busca = "a.example", tipo = TipoDeAnuncio.BANNER_HOME)).map { it.id }
        )
    }

    @Test
    fun escritaDeAnuncioNaoEAutorizada() {
        val item = anuncio()
        assertTrue(podeConsultarAnuncios(PapelNoPainel.ADMINISTRADOR))
        assertFalse(podeConsultarAnuncios(PapelNoPainel.MODERADOR))
        assertFalse(podeConsultarAnuncios(PapelNoPainel.NENHUM))
        assertTrue(autorizarAnuncio(PapelNoPainel.ADMINISTRADOR, item, AcaoDoAnuncio.VISUALIZAR).permitida)
        assertTrue(autorizarAnuncio(PapelNoPainel.ADMINISTRADOR, item, AcaoDoAnuncio.CONSULTAR_LINK).permitida)
        assertFalse(autorizarAnuncio(PapelNoPainel.ADMINISTRADOR, item.copy(link = null), AcaoDoAnuncio.CONSULTAR_LINK).permitida)
        listOf(AcaoDoAnuncio.CRIAR, AcaoDoAnuncio.EDITAR, AcaoDoAnuncio.ATIVAR).forEach { acao ->
            val decisao = autorizarAnuncio(PapelNoPainel.ADMINISTRADOR, item, acao)
            assertFalse(decisao.permitida)
            assertEquals(ACAO_INDISPONIVEL, decisao.motivo)
        }
        assertFalse(autorizarAnuncio(PapelNoPainel.MODERADOR, item, AcaoDoAnuncio.VISUALIZAR).permitida)
    }

    @Test
    fun empresaMascaraCnpjENaoGuardaONumeroInteiro() {
        val json = """
            {"id":"emp-1","razao_social":"Casa de Samba","cnpj":"12.345.678/0001-90","status":"pendente","criado_em":"2026-11-01T12:00:00.000Z","observacao":"Aguardar documento","user":{"nome":"Ana","email":"ana@exemplo.com"}}
        """.trimIndent()
        val empresa = gson.fromJson(json, LinhaDeEmpresa::class.java).paraEmpresa()!!
        assertEquals("emp-1", empresa.id)
        assertEquals("12.***.***/****-90", empresa.cnpjMascarado)
        assertFalse(empresa.cnpjMascarado!!.contains("345"))
        assertEquals("Ana", empresa.responsavel)
        assertEquals(SituacaoDaEmpresa.PENDENTE, empresa.status)
        assertEquals("CNPJ em formato não reconhecido", mascararCnpj("123"))
        assertNull(mascararCnpj("  "))
    }

    @Test
    fun filtrosDeEmpresaEAcoesDeEscrita() {
        val pendente = empresa(id = "1", nome = "Casa", status = SituacaoDaEmpresa.PENDENTE, email = "ana@exemplo.com")
        val rejeitada = empresa(id = "2", nome = "Palco", status = SituacaoDaEmpresa.REJEITADO, responsavel = "Bia")
        val lista = listOf(pendente, rejeitada)
        assertEquals(listOf("2"), lista.aplicar(FiltroDeEmpresas(busca = "bia", situacao = SituacaoDaEmpresa.REJEITADO)).map { it.id })
        assertTrue(podeConsultarEmpresas(PapelNoPainel.ADMINISTRADOR))
        assertFalse(podeConsultarEmpresas(PapelNoPainel.MODERADOR))
        assertTrue(autorizarEmpresa(PapelNoPainel.ADMINISTRADOR, AcaoDaEmpresa.VISUALIZAR).permitida)
        assertFalse(autorizarEmpresa(PapelNoPainel.ADMINISTRADOR, AcaoDaEmpresa.APROVAR).permitida)
        assertFalse(autorizarEmpresa(PapelNoPainel.ADMINISTRADOR, AcaoDaEmpresa.RECUSAR).permitida)
        assertTrue(AcaoDaEmpresa.APROVAR.exigeConfirmacao())
        assertTrue(AcaoDaEmpresa.RECUSAR.exigeConfirmacao())
        assertFalse(AcaoDaEmpresa.VISUALIZAR.exigeConfirmacao())
    }

    private fun anuncio(
        id: String = "1",
        tipo: TipoDeAnuncio = TipoDeAnuncio.BANNER_HOME,
        ativo: Boolean? = true,
        link: String? = "https://culturama.example/campanha"
    ) = AnuncioDoPainel(id, tipo, tipo.id, ativo, null, null, null, link, null)

    private fun empresa(
        id: String,
        nome: String,
        status: SituacaoDaEmpresa,
        email: String? = null,
        responsavel: String? = null
    ) = EmpresaDoPainel(id, nome, null, status, status.id, null, null, responsavel, email)
}
