package com.example.culturama

import com.example.culturama.data.preference.UserPreferencesRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o repositório de preferências do usuário (Etapa 3).
 */
class UserPreferencesRepositoryTest {

    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setup() {
        repository = UserPreferencesRepository.getInstance(null)
        repository.resetPreferences()
    }

    @Test
    fun getAvailableCategories_hasAllRequiredCategories() {
        val categories = repository.getAvailableCategories()
        assertTrue("Deve conter mais de 10 categorias", categories.size >= 12)

        val ids = categories.map { it.id }.toSet()
        val requiredIds = setOf(
            "musica",
            "feiras_gastronomia",
            "cultura_popular",
            "cinema",
            "samba",
            "capoeira",
            "batalha_rima",
            "danca",
            "esportes",
            "artes_cenicas"
        )
        assertTrue("Todas as categorias principais devem existir", ids.containsAll(requiredIds))
    }

    @Test
    fun getAvailableCategories_hasUniqueIdsAndNonBlankNames() {
        val categories = repository.getAvailableCategories()
        val ids = mutableSetOf<String>()
        for (cat in categories) {
            assertTrue("ID deve ser único: ${cat.id}", ids.add(cat.id))
            assertTrue("Nome não pode ser vazio", cat.name.isNotBlank())
        }
    }

    @Test
    fun toggleCategory_addsAndRemovesCorrectly() {
        // Toggle em categoria não selecionada adiciona
        val afterAdd = repository.toggleCategory("esportes")
        assertTrue(afterAdd.contains("esportes"))

        // Toggle novamente remove
        val afterRemove = repository.toggleCategory("esportes")
        assertFalse(afterRemove.contains("esportes"))
    }

    @Test
    fun savePreferences_updatesStateAndMarksConfigured() {
        assertFalse(repository.isPreferencesConfigured())

        val myInterests = setOf("samba", "capoeira", "batalha_rima")
        repository.savePreferences(myInterests)

        assertTrue(repository.isPreferencesConfigured())
        assertEquals(myInterests, repository.getSelectedCategoryIds())
    }

    @Test
    fun createPayloadForApi_formatsCorrectStructure() {
        val myInterests = setOf("musica", "cultura_popular")
        repository.savePreferences(myInterests)

        val payload = repository.createPayloadForApi("usuario_tcc_123")
        assertEquals("usuario_tcc_123", payload.userId)
        assertTrue(payload.selectedCategoryIds.contains("musica"))
        assertTrue(payload.selectedCategoryIds.contains("cultura_popular"))
        assertTrue(payload.configuredAtMillis > 0)
    }

    @Test
    fun resetPreferences_restoresDefaultState() {
        repository.savePreferences(setOf("esportes"))
        assertTrue(repository.isPreferencesConfigured())

        repository.resetPreferences()
        assertFalse(repository.isPreferencesConfigured())
    }
}
