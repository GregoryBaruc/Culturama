package com.example.culturama.data.admin

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lê `empresas` e o nome e o e-mail do usuário ligado. Só administrador. */
object EmpresasDoPainelRepository {

    suspend fun listar(): Result<List<EmpresaDoPainel>> {
        val papel = AcessoAdministrativo.verificar().getOrElse { return Result.failure(it) }
        if (!podeConsultarEmpresas(papel)) {
            return Result.failure(IllegalStateException("Sem permissão para consultar as empresas."))
        }
        return try {
            Result.success(withContext(Dispatchers.IO) { buscar() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buscar(): List<EmpresaDoPainel> =
        ConsultaDoPainel.paginas<LinhaDeEmpresa>(
            tabela = "empresas",
            select = "id,razao_social,cnpj,status,criado_em,observacao,user:users!user_id(nome,email)",
            ordem = "criado_em.desc.nullslast",
            tipo = tipoLista<LinhaDeEmpresa>()
        ).mapNotNull { it.paraEmpresa() }.distinctBy { it.id }
}
