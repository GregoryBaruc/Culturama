package com.example.culturama.data.map

import com.example.culturama.model.EventModel

/** Critérios da tela do mapa. Não é o estado da aba Eventos. */
data class FiltroDoMapa(
    val busca: String = "",
    val rapido: String = "Todos",
    val categoria: String? = null,
    val subcategoria: String? = null,
    val diaInicial: Int? = null,
    val diaFinal: Int? = null,
    val entrada: String = "Todos",
    val somenteAcessivel: Boolean = false,
    val raioKm: Int? = null
) {
    val avancadoAtivo: Boolean
        get() = categoria != null || subcategoria != null || diaInicial != null ||
            diaFinal != null || entrada != "Todos" || somenteAcessivel || raioKm != null

    val temAlgum: Boolean
        get() = busca.isNotBlank() || rapido != "Todos" || avancadoAtivo

    fun semFiltroRapido(): FiltroDoMapa = copy(rapido = "Todos")

    fun semAvancado(): FiltroDoMapa = copy(
        categoria = null,
        subcategoria = null,
        diaInicial = null,
        diaFinal = null,
        entrada = "Todos",
        somenteAcessivel = false,
        raioKm = null
    )
}

/**
 * Eventos que passam nos critérios e têm coordenadas válidas.
 * [hojeDia] e [hojeMes] vêm do calendário de quem chama, para o filtro "Hoje".
 */
fun filtrarParaOMapa(
    eventos: List<EventModel>,
    filtro: FiltroDoMapa,
    hojeDia: Int,
    hojeMes: String
): List<EventModel> = eventos.filter { evento ->
    evento.temCoordenadas && evento.atende(filtro, hojeDia, hojeMes)
}

private fun EventModel.atende(filtro: FiltroDoMapa, hojeDia: Int, hojeMes: String): Boolean {
    val consulta = filtro.busca.trim()
    if (consulta.isNotEmpty()) {
        val encontrado = title.contains(consulta, ignoreCase = true) ||
            category.contains(consulta, ignoreCase = true) ||
            venueName.contains(consulta, ignoreCase = true)
        if (!encontrado) return false
    }

    val rapidoOk = when (filtro.rapido) {
        "Hoje" -> dayOfMonth == hojeDia && monthAbbr.equals(hojeMes, ignoreCase = true)
        "Fim de semana" -> diaDeFimDeSemana(dayOfWeek)
        "Grátis" -> isFree
        else -> true
    }
    if (!rapidoOk) return false

    if (!filtro.categoria.isNullOrBlank() && !categoriaCombina(category, filtro.categoria)) return false
    if (!filtro.subcategoria.isNullOrBlank() && !categoriaCombina(category, filtro.subcategoria)) return false

    if (filtro.diaInicial != null || filtro.diaFinal != null) {
        if (dayOfMonth !in 1..31) return false
        if (filtro.diaInicial != null && dayOfMonth < filtro.diaInicial) return false
        if (filtro.diaFinal != null && dayOfMonth > filtro.diaFinal) return false
    }

    val entradaOk = when (filtro.entrada) {
        "Gratuitos" -> isFree
        "Pagos" -> !isFree
        else -> true
    }
    if (!entradaOk) return false

    if (filtro.somenteAcessivel && !pareceAcessivel()) return false
    return true
}

private fun categoriaCombina(categoriaEvento: String, criterio: String): Boolean =
    categoriaEvento.contains(criterio, ignoreCase = true) ||
        criterio.contains(categoriaEvento, ignoreCase = true)

private fun diaDeFimDeSemana(dia: String): Boolean {
    val texto = dia.lowercase()
    return texto.contains("sáb") || texto.contains("sab") || texto.contains("dom")
}

private fun EventModel.pareceAcessivel(): Boolean {
    if (isAccessible) return true
    val texto = "$description $venueName $accessibilityInfo"
    return texto.contains("acess", ignoreCase = true) ||
        texto.contains("libras", ignoreCase = true) ||
        texto.contains("rampa", ignoreCase = true) ||
        texto.contains("adaptad", ignoreCase = true)
}
