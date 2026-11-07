package com.example.culturama.data.favorite

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFavoriteRepositoryTest {

    @Test
    fun adicionarNaoDuplicaEOMaisRecenteFicaPrimeiro() {
        val repo = LocalFavoriteRepository(MemoryFavoriteStorage())
        repo.setFavorite("conta-a", "evento-1", true)
        repo.setFavorite("conta-a", "evento-2", true)
        repo.setFavorite("conta-a", "evento-1", true)
        assertEquals(listOf("evento-2", "evento-1"), repo.ids("conta-a"))
    }

    @Test
    fun removerTiraSoOIdEscolhido() {
        val repo = LocalFavoriteRepository(MemoryFavoriteStorage())
        repo.setFavorite("conta-a", "evento-1", true)
        repo.setFavorite("conta-a", "evento-2", true)
        repo.setFavorite("conta-a", "evento-1", false)
        assertEquals(listOf("evento-2"), repo.ids("conta-a"))
        assertFalse(repo.ids("conta-a").contains("evento-1"))
    }

    @Test
    fun contasDiferentesNaoMisturamOsIds() {
        val repo = LocalFavoriteRepository(MemoryFavoriteStorage())
        repo.setFavorite("conta-a", "evento-1", true)
        repo.setFavorite("conta-b", "evento-9", true)
        assertEquals(listOf("evento-1"), repo.ids("conta-a"))
        assertEquals(listOf("evento-9"), repo.ids("conta-b"))
        repo.setFavorite("conta-a", "evento-1", false)
        assertTrue(repo.ids("conta-b").contains("evento-9"))
    }

    @Test
    fun idEmBrancoNaoEGravado() {
        val repo = LocalFavoriteRepository(MemoryFavoriteStorage())
        repo.setFavorite("conta-a", " ", true)
        repo.setFavorite("", "evento-1", true)
        assertTrue(repo.ids("conta-a").isEmpty())
        assertTrue(repo.ids("").isEmpty())
    }

    @Test
    fun novaInstanciaRecuperaOsIdsSalvos() {
        val storage = MemoryFavoriteStorage()
        LocalFavoriteRepository(storage).setFavorite("conta-a", "evento-1", true)
        assertEquals(listOf("evento-1"), LocalFavoriteRepository(storage).ids("conta-a"))
    }
}
