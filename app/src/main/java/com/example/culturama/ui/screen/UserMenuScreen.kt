package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.culturama.data.acesso.IntencaoDeAcesso
import com.example.culturama.data.admin.AcessoAdministrativo
import com.example.culturama.data.admin.PapelNoPainel
import com.example.culturama.data.auth.ContaAutenticada
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
import com.example.culturama.data.preference.UserPreferencesRepository
import com.example.culturama.data.profile.Interesses
import com.example.culturama.data.profile.PerfilRepository
import com.example.culturama.navigation.Screen
import com.example.culturama.navigation.SecaoDoPainel
import com.example.culturama.navigation.SecaoDoUsuario
import com.example.culturama.ui.component.CulturamaIcons
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.HeroGradientMid
import com.example.culturama.ui.theme.HeroGradientTop
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

/**
 * Menu principal da conta: identificação e atalhos para as áreas do perfil.
 * Cada atalho só resume o que já existe; o conteúdo completo fica para as próximas telas.
 */
@Composable
fun UserMenuScreen(
    navController: NavController,
    authService: SupabaseAuthService = remember { SupabaseAuthService.instance }
) {
    val context = LocalContext.current
    val preferences = remember { UserPreferencesRepository.getInstance(context) }
    val favoritos = remember { RemoteFavoriteRepository.instance }
    val versaoFavoritos by favoritos.alteracoes.collectAsState()
    var conta by remember { mutableStateOf<ContaAutenticada?>(null) }
    var carregandoConta by remember { mutableStateOf(true) }
    var resumoFavoritos by remember { mutableStateOf<String?>(null) }
    var resumoInteresses by remember { mutableStateOf<String?>(null) }
    val logado by authService.isLoggedIn.collectAsState(initial = authService.contaAtual() != null)
    var papel by remember { mutableStateOf(PapelNoPainel.NENHUM) }

    LaunchedEffect(Unit) {
        val lida = authService.contaAtual()
        if (lida == null) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
                launchSingleTop = true
            }
            return@LaunchedEffect
        }
        conta = lida
        val cadastro = PerfilRepository.lerCadastro().getOrNull()
        if (cadastro != null && cadastro.nome.isNotBlank()) {
            conta = lida.copy(nome = cadastro.nome)
        }
        val grupos = if (cadastro != null) PerfilRepository.catalogo().getOrNull() else null
        resumoInteresses = if (cadastro != null && grupos != null) {
            Interesses.resumo(Interesses.nomesVisiveis(cadastro.interesses, grupos))
        } else {
            resumoLocalDeInteresses(preferences)
        }
        carregandoConta = false
    }

    LaunchedEffect(Unit) {
        idDoUsuarioAutenticado()?.let { favoritos.atualizar(it) }
    }

    LaunchedEffect(logado) {
        if (authService.contaAtual() == null) {
            papel = PapelNoPainel.NENHUM
            return@LaunchedEffect
        }
        if (!logado) return@LaunchedEffect
        papel = AcessoAdministrativo.verificar().getOrDefault(PapelNoPainel.NENHUM)
    }

    LaunchedEffect(versaoFavoritos) {
        val quantidade = idDoUsuarioAutenticado()?.let { favoritos.ids(it).size }
        resumoFavoritos = when (quantidade) {
            null, 0 -> null
            1 -> "1 evento salvo"
            else -> "$quantidade eventos salvos"
        }
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
                    navController.navigate(Screen.Home.route) { launchSingleTop = true }
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }
            Text(
                text = "Perfil",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CabecalhoDoPerfil(
                conta = conta,
                carregando = carregandoConta,
                onEditar = {
                    navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.EDITAR_PERFIL.id))
                }
            )

            Text(
                text = "Seu espaço",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            AtalhoDoPerfil(
                icone = Icons.Filled.Favorite,
                titulo = SecaoDoUsuario.FAVORITOS.titulo,
                resumo = resumoFavoritos ?: SecaoDoUsuario.FAVORITOS.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.FAVORITOS.id)) }
            )
            AtalhoDoPerfil(
                icone = CulturamaIcons.MusicNote,
                titulo = SecaoDoUsuario.INTERESSES.titulo,
                resumo = resumoInteresses ?: SecaoDoUsuario.INTERESSES.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.INTERESSES.id)) }
            )
            AtalhoDoPerfil(
                icone = CulturamaIcons.BookLiterature,
                titulo = SecaoDoUsuario.COMENTARIOS.titulo,
                resumo = SecaoDoUsuario.COMENTARIOS.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.COMENTARIOS.id)) }
            )
            AtalhoDoPerfil(
                icone = Icons.Filled.Star,
                titulo = SecaoDoUsuario.AVALIACOES.titulo,
                resumo = SecaoDoUsuario.AVALIACOES.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.AVALIACOES.id)) }
            )
            AtalhoDoPerfil(
                icone = Icons.Filled.DateRange,
                titulo = SecaoDoUsuario.EVENTOS.titulo,
                resumo = SecaoDoUsuario.EVENTOS.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.EVENTOS.id)) }
            )
            AtalhoDoPerfil(
                icone = Icons.Filled.Place,
                titulo = SecaoDoUsuario.TRAJETORIA.titulo,
                resumo = SecaoDoUsuario.TRAJETORIA.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.TRAJETORIA.id)) }
            )
            AtalhoDoPerfil(
                icone = CulturamaIcons.Groups,
                titulo = SecaoDoUsuario.ORGANIZACOES.titulo,
                resumo = SecaoDoUsuario.ORGANIZACOES.resumo,
                onClick = { navController.navigate(Screen.UserSection.createRoute(SecaoDoUsuario.ORGANIZACOES.id)) }
            )
            if (papel == PapelNoPainel.ADMINISTRADOR) {
                Text(
                    text = "Administração",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
                AtalhoDoPerfil(
                    icone = Icons.Filled.Lock,
                    titulo = "Painel administrativo",
                    resumo = "Visão geral do que está no ar",
                    onClick = {
                        navController.navigate(Screen.AdminPainel.createRoute(SecaoDoPainel.VISAO))
                    }
                )
                AtalhoDoPerfil(
                    icone = CulturamaIcons.Groups,
                    titulo = "Área do organizador",
                    resumo = "Abrir e usar o espaço cultural como admin",
                    onClick = {
                        navController.navigate(Screen.AreaDaConta.createRoute(IntencaoDeAcesso.ORGANIZADOR.id))
                    }
                )
                AtalhoDoPerfil(
                    icone = Icons.Filled.Star,
                    titulo = "Área empresarial",
                    resumo = "Abrir e usar o Insights como admin",
                    onClick = {
                        navController.navigate(Screen.AreaDaConta.createRoute(IntencaoDeAcesso.EMPRESA.id))
                    }
                )
            }
            if (papel == PapelNoPainel.MODERADOR) {
                Text(
                    text = "Moderação",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
                AtalhoDoPerfil(
                    icone = Icons.Filled.Lock,
                    titulo = "Painel de moderação",
                    resumo = "Revisão, eventos, comentários e denúncias",
                    onClick = { navController.navigate(Screen.Moderacao.route) }
                )
            }
            AtalhoDoPerfil(
                icone = Icons.Filled.Settings,
                titulo = "Configurações",
                resumo = "Conta, privacidade e sair",
                onClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}

private fun resumoLocalDeInteresses(preferences: UserPreferencesRepository): String? {
    if (!preferences.isPreferencesConfigured()) return null
    val escolhidos = preferences.getSelectedCategoryIds()
    val nomes = preferences.getAvailableCategories()
        .filter { it.id in escolhidos }
        .map { it.name.replace('\n', ' ') }
    return Interesses.resumo(nomes)
}

@Composable
private fun CabecalhoDoPerfil(
    conta: ContaAutenticada?,
    carregando: Boolean,
    onEditar: () -> Unit
) {
    val nome = conta?.nome?.takeIf { it.isNotBlank() }
    val usuario = conta?.nomeDeUsuario?.takeIf { it.isNotBlank() }
    val apresentacao = conta?.apresentacao?.takeIf { it.isNotBlank() }
    val titulo = nome ?: conta?.email ?: if (carregando) "Carregando perfil…" else "Sua conta"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(HeroGradientTop.copy(alpha = 0.92f), HeroGradientMid, CulturamaCard)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarDoPerfil(url = conta?.avatarUrl, nome = nome)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (usuario != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "@$usuario",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CulturamaOrange),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (apresentacao != null) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = apresentacao,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onEditar,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CulturamaOrange),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = CulturamaOrange
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Editar perfil",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun AvatarDoPerfil(url: String?, nome: String?) {
    var imagemFalhou by remember(url) { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(CulturamaOrange.copy(alpha = 0.22f))
            .border(2.dp, CulturamaOrange, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNullOrBlank() || imagemFalhou) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = if (nome.isNullOrBlank()) "Avatar padrão" else "Foto de $nome indisponível",
                tint = TextPrimary,
                modifier = Modifier.size(40.dp)
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
private fun AtalhoDoPerfil(
    icone: ImageVector,
    titulo: String,
    resumo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CulturamaOrange.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icone,
                contentDescription = null,
                tint = CulturamaOrange,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = resumo,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary
        )
    }
}
