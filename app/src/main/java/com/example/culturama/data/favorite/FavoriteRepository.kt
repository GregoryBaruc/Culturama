package com.example.culturama.data.favorite

import android.content.Context
import com.example.culturama.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Favoritos da instalação. A troca por uma implementação remota fica para quando
 * o Supabase tiver essa estrutura. As telas dependem só desta interface.
 */
interface FavoriteRepository {
    fun ids(userId: String): List<String>
    fun setFavorite(userId: String, eventId: String, favorite: Boolean): List<String>
    val alteracoes: StateFlow<Int>
}

/** Id da sessão atual. Vazio quando não há conta autenticada. */
fun idDoUsuarioAutenticado(): String? =
    SupabaseProvider.client?.auth?.currentUserOrNull()?.id?.trim()?.takeIf { it.isNotEmpty() }

/**
 * Guarda só os IDs reais dos eventos, um conjunto por conta.
 * Sair da conta não apaga o que já foi salvo neste aparelho.
 */
class LocalFavoriteRepository(
    private val storage: FavoriteStorage
) : FavoriteRepository {

    private val memoria = mutableMapOf<String, List<String>>()
    private val _alteracoes = MutableStateFlow(0)
    override val alteracoes: StateFlow<Int> = _alteracoes.asStateFlow()

    override fun ids(userId: String): List<String> {
        if (userId.isBlank()) return emptyList()
        return memoria.getOrPut(userId) { storage.read(userId) }
    }

    override fun setFavorite(userId: String, eventId: String, favorite: Boolean): List<String> {
        if (userId.isBlank() || eventId.isBlank()) return ids(userId)
        val atual = ids(userId)
        val novo = if (favorite) {
            if (eventId in atual) atual else listOf(eventId) + atual
        } else {
            atual.filterNot { it == eventId }
        }
        if (novo == atual) return atual
        memoria[userId] = novo
        storage.write(userId, novo)
        _alteracoes.value = _alteracoes.value + 1
        return novo
    }

    companion object {
        @Volatile private var instance: FavoriteRepository? = null

        fun from(context: Context): FavoriteRepository {
            return instance ?: synchronized(this) {
                instance ?: LocalFavoriteRepository(PreferencesFavoriteStorage(context)).also { instance = it }
            }
        }
    }
}

interface FavoriteStorage {
    fun read(userId: String): List<String>
    fun write(userId: String, ids: List<String>)
}

class MemoryFavoriteStorage : FavoriteStorage {
    private val dados = mutableMapOf<String, List<String>>()
    override fun read(userId: String): List<String> = dados[userId].orEmpty()
    override fun write(userId: String, ids: List<String>) {
        dados[userId] = ids
    }
}

class PreferencesFavoriteStorage(context: Context) : FavoriteStorage {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun read(userId: String): List<String> {
        val bruto = prefs.getString(chave(userId), "").orEmpty()
        if (bruto.isEmpty()) return emptyList()
        return bruto.split(SEPARADOR).filter { it.isNotBlank() }.distinct()
    }

    override fun write(userId: String, ids: List<String>) {
        prefs.edit().putString(chave(userId), ids.distinct().joinToString(SEPARADOR)).apply()
    }

    private fun chave(userId: String) = "usuario_$userId"

    private companion object {
        const val PREFS = "culturama_favoritos_locais"
        const val SEPARADOR = "\u001F"
    }
}
