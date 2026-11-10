package com.example.culturama.data.acesso

import org.junit.Assert.assertEquals
import org.junit.Test

class AcessoDaContaTest {

    @Test
    fun organizadorVerificadoEntra() {
        val linhas = listOf(LinhaOrganizador(id = "1", verificado = true, status = "pendente"))
        assertEquals(ResultadoDeAcesso.AUTORIZADO, interpretarOrganizadores(linhas, colunasDeSituacao = true))
    }

    @Test
    fun organizadorAprovadoPeloStatusEntra() {
        val linhas = listOf(LinhaOrganizador(id = "1", verificado = false, status = "aprovado"))
        assertEquals(ResultadoDeAcesso.AUTORIZADO, interpretarOrganizadores(linhas, colunasDeSituacao = true))
    }

    @Test
    fun organizadorPendenteNaoEntra() {
        val linhas = listOf(LinhaOrganizador(id = "1", verificado = false, status = "pendente"))
        assertEquals(ResultadoDeAcesso.PENDENTE, interpretarOrganizadores(linhas, colunasDeSituacao = true))
    }

    @Test
    fun organizadorRecusadoNaoEntra() {
        val linhas = listOf(LinhaOrganizador(id = "1", status = "rejeitado"))
        assertEquals(ResultadoDeAcesso.RECUSADO, interpretarOrganizadores(linhas, colunasDeSituacao = true))
    }

    @Test
    fun linhaSemSituacaoNaoAutoriza() {
        val linhas = listOf(LinhaOrganizador(id = "1"))
        assertEquals(ResultadoDeAcesso.INDETERMINADO, interpretarOrganizadores(linhas, colunasDeSituacao = false))
    }

    @Test
    fun semLinhaDeOrganizadorNaoAutoriza() {
        assertEquals(ResultadoDeAcesso.AUSENTE, interpretarOrganizadores(emptyList(), colunasDeSituacao = true))
    }

    @Test
    fun empresaSoEntraAprovada() {
        assertEquals(
            ResultadoDeAcesso.AUTORIZADO,
            interpretarEmpresas(listOf(LinhaEmpresa(id = "1", status = "aprovado")))
        )
        assertEquals(
            ResultadoDeAcesso.PENDENTE,
            interpretarEmpresas(listOf(LinhaEmpresa(id = "1", status = "pendente")))
        )
        assertEquals(
            ResultadoDeAcesso.RECUSADO,
            interpretarEmpresas(listOf(LinhaEmpresa(id = "1", status = "rejeitado")))
        )
        assertEquals(ResultadoDeAcesso.AUSENTE, interpretarEmpresas(emptyList()))
        assertEquals(
            ResultadoDeAcesso.INDETERMINADO,
            interpretarEmpresas(listOf(LinhaEmpresa(id = "1", status = null)))
        )
    }
}
