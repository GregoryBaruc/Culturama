package com.example.culturama.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Tamanhos variados para as bolinhas de categoria,
 * reproduzindo a composição orgânica da imagem de referência.
 */
enum class BubbleSize(val baseDp: Int, val textSp: Int, val iconDp: Int) {
    SMALL(baseDp = 86, textSp = 11, iconDp = 20),
    MEDIUM(baseDp = 102, textSp = 12, iconDp = 24),
    LARGE(baseDp = 122, textSp = 13, iconDp = 28)
}

/**
 * Modelo de categoria cultural para seleção de preferências.
 *
 * @param id Identificador único (ex: "musica", "samba")
 * @param name Nome exibido na bolinha (ex: "Música", "Feiras e gastronomia")
 * @param icon Ícone vetorial da categoria
 * @param sizeType Tamanho base visual da bolinha
 * @param emoji Fallback visual cultural
 */
data class CategoryPreference(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val sizeType: BubbleSize = BubbleSize.MEDIUM,
    val emoji: String = "✨"
)

/**
 * Estrutura preparada para envio futuro à API REST do Culturama.
 */
data class UserPreferencesPayload(
    val userId: String,
    val selectedCategoryIds: List<String>,
    val configuredAtMillis: Long = System.currentTimeMillis()
)
