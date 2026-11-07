package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.culturama.data.admin.AcessoAdministrativo
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.admin.ContagemPorCategoria
import com.example.culturama.data.admin.EventoMaisFavoritado
import com.example.culturama.data.admin.FavoritosDaPlataforma
import com.example.culturama.data.admin.MetricaAdministrativa
import com.example.culturama.data.admin.VisaoGeral
import com.example.culturama.data.admin.fracaoDaBarra
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.navigation.SecaoDoPainel
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

private sealed interface EstadoDoPainel {
    data object Carregando : EstadoDoPainel
    data object Negado : EstadoDoPainel
    data class Pronto(val visao: VisaoGeral) : EstadoDoPainel
    data class Erro(val mensagem: String) : EstadoDoPainel
}

private const val AVISO_METRICAS =
    "O servidor ainda não entrega estas métricas ao aplicativo. Nenhum número foi estimado."

private const val AVISO_FAVORITOS =
    "A lista de eventos mais favoritados da plataforma não está disponível. " +
        "Os favoritos deste aparelho são só da sua conta e não entram aqui."

/**
 * Painel administrativo em layout de celular.
 * A autorização é conferida de novo nesta tela: o atalho do menu não basta.
 */
@Composable
fun AdminPainelScreen(
    navController: NavController,
    secaoId: String,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val secao = SecaoDoPainel.porId(secaoId)
    val logado by authService.isLoggedIn.collectAsState(initial = authService.contaAtual() != null)
    var estado by remember { mutableStateOf<EstadoDoPainel>(EstadoDoPainel.Carregando) }
    var tentativa by remember { mutableIntStateOf(0) }

    LaunchedEffect(tentativa, logado) {
        if (authService.contaAtual() == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        if (!logado) return@LaunchedEffect
        estado = EstadoDoPainel.Carregando
        estado = AcessoAdministrativo.verificar().fold(
            onSuccess = { papel ->
                if (papel == PapelNoPainel.ADMINISTRADOR) EstadoDoPainel.Pronto(VisaoGeral.indisponivel())
                else EstadoDoPainel.Negado
            },
            onFailure = { erro ->
                EstadoDoPainel.Erro(erro.message ?: "Não foi possível confirmar a permissão.")
            }
        )
    }

    val titulo = when (estado) {
        is EstadoDoPainel.Pronto -> "Admin — ${secao?.titulo ?: "Painel"}"
        else -> "Admin"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                    contentDescription = "Voltar ao menu",
                    tint = TextPrimary
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (estado is EstadoDoPainel.Pronto) {
            SecoesDoPainel(
                atual = secao,
                onSelecionar = { destino ->
                    if (destino != secao) {
                        val destinoAtual = navController.currentDestination?.id
                        navController.navigate(Screen.AdminPainel.createRoute(destino)) {
                            if (destinoAtual != null) {
                                popUpTo(destinoAtual) { inclusive = true }
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        when (val atual = estado) {
            EstadoDoPainel.Carregando -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CulturamaOrange)
            }

            EstadoDoPainel.Negado -> AvisoDoPainel(
                titulo = "Sem permissão",
                texto = "O painel administrativo só abre quando o servidor marca a conta como administradora."
            )

            is EstadoDoPainel.Erro -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AvisoDoPainel(
                    titulo = "Não foi possível confirmar",
                    texto = atual.mensagem
                )
                OutlinedButton(
                    onClick = { tentativa++ },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Tentar de novo")
                }
            }

            is EstadoDoPainel.Pronto -> ConteudoDaSecao(secao, atual.visao)
        }
    }
}

@Composable
private fun SecoesDoPainel(atual: SecaoDoPainel?, onSelecionar: (SecaoDoPainel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecaoDoPainel.entries.forEach { secao ->
            val selecionada = secao == atual
            Text(
                text = secao.titulo,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (selecionada) CulturamaOrange else CulturamaCard)
                    .clickable { onSelecionar(secao) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (selecionada) CulturamaBackground else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun ConteudoDaSecao(secao: SecaoDoPainel?, visao: VisaoGeral) {
    when (secao) {
        SecaoDoPainel.VISAO -> VisaoGeralDoPainel(visao)
        SecaoDoPainel.EVENTOS, SecaoDoPainel.ANUNCIOS, SecaoDoPainel.EMPRESAS -> AvisoDoPainel(
            titulo = "Em desenvolvimento",
            texto = "A seção ${secao.titulo} entra numa próxima etapa. Nenhuma alteração é feita por aqui."
        )
        null -> AvisoDoPainel(
            titulo = "Seção indisponível",
            texto = "Esta parte do painel não está disponível."
        )
    }
}

@Composable
private fun VisaoGeralDoPainel(visao: VisaoGeral) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "O que está no ar e o que espera decisão.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        GradeDeMetricas(visao.metricas)
        if (visao.metricas.any { it.valor == null }) {
            Text(
                text = AVISO_METRICAS,
                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
            )
        }
        CartaoDoPainel(titulo = "Eventos publicados por categoria") {
            CategoriasPublicadas(visao.porCategoria)
        }
        CartaoDoPainel(titulo = "Mais favoritados") {
            MaisFavoritados(visao.maisFavoritados)
        }
    }
}

@Composable
private fun GradeDeMetricas(metricas: List<MetricaAdministrativa>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        metricas.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { metrica ->
                    CartaoMetrica(metrica, Modifier.weight(1f))
                }
                if (linha.size == 1) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CartaoMetrica(metrica: MetricaAdministrativa, modifier: Modifier = Modifier) {
    val valor = metrica.valor?.toString() ?: "—"
    val descricao = if (metrica.valor == null) {
        "${metrica.rotulo}: indisponível"
    } else {
        "${metrica.rotulo}: ${metrica.valor}"
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .padding(14.dp)
            .semantics { contentDescription = descricao },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = metrica.rotulo,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = if (metrica.valor == null) TextTertiary else TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun CategoriasPublicadas(categorias: List<ContagemPorCategoria>?) {
    when {
        categorias == null -> TextoDeIndisponibilidade(
            "A contagem por categoria ainda não vem do servidor."
        )
        categorias.isEmpty() -> Text(
            text = "Nenhum evento publicado por categoria.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
        else -> {
            val maior = categorias.maxOf { it.total }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categorias.forEach { categoria ->
                    BarraDeCategoria(categoria, maior)
                }
            }
        }
    }
}

@Composable
private fun BarraDeCategoria(categoria: ContagemPorCategoria, maior: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = categoria.nome,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = categoria.total.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(TextTertiary.copy(alpha = 0.35f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fracaoDaBarra(categoria.total, maior))
                    .fillMaxHeight()
                    .background(CulturamaOrange)
            )
        }
    }
}

@Composable
private fun MaisFavoritados(favoritos: FavoritosDaPlataforma) {
    when (favoritos) {
        FavoritosDaPlataforma.Indisponivel -> TextoDeIndisponibilidade(AVISO_FAVORITOS)
        is FavoritosDaPlataforma.Lista -> if (favoritos.itens.isEmpty()) {
            Text(
                text = "Nenhum evento favoritado na plataforma.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                favoritos.itens.forEachIndexed { indice, evento ->
                    EventoFavoritado(indice + 1, evento)
                }
            }
        }
    }
}

@Composable
private fun EventoFavoritado(posicao: Int, evento: EventoMaisFavoritado) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = posicao.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextTertiary,
                fontWeight = FontWeight.Bold
            )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = evento.nome,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!evento.categoria.isNullOrBlank()) {
                Text(
                    text = evento.categoria,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (evento.favoritos != null) {
            Text(
                text = evento.favoritos.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = CulturamaOrange,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun CartaoDoPainel(titulo: String, conteudo: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
        conteudo()
    }
}

@Composable
private fun TextoDeIndisponibilidade(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
    )
}

@Composable
private fun AvisoDoPainel(titulo: String, texto: String) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
        )
    }
}
