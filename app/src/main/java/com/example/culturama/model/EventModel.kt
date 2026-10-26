package com.example.culturama.model

/**
 * Modelo de dados de um evento cultural.
 * Estruturado para facilitar futura integração com API REST.
 */
data class EventModel(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val categoryColor: Long,       // ARGB color para a categoria
    val dayOfMonth: Int,
    val monthAbbr: String,
    val dayOfWeek: String,
    val time: String,
    val venueName: String,
    val address: String,
    val isFree: Boolean,
    val price: String = "",        // Ex: "R$ 40,00" ou vazio se gratuito
    val imageResId: Int? = null,   // Recurso drawable local (temporário)
    val imageUrl: String? = null,  // URL futura da API
    val organizerName: String = "",
    val isFavorited: Boolean = false
)
