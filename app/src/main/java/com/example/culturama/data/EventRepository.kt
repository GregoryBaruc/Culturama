package com.example.culturama.data

import com.example.culturama.data.remote.ApiClient
import com.example.culturama.data.remote.CategoriaDto
import com.example.culturama.data.remote.EventoDetalheDto
import com.example.culturama.data.remote.EventoResumoDto
import com.example.culturama.data.remote.FavoritoBody
import com.example.culturama.data.remote.FonteDto
import com.example.culturama.data.remote.chamarApi
import com.example.culturama.model.EventModel
import com.google.gson.JsonElement
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Eventos e favoritos vindos da API do site. */
object EventRepository {

    private val FUSO = TimeZone.getTimeZone("America/Sao_Paulo")
    private val MESES = listOf("JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ")
    private val DIAS = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    suspend fun getWeekEvents(): Result<List<EventModel>> = chamarApi {
        val dia = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = FUSO }
        val calendario = Calendar.getInstance(FUSO)
        val hoje = dia.format(calendario.time)
        calendario.add(Calendar.DAY_OF_YEAR, 7)
        val fim = dia.format(calendario.time)

        ApiClient.api.eventos(de = hoje, ate = fim, limite = 30).eventos.orEmpty().map { it.paraModelo() }
    }

    suspend fun getEventDetails(id: String): Result<EventModel> = chamarApi {
        ApiClient.api.evento(id).paraModelo()
    }

    suspend fun getFavoriteIds(): Result<Set<String>> = chamarApi {
        idsDeFavoritos(ApiClient.api.favoritos())
    }

    suspend fun setFavorite(id: String, favorite: Boolean): Result<Unit> = chamarApi {
        if (favorite) ApiClient.api.favoritar(FavoritoBody(id)) else ApiClient.api.desfavoritar(id)
    }

    /** O guia não mostra o JSON de GET /favoritos: aceita lista direta ou objeto com a lista. */
    private fun idsDeFavoritos(json: JsonElement): Set<String> {
        val lista = when {
            json.isJsonArray -> json.asJsonArray
            json.isJsonObject -> json.asJsonObject.entrySet()
                .firstOrNull { it.value.isJsonArray }?.value?.asJsonArray
            else -> null
        } ?: return emptySet()

        return lista.mapNotNull { item ->
            when {
                item.isJsonPrimitive -> item.asString
                item.isJsonObject -> item.asJsonObject.let { objeto ->
                    (objeto.get("id") ?: objeto.get("eventoId"))?.takeIf { it.isJsonPrimitive }?.asString
                }
                else -> null
            }
        }.toSet()
    }

    private fun EventoResumoDto.paraModelo() = modelo(
        id, titulo, dataHora, endereco, imagemCapa, gratuito, categoria, fonte
    )

    private fun EventoDetalheDto.paraModelo() = modelo(
        id, titulo, dataHora, endereco, imagemCapa, gratuito, categoria, fonte
    ).copy(
        description = descricao.orEmpty(),
        organizerName = organizador?.nome.orEmpty(),
        ticketUrl = linkIngresso,
        moreInfoUrl = linkSaibaMais,
        siteUrl = urlNoSite,
        detailsLoaded = true
    )

    private fun modelo(
        id: String,
        titulo: String?,
        dataHora: String?,
        endereco: String?,
        imagemCapa: String?,
        gratuito: Boolean?,
        categoria: CategoriaDto?,
        fonte: FonteDto?
    ): EventModel {
        val calendario = lerData(dataHora)?.let { data ->
            Calendar.getInstance(FUSO).apply { time = data }
        }
        return EventModel(
            id = id,
            title = titulo.orEmpty(),
            category = categoria?.nome ?: "Cultura",
            dayOfMonth = calendario?.get(Calendar.DAY_OF_MONTH) ?: 0,
            monthAbbr = calendario?.let { MESES[it.get(Calendar.MONTH)] }.orEmpty(),
            dayOfWeek = calendario?.let { DIAS[it.get(Calendar.DAY_OF_WEEK) - 1] }.orEmpty(),
            time = calendario?.let { horario(it) }.orEmpty(),
            venueName = endereco.orEmpty(),
            isFree = gratuito == true,
            price = "Pago",
            imageUrl = imagemCapa,
            sourceName = fonte?.nome,
            sourceUrl = fonte?.link
        )
    }

    private fun horario(calendario: Calendar): String {
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)
        return if (minuto == 0) "${hora}h" else "${hora}h%02d".format(minuto)
    }

    private fun lerData(iso: String?): Date? {
        if (iso == null) return null
        return listOf("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX").firstNotNullOfOrNull { formato ->
            runCatching { SimpleDateFormat(formato, Locale.US).parse(iso) }.getOrNull()
        }
    }
}
