package com.example.culturama.data.validation

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
     * Nome de exibição do perfil. Aceita um único nome: o cadastro do site
     * guarda esse texto na coluna `nome`, sem exigir sobrenome.
     */
    fun validateDisplayName(name: String): FieldValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> FieldValidationResult(
                isValid = false,
                errorMessage = "Digite o nome de exibição."
            )
            trimmed.length < 2 -> FieldValidationResult(
                isValid = false,
                errorMessage = "O nome precisa ter pelo menos 2 caracteres."
            )
            trimmed.length > 80 -> FieldValidationResult(
                isValid = false,
                errorMessage = "O nome pode ter no máximo 80 caracteres."
            )
            trimmed.any { it.isISOControl() } -> FieldValidationResult(
                isValid = false,
                errorMessage = "O nome não pode ter quebras de linha."
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
     * Valida a data de nascimento digitada só com números (DDMMAAAA).
     * O site confere de novo a idade mínima de 18 anos.
     */
    fun validateBirthDate(digits: String): FieldValidationResult {
        val nascimento = parseBirthDate(digits) ?: return FieldValidationResult(
            isValid = false,
            errorMessage = "Digite uma data válida no formato DD/MM/AAAA."
        )
        val limite = Calendar.getInstance().apply { add(Calendar.YEAR, -18) }
        return if (nascimento.after(limite)) {
            FieldValidationResult(
                isValid = false,
                errorMessage = "O cadastro é só para maiores de 18 anos."
            )
        } else {
            FieldValidationResult(isValid = true)
        }
    }

    /** Converte DDMMAAAA para o formato AAAA-MM-DD que a API espera. */
    fun birthDateToIso(digits: String): String? = parseBirthDate(digits)?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it.time)
    }

    private fun parseBirthDate(digits: String): Calendar? {
        if (digits.length != 8) return null
        return try {
            val data = SimpleDateFormat("ddMMyyyy", Locale.US).apply { isLenient = false }.parse(digits)
            data?.let { Calendar.getInstance().apply { time = it } }
                ?.takeIf { it.get(Calendar.YEAR) >= 1900 && it.before(Calendar.getInstance()) }
        } catch (e: ParseException) {
            null
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
