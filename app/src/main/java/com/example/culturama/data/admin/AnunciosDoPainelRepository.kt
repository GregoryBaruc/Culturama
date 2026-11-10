package com.example.culturama.data.admin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lê `ad_slots` e as contagens de `interacoes_evento`. Só administrador. */
object AnunciosDoPainelRepository {

    suspend fun listar(): Result<List<AnuncioDoPainel>> = consultar {
        ConsultaDoPainel.paginas<LinhaDeAnuncio>(
            tabela = "ad_slots",
            select = "id,ativo,data_inicio,data_fim,imagem,link,cliques,tipo",
            ordem = "data_inicio.desc.nullslast",
            tipo = tipoLista<LinhaDeAnuncio>()
        ).mapNotNull { it.paraAnuncio() }.distinctBy { it.id }
    }

    suspend fun resumo(): Result<ResumoDeInteracoes> = consultar {
        ResumoDeInteracoes(
            visualizacoes = ConsultaDoPainel.contar("interacoes_evento", "tipo=eq.visualizacao"),
            favoritos = ConsultaDoPainel.contar("interacoes_evento", "tipo=eq.favorito"),
            ingressos = ConsultaDoPainel.contar("interacoes_evento", "tipo=eq.ingresso")
        )
    }

    private suspend fun <T> consultar(bloco: () -> T): Result<T> {
        val papel = AcessoAdministrativo.verificar().getOrElse { return Result.failure(it) }
        if (!podeConsultarAnuncios(papel)) {
            return Result.failure(IllegalStateException("Sem permissão para consultar os anúncios."))
        }
        return try {
            Result.success(withContext(Dispatchers.IO) { bloco() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
