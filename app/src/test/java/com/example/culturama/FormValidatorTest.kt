package com.example.culturama

import com.example.culturama.data.validation.FormValidator
import org.junit.Assert.*
import org.junit.Test

/**
 * Testes unitários para as regras de validação dos formulários de cadastro e login.
 */
class FormValidatorTest {

    // ─── Testes de Nome ──────────────────────────────────────────────────────────

    @Test
    fun validateFullName_empty_returnsError() {
        val res = FormValidator.validateFullName("")
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun validateFullName_tooShort_returnsError() {
        val res = FormValidator.validateFullName("Ab")
        assertFalse(res.isValid)
        assertTrue(res.errorMessage!!.contains("3 caracteres"))
    }

    @Test
    fun validateFullName_noLastName_returnsError() {
        val res = FormValidator.validateFullName("Carlos")
        assertFalse(res.isValid)
        assertTrue(res.errorMessage!!.contains("sobrenome"))
    }

    @Test
    fun validateFullName_valid_returnsSuccess() {
        val res = FormValidator.validateFullName("Carlos Silva")
        assertTrue(res.isValid)
        assertNull(res.errorMessage)
    }

    // ─── Testes de E-mail ────────────────────────────────────────────────────────

    @Test
    fun validateEmail_empty_returnsError() {
        val res = FormValidator.validateEmail("")
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun validateEmail_invalidFormat_returnsError() {
        val invalidEmails = listOf("usuario", "usuario@", "usuario@site", "usuario@.com", "@dominio.com")
        for (email in invalidEmails) {
            val res = FormValidator.validateEmail(email)
            assertFalse("Deveria falhar para $email", res.isValid)
            assertNotNull(res.errorMessage)
        }
    }

    @Test
    fun validateEmail_valid_returnsSuccess() {
        val validEmails = listOf("usuario@email.com", "contato@culturama.br", "maria.clara@gmail.com")
        for (email in validEmails) {
            val res = FormValidator.validateEmail(email)
            assertTrue("Deveria ser válido para $email", res.isValid)
            assertNull(res.errorMessage)
        }
    }

    // ─── Testes de Senha ─────────────────────────────────────────────────────────

    @Test
    fun validatePassword_empty_returnsError() {
        val res = FormValidator.validatePassword("")
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun validatePassword_signUpLessThan8Chars_returnsError() {
        val res = FormValidator.validatePassword("1234567", isSignUp = true)
        assertFalse(res.isValid)
        assertTrue(res.errorMessage!!.contains("8 caracteres"))
    }

    @Test
    fun validatePassword_signUp8CharsOrMore_returnsSuccess() {
        val res = FormValidator.validatePassword("12345678", isSignUp = true)
        assertTrue(res.isValid)
        assertNull(res.errorMessage)
    }

    @Test
    fun validatePassword_loginValid_returnsSuccess() {
        val res = FormValidator.validatePassword("senha123", isSignUp = false)
        assertTrue(res.isValid)
        assertNull(res.errorMessage)
    }

    // ─── Testes de Confirmação de Senha ──────────────────────────────────────────

    @Test
    fun validatePasswordConfirmation_empty_returnsError() {
        val res = FormValidator.validatePasswordConfirmation("senha1234", "")
        assertFalse(res.isValid)
        assertNotNull(res.errorMessage)
    }

    @Test
    fun validatePasswordConfirmation_mismatch_returnsError() {
        val res = FormValidator.validatePasswordConfirmation("senha1234", "senha9999")
        assertFalse(res.isValid)
        assertTrue(res.errorMessage!!.contains("não coincidem"))
    }

    @Test
    fun validatePasswordConfirmation_match_returnsSuccess() {
        val res = FormValidator.validatePasswordConfirmation("culturama2026", "culturama2026")
        assertTrue(res.isValid)
        assertNull(res.errorMessage)
    }
}
