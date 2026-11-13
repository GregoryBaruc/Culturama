package com.example.culturama.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.profile.GrupoCultural
import com.example.culturama.data.profile.Interesses
import com.example.culturama.data.profile.OpcaoCultural
import com.example.culturama.data.profile.PerfilRepository
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.component.CulturamaPrimaryButton
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.max

/** Quantidade de chips visíveis antes de pedir "Ver mais". */
private const val LIMITE_CHIPS_COLAPSADOS = 8

/** Escolha de categorias culturais já publicadas no catálogo do site. */
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
    var gruposExpandidos by remember { mutableStateOf<Set<String>>(emptySet()) }

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
                .padding(horizontal = 4.dp, vertical = 2.dp),
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
                .padding(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Escolha o que você curte para descobrir eventos com a sua cara.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                modifier = Modifier.padding(bottom = 2.dp)
            )
            grupos.forEach { grupo ->
                val expandido = grupo.id in gruposExpandidos
                val precisaColapsar = grupo.opcoes.size > LIMITE_CHIPS_COLAPSADOS
                val opcoesVisiveis = opcoesVisiveisDoGrupo(
                    opcoes = grupo.opcoes,
                    selecionados = selecionados,
                    expandido = expandido || !precisaColapsar
                )
                val ocultas = (grupo.opcoes.size - opcoesVisiveis.size).coerceAtLeast(0)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CulturamaCard)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF2A2A32),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = iconeDoGrupo(grupo.slug, grupo.nome),
                            contentDescription = null,
                            tint = CulturamaOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = grupo.nome,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        val qtdNoGrupo = grupo.opcoes.count { it.id in selecionados }
                        if (qtdNoGrupo > 0) {
                            Text(
                                text = "$qtdNoGrupo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = CulturamaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    FluxoDeChips(
                        modifier = Modifier.fillMaxWidth(),
                        espacoHorizontal = 8.dp,
                        espacoVertical = 8.dp
                    ) {
                        opcoesVisiveis.forEach { opcao ->
                            val escolhida = opcao.id in selecionados
                            InteresseChip(
                                nome = opcao.nome,
                                selecionado = escolhida,
                                habilitado = podeSalvar && !salvando,
                                onClick = {
                                    selecionados = if (escolhida) {
                                        selecionados - opcao.id
                                    } else {
                                        selecionados + opcao.id
                                    }
                                    if (mensagemEhErro) mensagem = null
                                }
                            )
                        }
                    }

                    if (expandido || ocultas > 0) {
                        val rotulo = if (expandido) {
                            "Ver menos"
                        } else {
                            "Ver mais ($ocultas)"
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    gruposExpandidos = if (expandido) {
                                        gruposExpandidos - grupo.id
                                    } else {
                                        gruposExpandidos + grupo.id
                                    }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = rotulo,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = CulturamaOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Icon(
                                imageVector = if (expandido) {
                                    Icons.Filled.KeyboardArrowUp
                                } else {
                                    Icons.Filled.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = CulturamaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CulturamaBackground)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
            AnimatedVisibility(
                visible = mensagem != null,
                enter = fadeIn(tween(180)) + expandVertically(),
                exit = fadeOut(tween(120)) + shrinkVertically()
            ) {
                mensagem?.let { texto ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!mensagemEhErro) {
                            Icon(
                                imageVector = CulturamaIcons.CheckCircle,
                                contentDescription = null,
                                tint = CulturamaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = texto,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (mensagemEhErro) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    CulturamaOrange
                                },
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
            CulturamaPrimaryButton(
                text = "Salvar interesses",
                isLoading = salvando,
                enabled = podeSalvar,
                onClick = {
                    if (salvando || !podeSalvar) return@CulturamaPrimaryButton
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

/**
 * Quebra os chips em linhas sem usar FlowRow.
 * FlowRow, dentro da lista rolável, mede com altura infinita e fecha o app.
 */
@Composable
private fun FluxoDeChips(
    modifier: Modifier = Modifier,
    espacoHorizontal: Dp = 8.dp,
    espacoVertical: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        if (measurables.isEmpty()) {
            layout(constraints.minWidth, constraints.minHeight) {}
        } else {
            val larguraMaxima = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
            val folgaX = espacoHorizontal.roundToPx()
            val folgaY = espacoVertical.roundToPx()
            val limitesFilho = Constraints(maxWidth = larguraMaxima.coerceAtLeast(0))
            val itens = measurables.map { it.measure(limitesFilho) }

            val linhas = mutableListOf<List<Placeable>>()
            val alturas = mutableListOf<Int>()
            var linhaAtual = mutableListOf<Placeable>()
            var larguraLinha = 0
            var alturaLinha = 0

            itens.forEach { item ->
                val acrescimo = if (linhaAtual.isEmpty()) item.width else item.width + folgaX
                val estoura = linhaAtual.isNotEmpty() && larguraMaxima > 0 && larguraLinha + acrescimo > larguraMaxima
                if (estoura) {
                    linhas += linhaAtual
                    alturas += alturaLinha
                    linhaAtual = mutableListOf(item)
                    larguraLinha = item.width
                    alturaLinha = item.height
                } else {
                    linhaAtual += item
                    larguraLinha += acrescimo
                    alturaLinha = max(alturaLinha, item.height)
                }
            }
            if (linhaAtual.isNotEmpty()) {
                linhas += linhaAtual
                alturas += alturaLinha
            }

            val alturaConteudo = alturas.sum() + folgaY * (alturas.size - 1).coerceAtLeast(0)
            val largura = if (constraints.hasBoundedWidth) {
                constraints.maxWidth.coerceAtLeast(constraints.minWidth)
            } else {
                larguraLinha.coerceAtLeast(constraints.minWidth)
            }
            val altura = alturaConteudo.coerceIn(
                constraints.minHeight,
                if (constraints.hasBoundedHeight) constraints.maxHeight else alturaConteudo.coerceAtLeast(constraints.minHeight)
            )

            layout(largura, altura) {
                var y = 0
                linhas.forEachIndexed { indice, linha ->
                    var x = 0
                    linha.forEach { item ->
                        item.placeRelative(x, y)
                        x += item.width + folgaX
                    }
                    y += alturas[indice] + folgaY
                }
            }
        }
    }
}

/**
 * Colapsado: mostra até [LIMITE_CHIPS_COLAPSADOS], mas nunca esconde opções já selecionadas.
 * Expandido: mostra todas.
 */
private fun opcoesVisiveisDoGrupo(
    opcoes: List<OpcaoCultural>,
    selecionados: Set<String>,
    expandido: Boolean
): List<OpcaoCultural> {
    if (expandido || opcoes.size <= LIMITE_CHIPS_COLAPSADOS) return opcoes

    val escolhidas = opcoes.filter { it.id in selecionados }
    val demais = opcoes.filter { it.id !in selecionados }
    val vagas = (LIMITE_CHIPS_COLAPSADOS - escolhidas.size).coerceAtLeast(0)
    return (escolhidas + demais.take(vagas)).ifEmpty { opcoes.take(LIMITE_CHIPS_COLAPSADOS) }
}

@Composable
private fun InteresseChip(
    nome: String,
    selecionado: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val fundo by animateColorAsState(
        targetValue = if (selecionado) CulturamaOrange else CulturamaBackground,
        animationSpec = tween(180),
        label = "interesse_chip_fundo"
    )
    val borda by animateColorAsState(
        targetValue = if (selecionado) CulturamaOrange else Color(0xFF3A3A44),
        animationSpec = tween(180),
        label = "interesse_chip_borda"
    )
    val texto by animateColorAsState(
        targetValue = if (selecionado) Color.White else TextPrimary,
        animationSpec = tween(180),
        label = "interesse_chip_texto"
    )
    val escala by animateFloatAsState(
        targetValue = if (selecionado) 1.02f else 1f,
        animationSpec = tween(160),
        label = "interesse_chip_escala"
    )
    val interaction = remember { MutableInteractionSource() }
    val forma = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .scale(escala)
            .clip(forma)
            .background(fundo)
            .border(width = 1.dp, color = borda, shape = forma)
            .clickable(
                enabled = habilitado,
                interactionSource = interaction,
                indication = ripple(bounded = true, color = CulturamaOrange),
                role = Role.Checkbox,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selecionado) {
            Icon(
                imageVector = CulturamaIcons.Checkmark,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = nome,
            style = MaterialTheme.typography.labelLarge.copy(
                color = texto,
                fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 16.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Reaproveita os vetores das bolhas de preferências, por slug/nome do catálogo. */
private fun iconeDoGrupo(slug: String, nome: String): ImageVector {
    val chave = "${slug.lowercase()} ${nome.lowercase()}"
    return when {
        "samba" in chave -> CulturamaIcons.DrumSamba
        "capoeira" in chave -> CulturamaIcons.CapoeiraMartial
        "rap" in chave || "rima" in chave || "batalha" in chave -> CulturamaIcons.MicRap
        "musica" in chave || "música" in chave -> CulturamaIcons.MusicNote
        "feira" in chave || "gastro" in chave -> CulturamaIcons.ShoppingBasket
        "cinema" in chave || "audio" in chave || "áudio" in chave -> CulturamaIcons.Movie
        "literatura" in chave || "livro" in chave -> CulturamaIcons.BookLiterature
        "geek" in chave || "game" in chave -> CulturamaIcons.Games
        "cenic" in chave || "cênic" in chave || "teatro" in chave -> CulturamaIcons.Theater
        "danca" in chave || "dança" in chave -> CulturamaIcons.Dance
        "arte" in chave || "exposi" in chave -> CulturamaIcons.PaletteArt
        "oficina" in chave || "curso" in chave -> CulturamaIcons.SchoolCourse
        "esporte" in chave -> CulturamaIcons.TrophySport
        "sarau" in chave || "poesia" in chave -> CulturamaIcons.BookLiterature
        "popular" in chave || "tradicion" in chave -> CulturamaIcons.FlowerCulture
        else -> CulturamaIcons.ShapesOther
    }
}
