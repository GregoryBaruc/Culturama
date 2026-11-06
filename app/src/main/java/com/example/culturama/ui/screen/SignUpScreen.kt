package com.example.culturama.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val PRIVACY_URL = "https://culturama-iota-two.vercel.app/privacidade"

/** Mostra os dígitos digitados como DD/MM/AAAA. */
internal val BirthDateTransformation = VisualTransformation { texto ->
    val digitos = texto.text
    val formatado = buildString {
        digitos.forEachIndexed { indice, digito ->
            if (indice == 2 || indice == 4) append('/')
            append(digito)
        }
    }
    val mapeamento = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = when {
            offset <= 2 -> offset
            offset <= 4 -> offset + 1
            else -> offset + 2
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= 2 -> offset
            offset <= 5 -> offset - 1
            else -> offset - 2
        }.coerceAtMost(digitos.length)
    }
    TransformedText(AnnotatedString(formatado), mapeamento)
}

/**
 * Tela de Cadastro de Usuário Comum.
 *
 * Reproduz fielmente a identidade visual da imagem de referência:
 * - Fundo escuro com gradientes sutis em marrom/terracota.
 * - Banner "Seu próximo rolê começa aqui" com benefícios e mini cards.
 * - Card de formulário elegante e arredondado.
 * - Campos: Nome completo, E-mail, Data de nascimento, Senha e Confirmar senha.
 * - Validações amigáveis e contextualizadas abaixo de cada campo.
 * - Cria a conta pela API do site e entra pelo SDK do Supabase em seguida.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

    // ── Estados dos campos ───────────────────────────────────────────────
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // só os dígitos: DDMMAAAA
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    // ── Estados de visibilidade de senha ─────────────────────────────────
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // ── Mensagens de erro por campo ─────────────────────────────────────
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // ── Estado do processo ──────────────────────────────────────────────
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // ── Função de submissão do formulário ────────────────────────────────
    fun handleSubmit() {
        focusManager.clearFocus()
        generalError = null

        val nameVal = FormValidator.validateFullName(name)
        val emailVal = FormValidator.validateEmail(email)
        val birthVal = FormValidator.validateBirthDate(birthDate)
        val passVal = FormValidator.validatePassword(password, isSignUp = true)
        val confirmVal = FormValidator.validatePasswordConfirmation(password, confirmPassword)

        nameError = nameVal.errorMessage
        emailError = emailVal.errorMessage
        birthDateError = birthVal.errorMessage
        passwordError = passVal.errorMessage
        confirmPasswordError = confirmVal.errorMessage

        if (!termsAccepted) {
            generalError = "É necessário aceitar os termos para prosseguir."
            return
        }

        val isFormValid = nameVal.isValid && emailVal.isValid && birthVal.isValid &&
            passVal.isValid && confirmVal.isValid
        val birthDateIso = FormValidator.birthDateToIso(birthDate)
        if (!isFormValid || birthDateIso == null) return

        isLoading = true
        coroutineScope.launch {
            val result = authService.register(name, email, password, birthDateIso, termsAccepted)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    if (result.data) {
                        successMessage = "Conta criada! Bem-vindo ao Culturama."
                        delay(700)
                        navController.navigate(Screen.Preferences.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    } else {
                        // Conta criada, mas ainda sem confirmar o e-mail: segue pela tela de login
                        successMessage = "Conta criada! Abra o link que enviamos para o seu e-mail e depois entre aqui."
                        delay(3000)
                        val targetRoute = Screen.Login.createRoute(registeredEmail = email.trim())
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                }
                is AuthResult.Error -> {
                    generalError = result.message
                }
                else -> Unit
            }
        }
    }

    Scaffold(
        containerColor = CulturamaBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to HeroGradientTop.copy(alpha = 0.5f),
                            0.25f to HeroGradientMid.copy(alpha = 0.35f),
                            0.55f to Color(0xFF140E11),
                            1.0f to CulturamaBackground
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // ── Barra Superior com Logo e Botão Voltar ───────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }

                    // Logo "Culturama •" conforme a referência
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Culturama",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, top = 2.dp)
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(CulturamaOrange)
                        )
                    }

                    // Botão alternativo rápido para ir ao Login
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.SignUp.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text(
                            text = "Entrar",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Banner visual da referência ──────────────────────────────
                AuthHeroBanner(isSignUp = true)

                Spacer(Modifier.height(16.dp))

                // ── Card de Cadastro ─────────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF16161A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B2B33)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Título do Card
                        Text(
                            text = "Criar conta",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )

                        // Subtítulo da referência
                        Text(
                            text = "Cadastro só para maiores de 18 anos.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                        )

                        // Botão Google
                        GoogleSignInButton(
                            onClick = {
                                coroutineScope.launch {
                                    generalError = null
                                    isLoading = true
                                    val res = authService.loginWithGoogle(context)
                                    isLoading = false
                                    when (res) {
                                        is AuthResult.Success -> navController.navigate(Screen.Preferences.route) {
                                            popUpTo(Screen.SignUp.route) { inclusive = true }
                                        }
                                        is AuthResult.Error -> generalError = res.message
                                        else -> Unit
                                    }
                                }
                            },
                            enabled = !isLoading
                        )

                        Spacer(Modifier.height(18.dp))

                        // Divisor "ou com e-mail"
                        AuthOrDivider()

                        Spacer(Modifier.height(18.dp))

                        // ── Campo: Nome Completo ─────────────────────────────
                        CulturamaTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (nameError != null) nameError = null
                            },
                            label = "Nome",
                            placeholder = "Seu nome e sobrenome",
                            errorMessage = nameError,
                            leadingIcon = CulturamaIcons.Person,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        // ── Campo: E-mail ────────────────────────────────────
                        CulturamaTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                if (emailError != null) emailError = null
                            },
                            label = "E-mail",
                            placeholder = "seu.email@exemplo.com",
                            errorMessage = emailError,
                            leadingIcon = CulturamaIcons.Email,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        // ── Campo: Data de nascimento ────────────────────────
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            inputTransformation = BirthDateTransformation
                        )

                        Spacer(Modifier.height(14.dp))

                        // ── Campo: Senha ─────────────────────────────────────
                        CulturamaTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) passwordError = null
                            },
                            label = "Senha",
                            placeholder = "••••••••",
                            helperText = "Mínimo de 8 caracteres.",
                            errorMessage = passwordError,
                            leadingIcon = CulturamaIcons.Lock,
                            isPassword = true,
                            isPasswordVisible = isPasswordVisible,
                            onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        // ── Campo: Confirmar Senha ───────────────────────────
                        CulturamaTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                if (confirmPasswordError != null) confirmPasswordError = null
                            },
                            label = "Confirmar senha",
                            placeholder = "••••••••",
                            errorMessage = confirmPasswordError,
                            leadingIcon = CulturamaIcons.Lock,
                            isPassword = true,
                            isPasswordVisible = isConfirmPasswordVisible,
                            onPasswordVisibilityToggle = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleSubmit() }
                            )
                        )

                        Spacer(Modifier.height(14.dp))

                        // ── Checkbox: Termos e Política de Privacidade ───────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { termsAccepted = !termsAccepted }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = CulturamaOrange,
                                    uncheckedColor = TextTertiary,
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Li e aceito a ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.5.sp
                                )
                            )
                            Text(
                                text = "Política de Privacidade",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CulturamaOrange,
                                    fontSize = 12.5.sp,
                                    textDecoration = TextDecoration.Underline
                                ),
                                modifier = Modifier.clickable {
                                    runCatching { uriHandler.openUri(PRIVACY_URL) }
                                }
                            )
                        }

                        // Alerta de erro geral (se houver)
                        AnimatedVisibility(
                            visible = !generalError.isNullOrBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF381518),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7A2026))
                            ) {
                                Text(
                                    text = generalError.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFFF9E9E),
                                        fontSize = 12.5.sp
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Alerta de sucesso visual
                        AnimatedVisibility(
                            visible = !successMessage.isNullOrBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF132F1A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF276D38))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = CulturamaIcons.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = successMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFB8F5CD),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ── Botão Principal "Criar conta" ────────────────────
                        CulturamaPrimaryButton(
                            text = "Criar conta",
                            onClick = { handleSubmit() },
                            isLoading = isLoading,
                            enabled = !isLoading
                        )

                        Spacer(Modifier.height(20.dp))

                        // ── Link inferior: "Já tem conta? Entrar" ───────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Já tem uma conta? ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                            Text(
                                text = "Entrar",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CulturamaOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.SignUp.route) { inclusive = true }
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                    }
                }
            }
        }
    }
}
