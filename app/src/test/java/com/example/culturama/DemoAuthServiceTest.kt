package com.example.culturama

import com.example.culturama.data.auth.AuthResult
import com.example.culturama.data.auth.DemoAuthService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para o fluxo de autenticação demonstrativo.
 */
class DemoAuthServiceTest {

    private lateinit var authService: DemoAuthService

    @Before
    fun setup() {
        authService = DemoAuthService()
    }

    @Test
    fun register_success_returnsDemoUser() = runBlocking {
        val result = authService.register("Ana Souza", "ana@culturama.com", "senha1234")
        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).data
        assertEquals("Ana Souza", user.name)
        assertEquals("ana@culturama.com", user.email)
        assertTrue(user.isDemo)
    }

    @Test
    fun login_afterRegister_retainsUserName() = runBlocking {
        authService.register("Carlos Santos", "carlos@culturama.com", "senha1234")
        val result = authService.login("carlos@culturama.com", "senha1234")
        assertTrue(result is AuthResult.Success)
        val user = (result as AuthResult.Success).data
        assertEquals("Carlos Santos", user.name)
        assertEquals(user, authService.getCurrentUser())
    }

    @Test
    fun loginWithGoogle_success() = runBlocking {
        val result = authService.loginWithGoogle()
        assertTrue(result is AuthResult.Success)
        assertNotNull(authService.getCurrentUser())
    }

    @Test
    fun logout_clearsCurrentUser() = runBlocking {
        authService.login("teste@culturama.com", "senha1234")
        assertNotNull(authService.getCurrentUser())
        authService.logout()
        assertNull(authService.getCurrentUser())
    }
}
