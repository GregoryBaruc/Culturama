package com.example.culturama.data.validation

/**
 * Resultado da validação de um campo específico.
 */
data class FieldValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Utilitário central de validação de formulários de cadastro e login.
 *
 * As regras são desacopladas da interface com o usuário para permitir testes unitários
 * e fácil manutenção ou adição de novas regras exigidas pela API.
 */
object FormValidator {

    private val EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    /**
     * Valida o nome completo do usuário.
     * Deve ser preenchido e conter pelo menos nome e sobrenome básicos (mínimo 3 caracteres).
     */
    fun validateFullName(name: String): FieldValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> FieldValidationResult(
                isValid = false,
                errorMessage = "Por favor, digite seu nome completo."
            )
            trimmed.length < 3 -> FieldValidationResult(
                isValid = false,
                errorMessage = "O nome deve ter pelo menos 3 caracteres."
            )
            !trimmed.contains(" ") -> FieldValidationResult(
                isValid = false,
                errorMessage = "Digite seu nome e sobrenome."
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Valida o formato de e-mail.
     */
    fun validateEmail(email: String): FieldValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> FieldValidationResult(
                isValid = false,
                errorMessage = "Por favor, digite seu e-mail."
            )
            !trimmed.matches(EMAIL_PATTERN) -> FieldValidationResult(
                isValid = false,
                errorMessage = "Digite um e-mail válido (ex: seu.nome@exemplo.com)."
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Valida o campo de senha.
     * @param isSignUp se for cadastro, exige a regra da referência ("Mínimo de 8 caracteres").
     */
    fun validatePassword(password: String, isSignUp: Boolean = false): FieldValidationResult {
        return when {
            password.isEmpty() -> FieldValidationResult(
                isValid = false,
                errorMessage = "Por favor, digite sua senha."
            )
            isSignUp && password.length < 8 -> FieldValidationResult(
                isValid = false,
                errorMessage = "A senha deve ter no mínimo 8 caracteres."
            )
            else -> FieldValidationResult(isValid = true)
        }
    }

    /**
     * Valida se a confirmação de senha é idêntica à senha digitada.
     */
    fun validatePasswordConfirmation(password: String, confirmation: String): FieldValidationResult {
        return when {
            confirmation.isEmpty() -> FieldValidationResult(
                isValid = false,
                errorMessage = "Por favor, confirme sua senha."
            )
            password != confirmation -> FieldValidationResult(
                isValid = false,
                errorMessage = "As senhas não coincidem. Verifique a digitação."
            )
            else -> FieldValidationResult(isValid = true)
        }
    }
}
