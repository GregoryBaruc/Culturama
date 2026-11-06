package com.example.culturama.data.remote

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RenovadorDeTokenTest {

    private var token: String? = "velho"
    private var renovacoes = 0
    private var encerrada = false

    private fun renovador(resultado: Renovacao = Renovacao.OK, tokenNovo: String? = "novo") = RenovadorDeToken(
        tokenAtual = { token },
        renovar = {
            renovacoes++
            if (resultado == Renovacao.OK) token = tokenNovo
            resultado
        },
        encerrarSessao = { encerrada = true }
    )

    private fun resposta401(tokenDoPedido: String? = "velho", anterior: Response? = null): Response {
        val pedido = Request.Builder().url("https://exemplo.test/api/v1/eu")
        tokenDoPedido?.let { pedido.header("Authorization", "Bearer $it") }
        return Response.Builder()
            .request(pedido.build())
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(anterior)
            .build()
    }

    @Test
    fun `sem token no pedido nao renova`() {
        assertNull(renovador().authenticate(null, resposta401(tokenDoPedido = null)))
        assertEquals(0, renovacoes)
        assertEquals(false, encerrada)
    }

    @Test
    fun `renova e tenta de novo com o token novo`() {
        val pedido = renovador().authenticate(null, resposta401())
        assertEquals("Bearer novo", pedido?.header("Authorization"))
        assertEquals(1, renovacoes)
    }

    @Test
    fun `usa o token que outra chamada ja renovou`() {
        token = "novo"
        val pedido = renovador().authenticate(null, resposta401(tokenDoPedido = "velho"))
        assertEquals("Bearer novo", pedido?.header("Authorization"))
        assertEquals(0, renovacoes)
    }

    @Test
    fun `segundo 401 encerra a sessao`() {
        val segundo = resposta401(tokenDoPedido = "novo", anterior = resposta401())
        assertNull(renovador().authenticate(null, segundo))
        assertEquals(true, encerrada)
    }

    @Test
    fun `renovacao recusada encerra a sessao`() {
        assertNull(renovador(Renovacao.SESSAO_ACABOU).authenticate(null, resposta401()))
        assertEquals(true, encerrada)
    }

    @Test
    fun `falha de rede mantem a sessao`() {
        assertNull(renovador(Renovacao.FALHA_DE_REDE).authenticate(null, resposta401()))
        assertEquals(false, encerrada)
    }
}
