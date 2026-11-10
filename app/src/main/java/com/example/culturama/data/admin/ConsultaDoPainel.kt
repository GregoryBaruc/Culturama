package com.example.culturama.data.admin

import android.util.Log
import com.example.culturama.BuildConfig
import com.example.culturama.data.remote.SupabaseProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.jan.supabase.auth.auth
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Leitura do Supabase com o token da sessão e a chave pública.
 * Quem chama já conferiu o papel. Não grava e não usa service_role.
 */
internal object ConsultaDoPainel {

    private const val PAGINA = 100
    private const val TETO = 50

    fun <T> paginas(tabela: String, select: String, ordem: String, tipo: java.lang.reflect.Type): List<T> {
        val gson = Gson()
        val todos = mutableListOf<T>()
        var offset = 0
        var pagina = 0
        while (pagina < TETO) {
            val corpo = pedir(
                caminho = "$tabela?select=${URLEncoder.encode(select, "UTF-8")}" +
                    "&order=$ordem&limit=$PAGINA&offset=$offset"
            ).corpo
            val linhas: List<T> = gson.fromJson(corpo, tipo) ?: emptyList()
            todos += linhas
            if (linhas.size < PAGINA) break
            offset += PAGINA
            pagina++
        }
        return todos
    }

    /** Total exato pelo Content-Range. Nulo se o servidor não mandar a contagem. */
    fun contar(tabela: String, filtro: String): Int? {
        val faixa = pedir(
            caminho = "$tabela?$filtro&select=id&limit=1",
            contar = true
        ).faixa ?: return null
        return faixa.substringAfter('/', "").trim().toIntOrNull()
    }

    private data class Resposta(val corpo: String, val faixa: String?)

    private fun pedir(caminho: String, contar: Boolean = false): Resposta {
        val token = SupabaseProvider.client?.auth?.currentAccessTokenOrNull()?.trim().orEmpty()
        val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (token.isEmpty() || chave.isBlank()) {
            throw IllegalStateException("Entre de novo para consultar o painel.")
        }
        val conexao = (URL("${BuildConfig.SUPABASE_URL}/rest/v1/$caminho").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("apikey", chave)
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (contar) setRequestProperty("Prefer", "count=exact")
        }
        try {
            val codigo = conexao.responseCode
            val fluxo = if (codigo in 200..299) conexao.inputStream else conexao.errorStream
            val texto = fluxo?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (codigo !in 200..299) {
                Log.w("CulturamaPainel", "Consulta recusada: HTTP $codigo")
                throw IllegalStateException(
                    if (codigo == 401 || codigo == 403) {
                        "A sessão não autorizou esta consulta. Entre de novo."
                    } else {
                        "Não foi possível consultar o painel."
                    }
                )
            }
            return Resposta(texto.ifBlank { "[]" }, conexao.getHeaderField("Content-Range"))
        } finally {
            conexao.disconnect()
        }
    }
}

internal inline fun <reified T> tipoLista(): java.lang.reflect.Type =
    object : TypeToken<List<T>>() {}.type
