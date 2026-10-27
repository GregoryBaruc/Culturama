package com.example.culturama.data.preference

import android.content.Context
import com.example.culturama.model.BubbleSize
import com.example.culturama.model.CategoryPreference
import com.example.culturama.model.UserPreferencesPayload
import com.example.culturama.ui.component.CulturamaIcons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositório responsável por gerenciar as categorias de interesses culturais
 * e as escolhas do usuário no aplicativo Culturama.
 *
 * ARQUITETURA E PERSISTÊNCIA (TCC ADS):
 * - Nesta etapa demonstrativa, mantém o estado em memória com persistência local em SharedPreferences
 *   apenas para preservar os identificadores escolhidos e a flag de configuração concluída.
 * - NENHUMA senha ou dado sensível é manipulado neste repositório.
 * - Desacoplado da camada de rede, pronto para enviar o [UserPreferencesPayload] à API REST na Etapa 4.
 */
class UserPreferencesRepository private constructor(context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Categorias culturais disponíveis no Culturama, alinhadas à imagem de referência
    private val categories = listOf(
        CategoryPreference(
            id = "musica",
            name = "Música",
            icon = CulturamaIcons.MusicNote,
            sizeType = BubbleSize.LARGE,
            emoji = "🎵"
        ),
        CategoryPreference(
            id = "feiras_gastronomia",
            name = "Feiras e\ngastronomia",
            icon = CulturamaIcons.ShoppingBasket,
            sizeType = BubbleSize.LARGE,
            emoji = "🧺"
        ),
        CategoryPreference(
            id = "cultura_popular",
            name = "Cultura\npopular\ne tradicional",
            icon = CulturamaIcons.FlowerCulture,
            sizeType = BubbleSize.LARGE,
            emoji = "🌸"
        ),
        CategoryPreference(
            id = "cinema",
            name = "Cinema e\naudiovisual",
            icon = CulturamaIcons.Movie,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎬"
        ),
        CategoryPreference(
            id = "literatura",
            name = "Literatura",
            icon = CulturamaIcons.BookLiterature,
            sizeType = BubbleSize.SMALL,
            emoji = "📚"
        ),
        CategoryPreference(
            id = "outros",
            name = "Outros",
            icon = CulturamaIcons.ShapesOther,
            sizeType = BubbleSize.SMALL,
            emoji = "✨"
        ),
        CategoryPreference(
            id = "geek_games",
            name = "Geek e games",
            icon = CulturamaIcons.Games,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎮"
        ),
        CategoryPreference(
            id = "artes_cenicas",
            name = "Artes cênicas",
            icon = CulturamaIcons.Theater,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎭"
        ),
        CategoryPreference(
            id = "danca",
            name = "Dança",
            icon = CulturamaIcons.Dance,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🩰"
        ),
        CategoryPreference(
            id = "arte_exposicao",
            name = "Arte e\nexposição",
            icon = CulturamaIcons.PaletteArt,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎨"
        ),
        CategoryPreference(
            id = "oficinas",
            name = "Oficinas\ne cursos",
            icon = CulturamaIcons.SchoolCourse,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎓"
        ),
        CategoryPreference(
            id = "esportes",
            name = "Esportes",
            icon = CulturamaIcons.TrophySport,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🏆"
        ),
        CategoryPreference(
            id = "samba",
            name = "Samba",
            icon = CulturamaIcons.DrumSamba,
            sizeType = BubbleSize.SMALL,
            emoji = "🥁"
        ),
        CategoryPreference(
            id = "batalha_rima",
            name = "Batalha\nde rima",
            icon = CulturamaIcons.MicRap,
            sizeType = BubbleSize.MEDIUM,
            emoji = "🎤"
        ),
        CategoryPreference(
            id = "capoeira",
            name = "Capoeira",
            icon = CulturamaIcons.CapoeiraMartial,
            sizeType = BubbleSize.SMALL,
            emoji = "🥋"
        ),
        CategoryPreference(
            id = "sarau",
            name = "Sarau e\npoesia",
            icon = CulturamaIcons.BookLiterature,
            sizeType = BubbleSize.SMALL,
            emoji = "📖"
        )
    )

    // Categorias selecionadas por padrão conforme destaque da imagem de referência
    private val defaultSelected = setOf("musica", "feiras_gastronomia", "cultura_popular")

    private val _selectedCategories = MutableStateFlow(
        prefs?.getStringSet(KEY_SELECTED_CATEGORIES, null) ?: defaultSelected
    )
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private var _isConfigured = prefs?.getBoolean(KEY_IS_CONFIGURED, false) ?: false

    fun getAvailableCategories(): List<CategoryPreference> = categories

    fun getSelectedCategoryIds(): Set<String> = _selectedCategories.value

    fun toggleCategory(categoryId: String): Set<String> {
        val current = _selectedCategories.value.toMutableSet()
        if (categoryId in current) {
            current.remove(categoryId)
        } else {
            current.add(categoryId)
        }
        _selectedCategories.value = current
        prefs?.edit()?.putStringSet(KEY_SELECTED_CATEGORIES, current)?.apply()
        return current
    }

    fun savePreferences(selectedIds: Set<String>) {
        _selectedCategories.value = selectedIds
        _isConfigured = true
        prefs?.edit()
            ?.putStringSet(KEY_SELECTED_CATEGORIES, selectedIds)
            ?.putBoolean(KEY_IS_CONFIGURED, true)
            ?.putLong(KEY_CONFIGURED_AT, System.currentTimeMillis())
            ?.apply()
    }

    fun isPreferencesConfigured(): Boolean = _isConfigured

    fun setPreferencesConfigured(configured: Boolean) {
        _isConfigured = configured
        prefs?.edit()?.putBoolean(KEY_IS_CONFIGURED, configured)?.apply()
    }

    /**
     * Prepara o payload para envio futuro à API REST.
     */
    fun createPayloadForApi(userId: String = "demo_user"): UserPreferencesPayload {
        return UserPreferencesPayload(
            userId = userId,
            selectedCategoryIds = _selectedCategories.value.toList(),
            configuredAtMillis = prefs?.getLong(KEY_CONFIGURED_AT, System.currentTimeMillis())
                ?: System.currentTimeMillis()
        )
    }

    /**
     * Reseta as preferências (útil para testes do fluxo de novos acessos).
     */
    fun resetPreferences() {
        _isConfigured = false
        _selectedCategories.value = defaultSelected
        prefs?.edit()?.clear()?.apply()
    }

    companion object {
        private const val PREFS_NAME = "culturama_preferences"
        private const val KEY_SELECTED_CATEGORIES = "selected_categories"
        private const val KEY_IS_CONFIGURED = "is_preferences_configured"
        private const val KEY_CONFIGURED_AT = "preferences_configured_at"

        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context? = null): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context).also { INSTANCE = it }
            }
        }
    }
}
