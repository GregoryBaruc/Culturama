package com.example.culturama.data.remote

import android.util.Log
import com.example.culturama.BuildConfig
import com.google.gson.Gson
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://culturama-iota-two.vercel.app/api/v1/"
private const val ERRO_CONEXAO = "Não foi possível conectar. Tente de novo."

private data class ErroDto(val codigo: String?, val mensagem: String?)
private data class RespostaErroDto(val erro: ErroDto?)

object ApiClient {
    /** Manda a chave do app em toda chamada e o token do Supabase quando há sessão. */
    private val cabecalhos = Interceptor { chain ->
        val pedido = chain.request().newBuilder()
            .header("x-culturama-app", BuildConfig.CULTURAMA_APP_CHAVE)
        SupabaseProvider.client?.auth?.currentAccessTokenOrNull()?.let { token ->
            pedido.header("Authorization", "Bearer $token")
        }
        chain.proceed(pedido.build())
    }

    private val _sessaoEncerrada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Avisa que a sessão acabou no servidor e a pessoa precisa entrar de novo. */
    val sessaoEncerrada: SharedFlow<Unit> = _sessaoEncerrada

    private val renovadorDeToken = RenovadorDeToken(
        tokenAtual = { SupabaseProvider.client?.auth?.currentAccessTokenOrNull() },
        renovar = {
            try {
                runBlocking { SupabaseProvider.client?.auth?.refreshCurrentSession() }
                Renovacao.OK
            } catch (e: RestException) {
                Renovacao.SESSAO_ACABOU
            } catch (e: Exception) {
                Renovacao.FALHA_DE_REDE
            }
        },
        encerrarSessao = {
            runBlocking { SupabaseProvider.client?.auth?.clearSession() }
            _sessaoEncerrada.tryEmit(Unit)
        }
    )

    val api: CulturamaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(OkHttpClient.Builder().addInterceptor(cabecalhos).authenticator(renovadorDeToken).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CulturamaApi::class.java)
    }
}

/** Erro da API com a mensagem em português, pronta para ir para a tela. */
class ApiException(val codigo: String?, message: String) : Exception(message)

private fun Throwable.paraApiException(): ApiException {
    if (this is HttpException) {
        val erro = runCatching {
            Gson().fromJson(response()?.errorBody()?.string(), RespostaErroDto::class.java).erro
        }.getOrNull()
        return ApiException(erro?.codigo, erro?.mensagem ?: ERRO_CONEXAO)
    }
    Log.w("CulturamaApi", "Falha de rede ou de leitura da resposta", this)
    return ApiException(null, ERRO_CONEXAO)
}

/** Roda uma chamada da API sem deixar exceção de rede chegar nas telas. */
suspend fun <T> chamarApi(bloco: suspend () -> T): Result<T> = try {
    Result.success(bloco())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e.paraApiException())
}
