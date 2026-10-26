package com.example.culturama.model

/**
 * Modelo de dados para imagens flutuantes da seção Hero.
 * Cada imagem pode estar associada a um evento ou ser puramente decorativa.
 */
data class FloatingImageModel(
    val id: String,
    val imageResId: Int? = null,
    val imageUrl: String? = null,
    val contentDescription: String,
    val associatedEventId: String? = null  // null = decorativa
)
