package com.example.culturama.ui.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.component.CulturamaLogo
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

/**
 * Página pública "Para empresas", no mesmo papel da página do site.
 * Não escolhe tipo de conta e não abre o painel. O cadastro e o login
 * reutilizam o fluxo empresarial que já existe.
 */
@Composable
fun ParaEmpresasScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val contexto = LocalContext.current
    val logado by authService.isLoggedIn.collectAsState(initial = false)
    val rolagem = rememberScrollState()

    fun cadastrar() {
        navController.navigate(Screen.SolicitacaoDeAcesso.createRoute(IntencaoDeAcesso.EMPRESA.id))
    }

    fun entrar() {
        if (logado) {
            navController.navigate(Screen.AreaDaConta.createRoute(IntencaoDeAcesso.EMPRESA.id))
        } else {
            navController.navigate(Screen.Login.createRoute(destino = IntencaoDeAcesso.EMPRESA.id))
        }
    }

    fun conhecerPlanos() {
        navController.navigate(Screen.PlanosEmpresa.route) { launchSingleTop = true }
    }

    fun abrirFonte(url: String) {
        try {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(contexto, "Não foi possível abrir a fonte.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rolagem)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
        ) {
            Cabecalho(onVoltar = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                }
            })
            Apresentacao(onCadastrar = ::cadastrar, onEntrar = ::entrar, onPlanos = ::conhecerPlanos)
            SecaoMercado(onFonte = ::abrirFonte)
            SecaoMedicao()
            SecaoInsights()
            SecaoPrivacidade()
            ChamadaFinal(onCadastrar = ::cadastrar)
        }
    }
}

@Composable
private fun Cabecalho(onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoltar) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
        }
        CulturamaLogo(height = 26.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Para empresas",
            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun Apresentacao(
    onCadastrar: () -> Unit,
    onEntrar: () -> Unit,
    onPlanos: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PARA EMPRESAS",
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, CulturamaOrange.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = CulturamaOrange,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = buildAnnotatedString {
                append("Patrocine a cultura que\no seu público ")
                withStyle(SpanStyle(color = CulturamaOrange, fontWeight = FontWeight.Black)) {
                    append("já procura.")
                }
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp
            )
        )
        Text(
            text = "O Culturama Insights mostra quais cenas, eventos e bairros concentram interesse, para a sua marca chegar onde as pessoas já querem estar.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 21.sp)
        )
        Button(
            onClick = onCadastrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
        ) { Text("Cadastrar minha empresa", fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onEntrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Já sou empresa") }
        OutlinedButton(
            onClick = onPlanos,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Conhecer planos") }
    }
}

@Composable
private fun SecaoMercado(onFonte: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "O mercado de experiências cresce",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "Pesquisas recentes do setor, cada uma com link para a fonte.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        indicadores.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CulturamaCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    item.numero,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp
                    )
                )
                Text(
                    item.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                Text(
                    item.explicacao,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                )
                Text(
                    text = item.fonte,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable { onFonte(item.url) },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextTertiary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                )
            }
        }
        Text(
            text = buildAnnotatedString {
                append("Nem tudo cresce: as vagas formais em organização de eventos caíram 45,5% de janeiro a agosto de 2026 em relação a 2025. O público gasta mais; quem produz segue com pouca estrutura, e é aí que o patrocínio faz diferença. ")
                withStyle(SpanStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                    append("Radar Econômico ABRAPE, via Promoview")
                }
            },
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { onFonte(URL_ABRAPE) },
            style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, lineHeight = 18.sp)
        )
    }
}

@Composable
private fun SecaoMedicao() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "O que o Culturama mede",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "Cada ação vira ponto no índice de impacto do evento. Quanto mais perto de ir, mais pesa.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
        )
        acoes.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { acao ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CulturamaCard)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CulturamaOrange.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(acao.icone, contentDescription = null, tint = CulturamaOrange, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                acao.pontos,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = CulturamaOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            acao.nome,
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            acao.detalhe,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                        )
                    }
                }
                if (linha.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text(
            text = "O Culturama mede interesse digital, não presença confirmada nos eventos. Não conta quem passou pela porta; mostra quem viu, salvou e pediu rota. O público no dia vem dos números do organizador.",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CulturamaOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
        )
    }
}

@Composable
private fun SecaoInsights() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Conheça o Culturama Insights",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "Amostra com os dados de exemplo do site. Não são registros do aplicativo.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CulturamaCard)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "DADOS DEMONSTRATIVOS",
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CulturamaOrange)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IndicadorDemo("Interações em 30 dias", "236", Modifier.weight(1f))
                IndicadorDemo("Categoria em alta", "Geek e games", Modifier.weight(1f))
            }
            Text(
                "Ranking por impacto",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
            )
            ranking.forEach { BarraDeExemplo(it.posicao, it.nome, it.categoria, it.valor, MAIOR_EXEMPLO) }
            Text(
                "Bairros por interesse",
                style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
            )
            bairros.forEach { BarraDeExemplo(it.posicao, it.nome, null, it.valor, MAIOR_EXEMPLO) }
        }
    }
}

