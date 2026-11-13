package com.example.culturama.data.planos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlanosCatalogoTest {

    @Test
    fun organizadores_temTresPlanosComPrecosTransparentes() {
        val planos = PlanosCatalogo.organizadores
        assertEquals(3, planos.size)
        assertEquals("R$ 0", planos[0].preco)
        assertEquals(AcaoDoPlano.CADASTRO_ORGANIZADOR, planos[0].acao)
        assertEquals(AcaoDoPlano.EM_BREVE, planos[1].acao)
        assertEquals(AcaoDoPlano.EM_BREVE, planos[2].acao)
        assertFalse(planos[1].preco.contains("R$"))
        assertFalse(planos[2].preco.contains("R$"))
    }

    @Test
    fun empresas_naoInventamPercentuais() {
        val planos = PlanosCatalogo.empresas
        assertEquals(3, planos.size)
        assertEquals("R$ 0", planos[0].preco)
        assertEquals(AcaoDoPlano.CADASTRO_EMPRESA, planos[0].acao)
        planos.forEach { plano ->
            assertFalse(plano.preco.contains("%"))
            assertFalse(plano.beneficios.any { it.contains("%") })
        }
        assertTrue(planos[1].preco.contains("a definir") || planos[1].preco.contains("definir"))
        assertTrue(planos[2].preco.contains("personalizadas") || planos[2].preco.contains("personaliz"))
    }
}
