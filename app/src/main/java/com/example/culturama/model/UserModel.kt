package com.example.culturama.model

/**
 * Representa um usuário no aplicativo Culturama.
 * Nesta etapa demonstrativa, mantém os dados de sessão sem persistência de senha.
 */
data class UserModel(
    val name: String,
    val email: String,
    val isDemo: Boolean = true
)
