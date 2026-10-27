package com.example.culturama.data.auth

import com.example.culturama.model.UserModel
import kotlinx.coroutines.delay

/**
 * Implementação demonstrativa em memória do serviço de autenticação.
 *
 * NOTA DE SEGURANÇA E ARQUITETURA (TCC ADS):
 * - Esta classe funciona exclusivamente em memória durante o ciclo de execução do app.
 * - NENHUMA senha é armazenada ou persistida (nem em SharedPreferences, nem em banco de dados).
 * - Nenhuma credencial secreta ou fixa é utilizada.
 * - Nenhuma requisição externa para servidores é efetuada nesta etapa.
 * - Na próxima etapa do projeto, esta implementação será substituída por uma classe
 *   conectada à API REST oficial do Culturama.
 */
class DemoAuthService : AuthService {

    // Guarda apenas nomes associados aos e-mails cadastrados na sessão atual (SEM SENHAS).
    private val sessionRegisteredUsers = mutableMapOf<String, String>()

    // Usuário autenticado na sessão ativa
    private var currentUser: UserModel? = null

    override suspend fun register(name: String, email: String, password: String): AuthResult<UserModel> {
        // Simulação de processamento para exibição de feedback visual
        delay(700)

        val cleanEmail = email.trim().lowercase()
        val cleanName = name.trim()

        // Registra apenas o nome associado ao e-mail para a sessão atual demonstrativa
        sessionRegisteredUsers[cleanEmail] = cleanName

        val user = UserModel(
            name = cleanName,
            email = cleanEmail,
            isDemo = true
        )

        return AuthResult.Success(
            data = user,
            message = "Conta demonstrativa criada com sucesso!"
        )
    }

    override suspend fun login(email: String, password: String): AuthResult<UserModel> {
        // Simulação de processamento para exibição de feedback visual
        delay(700)

        val cleanEmail = email.trim().lowercase()
        // Se o usuário foi cadastrado na sessão, recupera seu nome; caso contrário, formata a partir do e-mail
        val userName = sessionRegisteredUsers[cleanEmail] ?: cleanEmail.substringBefore('@')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        val user = UserModel(
            name = userName,
            email = cleanEmail,
            isDemo = true
        )

        currentUser = user

        return AuthResult.Success(
            data = user,
            message = "Acesso demonstrativo realizado com sucesso!"
        )
    }

    override suspend fun loginWithGoogle(): AuthResult<UserModel> {
        delay(600)
        val user = UserModel(
            name = "Usuário Culturama",
            email = "usuario@culturama.demo",
            isDemo = true
        )
        currentUser = user
        return AuthResult.Success(
            data = user,
            message = "Acesso demonstrativo via Google realizado!"
        )
    }

    override fun getCurrentUser(): UserModel? = currentUser

    override fun logout() {
        currentUser = null
    }

    companion object {
        val instance: DemoAuthService by lazy { DemoAuthService() }
    }
}
