package com.example.culturama.data.admin

import com.google.gson.annotations.SerializedName
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Evento da tabela `events`, com os status e as fontes que o banco realmente enumera.
 * A lista pública de /api/v1 não traz rascunho, revisão nem a fonte canônica.
 */
data class EventoDoPainel(
    val id: String,
    val titulo: String,
    val categoria: String?,
    val endereco: String?,
    val organizador: String?,
    val instante: Long?,
    val fim: Long?,
    val descricao: String?,
    val status: StatusDoEvento,
    val statusBruto: String?,
    val fonte: FonteDoEvento,
    val fonteBruta: String?,
    val destaque: Boolean,
    val gratuito: Boolean?,
    val idExterno: String?,
    val imagemCapa: String?
)

enum class StatusDoEvento(val id: String, val rotulo: String) {
    PUBLICADO("publicado", "Publicado"),
    RASCUNHO("rascunho", "Rascunho"),
    EM_REVISAO("em_revisao", "Em revisão"),
    CANCELADO("cancelado", "Cancelado"),
    INATIVO("inativo", "Inativo"),
    OUTRO("", "Outro");

    companion object {
        fun de(valor: String?): StatusDoEvento {
            val texto = valor?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it != OUTRO && it.id == texto } ?: OUTRO
        }
    }
}

enum class FonteDoEvento(val id: String, val rotulo: String) {
    INTERNO("interno", "Eventos internos"),
    TICKETMASTER("ticketmaster", "Ticketmaster"),
    MAPAS_CULTURAIS("mapas_culturais", "Mapas Culturais"),
    DESCONHECIDA("", "Outra fonte");

    companion object {
        fun de(valor: String?): FonteDoEvento {
            val texto = valor?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it != DESCONHECIDA && it.id == texto } ?: DESCONHECIDA
        }
    }
}

fun EventoDoPainel.rotuloStatus(): String =
    if (status == StatusDoEvento.OUTRO) statusBruto?.trim().orEmpty().ifBlank { "Sem status" } else status.rotulo

fun EventoDoPainel.rotuloFonte(): String =
    if (fonte == FonteDoEvento.DESCONHECIDA) fonteBruta?.trim().orEmpty().ifBlank { "Fonte não informada" } else fonte.rotulo

enum class AcaoDoEvento {
    VISUALIZAR,
    EDITAR,
    PUBLICAR,
    DESPUBLICAR,
    DESTACAR,
    EXCLUIR
}

fun acoesDoMenu(evento: EventoDoPainel): List<AcaoDoEvento> = listOf(
    AcaoDoEvento.VISUALIZAR,
    AcaoDoEvento.EDITAR,
    if (evento.status == StatusDoEvento.PUBLICADO) AcaoDoEvento.DESPUBLICAR else AcaoDoEvento.PUBLICAR,
    AcaoDoEvento.DESTACAR,
    AcaoDoEvento.EXCLUIR
)

fun AcaoDoEvento.rotulo(evento: EventoDoPainel): String = when (this) {
    AcaoDoEvento.VISUALIZAR -> "Visualizar"
    AcaoDoEvento.EDITAR -> "Editar"
    AcaoDoEvento.PUBLICAR -> "Publicar"
    AcaoDoEvento.DESPUBLICAR -> "Despublicar"
    AcaoDoEvento.DESTACAR -> if (evento.destaque) "Remover destaque" else "Destacar"
    AcaoDoEvento.EXCLUIR -> "Excluir"
}

/** Excluir pede confirmação antes de qualquer pedido. Hoje a ação nem chega a ser autorizada. */
fun AcaoDoEvento.exigeConfirmacao(): Boolean = this == AcaoDoEvento.EXCLUIR

data class DecisaoDeAcao(val permitida: Boolean, val motivo: String)

private const val SEM_PERMISSAO = "Sem permissão para gerenciar eventos."

/**
 * O site não publica operação de escrita em /api/v1.
 * Colunas como `status` e `destaque` existem, mas o aplicativo não grava nelas:
 * não há regra confirmada para moderador, nem para evento sincronizado.
 */
const val ACAO_INDISPONIVEL =
    "Em desenvolvimento. O servidor ainda não autoriza esta operação no aplicativo."

fun podeConsultarEventos(papel: PapelNoPainel): Boolean =
    papel == PapelNoPainel.ADMINISTRADOR || papel == PapelNoPainel.MODERADOR

fun autorizar(papel: PapelNoPainel, evento: EventoDoPainel, acao: AcaoDoEvento): DecisaoDeAcao {
    if (!podeConsultarEventos(papel)) return DecisaoDeAcao(false, SEM_PERMISSAO)
    if (acao == AcaoDoEvento.VISUALIZAR) return DecisaoDeAcao(true, "")
    // Nenhuma fonte — interna, Ticketmaster, Mapas Culturais ou desconhecida — tem rota de escrita.
    if (evento.id.isEmpty()) return DecisaoDeAcao(false, ACAO_INDISPONIVEL)
    return DecisaoDeAcao(false, ACAO_INDISPONIVEL)
}

fun autorizarCriacao(papel: PapelNoPainel): DecisaoDeAcao {
    if (!podeConsultarEventos(papel)) return DecisaoDeAcao(false, SEM_PERMISSAO)
    return DecisaoDeAcao(false, ACAO_INDISPONIVEL)
}

