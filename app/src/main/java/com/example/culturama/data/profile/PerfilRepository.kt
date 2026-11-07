package com.example.culturama.data.profile

import android.util.Log
import com.example.culturama.BuildConfig
import com.example.culturama.data.auth.AuthResult
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.data.remote.SupabaseProvider
import com.example.culturama.data.remote.chamarApi
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lê e grava somente a linha de `public.users` cujo `auth_id` é o usuário da sessão.
 * Nome de usuário, biografia e foto não têm coluna nessa tabela: não são enviados.
 */
object PerfilRepository {

    private const val ERRO_SALVAR = "Não foi possível salvar. Tente de novo."
    private const val ERRO_LER = "Não foi possível carregar o seu cadastro."
    private const val ERRO_SESSAO = "Entre na sua conta para continuar."
    private const val ERRO_CONFIRMAR = "O servidor não confirmou a alteração. Nada foi atualizado na tela."

    suspend fun catalogo(): Result<List<GrupoCultural>> = chamarApi {
        CatalogoCultural.grupos(ApiClient.api.categorias())
    }

    suspend fun lerCadastro(): Result<CadastroDaConta> = withContext(Dispatchers.IO) {
        val authId = idDaSessao() ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val caminho = CadastroRemoto.caminhoDaPropriaConta(authId)
            ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val resposta = pedir("GET", caminho, corpo = null)
        if (resposta == null) return@withContext Result.failure(IllegalStateException(ERRO_LER))
        if (resposta.codigo == 401) return@withContext Result.failure(IllegalStateException("Sua sessão expirou. Entre de novo."))
        val cadastro = CadastroRemoto.ler(resposta.corpo, authId)
            ?: return@withContext Result.failure(IllegalStateException(ERRO_LER))
        Result.success(cadastro)
    }

    suspend fun salvarNome(nome: String): Result<String> = withContext(Dispatchers.IO) {
        val authId = idDaSessao() ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val caminho = CadastroRemoto.caminhoDaPropriaConta(authId)
            ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val resposta = pedir("PATCH", caminho, CadastroRemoto.corpoDoNome(nome))
        if (resposta == null || resposta.codigo == 401) {
            return@withContext Result.failure(IllegalStateException(if (resposta?.codigo == 401) "Sua sessão expirou. Entre de novo." else ERRO_SALVAR))
        }
        if (!CadastroRemoto.confirmarNome(resposta.corpo, authId, nome)) {
            return@withContext Result.failure(IllegalStateException(ERRO_CONFIRMAR))
        }
        when (val sessao = SupabaseAuthService.instance.atualizarNomeNaSessao(nome)) {
            is AuthResult.Error -> Log.w("CulturamaPerfil", sessao.message)
            else -> Unit
        }
        Result.success(nome)
    }

    suspend fun salvarInteresses(tokens: List<String>): Result<List<String>> = withContext(Dispatchers.IO) {
        val authId = idDaSessao() ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val caminho = CadastroRemoto.caminhoDaPropriaConta(authId)
            ?: return@withContext Result.failure(IllegalStateException(ERRO_SESSAO))
        val resposta = pedir("PATCH", caminho, CadastroRemoto.corpoDosInteresses(tokens))
        if (resposta == null || resposta.codigo == 401) {
            return@withContext Result.failure(IllegalStateException(if (resposta?.codigo == 401) "Sua sessão expirou. Entre de novo." else ERRO_SALVAR))
        }
        if (!CadastroRemoto.confirmarInteresses(resposta.corpo, authId, tokens)) {
            return@withContext Result.failure(IllegalStateException(ERRO_CONFIRMAR))
        }
        Result.success(tokens)
    }

    private fun idDaSessao(): String? {
        return SupabaseProvider.client?.auth?.currentUserOrNull()?.id
            ?.takeIf { CadastroRemoto.idValido(it) }
    }

    private val http by lazy { HttpClient(OkHttp) }

    private data class RespostaHttp(val codigo: Int, val corpo: String)

    private suspend fun pedir(metodo: String, caminho: String, corpo: String?): RespostaHttp? {
        val chave = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        val token = SupabaseProvider.client?.auth?.currentAccessTokenOrNull()
        if (chave.isBlank() || token.isNullOrBlank()) return null
        val url = "${BuildConfig.SUPABASE_URL.trimEnd('/')}/rest/v1/$caminho"
        return try {
            val resposta = when (metodo) {
                "PATCH" -> http.patch(url) {
                    header("apikey", chave)
                    header("Authorization", "Bearer $token")
                    header("Accept", "application/json")
                    header("Prefer", "return=representation")
                    contentType(ContentType.Application.Json)
                    setBody(corpo.orEmpty())
                }
                else -> http.get(url) {
                    header("apikey", chave)
                    header("Authorization", "Bearer $token")
                    header("Accept", "application/json")
                }
            }
            resposta.paraResultado()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("CulturamaPerfil", "Falha ao falar com o cadastro", e)
            null
        }
    }

    private suspend fun HttpResponse.paraResultado(): RespostaHttp {
        val codigo = status.value
        if (codigo !in 200..299) Log.w("CulturamaPerfil", "Cadastro respondeu $codigo")
        return RespostaHttp(codigo, bodyAsText())
    }
}
