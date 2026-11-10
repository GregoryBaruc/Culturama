package com.example.culturama.data.admin

import com.google.gson.annotations.SerializedName

/**
 * Espaço de anúncio em `ad_slots`.
 * Não há título, empresa nem evento nessa tabela: o que identifica o espaço é `tipo`.
 */
data class AnuncioDoPainel(
    val id: String,
    val tipo: TipoDeAnuncio,
    val tipoBruto: String?,
    val ativo: Boolean?,
    val inicio: String?,
    val fim: String?,
    val imagem: String?,
    val link: String?,
    val cliques: Int?
)

enum class TipoDeAnuncio(val id: String, val rotulo: String) {
    BANNER_HOME("banner_home", "Banner da home"),
    BANNER_CATEGORIA("banner_categoria", "Banner de categoria"),
    OUTRO("", "Outro tipo");

    companion object {
        fun de(valor: String?): TipoDeAnuncio {
            val texto = valor?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it != OUTRO && it.id == texto } ?: OUTRO
        }
    }
}

fun AnuncioDoPainel.rotuloTipo(): String =
    if (tipo == TipoDeAnuncio.OUTRO) tipoBruto?.trim().orEmpty().ifBlank { "Tipo não informado" } else tipo.rotulo

fun AnuncioDoPainel.rotuloSituacao(): String = when (ativo) {
    true -> "Ativo"
    false -> "Inativo"
    null -> "Sem status"
}

enum class AcaoDoAnuncio {
    VISUALIZAR,
    CRIAR,
    EDITAR,
    ATIVAR,
    CONSULTAR_LINK
}

fun AcaoDoAnuncio.rotulo(anuncio: AnuncioDoPainel): String = when (this) {
    AcaoDoAnuncio.VISUALIZAR -> "Visualizar"
    AcaoDoAnuncio.CRIAR -> "Novo anúncio"
    AcaoDoAnuncio.EDITAR -> "Editar"
    AcaoDoAnuncio.ATIVAR -> if (anuncio.ativo == true) "Desativar" else "Ativar"
    AcaoDoAnuncio.CONSULTAR_LINK -> "Consultar campanha"
}

fun acoesDoAnuncio(anuncio: AnuncioDoPainel): List<AcaoDoAnuncio> = listOf(
    AcaoDoAnuncio.VISUALIZAR,
    AcaoDoAnuncio.EDITAR,
    AcaoDoAnuncio.ATIVAR,
    AcaoDoAnuncio.CONSULTAR_LINK
)

fun podeConsultarAnuncios(papel: PapelNoPainel): Boolean = papel == PapelNoPainel.ADMINISTRADOR

fun autorizarAnuncio(papel: PapelNoPainel, anuncio: AnuncioDoPainel?, acao: AcaoDoAnuncio): DecisaoDeAcao {
    if (!podeConsultarAnuncios(papel)) return DecisaoDeAcao(false, "Sem permissão para gerenciar anúncios.")
    return when (acao) {
        AcaoDoAnuncio.VISUALIZAR -> DecisaoDeAcao(true, "")
        AcaoDoAnuncio.CONSULTAR_LINK -> {
            val link = anuncio?.link?.trim().orEmpty()
            if (link.startsWith("https://") || link.startsWith("http://")) DecisaoDeAcao(true, "")
            else DecisaoDeAcao(false, "Este anúncio não tem link de campanha.")
        }
        else -> DecisaoDeAcao(false, ACAO_INDISPONIVEL)
    }
}

enum class SituacaoDoAnuncio { TODOS, ATIVOS, INATIVOS }

data class FiltroDeAnuncios(
    val busca: String = "",
    val situacao: SituacaoDoAnuncio = SituacaoDoAnuncio.TODOS,
    val tipo: TipoDeAnuncio? = null
) {
    val ativo: Boolean
        get() = busca.isNotBlank() || situacao != SituacaoDoAnuncio.TODOS || tipo != null
}

fun List<AnuncioDoPainel>.aplicar(filtro: FiltroDeAnuncios): List<AnuncioDoPainel> {
    val termo = normalizar(filtro.busca)
    return filter { anuncio ->
        val casaBusca = termo.isEmpty() ||
            normalizar(anuncio.rotuloTipo()).contains(termo) ||
            normalizar(anuncio.link.orEmpty()).contains(termo)
        val casaSituacao = when (filtro.situacao) {
            SituacaoDoAnuncio.TODOS -> true
            SituacaoDoAnuncio.ATIVOS -> anuncio.ativo == true
            SituacaoDoAnuncio.INATIVOS -> anuncio.ativo == false
        }
        casaBusca && casaSituacao && (filtro.tipo == null || anuncio.tipo == filtro.tipo)
    }
}

/** Contagens de `interacoes_evento`. Nulo significa que o servidor não informou o número. */
data class ResumoDeInteracoes(
    val visualizacoes: Int?,
    val favoritos: Int?,
    val ingressos: Int?
)

fun textoDeQuando(valor: String?): String {
    if (valor.isNullOrBlank()) return "Não informada"
    val instante = lerInstante(valor)
    if (instante != null) return textoDaData(instante)
    val dia = Regex("""(\d{4})-(\d{2})-(\d{2})""").find(valor) ?: return "Não informada"
    return "${dia.groupValues[3]}/${dia.groupValues[2]}/${dia.groupValues[1]}"
}

internal data class LinhaDeAnuncio(
    val id: String? = null,
    val tipo: String? = null,
    val ativo: Boolean? = null,
    @SerializedName("data_inicio") val dataInicio: String? = null,
    @SerializedName("data_fim") val dataFim: String? = null,
    val imagem: String? = null,
    val link: String? = null,
    val cliques: Int? = null
)

internal fun LinhaDeAnuncio.paraAnuncio(): AnuncioDoPainel? {
    val identificador = id?.trim().orEmpty()
    if (identificador.isEmpty()) return null
    return AnuncioDoPainel(
        id = identificador,
        tipo = TipoDeAnuncio.de(tipo),
        tipoBruto = tipo,
        ativo = ativo,
        inicio = dataInicio?.trim()?.takeIf { it.isNotEmpty() },
        fim = dataFim?.trim()?.takeIf { it.isNotEmpty() },
        imagem = imagem?.trim()?.takeIf { it.startsWith("https://") || it.startsWith("http://") },
        link = link?.trim()?.takeIf { it.isNotEmpty() },
        cliques = cliques
    )
}