data class FiltroDeEventos(
    val busca: String = "",
    val status: StatusDoEvento? = null,
    val fonte: FonteDoEvento? = null,
    val categoria: String? = null,
    /** Dia yyyy-MM-dd escolhido no calendário, em UTC, como o seletor entrega. */
    val diaInicial: String? = null,
    val diaFinal: String? = null
) {
    val ativo: Boolean
        get() = busca.isNotBlank() || status != null || fonte != null ||
            categoria != null || diaInicial != null || diaFinal != null
}

fun List<EventoDoPainel>.aplicar(filtro: FiltroDeEventos): List<EventoDoPainel> {
    val termo = normalizar(filtro.busca)
    return filter { evento ->
        (termo.isEmpty() || evento.casaCom(termo)) &&
            (filtro.status == null || evento.status == filtro.status) &&
            (filtro.fonte == null || evento.fonte == filtro.fonte) &&
            (filtro.categoria == null || evento.categoria == filtro.categoria) &&
            evento.caiNoIntervalo(filtro.diaInicial, filtro.diaFinal)
    }
}

private fun EventoDoPainel.casaCom(termo: String): Boolean {
    val campos = listOfNotNull(titulo, endereco, organizador)
    return campos.any { normalizar(it).contains(termo) }
}

private fun EventoDoPainel.caiNoIntervalo(inicio: String?, fim: String?): Boolean {
    if (inicio == null && fim == null) return true
    val dia = instante?.let { diaEmSaoPaulo(it) } ?: return false
    if (inicio != null && dia < inicio) return false
    if (fim != null && dia > fim) return false
    return true
}

fun normalizar(texto: String): String =
    Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .trim()

private val FUSO = TimeZone.getTimeZone("America/Sao_Paulo")
private val MESES = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

fun diaEmSaoPaulo(instante: Long): String = formatar("yyyy-MM-dd", FUSO, instante)

/** O seletor de data devolve a meia-noite UTC do dia escolhido. */
fun diaDoSeletor(millisUtc: Long): String = formatar("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), millisUtc)

fun textoDaData(instante: Long?): String {
    if (instante == null) return "Data não informada"
    val calendario = java.util.Calendar.getInstance(FUSO).apply { timeInMillis = instante }
    val dia = calendario.get(java.util.Calendar.DAY_OF_MONTH)
    val mes = MESES[calendario.get(java.util.Calendar.MONTH)]
    val ano = calendario.get(java.util.Calendar.YEAR)
    val hora = calendario.get(java.util.Calendar.HOUR_OF_DAY)
    val minuto = calendario.get(java.util.Calendar.MINUTE)
    val horario = if (minuto == 0) "${hora}h" else "${hora}h%02d".format(minuto)
    return "$dia $mes $ano · $horario"
}

fun textoDoDia(iso: String?): String {
    if (iso.isNullOrBlank()) return "Escolher"
    val partes = iso.split("-")
    if (partes.size != 3) return iso
    return "${partes[2]}/${partes[1]}/${partes[0]}"
}

private fun formatar(padrao: String, fuso: TimeZone, instante: Long): String =
    SimpleDateFormat(padrao, Locale.US).apply { timeZone = fuso }.format(Date(instante))

fun lerInstante(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    val formatos = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    return formatos.firstNotNullOfOrNull { padrao ->
        runCatching { SimpleDateFormat(padrao, Locale.US).parse(iso)?.time }.getOrNull()
    }
}

internal data class LinhaDeEvento(
    val id: String? = null,
    val titulo: String? = null,
    val status: String? = null,
    val fonte: String? = null,
    @SerializedName("categoria_id") val categoriaId: String? = null,
    val endereco: String? = null,
    @SerializedName("data_hora") val dataHora: String? = null,
    @SerializedName("data_fim") val dataFim: String? = null,
    val descricao: String? = null,
    val destaque: Boolean? = null,
    val gratuito: Boolean? = null,
    @SerializedName("id_externo") val idExterno: String? = null,
    @SerializedName("imagem_capa") val imagemCapa: String? = null,
    val categoria: LinhaDeCategoria? = null,
    val organizer: LinhaDeOrganizador? = null
)

internal data class LinhaDeCategoria(val nome: String? = null)
internal data class LinhaDeOrganizador(val user: LinhaDeUsuarioNome? = null)
internal data class LinhaDeUsuarioNome(val nome: String? = null)

internal fun LinhaDeEvento.paraEvento(): EventoDoPainel? {
    val identificador = id?.trim().orEmpty()
    if (identificador.isEmpty()) return null
    val statusEnum = StatusDoEvento.de(status)
    val fonteEnum = FonteDoEvento.de(fonte)
    return EventoDoPainel(
        id = identificador,
        titulo = titulo?.trim().orEmpty().ifBlank { "Sem título" },
        categoria = categoria?.nome?.trim()?.takeIf { it.isNotEmpty() },
        endereco = endereco?.trim()?.takeIf { it.isNotEmpty() },
        organizador = organizer?.user?.nome?.trim()?.takeIf { it.isNotEmpty() },
        instante = lerInstante(dataHora),
        fim = lerInstante(dataFim),
        descricao = descricao?.trim()?.takeIf { it.isNotEmpty() },
        status = statusEnum,
        statusBruto = status,
        fonte = fonteEnum,
        fonteBruta = fonte,
        destaque = destaque == true,
        gratuito = gratuito,
        idExterno = idExterno?.trim()?.takeIf { it.isNotEmpty() },
        imagemCapa = imagemCapa?.trim()?.takeIf { it.startsWith("http://") || it.startsWith("https://") }
    )
}
