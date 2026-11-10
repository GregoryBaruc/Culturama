package com.example.culturama.data.admin

import com.google.gson.annotations.SerializedName

/**
 * Empresa em `empresas`. O responsável e o e-mail vêm do usuário ligado por `user_id`.
 * Não há coluna de recusa automática.
 */
data class EmpresaDoPainel(
    val id: String,
    val razaoSocial: String,
    val cnpjMascarado: String?,
    val status: SituacaoDaEmpresa,
    val statusBruto: String?,
    val criadoEm: String?,
    val observacao: String?,
    val responsavel: String?,
    val email: String?
)

enum class SituacaoDaEmpresa(val id: String, val rotulo: String) {
    PENDENTE("pendente", "Pendente"),
    APROVADO("aprovado", "Aprovado"),
    REJEITADO("rejeitado", "Rejeitado"),
    OUTRA("", "Outra situação");

    companion object {
        fun de(valor: String?): SituacaoDaEmpresa {
            val texto = valor?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it != OUTRA && it.id == texto } ?: OUTRA
        }
    }
}

fun EmpresaDoPainel.rotuloSituacao(): String =
    if (status == SituacaoDaEmpresa.OUTRA) statusBruto?.trim().orEmpty().ifBlank { "Sem situação" } else status.rotulo

enum class AcaoDaEmpresa {
    VISUALIZAR,
    APROVAR,
    RECUSAR
}

fun AcaoDaEmpresa.rotulo(): String = when (this) {
    AcaoDaEmpresa.VISUALIZAR -> "Visualizar"
    AcaoDaEmpresa.APROVAR -> "Aprovar"
    AcaoDaEmpresa.RECUSAR -> "Recusar"
}

fun AcaoDaEmpresa.exigeConfirmacao(): Boolean =
    this == AcaoDaEmpresa.APROVAR || this == AcaoDaEmpresa.RECUSAR

fun podeConsultarEmpresas(papel: PapelNoPainel): Boolean = papel == PapelNoPainel.ADMINISTRADOR

fun autorizarEmpresa(papel: PapelNoPainel, acao: AcaoDaEmpresa): DecisaoDeAcao {
    if (!podeConsultarEmpresas(papel)) return DecisaoDeAcao(false, "Sem permissão para gerenciar empresas.")
    if (acao == AcaoDaEmpresa.VISUALIZAR) return DecisaoDeAcao(true, "")
    return DecisaoDeAcao(false, ACAO_INDISPONIVEL)
}

data class FiltroDeEmpresas(
    val busca: String = "",
    val situacao: SituacaoDaEmpresa? = null
) {
    val ativo: Boolean get() = busca.isNotBlank() || situacao != null
}

fun List<EmpresaDoPainel>.aplicar(filtro: FiltroDeEmpresas): List<EmpresaDoPainel> {
    val termo = normalizar(filtro.busca)
    return filter { empresa ->
        val casaBusca = termo.isEmpty() || listOfNotNull(empresa.razaoSocial, empresa.responsavel, empresa.email)
            .any { normalizar(it).contains(termo) }
        casaBusca && (filtro.situacao == null || empresa.status == filtro.situacao)
    }
}

/** Mostra só as pontas do CNPJ. Valor fora do padrão não é repetido na tela. */
fun mascararCnpj(valor: String?): String? {
    val digitos = valor?.filter { it.isDigit() }.orEmpty()
    if (digitos.isEmpty()) return null
    if (digitos.length != 14) return "CNPJ em formato não reconhecido"
    return "${digitos.take(2)}.***.***/****-${digitos.takeLast(2)}"
}

internal data class LinhaDeEmpresa(
    val id: String? = null,
    @SerializedName("razao_social") val razaoSocial: String? = null,
    val cnpj: String? = null,
    val status: String? = null,
    @SerializedName("criado_em") val criadoEm: String? = null,
    val observacao: String? = null,
    val user: LinhaDeResponsavel? = null
)

internal data class LinhaDeResponsavel(
    val nome: String? = null,
    val email: String? = null
)

internal fun LinhaDeEmpresa.paraEmpresa(): EmpresaDoPainel? {
    val identificador = id?.trim().orEmpty()
    if (identificador.isEmpty()) return null
    return EmpresaDoPainel(
        id = identificador,
        razaoSocial = razaoSocial?.trim().orEmpty().ifBlank { "Sem razão social" },
        cnpjMascarado = mascararCnpj(cnpj),
        status = SituacaoDaEmpresa.de(status),
        statusBruto = status,
        criadoEm = criadoEm?.trim()?.takeIf { it.isNotEmpty() },
        observacao = observacao?.trim()?.takeIf { it.isNotEmpty() },
        responsavel = user?.nome?.trim()?.takeIf { it.isNotEmpty() },
        email = user?.email?.trim()?.takeIf { it.isNotEmpty() }
    )
}
