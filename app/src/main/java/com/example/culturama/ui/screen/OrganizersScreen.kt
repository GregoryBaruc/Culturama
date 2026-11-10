package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.component.CulturamaLogo
import com.example.culturama.ui.component.EventCover
import com.example.culturama.ui.component.PlaceholderFestival
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

/**
 * Página pública para quem divulga eventos. Não escolhe tipo de conta.
 * Cadastro e entrada reutilizam o fluxo de organizador já existente.
 */
@Composable
fun OrganizersScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val logado by authService.isLoggedIn.collectAsState(initial = false)
    var cenasPublicas by remember { mutableStateOf<List<EventModel>>(emptyList()) }

    LaunchedEffect(Unit) {
        cenasPublicas = EventRepository.getWeekEvents()
            .getOrDefault(emptyList())
            .filter { !it.imageUrl.isNullOrBlank() }
            .take(8)
    }

    fun cadastrar() {
        navController.navigate(Screen.SolicitacaoDeAcesso.createRoute(IntencaoDeAcesso.ORGANIZADOR.id))
    }

    fun entrar() {
        if (logado) {
            navController.navigate(Screen.AreaDaConta.createRoute(IntencaoDeAcesso.ORGANIZADOR.id))
        } else {
            navController.navigate(Screen.Login.createRoute(destino = IntencaoDeAcesso.ORGANIZADOR.id))
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
        ) {
            Cabecalho(onVoltar = {
                if (!navController.popBackStack()) {
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                }
            })
            Apresentacao(onCadastrar = ::cadastrar, onEntrar = ::entrar)
            SecaoBeneficios()
            SecaoCenas(cenasPublicas)
            SecaoPassos()
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
            text = "Para organizadores",
            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun Apresentacao(onCadastrar: () -> Unit, onEntrar: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "PARA ORGANIZADORES",
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
                append("Sua cena merece\nser ")
                withStyle(SpanStyle(color = CulturamaOrange, fontWeight = FontWeight.Black)) {
                    append("descoberta.")
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
            text = "Divulgue seus eventos, fortaleça sua comunidade e conecte sua cultura a pessoas que querem viver novas experiências.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 21.sp)
        )
        Button(
            onClick = onCadastrar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
        ) { Text("Cadastrar como organizador", fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onEntrar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) { Text("Já sou organizador") }
    }
}

@Composable
private fun SecaoBeneficios() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Por que divulgar no Culturama?",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        beneficios.chunked(2).forEach { linha ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                linha.forEach { item ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CulturamaCard)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CulturamaOrange.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icone, contentDescription = null, tint = CulturamaOrange, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            item.titulo,
                            style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            item.texto,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecaoCenas(eventos: List<EventModel>) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Quem pode divulgar",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "Coletivos, espaços e cenas independentes. Os cartazes abaixo, quando aparecem, são eventos públicos da semana — não são da conta que está nesta página.",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cenas.chunked(2).forEach { linha ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    linha.forEach { cena ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CulturamaCard)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(cena.icone, contentDescription = null, tint = CulturamaOrange, modifier = Modifier.size(16.dp))
                            Text(
                                cena.nome,
                                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimary),
                                maxLines = 2
                            )
                        }
                    }
                    if (linha.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
        if (eventos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                eventos.forEach { evento ->
                    Column(modifier = Modifier.width(132.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            EventCover(
                                imageUrl = evento.imageUrl,
                                label = evento.title,
                                emoji = "🎭",
                                gradient = PlaceholderFestival,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            evento.title,
                            modifier = Modifier.padding(top = 6.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            evento.category,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecaoPassos() {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Como funciona",
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        passos.forEach { passo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CulturamaCard)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    passo.numero,
                    style = MaterialTheme.typography.titleLarge.copy(color = CulturamaOrange, fontWeight = FontWeight.Black)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        passo.titulo,
                        style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        passo.texto,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChamadaFinal(onCadastrar: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Faça sua cultura chegar mais longe.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
        )
        Text(
            "O Culturama aproxima iniciativas culturais independentes de pessoas que querem descobrir o que acontece à sua volta.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 21.sp)
        )
        Button(
            onClick = onCadastrar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
        ) { Text("Quero divulgar meus eventos", fontWeight = FontWeight.Bold) }
    }
}

private data class Beneficio(val icone: ImageVector, val titulo: String, val texto: String)

private val beneficios = listOf(
    Beneficio(CulturamaIcons.PublishEvent, "Divulgue seus eventos", "Cadastre atividades culturais para que mais pessoas possam descobri-las."),
    Beneficio(CulturamaIcons.Groups, "Alcance sua comunidade", "Conecte suas iniciativas com pessoas interessadas em manifestações culturais."),
    Beneficio(CulturamaIcons.MapPin, "Apareça no mapa", "Eventos com localização válida podem ser encontrados no mapa cultural."),
    Beneficio(Icons.Filled.DateRange, "Organize suas atividades", "Acompanhe seus eventos na área do organizador, conforme o que o servidor autorizar.")
)

private data class Cena(val icone: ImageVector, val nome: String)

private val cenas = listOf(
    Cena(CulturamaIcons.CapoeiraMartial, "Rodas de capoeira"),
    Cena(CulturamaIcons.MicRap, "Batalhas de rima"),
    Cena(CulturamaIcons.DrumSamba, "Rodas de samba"),
    Cena(CulturamaIcons.MapPin, "Espaços culturais"),
    Cena(CulturamaIcons.SchoolCourse, "Oficinas"),
    Cena(CulturamaIcons.ShoppingBasket, "Feiras culturais"),
    Cena(CulturamaIcons.Groups, "Coletivos artísticos")
)

private data class Passo(val numero: String, val titulo: String, val texto: String)

private val passos = listOf(
    Passo(
        "1",
        "Crie sua conta",
        "E-mail e senha ou Google. Essa conta cultural ainda não é um organizador."
    ),
    Passo(
        "2",
        "Cadastre seu espaço ou coletivo",
        "No site, o cadastro pede CPF ou CNPJ, o nome do coletivo ou da empresa e um documento. O aplicativo ainda não envia esses dados."
    ),
    Passo(
        "3",
        "Divulgue seus eventos",
        "A publicação não é imediata. Depois da verificação, o evento autorizado entra no mapa e na lista."
    )
)
