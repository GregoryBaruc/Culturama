package com.example.culturama.data.auth

/**
 * Dados de identificação já presentes na sessão autenticada.
 * Campos ausentes ficam nulos: a tela não completa com texto inventado.
 */
data class ContaAutenticada(
    val nome: String?,
    val nomeDeUsuario: String?,
    val apresentacao: String?,
    val avatarUrl: String?,
    val email: String?
)
