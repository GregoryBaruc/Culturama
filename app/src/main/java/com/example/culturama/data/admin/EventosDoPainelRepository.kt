package com.example.culturama.data.admin

import android.util.Log
import com.example.culturama.BuildConfig
import com.example.culturama.data.remote.SupabaseProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Lê `events` com o token da sessão. A RLS do Supabase decide o que a conta enxerga.
 * Não usa service_role e não grava: publicar, destacar, editar e excluir não têm rota no site.
 */
object EventosDoPainelRepository {

    private const val PAGINA = 100
    private const val TETO = 50
    private const val SELECT =
        "id,titulo,status,fonte,categoria_id,endereco,data_hora,data_fim,descricao,destaque," +
            "gratuito,id_externo,imagem_capa,categoria:categories(nome)," +
            "organizer:organizers(user:users(nome))"

    suspend fun listar(): Result<List<EventoDoPainel>> {
        val papel = AcessoAdministrativo.verificar().getOrElse { return Result.failure(it) }
        if (!podeConsultarEventos(papel)) {
            return Result.failure(IllegalStateException("Sem permissão para consultar os eventos."))
        }
        return try {
            Result.success(withContext(Dispatchers.IO) { buscar() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buscar(): List<EventoDoPainel> {
        val token = SupabaseProvider.client?.auth?.currentAccessTokenOrNull()?.trim().orEmpty()
        val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (token.isEmpty() || chave.isBlank()) {
            throw IllegalStateException("Entre de novo para consultar os eventos.")
        }
        val gson = Gson()
        val tipo = object : TypeToken<List<LinhaDeEvento>>() {}.type
        val todos = mutableListOf<EventoDoPainel>()
        var offset = 0
        var paginas = 0
        while (paginas < TETO) {
            val corpo = pedir(chave, token, offset)
            val linhas: List<LinhaDeEvento> = gson.fromJson(corpo, tipo) ?: emptyList()
            todos += linhas.mapNotNull { it.paraEvento() }
            if (linhas.size < PAGINA) break
            offset += PAGINA
            paginas++
        }
        return todos.distinctBy { it.id }
    }

    private fun pedir(chave: String, token: String, offset: Int): String {
        val select = URLEncoder.encode(SELECT, "UTF-8")
        val url = URL(
            "${BuildConfig.SUPABASE_URL}/rest/v1/events?select=$select" +
                "&order=data_hora.desc.nullslast&limit=$PAGINA&offset=$offset"
        )
        val conexao = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("apikey", chave)
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val codigo = conexao.responseCode
            val fluxo = if (codigo in 200..299) conexao.inputStream else conexao.errorStream
            val texto = fluxo?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (codigo !in 200..299) {
                Log.w("CulturamaEventos", "Consulta de eventos recusada: HTTP $codigo")
                throw IllegalStateException(mensagemDe(codigo))
            }
            return texto.ifBlank { "[]" }
        } finally {
            conexao.disconnect()
        }
    }

    private fun mensagemDe(codigo: Int): String = when (codigo) {
        401, 403 -> "A sessão não autorizou a consulta dos eventos. Entre de novo."
        else -> "Não foi possível consultar os eventos."
    }
}
