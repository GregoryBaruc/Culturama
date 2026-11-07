package com.example.culturama.data.profile

import com.example.culturama.data.remote.CategoriaArvoreDto
import com.example.culturama.data.remote.ListaCategoriasDto
import com.example.culturama.data.validation.FormValidator
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilEdicaoTest {

    private val authId = "11111111-1111-1111-1111-111111111111"
    private val outroId = "22222222-2222-2222-2222-222222222222"

    private val catalogo = CatalogoCultural.grupos(
        ListaCategoriasDto(
            listOf(
                CategoriaArvoreDto(
                    id = "cat_musica",
                    nome = "Música",
                    slug = "musica",
                    filhas = listOf(
                        CategoriaArvoreDto(id = "id-rock", nome = "Rock", slug = "rock"),
                        CategoriaArvoreDto(id = "id-rap", nome = "Rap e rima", slug = "rap-e-rima")
                    )
                ),
                CategoriaArvoreDto(
                    id = "id-literatura",
                    nome = "Literatura",
                    slug = "literatura",
                    filhas = emptyList()
                )
            )
        )
    )

    @Test
    fun catalogo_usaFilhasComoChipsECategoriaSemFilhaComoOpcao() {
        assertEquals(listOf("Música", "Literatura"), catalogo.map { it.nome })
        assertEquals(listOf("Rock", "Rap e rima"), catalogo[0].opcoes.map { it.nome })
        assertEquals("literatura", catalogo[1].opcoes.single().slug)
    }

    @Test
    fun interesses_reconhecemIdESlugJaGravados() {
        val porId = Interesses.selecionados(listOf("id-rock"), catalogo)
        val porSlug = Interesses.selecionados(listOf("rap-e-rima", "literatura"), catalogo)
        assertEquals(setOf("id-rock"), porId)
        assertEquals(setOf("id-rap", "id-literatura"), porSlug)
    }

    @Test
    fun interesses_contaNovaGravaIds() {
        val tokens = Interesses.tokensParaGravar(setOf("id-rock", "id-literatura"), emptyList(), catalogo)
        assertEquals(listOf("id-rock", "id-literatura"), tokens)
    }

    @Test
    fun interesses_contaQueJaUsaSlugContinuaEmSlug() {
        val tokens = Interesses.tokensParaGravar(setOf("id-rap"), listOf("rock"), catalogo)
        assertEquals(listOf("rap-e-rima"), tokens)
    }

    @Test
    fun corpoDoNome_soEnviaONome() {
        val json = Gson().fromJson(CadastroRemoto.corpoDoNome("Ana"), Map::class.java)
        assertEquals(setOf("nome"), json.keys)
        assertEquals("Ana", json["nome"])
    }

    @Test
    fun corpoDosInteresses_soEnviaALista() {
        val json = Gson().fromJson(
            CadastroRemoto.corpoDosInteresses(listOf("id-rock")),
            Map::class.java
        )
        assertEquals(setOf("interesses"), json.keys)
    }

    @Test
    fun caminho_rejeitaIdQueNaoEhDaSessao() {
        assertNull(CadastroRemoto.caminhoDaPropriaConta("cat_musica"))
        assertNull(CadastroRemoto.caminhoDaPropriaConta(""))
        assertTrue(CadastroRemoto.caminhoDaPropriaConta(authId)!!.contains("auth_id=eq.$authId"))
    }

    @Test
    fun leitura_ignoraLinhaDeOutraConta() {
        val json = """[{"auth_id":"$outroId","nome":"Outra","interesses":["rock"]}]"""
        assertNull(CadastroRemoto.ler(json, authId))
    }

    @Test
    fun confirmacao_soAceitaALinhaDaPropriaConta() {
        val json = """[{"auth_id":"$authId","nome":"Ana Lima","interesses":["id-rock"]}]"""
        assertTrue(CadastroRemoto.confirmarNome(json, authId, "Ana Lima"))
        assertFalse(CadastroRemoto.confirmarNome(json, authId, "Outro nome"))
        assertFalse(CadastroRemoto.confirmarNome(json, outroId, "Ana Lima"))
        assertTrue(CadastroRemoto.confirmarInteresses(json, authId, listOf("id-rock")))
        assertNull(CadastroRemoto.ler("[]", authId))
    }

    @Test
    fun nomeDeExibicao_aceitaNomeCurtoERecusaVazio() {
        assertTrue(FormValidator.validateDisplayName("Ana").isValid)
        assertFalse(FormValidator.validateDisplayName("  ").isValid)
        assertFalse(FormValidator.validateDisplayName("A").isValid)
        assertFalse(FormValidator.validateDisplayName("n".repeat(81)).isValid)
    }
}
