package com.example.culturama.data.acesso

import com.example.culturama.BuildConfig
import com.example.culturama.data.remote.SupabaseProvider
import com.google.gson.Gson
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Caminho pedido na entrada. Não é um papel e não é gravado no servidor.
 * Uma conta pode ter mais de um papel ao mesmo tempo.
 */
enum class IntencaoDeAcesso(val id: String) {
    DESCOBERTA("descoberta"),
    PERFIL("perfil"),
    ORGANIZADOR("organizador"),
    EMPRESA("empresa");

    companion object {
        fun porId(id: String?): IntencaoDeAcesso =
            entries.firstOrNull { it.id == id?.trim() } ?: DESCOBERTA
    }
}

/**
 * O que a leitura da própria conta permite concluir.
 * [INDETERMINADO] e [FALHA] não liberam área privilegiada.
 */
enum class ResultadoDeAcesso {
    AUTORIZADO,
    PENDENTE,
    RECUSADO,
    AUSENTE,
    INDETERMINADO,
    FALHA
}

data class LinhaOrganizador(
    val id: String? = null,
    val verificado: Boolean? = null,
    val status: String? = null
)

data class LinhaEmpresa(
    val id: String? = null,
    val status: String? = null
)

/** Status que o servidor já usa em `empresas` e equivalentes de aprovação. */
internal fun statusAutorizado(status: String?): Boolean =
    normalizar(status) in setOf("aprovado", "verificado", "ativo")

internal fun statusPendente(status: String?): Boolean =
    normalizar(status) in setOf("pendente", "em_analise", "em analise", "analise")

internal fun statusRecusado(status: String?): Boolean =
    normalizar(status) in setOf("rejeitado", "recusado", "negado")

/**
 * Organizador só entra se `organizers` da própria conta estiver verificado ou aprovado.
 * Linha sem essas colunas não autoriza. Moderador não entra por aqui;
 * administrador é liberado depois, em [comPrivilegioDeAdministrador].
 */
fun interpretarOrganizadores(
    linhas: List<LinhaOrganizador>,
    colunasDeSituacao: Boolean
): ResultadoDeAcesso {
    val existentes = linhas.filter { !it.id.isNullOrBlank() }
    if (existentes.isEmpty()) return ResultadoDeAcesso.AUSENTE
    if (!colunasDeSituacao) return ResultadoDeAcesso.INDETERMINADO
    if (existentes.any { it.verificado == true || statusAutorizado(it.status) }) {
        return ResultadoDeAcesso.AUTORIZADO
    }
    if (existentes.all { statusRecusado(it.status) }) return ResultadoDeAcesso.RECUSADO
    return ResultadoDeAcesso.PENDENTE
}

/** Empresa só abre o Insights com `empresas.status` aprovado na conta da sessão. */
fun interpretarEmpresas(linhas: List<LinhaEmpresa>): ResultadoDeAcesso {
    val existentes = linhas.filter { !it.id.isNullOrBlank() }
    if (existentes.isEmpty()) return ResultadoDeAcesso.AUSENTE
    if (existentes.any { statusAutorizado(it.status) }) return ResultadoDeAcesso.AUTORIZADO
    if (existentes.any { statusPendente(it.status) }) return ResultadoDeAcesso.PENDENTE
    if (existentes.all { statusRecusado(it.status) }) return ResultadoDeAcesso.RECUSADO
    return ResultadoDeAcesso.INDETERMINADO
}

/**
 * Administrador do painel pode entrar e usar as áreas de organizador e empresa
 * mesmo sem cadastro próprio nesses papéis. Moderador continua bloqueado.
 */
fun comPrivilegioDeAdministrador(
    resultadoDaConta: ResultadoDeAcesso,
    administrador: Boolean
): ResultadoDeAcesso =
    if (administrador) ResultadoDeAcesso.AUTORIZADO else resultadoDaConta

/**
 * Lê só a linha da sessão em `organizers` e `empresas`.
 * Usa o JWT do usuário e a chave pública. Não grava e não usa service_role.
 */
object AcessoDaConta {

    suspend fun situacaoOrganizador(): ResultadoDeAcesso = withContext(Dispatchers.IO) {
        val userId = idDaSessao() ?: return@withContext ResultadoDeAcesso.FALHA
        val completa = ler(
            tabela = "organizers",
            select = "id,verificado,status",
            userId = userId,
            tipo = Array<LinhaOrganizador>::class.java
        )
        when (completa) {
            is Leitura.Ok -> interpretarOrganizadores(completa.linhas.toList(), colunasDeSituacao = true)
            is Leitura.ColunaAusente -> {
                val soId = ler("organizers", "id", userId, Array<LinhaOrganizador>::class.java)
                when (soId) {
                    is Leitura.Ok -> interpretarOrganizadores(soId.linhas.toList(), colunasDeSituacao = false)
                    else -> ResultadoDeAcesso.FALHA
                }
            }
            is Leitura.Falhou -> ResultadoDeAcesso.FALHA
        }
    }

    suspend fun situacaoEmpresa(): ResultadoDeAcesso = withContext(Dispatchers.IO) {
        val userId = idDaSessao() ?: return@withContext ResultadoDeAcesso.FALHA
        when (val leitura = ler("empresas", "id,status", userId, Array<LinhaEmpresa>::class.java)) {
            is Leitura.Ok -> interpretarEmpresas(leitura.linhas.toList())
            else -> ResultadoDeAcesso.FALHA
        }
    }

    private fun idDaSessao(): String? =
        SupabaseProvider.client?.auth?.currentUserOrNull()?.id?.trim()?.takeIf { it.isNotEmpty() }

    private sealed interface Leitura<T> {
        class Ok<T>(val linhas: Array<T>) : Leitura<T>
        class ColunaAusente<T> : Leitura<T>
        class Falhou<T> : Leitura<T>
    }

    private fun <T> ler(tabela: String, select: String, userId: String, tipo: Class<Array<T>>): Leitura<T> {
        val token = SupabaseProvider.client?.auth?.currentAccessTokenOrNull()?.trim().orEmpty()
        val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (token.isEmpty() || chave.isBlank()) return Leitura.Falhou()
        val id = URLEncoder.encode(userId, "UTF-8")
        val colunas = URLEncoder.encode(select, "UTF-8")
        val url = URL("${BuildConfig.SUPABASE_URL}/rest/v1/$tabela?user_id=eq.$id&select=$colunas&limit=5")
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
            if (codigo == 400) return Leitura.ColunaAusente()
            if (codigo !in 200..299) return Leitura.Falhou()
            val texto = conexao.inputStream.bufferedReader().use { it.readText() }
            val linhas = Gson().fromJson(texto, tipo) ?: return Leitura.Falhou()
            return Leitura.Ok(linhas)
        } catch (_: Exception) {
            return Leitura.Falhou()
        } finally {
            conexao.disconnect()
        }
    }

}

private fun normalizar(status: String?): String =
    status?.trim()?.lowercase()?.replace("ç", "c")?.replace("ã", "a")?.replace("á", "a").orEmpty()
