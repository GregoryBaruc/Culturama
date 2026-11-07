package com.example.culturama.data.admin

import com.example.culturama.BuildConfig
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.data.remote.SupabaseProvider
import com.example.culturama.data.remote.UsuarioDto
import com.example.culturama.data.remote.chamarApi
import com.google.gson.Gson
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Papel que o servidor informa em `GET /eu`.
 * No site, a sessão usa os booleanos `admin` e `moderador`.
 * O JWT do Supabase só traz `role = authenticated` e não distingue esses papéis.
 * `empresa` existe na sessão do site e não abre painel. Não há boolean de organizador nessa sessão.
 * Em `users` não existe coluna `admin`: o papel da conta está em `tipo`.
 */
enum class PapelNoPainel {
    NENHUM,
    MODERADOR,
    ADMINISTRADOR
}

object AcessoAdministrativo {

    /** Administrador prevalece quando as duas marcas vêm verdadeiras. */
    fun papelDe(usuario: UsuarioDto?): PapelNoPainel = when {
        usuario?.admin == true -> PapelNoPainel.ADMINISTRADOR
        usuario?.moderador == true -> PapelNoPainel.MODERADOR
        usuario?.admin == null && usuario?.moderador == null -> papelDoTipo(usuario?.tipo)
        else -> PapelNoPainel.NENHUM
    }

    /** `users.tipo` da própria conta. Outros textos não promovem ninguém. */
    fun papelDoTipo(tipo: String?): PapelNoPainel {
        val texto = tipo?.trim()?.lowercase()
            ?.replace("ç", "c")
            ?.replace("ã", "a")
            ?.replace("á", "a")
            .orEmpty()
        return when (texto) {
            "admin", "administrador" -> PapelNoPainel.ADMINISTRADOR
            "moderador", "moderacao" -> PapelNoPainel.MODERADOR
            else -> PapelNoPainel.NENHUM
        }
    }

    /**
     * Primeiro o `GET /eu`. Se ele não trouxer papel, lê só o `tipo` da linha
     * da sessão em `users`. O app não grava esse campo.
     */
    suspend fun verificar(): Result<PapelNoPainel> {
        val resposta = chamarApi { ApiClient.api.eu() }
        val usuario = resposta.getOrNull()?.usuario
        val pelaApi = if (resposta.isSuccess) papelDe(usuario) else null
        if (pelaApi != null && pelaApi != PapelNoPainel.NENHUM) return Result.success(pelaApi)

        val semBooleano = usuario == null || (usuario.admin == null && usuario.moderador == null)
        if (semBooleano) {
            val pelaTabela = papelDoTipo(runCatching { lerTipoDaConta() }.getOrNull())
            if (pelaTabela != PapelNoPainel.NENHUM) return Result.success(pelaTabela)
        }
        return resposta.map { papelDe(it.usuario) }
    }

    private suspend fun lerTipoDaConta(): String? = withContext(Dispatchers.IO) {
        val client = SupabaseProvider.client ?: return@withContext null
        val userId = client.auth.currentUserOrNull()?.id?.trim().orEmpty()
        val token = client.auth.currentAccessTokenOrNull()?.trim().orEmpty()
        if (userId.isEmpty() || token.isEmpty() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) {
            return@withContext null
        }
        val id = URLEncoder.encode(userId, "UTF-8")
        val url = URL("${BuildConfig.SUPABASE_URL}/rest/v1/users?id=eq.$id&select=tipo&limit=1")
        val conexao = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val codigo = conexao.responseCode
            val fluxo = if (codigo in 200..299) conexao.inputStream else return@withContext null
            val texto = fluxo.bufferedReader().use { it.readText() }
            Gson().fromJson(texto, Array<LinhaTipo>::class.java)?.firstOrNull()?.tipo
        } finally {
            conexao.disconnect()
        }
    }

    private data class LinhaTipo(val tipo: String?)
}

/** Uma métrica que o servidor ainda não entregou fica nula. Zero só aparece se a API disser zero. */
data class MetricaAdministrativa(val rotulo: String, val valor: Int?)

data class ContagemPorCategoria(val nome: String, val total: Int)

data class EventoMaisFavoritado(
    val nome: String,
    val categoria: String?,
    val favoritos: Int?
)

/**
 * Favoritos salvos no aparelho são da conta, não da plataforma.
 * Sem uma lista remota, a visão geral não transforma esse cache em ranking.
 */
sealed interface FavoritosDaPlataforma {
    data object Indisponivel : FavoritosDaPlataforma
    data class Lista(val itens: List<EventoMaisFavoritado>) : FavoritosDaPlataforma
}

data class VisaoGeral(
    val metricas: List<MetricaAdministrativa>,
    val porCategoria: List<ContagemPorCategoria>?,
    val maisFavoritados: FavoritosDaPlataforma
) {
    companion object {
        /**
         * O site monta estes números no servidor e não os publica em /api/v1.
         * O app não conta eventos públicos nem favoritos locais no lugar disso.
         */
        fun indisponivel(): VisaoGeral = VisaoGeral(
            metricas = listOf(
                MetricaAdministrativa("Eventos publicados", null),
                MetricaAdministrativa("Eventos em revisão", null),
                MetricaAdministrativa("Organizadores pendentes", null),
                MetricaAdministrativa("Usuários cadastrados", null),
                MetricaAdministrativa("Anúncios ativos", null),
                MetricaAdministrativa("Denúncias abertas", null)
            ),
            porCategoria = null,
            maisFavoritados = FavoritosDaPlataforma.Indisponivel
        )
    }
}

fun fracaoDaBarra(total: Int, maior: Int): Float {
    if (total <= 0 || maior <= 0) return 0f
    return (total.toFloat() / maior.toFloat()).coerceIn(0f, 1f)
}
