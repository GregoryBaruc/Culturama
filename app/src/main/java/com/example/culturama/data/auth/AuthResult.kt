package com.example.culturama.data.auth

/**
 * Estados do fluxo de autenticação para controle de UI e feedback visual.
 */
sealed interface AuthResult<out T> {
    data class Success<T>(val data: T, val message: String = "") : AuthResult<T>
    data class Error(val message: String) : AuthResult<Nothing>
    object Loading : AuthResult<Nothing>
    object Idle : AuthResult<Nothing>
}
