package com.example.culturama.data.favorite

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteFavoriteRepositoryTest {

    private class Falso(var noServidor: Set<String> = emptySet()) : FavoritosRemotos {
        var falhaAoLer = false
        var falhaAoGravar = false
        var leituras = 0
        var portaoDaLeitura: CompletableDeferred<Unit>? = null
        var portaoDaGravacao: CompletableDeferred<Unit>? = null
        val gravacoes = mutableListOf<Pair<String, Boolean>>()

        override suspend fun ids(): Result<Set<String>> {
            leituras++
            portaoDaLeitura?.await()
            return if (falhaAoLer) Result.failure(Exception("sem rede")) else Result.success(noServidor)
        }

        override suspend fun definir(eventId: String, favorito: Boolean): Result<Unit> {
            gravacoes += eventId to favorito
            portaoDaGravacao?.await()
            if (falhaAoGravar) return Result.failure(Exception("recusado"))
            noServidor = if (favorito) noServidor + eventId else noServidor - eventId
            return Result.success(Unit)
        }
    }

    private fun repo(falso: Falso) = RemoteFavoriteRepository(falso, CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun primeiraLeituraTrazOQueEstaNoServidor() {
        val falso = Falso(setOf("evento-1"))
        val repo = repo(falso)
        assertEquals(listOf("evento-1"), repo.ids("conta-a"))
        assertTrue(repo.carregado("conta-a"))
        repo.ids("conta-a")
        assertEquals(1, falso.leituras)
    }

    @Test
    fun favoritarGravaNoServidorSemDuplicar() {
        val falso = Falso()
        val repo = repo(falso)
        repo.ids("conta-a")
        repo.setFavorite("conta-a", "evento-1", true)
        repo.setFavorite("conta-a", "evento-1", true)
        assertEquals(listOf("evento-1"), repo.ids("conta-a"))
        assertEquals(listOf("evento-1" to true), falso.gravacoes)
        assertEquals(setOf("evento-1"), falso.noServidor)
    }

    @Test
    fun removerTiraSoOEventoEscolhido() {
        val falso = Falso(setOf("evento-1", "evento-2"))
        val repo = repo(falso)
        repo.ids("conta-a")
        repo.setFavorite("conta-a", "evento-1", false)
        assertEquals(listOf("evento-2"), repo.ids("conta-a"))
        assertEquals(setOf("evento-2"), falso.noServidor)
    }

    @Test
    fun servidorRecusouDesfazOToque() {
        val falso = Falso().apply { falhaAoGravar = true }
        val repo = repo(falso)
        repo.ids("conta-a")
        repo.setFavorite("conta-a", "evento-1", true)
        assertEquals(emptyList<String>(), repo.ids("conta-a"))
    }

    @Test
    fun falhaAoLerNaoPareceListaVaziaENaoTentaSozinhoSemParar() {
        val falso = Falso(setOf("evento-1")).apply { falhaAoLer = true }
        val repo = repo(falso)
        repo.ids("conta-a")
        repo.ids("conta-a")
        assertTrue(repo.falhouAoCarregar("conta-a"))
        assertFalse(repo.carregado("conta-a"))
        assertEquals(1, falso.leituras)

        falso.falhaAoLer = false
        repo.atualizar("conta-a")
        assertEquals(listOf("evento-1"), repo.ids("conta-a"))
        assertFalse(repo.falhouAoCarregar("conta-a"))
    }

    @Test
    fun atualizarTrazOQueMudouNoSite() {
        val falso = Falso(setOf("evento-1"))
        val repo = repo(falso)
        repo.ids("conta-a")
        falso.noServidor = setOf("evento-1", "evento-2")
        repo.atualizar("conta-a")
        assertEquals(setOf("evento-1", "evento-2"), repo.ids("conta-a").toSet())
    }

    @Test
    fun trocarDeContaNaoMisturaAsListas() {
        val falso = Falso(setOf("evento-1"))
        val repo = repo(falso)
        repo.ids("conta-a")
        falso.noServidor = setOf("evento-9")
        assertEquals(listOf("evento-9"), repo.ids("conta-b"))
        assertFalse(repo.carregado("conta-a"))
    }

    @Test
    fun idEmBrancoNaoVaiParaOServidor() {
        val falso = Falso()
        val repo = repo(falso)
        repo.setFavorite("conta-a", " ", true)
        repo.setFavorite(" ", "evento-1", true)
        assertTrue(falso.gravacoes.isEmpty())
    }

    @Test
    fun toqueFeitoDuranteALeituraNaoSePerde() {
        val leitura = CompletableDeferred<Unit>()
        val gravacao = CompletableDeferred<Unit>()
        val falso = Falso(setOf("evento-1")).apply {
            portaoDaLeitura = leitura
            portaoDaGravacao = gravacao
        }
        val repo = repo(falso)
        repo.ids("conta-a")
        repo.setFavorite("conta-a", "evento-2", true)
        leitura.complete(Unit)
        assertEquals(setOf("evento-1", "evento-2"), repo.ids("conta-a").toSet())
        gravacao.complete(Unit)
        assertEquals(setOf("evento-1", "evento-2"), falso.noServidor)
    }
}
