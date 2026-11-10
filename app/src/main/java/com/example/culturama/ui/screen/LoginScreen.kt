package com.example.culturama.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.AuthResult
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.preference.UserPreferencesRepository
import com.example.culturama.data.validation.FormValidator
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tela de Login de Usuário Comum.
 *
 * Integração Etapa 3:
 * - Primeiro acesso: direciona para Seleção de Preferências (Screen.Preferences).
 * - Acessos seguintes (configuração já concluída): retorna diretamente à Tela Inicial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    initialEmail: String = "",
    destino: String = IntencaoDeAcesso.DESCOBERTA.id,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val context = LocalContext.current
    val preferencesRepository = remember { UserPreferencesRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // ── Estados dos campos ───────────────────────────────────────────────
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // ── Mensagens de validação ──────────────────────────────────────────
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    // ── Estados de processo e diálogos ──────────────────────────────────
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var successFeedback by remember { mutableStateOf<String?>(null) }

    fun navigateBackToHome() {
        val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
        if (!popped) {
            navController.navigate(Screen.Home.route) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    /**
     * Direciona o fluxo após login bem-sucedido:
     * - Se as preferências ainda não foram configuradas: abre a tela de Preferências.
     * - Se já foram configuradas: retorna à Tela Inicial sem duplicar a pilha.
     */
    val intencao = IntencaoDeAcesso.porId(destino)

    fun navigateAfterLogin() {
        when (intencao) {
            IntencaoDeAcesso.PERFIL -> navController.navigate(Screen.Profile.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
            IntencaoDeAcesso.ORGANIZADOR, IntencaoDeAcesso.EMPRESA ->
                navController.navigate(Screen.AreaDaConta.createRoute(intencao.id)) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            IntencaoDeAcesso.DESCOBERTA -> {
                if (!preferencesRepository.isPreferencesConfigured()) {
                    navController.navigate(Screen.Preferences.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navigateBackToHome()
                }
            }
        }
    }

    // ── Submissão do login ──────────────────────────────────────────────
    fun handleLogin() {
        focusManager.clearFocus()
        generalError = null

        val emailVal = FormValidator.validateEmail(email)
        val passVal = FormValidator.validatePassword(password, isSignUp = false)

        emailError = emailVal.errorMessage
        passwordError = passVal.errorMessage

        val isFormValid = emailVal.isValid && passVal.isValid
        if (!isFormValid) return

        isLoading = true
        coroutineScope.launch {
            val result = authService.login(email, password)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    successFeedback = "Bem-vindo de volta ao Culturama!"
                    delay(700)
                    navigateAfterLogin()
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
                // ── Barra Superior com Botão Voltar e Logo ───────────────────
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

                    // Logo "Culturama •"
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

                    // Acesso direto a criar conta
                    if (intencao == IntencaoDeAcesso.DESCOBERTA || intencao == IntencaoDeAcesso.PERFIL) {
                    TextButton(
                        onClick = {
                            navController.navigate(Screen.SignUp.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    ) {
                        Text(
                            text = "Criar conta",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = CulturamaOrange,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    } else {
                        Spacer(Modifier.size(40.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Banner da referência ─────────────────────────────────────
                if (intencao == IntencaoDeAcesso.DESCOBERTA || intencao == IntencaoDeAcesso.PERFIL) {
                    AuthHeroBanner(isSignUp = false)
                } else {
                    Text(
                        text = if (intencao == IntencaoDeAcesso.EMPRESA) {
                            "Patrocínio e inteligência cultural"
                        } else {
                            "Publicação de eventos"
                        },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "A sessão é a mesma da conta cultural. Este login não concede o papel.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Card de Login ────────────────────────────────────────────
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
                            text = when (intencao) {
                                IntencaoDeAcesso.ORGANIZADOR -> "Entrar como organizador"
                                IntencaoDeAcesso.EMPRESA -> "Entrar como empresa"
                                IntencaoDeAcesso.PERFIL -> "Entrar no perfil"
                                IntencaoDeAcesso.DESCOBERTA -> "Entrar"
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        )

                        // Subtítulo
                        Text(
                            text = when (intencao) {
                                IntencaoDeAcesso.ORGANIZADOR ->
                                    "Depois do login, a área abre só se esta conta já for organizador autorizado."
                                IntencaoDeAcesso.EMPRESA ->
                                    "Depois do login, o Insights abre só se a empresa desta conta já estiver aprovada."
                                else -> "Acesse sua conta para descobrir os eventos da sua cidade."
                            },
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
                                        is AuthResult.Success -> navigateAfterLogin()
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

                        // ── Campo: Senha ─────────────────────────────────────
                        CulturamaTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                if (passwordError != null) passwordError = null
                            },
                            label = "Senha",
                            placeholder = "••••••••",
                            errorMessage = passwordError,
                            leadingIcon = CulturamaIcons.Lock,
                            isPassword = true,
                            isPasswordVisible = isPasswordVisible,
                            onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { handleLogin() }
                            )
                        )

                        // ── Link: "Esqueci minha senha" ──────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Esqueci minha senha",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 12.5.sp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showForgotPasswordDialog = true }
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }

                        // Feedback de erro geral
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

                        // Feedback de sucesso
                        AnimatedVisibility(
                            visible = !successFeedback.isNullOrBlank(),
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
                                        text = successFeedback.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFB8F5CD),
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // ── Botão Principal "Entrar" ──────────────────────────
                        CulturamaPrimaryButton(
                            text = "Entrar",
                            onClick = { handleLogin() },
                            isLoading = isLoading,
                            enabled = !isLoading
                        )

                        Spacer(Modifier.height(20.dp))

                        // ── Link: "Ainda não tem uma conta? Criar conta" ────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ainda não tem uma conta? ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                            Text(
                                text = "Criar conta",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = CulturamaOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        navController.navigate(Screen.SignUp.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
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

    // ── Diálogo Provisório de "Esqueci minha senha" ───────────────────────────
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text(text = "Entendi", color = CulturamaOrange, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "Recuperação de Senha",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = "Esta funcionalidade será disponibilizada futuramente com o envio de link seguro de redefinição de senha para o seu e-mail cadastrado.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            },
            containerColor = Color(0xFF1C1C22),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
