package com.example.culturama.data.profile

import android.util.Log
import com.example.culturama.data.auth.AuthResult
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.data.remote.AtualizarContaBody
import com.example.culturama.data.remote.chamarApi

/**
 * Lê e grava nome e interesses pela API do site, que descobre a conta pelo token.
 * O app não acessa a tabela `users`: as regras e as permissões ficam no servidor.
 */
object PerfilRepository {

    private const val ERRO_LER = "Não foi possível carregar o seu cadastro."
    private const val ERRO_CONFIRMAR = "O servidor não confirmou a alteração. Nada foi atualizado na tela."

    suspend fun catalogo(): Result<List<GrupoCultural>> = chamarApi {
        CatalogoCultural.grupos(ApiClient.api.categorias())
    }

    suspend fun lerCadastro(): Result<CadastroDaConta> =
        chamarApi { ApiClient.api.eu() }.mapCatching { resposta ->
            CadastroRemoto.ler(resposta) ?: throw IllegalStateException(ERRO_LER)
        }

    suspend fun salvarNome(nome: String): Result<String> {
        val gravado = atualizar(AtualizarContaBody(nome = nome)).map { it.nome }
        val nomeGravado = gravado.getOrNull()
        if (nomeGravado != null) {
            when (val sessao = SupabaseAuthService.instance.atualizarNomeNaSessao(nomeGravado)) {
                is AuthResult.Error -> Log.w("CulturamaPerfil", sessao.message)
                else -> Unit
            }
        }
        return gravado
    }

    /** Devolve os interesses na forma canônica que o servidor gravou. */
    suspend fun salvarInteresses(slugs: List<String>): Result<List<String>> =
        atualizar(AtualizarContaBody(interesses = slugs)).map { it.interesses }

    private suspend fun atualizar(corpo: AtualizarContaBody): Result<CadastroDaConta> =
        chamarApi { ApiClient.api.atualizarConta(corpo) }.mapCatching { resposta ->
            CadastroRemoto.ler(resposta) ?: throw IllegalStateException(ERRO_CONFIRMAR)
        }
}
