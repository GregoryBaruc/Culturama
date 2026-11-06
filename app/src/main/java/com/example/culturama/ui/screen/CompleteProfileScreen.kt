package com.example.culturama.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.auth.AuthResult
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.validation.FormValidator
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Conta nova pelo Google não informa idade nem aceita a Política de Privacidade no login.
 * Como no site, a pessoa só segue no app depois de completar esses dois itens.
 */
@Composable
fun CompleteProfileScreen(navController: NavController) {
    val authService = remember { SupabaseAuthService.instance }
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var birthDate by remember { mutableStateOf("") } // só os dígitos: DDMMAAAA
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var termsAccepted by remember { mutableStateOf(false) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Voltar não pula esta etapa: quem não quiser completar usa "Sair da conta".
    BackHandler {}

    fun handleSubmit() {
        generalError = null
        val validacao = FormValidator.validateBirthDate(birthDate)
        birthDateError = validacao.errorMessage
        if (!termsAccepted) generalError = "É necessário aceitar a Política de Privacidade para prosseguir."
        val birthDateIso = FormValidator.birthDateToIso(birthDate)
        if (!validacao.isValid || !termsAccepted || birthDateIso == null) return

        scope.launch {
            isLoading = true
            when (val result = authService.completeProfile(birthDateIso, termsAccepted)) {
                is AuthResult.Error -> generalError = result.message
                else -> navController.popBackStack()
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Complete seu cadastro",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "O Culturama é para maiores de 18 anos. Confirme sua data de nascimento e aceite a Política de Privacidade para continuar.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        CulturamaTextField(
            value = birthDate,
            onValueChange = { digitado ->
                birthDate = digitado.filter { it.isDigit() }.take(8)
                if (birthDateError != null) birthDateError = null
            },
            label = "Data de nascimento",
            placeholder = "DD/MM/AAAA",
            errorMessage = birthDateError,
            leadingIcon = CulturamaIcons.CalendarMonth,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            inputTransformation = BirthDateTransformation
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) { termsAccepted = !termsAccepted }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                enabled = !isLoading,
                colors = CheckboxDefaults.colors(
                    checkedColor = CulturamaOrange,
                    uncheckedColor = TextTertiary
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Li e aceito a ",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.5.sp)
            )
            Text(
                text = "Política de Privacidade",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CulturamaOrange,
                    fontSize = 12.5.sp,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.clickable { runCatching { uriHandler.openUri(PRIVACY_URL) } }
            )
        }

        generalError?.let { mensagem ->
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error)
            )
        }

        CulturamaPrimaryButton(
            text = "Continuar",
            onClick = { handleSubmit() },
            isLoading = isLoading
        )

        TextButton(
            onClick = {
                scope.launch {
                    authService.logout()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Sair da conta", color = TextSecondary)
        }
    }
}
