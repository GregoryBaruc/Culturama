package com.example.culturama.data.remote

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

enum class Renovacao { OK, SESSAO_ACABOU, FALHA_DE_REDE }

/**
 * Regra do 401 nas rotas da conta: renova o token e tenta de novo uma vez.
 * Se continuar dando 401, ou se o Supabase recusar a renovação, a sessão acabou.
 * Falha de rede não encerra a sessão: a pessoa só precisa tentar de novo.
 */
class RenovadorDeToken(
    private val tokenAtual: () -> String?,
    private val renovar: () -> Renovacao,
    private val encerrarSessao: () -> Unit
) : Authenticator {

    private val trava = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Sem token no pedido não há o que renovar: é uma rota chamada sem login.
        val tokenUsado = response.request.header("Authorization")?.removePrefix("Bearer ") ?: return null

        synchronized(trava) {
            if (response.priorResponse != null) {
                encerrarSessao()
                return null
            }
            // Outra chamada pode já ter renovado enquanto esta esperava a trava.
            if (tokenAtual() == tokenUsado) {
                when (renovar()) {
                    Renovacao.OK -> Unit
                    Renovacao.FALHA_DE_REDE -> return null
                    Renovacao.SESSAO_ACABOU -> {
                        encerrarSessao()
                        return null
                    }
                }
            }
            val novoToken = tokenAtual() ?: return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer $novoToken")
                .build()
        }
    }
}
