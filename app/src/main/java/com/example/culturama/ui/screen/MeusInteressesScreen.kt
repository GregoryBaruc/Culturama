package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.profile.GrupoCultural
import com.example.culturama.data.profile.Interesses
import com.example.culturama.data.profile.PerfilRepository
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaPrimaryButton
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Escolha de categorias culturais já publicadas no catálogo do site. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeusInteressesScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val scope = rememberCoroutineScope()
    var carregando by remember { mutableStateOf(true) }
    var salvando by remember { mutableStateOf(false) }
    var podeSalvar by remember { mutableStateOf(false) }
    var grupos by remember { mutableStateOf<List<GrupoCultural>>(emptyList()) }
    var selecionados by remember { mutableStateOf<Set<String>>(emptySet()) }
    var mensagem by remember { mutableStateOf<String?>(null) }
    var mensagemEhErro by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        val catalogo = PerfilRepository.catalogo()
        val cadastro = PerfilRepository.lerCadastro()
        val lista = catalogo.getOrNull()
        val conta = cadastro.getOrNull()
        if (lista.isNullOrEmpty() || conta == null) {
            mensagem = catalogo.exceptionOrNull()?.message
                ?: cadastro.exceptionOrNull()?.message
                ?: "Não foi possível carregar os interesses."
            mensagemEhErro = true
        } else {
            grupos = lista
            selecionados = Interesses.selecionados(conta.interesses, lista)
            podeSalvar = true
        }
        carregando = false
    }

    val quantidade = selecionados.size
    val contador = when (quantidade) {
        0 -> "Nenhum interesse selecionado"
        1 -> "1 interesse selecionado"
        else -> "$quantidade interesses selecionados"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
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
                text = "Meus interesses",
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
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Escolha o que você curte para descobrir eventos com a sua cara.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            grupos.forEach { grupo ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CulturamaCard)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = grupo.nome,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        grupo.opcoes.forEach { opcao ->
                            val escolhida = opcao.id in selecionados
                            FilterChip(
                                selected = escolhida,
                                onClick = {
                                    if (!podeSalvar || salvando) return@FilterChip
                                    selecionados = if (escolhida) {
                                        selecionados - opcao.id
                                    } else {
                                        selecionados + opcao.id
                                    }
                                    if (mensagemEhErro) mensagem = null
                                },
                                label = {
                                    Text(text = opcao.nome)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = CulturamaBackground,
                                    labelColor = TextPrimary,
                                    selectedContainerColor = CulturamaOrange,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = escolhida,
                                    borderColor = Color(0xFF2C2C34),
                                    selectedBorderColor = CulturamaOrange
                                )
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = contador,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (quantidade == 0) TextSecondary else CulturamaOrange,
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (quantidade == 0 && podeSalvar) {
                Text(
                    text = "Nenhum interesse selecionado. Você pode salvar assim mesmo, ou escolher algumas cenas antes.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
            mensagem?.let { texto ->
                Text(
                    text = texto,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (mensagemEhErro) MaterialTheme.colorScheme.error else CulturamaOrange
                    )
                )
            }
            CulturamaPrimaryButton(
                text = "Salvar interesses",
                isLoading = salvando,
                enabled = podeSalvar,
                onClick = {
                    val tokens = Interesses.tokensParaGravar(selecionados, grupos)
                    val escolhaAtual = selecionados
                    scope.launch {
                        salvando = true
                        mensagem = null
                        PerfilRepository.salvarInteresses(tokens)
                            .onSuccess { gravados ->
                                selecionados = Interesses.selecionados(gravados, grupos)
                                mensagem = "Interesses salvos."
                                mensagemEhErro = false
                            }
                            .onFailure { falha ->
                                selecionados = escolhaAtual
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
