package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.profile.PerfilRepository
import com.example.culturama.data.validation.FormValidator
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaPrimaryButton
import com.example.culturama.ui.component.CulturamaTextField
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Edição do nome de exibição da conta autenticada.
 * Usuário, biografia e foto não têm coluna editável no cadastro atual.
 */
@Composable
fun EditProfileScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val scope = rememberCoroutineScope()
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var podeSalvar by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var erroNome by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var usuario by remember { mutableStateOf<String?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var mensagem by remember { mutableStateOf<String?>(null) }
    var mensagemEhErro by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val sessao = authService.contaAtual()
        if (sessao == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        email = sessao.email
        usuario = sessao.nomeDeUsuario
        avatarUrl = sessao.avatarUrl
        PerfilRepository.lerCadastro()
            .onSuccess { cadastro ->
                nome = cadastro.nome
                podeSalvar = true
            }
            .onFailure { falha ->
                mensagem = falha.message
                mensagemEhErro = true
            }
        carregando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Editar perfil",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (carregando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CulturamaOrange)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(CulturamaCard)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FotoAtual(url = avatarUrl, nome = nome)
                Text(
                    text = "A foto atual continua a mesma. A troca de imagem ainda não está disponível neste aplicativo.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            CulturamaTextField(
                value = nome,
                onValueChange = { digitado ->
                    nome = digitado.take(80)
                    if (erroNome != null) erroNome = null
                    if (mensagemEhErro) mensagem = null
                },
                label = "Nome de exibição",
                placeholder = "Como você quer ser chamado",
                errorMessage = erroNome,
                enabled = podeSalvar && !salvando
            )

            DadoFixo(
                rotulo = "Nome de usuário",
                valor = usuario?.let { "@$it" },
                aviso = if (usuario.isNullOrBlank()) {
                    "Este cadastro não tem nome de usuário."
                } else {
                    "O nome de usuário não pode ser alterado por aqui."
                }
            )

            DadoFixo(
                rotulo = "E-mail",
                valor = email,
                aviso = "O e-mail da conta não é alterado nesta tela."
            )

            Text(
                text = "A biografia ainda não faz parte do cadastro, então não há campo para editá-la.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            mensagem?.let { texto ->
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (mensagemEhErro) MaterialTheme.colorScheme.error else CulturamaOrange
                    )
                )
            }

            CulturamaPrimaryButton(
                text = "Salvar alterações",
                isLoading = salvando,
                enabled = podeSalvar,
                onClick = {
                    val validacao = FormValidator.validateDisplayName(nome)
                    erroNome = validacao.errorMessage
                    if (!validacao.isValid) return@CulturamaPrimaryButton
                    val nomeLimpo = nome.trim()
                    scope.launch {
                        salvando = true
                        mensagem = null
                        PerfilRepository.salvarNome(nomeLimpo)
                            .onSuccess {
                                nome = nomeLimpo
                                mensagem = "Alterações salvas."
                                mensagemEhErro = false
                            }
                            .onFailure { falha ->
                                mensagem = falha.message ?: "Não foi possível salvar. Tente de novo."
                                mensagemEhErro = true
                            }
                        salvando = false
                    }
                }
            )
        }
    }
}

@Composable
private fun FotoAtual(url: String?, nome: String) {
    var imagemFalhou by remember(url) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(CulturamaOrange.copy(alpha = 0.22f))
            .border(2.dp, CulturamaOrange, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank() || imagemFalhou) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = if (nome.isBlank()) "Avatar padrão" else "Foto de $nome indisponível",
                tint = TextPrimary,
                modifier = Modifier.size(48.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = "Foto do perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imagemFalhou = true }
            )
        }
    }
}

@Composable
private fun DadoFixo(rotulo: String, valor: String?, aviso: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
        )
        if (!valor.isNullOrBlank()) {
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Text(
            text = aviso,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
    }
}
