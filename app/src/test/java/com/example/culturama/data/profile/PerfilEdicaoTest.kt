package com.example.culturama.data.profile

import com.example.culturama.data.remote.AtualizarContaBody
import com.example.culturama.data.remote.CategoriaArvoreDto
import com.example.culturama.data.remote.ListaCategoriasDto
import com.example.culturama.data.remote.RespostaEuDto
import com.example.culturama.data.remote.UsuarioDto
import com.example.culturama.data.validation.FormValidator
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfilEdicaoTest {

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
    fun interesses_categoriaMaeMarcaTodasAsFilhas() {
        assertEquals(setOf("id-rock", "id-rap"), Interesses.selecionados(listOf("musica"), catalogo))
        assertEquals(setOf("id-rock", "id-rap"), Interesses.selecionados(listOf("cat_musica"), catalogo))
    }

    @Test
    fun interesses_valorDesconhecidoNaoMarcaNada() {
        assertEquals(emptySet<String>(), Interesses.selecionados(listOf("lixo"), catalogo))
    }

    @Test
    fun interesses_gravacaoSempreMandaSlugs() {
        assertEquals(listOf("rock", "literatura"), Interesses.tokensParaGravar(setOf("id-rock", "id-literatura"), catalogo))
    }

    @Test
    fun corpoDoNome_soEnviaONome() {
        val json = Gson().fromJson(Gson().toJson(AtualizarContaBody(nome = "Ana")), Map::class.java)
        assertEquals(setOf("nome"), json.keys)
        assertEquals("Ana", json["nome"])
    }

    @Test
    fun corpoDosInteresses_soEnviaALista() {
        val json = Gson().fromJson(Gson().toJson(AtualizarContaBody(interesses = listOf("rock"))), Map::class.java)
        assertEquals(setOf("interesses"), json.keys)
    }

    @Test
    fun corpoDosInteresses_listaVaziaLimpaOsInteresses() {
        assertEquals("""{"interesses":[]}""", Gson().toJson(AtualizarContaBody(interesses = emptyList())))
    }

    @Test
    fun leitura_limpaOsValoresDaConta() {
        val resposta = RespostaEuDto(UsuarioDto(nome = " Ana ", interesses = listOf(" rock ", " ")))
        assertEquals(CadastroDaConta("Ana", listOf("rock")), CadastroRemoto.ler(resposta))
    }

    @Test
    fun leitura_semContaNaoViraCadastroVazio() {
        assertNull(CadastroRemoto.ler(RespostaEuDto(null)))
    }

    @Test
    fun nomeDeExibicao_aceitaNomeCurtoERecusaVazio() {
        assertTrue(FormValidator.validateDisplayName("Ana").isValid)
        assertFalse(FormValidator.validateDisplayName("  ").isValid)
        assertFalse(FormValidator.validateDisplayName("A").isValid)
        assertFalse(FormValidator.validateDisplayName("n".repeat(81)).isValid)
    }
}