@Composable
private fun IndicadorDemo(rotulo: String, valor: String, modifier: Modifier) {
    Column(modifier) {
        Text(rotulo, style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary))
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun BarraDeExemplo(posicao: String, nome: String, detalhe: String?, valor: Int, maior: Int) {
    val fracao = if (maior <= 0) 0f else valor.toFloat() / maior.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                posicao,
                modifier = Modifier.width(18.dp),
                style = MaterialTheme.typography.labelMedium.copy(color = TextTertiary)
            )
            Column(Modifier.weight(1f)) {
                Text(nome, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                if (detalhe != null) {
                    Text(detalhe, style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary))
                }
            }
            Text(
                valor.toString(),
                style = MaterialTheme.typography.labelLarge.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 18.dp)
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(TextTertiary.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fracao.coerceIn(0.08f, 1f))
                    .background(CulturamaOrange)
            )
        }
    }
}

@Composable
private fun SecaoPrivacidade() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = CulturamaOrange, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Só números agregados",
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
            Text(
                "Nenhum nome, e-mail ou identificador de pessoa chega à empresa. Recortes com menos de 20 pessoas aparecem como “dados insuficientes”, para ninguém ser reconhecido. Tudo dentro da LGPD.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
            )
        }
    }
}

@Composable
private fun ChamadaFinal(onCadastrar: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 22.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Pronto para encontrar o seu evento?",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "Cadastre sua empresa e descubra oportunidades de conexão com a cultura local. O cadastro é analisado pela equipe. Depois da aprovação, o Insights abre na sua conta.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 21.sp)
        )
        Button(
            onClick = onCadastrar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
        ) { Text("Cadastrar minha empresa", fontWeight = FontWeight.Bold) }
    }
}

private const val URL_ABRAPE = "https://www.promoview.com.br/gasto-com-lazer-recorde-abrape-2026/"
private const val URL_PROMOVIEW = "https://www.promoview.com.br/melhores-do-mes-pesquisas-experiencia-2027/"
private const val URL_GENSLER =
    "https://maquinadoesporte.com.br/mercado/estudo-valor-do-dia-de-jogo-passa-pelo-entorno-do-estadio-nao-so-pelo-preco-do-ingresso/"
private const val MAIOR_EXEMPLO = 220

private data class Indicador(val numero: String, val titulo: String, val explicacao: String, val fonte: String, val url: String)

private val indicadores = listOf(
    Indicador(
        "R$ 102,6 bi",
        "Gastos com lazer",
        "De janeiro a agosto de 2026, o maior valor desde 2019 e 7,4% acima de 2025.",
        "Radar Econômico ABRAPE, via Promoview",
        URL_ABRAPE
    ),
    Indicador(
        "82%",
        "Dos CMOs",
        "Planejam investir mais em experiências no mundo real. Foram ouvidos 1.950 decisores em 14 mercados.",
        "Dentsu Creative, Making it Real, via Promoview",
        URL_PROMOVIEW
    ),
    Indicador(
        "≈ 4×",
        "Mais compartilhamento",
        "Depois do festival do que durante ele: o patrocínio continua aparecendo quando o evento acaba.",
        "BR Media/Publicis, festivais de música, via Promoview",
        URL_PROMOVIEW
    ),
    Indicador(
        "5,45%",
        "De engajamento local",
        "Criadores da mesma região, contra 1,57% dos de fora. A cena local conversa melhor com o público local.",
        "Entre Brasis 2026, via Promoview",
        URL_PROMOVIEW
    ),
    Indicador(
        "57%",
        "Querem atividade para a família",
        "No entorno do evento. O valor do dia passa pelo bairro, pela comida e pela identidade local, não só pelo ingresso.",
        "Gensler Research Institute, via Máquina do Esporte",
        URL_GENSLER
    )
)

private data class AcaoMedida(val icone: ImageVector, val pontos: String, val nome: String, val detalhe: String)

private val acoes = listOf(
    AcaoMedida(CulturamaIcons.Visibility, "1 ponto", "Visualização", "Alguém abriu a página do evento."),
    AcaoMedida(Icons.Outlined.FavoriteBorder, "3 pontos", "Favorito", "Salvou para não esquecer."),
    AcaoMedida(Icons.Outlined.Place, "5 pontos", "Como chegar", "Pediu a rota até o local."),
    AcaoMedida(CulturamaIcons.TagFree, "5 pontos", "Ingresso", "Clicou para comprar ou se inscrever.")
)

private data class ItemDemo(val posicao: String, val nome: String, val categoria: String?, val valor: Int)

private val ranking = listOf(
    ItemDemo("1", "Festival de jogos", "Geek e games", 220),
    ItemDemo("2", "Show na Vila", "Música", 220),
    ItemDemo("3", "Peça no Centro", "Artes cênicas", 132)
)

private val bairros = listOf(
    ItemDemo("1", "Pinheiros", null, 220),
    ItemDemo("2", "Vila Madalena", null, 220),
    ItemDemo("3", "Centro", null, 132)
)
