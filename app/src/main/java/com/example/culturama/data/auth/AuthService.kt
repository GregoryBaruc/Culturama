package com.example.culturama.data.auth

import com.example.culturama.model.UserModel

/**
 * Interface que define o contrato do serviço de autenticação.
 *
 * Desacopla a interface com o usuário da camada de dados.
 * Futuramente, esta interface será implementada por uma classe que se conecta
 * à API REST do Culturama, sem necessidade de reescrever as telas de cadastro e login.
 */
interface AuthService {
    suspend fun register(name: String, email: String, password: String): AuthResult<UserModel>
    suspend fun login(email: String, password: String): AuthResult<UserModel>
    suspend fun loginWithGoogle(): AuthResult<UserModel>
    fun getCurrentUser(): UserModel?
    fun logout()
}
