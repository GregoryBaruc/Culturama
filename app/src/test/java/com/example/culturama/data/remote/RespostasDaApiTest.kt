package com.example.culturama.data.remote

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Confere o app contra o formato real das respostas da API do site. */
class RespostasDaApiTest {

    private val gson = Gson()

    @Test
    fun `detalhe do evento vem dentro de evento`() {
        val json = """{"evento":{"id":"abc","titulo":"Roda de samba","descricao":"Na praça"}}"""
        val resposta = gson.fromJson(json, RespostaEventoDto::class.java)
        assertEquals("abc", resposta.evento.id)
        assertEquals("Na praça", resposta.evento.descricao)
    }

    @Test
    fun `eu informa se o perfil esta completo`() {
        val json = """{"usuario":{"id":"abc","perfilCompleto":false}}"""
        val resposta = gson.fromJson(json, RespostaEuDto::class.java)
        assertFalse(resposta.usuario?.perfilCompleto ?: true)
    }
}
