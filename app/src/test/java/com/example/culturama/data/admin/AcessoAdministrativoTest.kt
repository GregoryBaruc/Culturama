package com.example.culturama.data.admin

import com.example.culturama.data.remote.RespostaEuDto
import com.example.culturama.data.remote.UsuarioDto
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcessoAdministrativoTest {

    private val gson = Gson()

    @Test
    fun eu_comAdminVerdadeiroEAdministrador() {
        val json = """{"usuario":{"id":"abc","admin":true,"moderador":false,"email":"alguem@exemplo.com"}}"""
        val usuario = gson.fromJson(json, RespostaEuDto::class.java).usuario
        assertEquals(PapelNoPainel.ADMINISTRADOR, AcessoAdministrativo.papelDe(usuario))
    }

    @Test
    fun eu_comModeradorVerdadeiroNaoViraAdministrador() {
        val json = """{"usuario":{"id":"abc","admin":false,"moderador":true}}"""
        val usuario = gson.fromJson(json, RespostaEuDto::class.java).usuario
        assertEquals(PapelNoPainel.MODERADOR, AcessoAdministrativo.papelDe(usuario))
    }

    @Test
    fun asDuasMarcasVerdadeirasPrevalecemComoAdministrador() {
        val usuario = UsuarioDto(id = "abc", admin = true, moderador = true)
        assertEquals(PapelNoPainel.ADMINISTRADOR, AcessoAdministrativo.papelDe(usuario))
    }

    @Test
    fun eu_soComEmailNaoAutoriza() {
        val json = """{"usuario":{"id":"abc","email":"admin@culturama.app"}}"""
        val usuario = gson.fromJson(json, RespostaEuDto::class.java).usuario
        assertEquals(PapelNoPainel.NENHUM, AcessoAdministrativo.papelDe(usuario))
    }

    @Test
    fun eu_semBandeiraNaoAutoriza() {
        val json = """{"usuario":{"id":"abc","perfilCompleto":true,"nome":"Ana"}}"""
        val usuario = gson.fromJson(json, RespostaEuDto::class.java).usuario
        assertNull(usuario?.admin)
        assertNull(usuario?.moderador)
        assertEquals(PapelNoPainel.NENHUM, AcessoAdministrativo.papelDe(usuario))
    }

    @Test
    fun tipoAdminViraAdministradorQuandoAApiNaoMandaBooleano() {
        val json = """{"usuario":{"id":"abc","nome":"Ana","tipo":"ADMINISTRADOR"}}"""
        val usuario = gson.fromJson(json, RespostaEuDto::class.java).usuario
        assertEquals(PapelNoPainel.ADMINISTRADOR, AcessoAdministrativo.papelDe(usuario))
        assertEquals(PapelNoPainel.MODERADOR, AcessoAdministrativo.papelDoTipo("moderação"))
        assertEquals(PapelNoPainel.NENHUM, AcessoAdministrativo.papelDoTipo("organizador"))
    }

    @Test
    fun bandeirasFalsasNaoAutorizam() {
        assertEquals(
            PapelNoPainel.NENHUM,
            AcessoAdministrativo.papelDe(UsuarioDto(id = "abc", admin = false, moderador = false))
        )
        assertEquals(PapelNoPainel.NENHUM, AcessoAdministrativo.papelDe(null))
    }

    @Test
    fun visaoSemEndpointNaoInventaNumero() {
        val visao = VisaoGeral.indisponivel()
        assertTrue(visao.metricas.all { it.valor == null })
        assertNull(visao.porCategoria)
        assertEquals(FavoritosDaPlataforma.Indisponivel, visao.maisFavoritados)
    }

    @Test
    fun barraAcompanhaOMaiorTotal() {
        assertEquals(1f, fracaoDaBarra(89, 89), 0.001f)
        assertEquals(0.5f, fracaoDaBarra(10, 20), 0.001f)
        assertEquals(0f, fracaoDaBarra(0, 20), 0.001f)
        assertEquals(0f, fracaoDaBarra(4, 0), 0.001f)
    }
}
