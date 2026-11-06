package com.example.culturama.model

/**
 * Evento cultural vindo da API do site. Os campos de detalhe sÃ³ sÃ£o
 * preenchidos depois de abrir o evento ([detailsLoaded]).
 */
data class EventModel(
    val id: String,
    val title: String,
    val category: String,
    val dayOfMonth: Int,
    val monthAbbr: String,
    val dayOfWeek: String,
    val time: String,
    val venueName: String,
    val isFree: Boolean,
    val price: String = "",
    val imageUrl: String? = null,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val description: String = "",
    val organizerName: String = "",
    val ticketUrl: String? = null,
    val moreInfoUrl: String? = null,
    val siteUrl: String? = null,
    val isAccessible: Boolean = false,
    val accessibilityInfo: String = "",
    val detailsLoaded: Boolean = false
)
