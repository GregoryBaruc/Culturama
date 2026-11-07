package com.example.culturama.data.favorite

import com.example.culturama.data.EventRepository
import com.example.culturama.data.remote.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Favoritos da conta, os mesmos do site. As telas dependem só desta interface. */
interface FavoriteRepository {
    /** Ids conhecidos agora. A primeira chamada de cada conta busca a lista no servidor. */
    fun ids(userId: String): List<String>
    /** Muda na hora e confirma no servidor; se o servidor recusar, desfaz e avisa em [avisos]. */
    fun setFavorite(userId: String, eventId: String, favorite: Boolean): List<String>
    /** Busca de novo, para trazer o que mudou no site. */
    fun atualizar(userId: String)
    fun carregado(userId: String): Boolean
    fun falhouAoCarregar(userId: String): Boolean
    val alteracoes: StateFlow<Int>
    val avisos: SharedFlow<String>
}

/** Id da sessão atual. Vazio quando não há conta autenticada. */
fun idDoUsuarioAutenticado(): String? =
    SupabaseProvider.client?.auth?.currentUserOrNull()?.id?.trim()?.takeIf { it.isNotEmpty() }

/** As rotas `/favoritos` da API do site. */
interface FavoritosRemotos {
    suspend fun ids(): Result<Set<String>>
    suspend fun definir(eventId: String, favorito: Boolean): Result<Unit>
}

private object FavoritosDaApi : FavoritosRemotos {
    override suspend fun ids() = EventRepository.getFavoriteIds()
    override suspend fun definir(eventId: String, favorito: Boolean) =
        EventRepository.setFavorite(eventId, favorito)
}

/**
 * Guarda em memória a lista da conta e fala com a API. O estado só é tocado na thread do
 * [escopo], que no app é a principal, a mesma das telas.
 */
class RemoteFavoriteRepository(
    private val remoto: FavoritosRemotos,
    private val escopo: CoroutineScope
) : FavoriteRepository {

    private enum class Estado { NAO_CARREGADO, CARREGANDO, CARREGADO, FALHOU }

    private var dono: String? = null
    private var geracao = 0
    private var lista: List<String> = emptyList()
    private var estado = Estado.NAO_CARREGADO
    private var buscando = false
    /** Toques ainda não confirmados: valem por cima de uma lista que chegue do servidor nesse meio-tempo. */
    private val pendentes = mutableMapOf<String, Boolean>()
    /** Mantém a ordem dos toques no mesmo evento. */
    private val ordem = Mutex()

    private val _alteracoes = MutableStateFlow(0)
    override val alteracoes: StateFlow<Int> = _alteracoes.asStateFlow()

    private val _avisos = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val avisos: SharedFlow<String> = _avisos

    override fun ids(userId: String): List<String> {
        if (userId.isBlank()) return emptyList()
        usarConta(userId)
        if (estado == Estado.NAO_CARREGADO) buscar()
        return lista
    }

    override fun atualizar(userId: String) {
        if (userId.isBlank()) return
        usarConta(userId)
        buscar()
    }

    override fun carregado(userId: String) = dono == userId && estado == Estado.CARREGADO

    override fun falhouAoCarregar(userId: String) = dono == userId && estado == Estado.FALHOU

    override fun setFavorite(userId: String, eventId: String, favorite: Boolean): List<String> {
        if (userId.isBlank() || eventId.isBlank()) return ids(userId)
        usarConta(userId)
        if ((eventId in lista) == favorite) return lista

        lista = aplicar(lista, eventId, favorite)
        pendentes[eventId] = favorite
        avisarMudanca()

        val minhaGeracao = geracao
        escopo.launch {
            val resultado = ordem.withLock { remoto.definir(eventId, favorite) }
            if (minhaGeracao != geracao) return@launch
            val ultimoToque = pendentes[eventId] == favorite
            if (ultimoToque) pendentes.remove(eventId)
            resultado.onFailure { falha ->
                if (ultimoToque) {
                    lista = aplicar(lista, eventId, !favorite)
                    avisarMudanca()
                }
                _avisos.tryEmit(falha.message ?: "Não foi possível salvar o favorito.")
            }
        }
        return lista
    }

    /** Trocou de conta: nada da anterior pode aparecer nem chegar depois. */
    private fun usarConta(userId: String) {
        if (dono == userId) return
        dono = userId
        geracao++
        lista = emptyList()
        estado = Estado.NAO_CARREGADO
        buscando = false
        pendentes.clear()
    }

    private fun buscar() {
        if (buscando) return
        buscando = true
        if (estado != Estado.CARREGADO) estado = Estado.CARREGANDO
        val minhaGeracao = geracao
        escopo.launch {
            val resultado = remoto.ids()
            if (minhaGeracao != geracao) return@launch
            buscando = false
            resultado
                .onSuccess { doServidor ->
                    val nova = pendentes.entries.fold(doServidor.toList()) { atual, (id, favorito) ->
                        aplicar(atual, id, favorito)
                    }
                    val mudou = estado != Estado.CARREGADO || nova.toSet() != lista.toSet()
                    lista = nova
                    estado = Estado.CARREGADO
                    if (mudou) avisarMudanca()
                }
                .onFailure {
                    if (estado != Estado.CARREGADO) {
                        estado = Estado.FALHOU
                        avisarMudanca()
                    }
                }
        }
    }

    private fun aplicar(atual: List<String>, eventId: String, favorito: Boolean): List<String> = when {
        favorito && eventId !in atual -> listOf(eventId) + atual
        !favorito -> atual.filterNot { it == eventId }
        else -> atual
    }

    private fun avisarMudanca() {
        _alteracoes.value = _alteracoes.value + 1
    }

    companion object {
        val instance: FavoriteRepository by lazy {
            RemoteFavoriteRepository(
                FavoritosDaApi,
                CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            )
        }
    }
}
