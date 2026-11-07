package com.example.culturama.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.culturama.data.EventRepository
import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.favorite.RemoteFavoriteRepository
import com.example.culturama.data.favorite.idDoUsuarioAutenticado
import java.util.Calendar
import java.util.TimeZone
import com.example.culturama.model.EventModel
import com.example.culturama.navigation.Screen
import com.example.culturama.ui.component.*
import com.example.culturama.ui.theme.*

// ─── Estado dos Filtros Avançados ─────────────────────────────────────────────

data class AdvancedFiltersState(
    val category: String? = null,
    val startDay: Int? = null,
    val endDay: Int? = null,
    val pricing: String = "Todas", // "Todas", "Grátis", "Pago"
    val region: String? = null,
    val accessibilityOnly: Boolean = false
) {
    val isActive: Boolean
        get() = category != null || startDay != null || endDay != null || pricing != "Todas" || region != null || accessibilityOnly
}

// ─── Dados fictícios ──────────────────────────────────────────────────────────

internal val sampleEvents = listOf(
    EventModel(
        id = "1",
        title = "Batalha de Rima \u2014 Centro Cultural",
        category = "Hip-Hop",
        dayOfMonth = 8,
        monthAbbr = "NOV",
        dayOfWeek = "S\u00e1b",
        time = "19:00",
        venueName = "Centro Cultural da Juventude, Zona Norte",
        isFree = true,
        description = "Batalha tradicional reunindo MCs da região com rimas improvisadas e microfone aberto. Espaço amplo com rampa de acesso e banheiro acessível."
    ),
    EventModel(
        id = "2",
        title = "Roda de Samba do Largo",
        category = "Samba",
        dayOfMonth = 9,
        monthAbbr = "NOV",
        dayOfWeek = "Dom",
        time = "14:00",
        venueName = "Largo da Mem\u00f3ria, Centro",
        isFree = true,
        description = "Tarde com clássicos do samba de raiz, partido alto e comunidade unida em torno da música popular."
    ),
    EventModel(
        id = "3",
        title = "Roda de Capoeira Angola",
        category = "Capoeira",
        dayOfMonth = 10,
        monthAbbr = "NOV",
        dayOfWeek = "Seg",
        time = "18:30",
        venueName = "Associa\u00e7\u00e3o Cultural Ra\u00edzes, Zona Leste",
        isFree = true,
        description = "Vivência cultural e histórica com mestres e alunos de capoeira Angola, com berimbau e cânticos ancestrais."
    ),
    EventModel(
        id = "4",
        title = "Sarau Liter\u00e1rio da Periferia",
        category = "Sarau",
        dayOfMonth = 11,
        monthAbbr = "NOV",
        dayOfWeek = "Ter",
        time = "20:00",
        venueName = "Bar do Z\u00e9, Zona Sul",
        isFree = false,
        price = "R\$ 10,00",
        description = "Microfone aberto para poetas independentes, declamação de poesia marginal e música acústica."
    ),
    EventModel(
        id = "5",
        title = "Feira Livre de Artesanato",
        category = "Feiras e Gastronomia",
        dayOfMonth = 12,
        monthAbbr = "NOV",
        dayOfWeek = "Qua",
        time = "09:00",
        venueName = "Pra\u00e7a da Liberdade, Centro",
        isFree = true,
        description = "Exposição ao ar livre com artesãos locais, gastronomia típica e artesanato sustentável. Local plano com fácil circulação e acessibilidade total para cadeirantes."
    ),
    EventModel(
        id = "6",
        title = "Show de M\u00fasica Independente",
        category = "M\u00fasica",
        dayOfMonth = 13,
        monthAbbr = "NOV",
        dayOfWeek = "Qui",
        time = "21:00",
        venueName = "Est\u00fadio Vivo, Zona Oeste",
        isFree = false,
        price = "R\$ 25,00",
        description = "Apresentação ao vivo de bandas autorais de indie rock, MPB e ritmos experimentais."
    ),
    EventModel(
        id = "7",
        title = "Campeonato de Skate Comunit\u00e1rio",
        category = "Esportes",
        dayOfMonth = 14,
        monthAbbr = "NOV",
        dayOfWeek = "Sex",
        time = "10:00",
        venueName = "Pista do Ibirapuera, Zona Sul",
        isFree = true,
        description = "Competição amadora aberta para todas as idades nas categorias street e bowl, com premiações comunitárias."
    ),
    EventModel(
        id = "8",
        title = "Expo Geek & Games \u2014 Edi\u00e7\u00e3o Ver\u00e3o",
        category = "Geek e Games",
        dayOfMonth = 15,
        monthAbbr = "NOV",
        dayOfWeek = "S\u00e1b",
        time = "12:00",
        venueName = "Centro de Conven\u00e7\u00f5es Anhembi, Zona Norte",
        isFree = false,
        price = "R\$ 30,00",
        description = "Espaço gamer com campeonatos de esports, concurso de cosplay, estandes e jogos de tabuleiro. Pavilhão totalmente adaptado com elevadores e acessibilidade."
    ),
    EventModel(
        id = "9",
        title = "Oficina de Grafite e Arte Urbana",
        category = "Oficinas",
        dayOfMonth = 16,
        monthAbbr = "NOV",
        dayOfWeek = "Dom",
        time = "10:00",
        venueName = "Beco da Arte, Centro",
        isFree = true,
        description = "Oficina prática gratuita com noções básicas de desenho, sprays, técnicas de stencil e muralismo comunitário."
    ),
    EventModel(
        id = "10",
        title = "Espet\u00e1culo de Teatro de Rua",
        category = "Artes C\u00eanicas",
        dayOfMonth = 17,
        monthAbbr = "NOV",
        dayOfWeek = "Seg",
        time = "17:00",
        venueName = "Largo do Arouche, Centro",
        isFree = true,
        description = "Apresentação ao ar livre de comédia dramática com elenco popular, com interpretação em Libras e acesso livre para todos os públicos."
    )
)

// ─── Modelos de filtros e categorias ─────────────────────────────────────────

private data class QuickFilter(val label: String)

private val quickFilters = listOf(
    QuickFilter("Tudo"),
    QuickFilter("Hoje"),
    QuickFilter("Fim de semana"),
    QuickFilter("Gr\u00e1tis"),
    QuickFilter("Acess\u00edvel"),
    QuickFilter("Perto de mim")
)

private data class CategoryItem(val label: String, val emoji: String)

private val categories = listOf(
    CategoryItem("M\u00fasica",             "\uD83C\uDFB8"),
    CategoryItem("Samba",              "\uD83E\uDD41"),
    CategoryItem("Esportes",           "\u26BD"),
    CategoryItem("Geek e Games",       "\uD83C\uDFAE"),
    CategoryItem("Dan\u00e7a",             "\uD83D\uDC83"),
    CategoryItem("Artes C\u00eanicas",     "\uD83C\uDFAD"),
    CategoryItem("Arte e Exposi\u00e7\u00e3o",  "\uD83C\uDFA8"),
    CategoryItem("Feiras e Gastronomia", "\uD83C\uDF5C"),
    CategoryItem("Cultura Popular",    "\uD83C\uDF3A"),
    CategoryItem("Oficinas",           "\uD83D\uDD27"),
    CategoryItem("Outros",             "\u2728")
)

private val regionsList = listOf("Centro", "Zona Sul", "Zona Norte", "Zona Leste", "Zona Oeste")

// ─── Tela Principal ───────────────────────────────────────────────────────────

@Composable
fun EventsScreen(navController: NavController) {
    val authService = remember { SupabaseAuthService.instance }
    val favoritos = remember { RemoteFavoriteRepository.instance }
    val isLoggedIn by authService.isLoggedIn.collectAsState(initial = false)
    val versaoFavoritos by favoritos.alteracoes.collectAsState()

    var events               by remember { mutableStateOf(emptyList<EventModel>()) }
    var isLoading            by remember { mutableStateOf(true) }
    var errorMessage         by remember { mutableStateOf<String?>(null) }
    var reloadKey            by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        EventRepository.getAllEvents()
            .onSuccess {
                events = it
                isLoading = false
            }
            .onFailure {
                errorMessage = it.message ?: "Não foi possível carregar os eventos. Verifique sua conexão."
                isLoading = false
            }
    }

    var searchQuery          by remember { mutableStateOf("") }
    var selectedFilter       by remember { mutableStateOf("Tudo") }
    var selectedCategory     by remember { mutableStateOf<String?>(null) }
    var isListView           by remember { mutableStateOf(true) }
    var favoritedIds         by remember { mutableStateOf(setOf<String>()) }
    var showMoreFiltersSheet by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, versaoFavoritos) {
        val userId = if (isLoggedIn) idDoUsuarioAutenticado() else null
        favoritedIds = userId?.let { favoritos.ids(it).toSet() } ?: emptySet()
    }

    // Estado dos filtros avançados persistidos
    var advancedFilters by remember { mutableStateOf(AdvancedFiltersState()) }

    fun clearAllFilters() {
        searchQuery = ""
        selectedFilter = "Tudo"
        selectedCategory = null
        advancedFilters = AdvancedFiltersState()
    }

    val hasActiveFilters = searchQuery.isNotBlank() ||
            selectedFilter != "Tudo" ||
            selectedCategory != null ||
            advancedFilters.isActive

    // Filtragem combinada
    val currentDayOfMonth = remember { Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).get(Calendar.DAY_OF_MONTH) }
    val displayedEvents = remember(events, searchQuery, selectedFilter, selectedCategory, advancedFilters) {
        val query = searchQuery.trim()
        events.filter { event ->
            // 1. Pesquisa
            val matchesSearch = query.isBlank() ||
                event.title.contains(query, ignoreCase = true) ||
                event.category.contains(query, ignoreCase = true) ||
                event.venueName.contains(query, ignoreCase = true)

            // 2. Filtros rápidos
            val matchesFilter = when (selectedFilter) {
                "Gr\u00e1tis"        -> event.isFree
                "Hoje"          -> event.dayOfMonth == (currentDayOfMonth as Int)
                "Fim de semana" -> event.dayOfWeek.contains("S\u00e1b", ignoreCase = true) ||
                                   event.dayOfWeek.contains("Dom", ignoreCase = true)
                "Acess\u00edvel"    -> event.isAccessible || run {
                    val combinedText = "${event.description} ${event.venueName}"
                    combinedText.contains("acess", ignoreCase = true) ||
                    combinedText.contains("libras", ignoreCase = true) ||
                    combinedText.contains("rampa", ignoreCase = true) ||
                    combinedText.contains("adaptad", ignoreCase = true)
                }
                "Perto de mim"  -> true
                else            -> true
            }

            // 3. Categoria horizontal
            val matchesCategory = selectedCategory == null ||
                event.category.contains(selectedCategory!!, ignoreCase = true) ||
                selectedCategory!!.contains(event.category, ignoreCase = true)

            // 4. Filtros Avançados
            val matchesAdvCategory = advancedFilters.category == null ||
                event.category.contains(advancedFilters.category!!, ignoreCase = true)

            val matchesAdvStartDate = advancedFilters.startDay == null ||
                event.dayOfMonth >= advancedFilters.startDay!!

            val matchesAdvEndDate = advancedFilters.endDay == null ||
                event.dayOfMonth <= advancedFilters.endDay!!

            val matchesAdvPricing = when (advancedFilters.pricing) {
                "Gr\u00e1tis" -> event.isFree
                "Pago"   -> !event.isFree
                else     -> true
            }

            val matchesAdvRegion = advancedFilters.region == null ||
                event.venueName.contains(advancedFilters.region!!, ignoreCase = true)

            val matchesAdvAccessibility = !advancedFilters.accessibilityOnly || (event.isAccessible || run {
                val combinedText = "${event.description} ${event.venueName}"
                combinedText.contains("acess", ignoreCase = true) ||
                combinedText.contains("libras", ignoreCase = true) ||
                combinedText.contains("rampa", ignoreCase = true) ||
                combinedText.contains("adaptad", ignoreCase = true)
            })

            matchesSearch && matchesFilter && matchesCategory &&
            matchesAdvCategory && matchesAdvStartDate && matchesAdvEndDate &&
            matchesAdvPricing && matchesAdvRegion && matchesAdvAccessibility
        }
    }

    Scaffold(containerColor = CulturamaBackground) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // ── Cabeçalho ─────────────────────────────────────────────────
            item {
                EventsHeader(
                    searchQuery    = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onClearSearch  = { searchQuery = "" },
                    isListView     = isListView,
                    onToggleView   = { isListView = !isListView }
                )
            }

            // ── Filtros rápidos + Botão Mais filtros ──────────────────────
            item {
                QuickFiltersRow(
                    filters             = quickFilters,
                    selectedFilter      = selectedFilter,
                    onFilterSelect      = { selectedFilter = it },
                    isAdvancedActive    = advancedFilters.isActive,
                    onOpenMoreFilters   = { showMoreFiltersSheet = true }
                )
            }

            // ── Categorias ────────────────────────────────────────────────
            item {
                CategoriesSection(
                    categories       = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { cat ->
                        selectedCategory = if (selectedCategory == cat) null else cat
                    }
                )
            }

            // ── Contador de resultados e ações de filtro ativo ────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "${displayedEvents.size} ${if (displayedEvents.size == 1) "evento encontrado" else "eventos encontrados"}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedCategory != null) {
                            Text(
                                text     = "\u00d7 $selectedCategory",
                                style    = MaterialTheme.typography.labelSmall.copy(
                                    color      = CulturamaOrange,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CulturamaOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { selectedCategory = null }
                            )
                        }

                        if (advancedFilters.isActive) {
                            Text(
                                text     = "\u2699 Filtros+",
                                style    = MaterialTheme.typography.labelSmall.copy(
                                    color      = CulturamaOrange,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CulturamaOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showMoreFiltersSheet = true }
                            )
                        }

                        if (hasActiveFilters) {
                            Text(
                                text     = "Limpar tudo",
                                style    = MaterialTheme.typography.labelSmall.copy(
                                    color      = CulturamaOrange,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { clearAllFilters() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Cards de eventos ou mensagem de nenhum evento encontrado ──
            if (displayedEvents.isEmpty()) {
                item {
                    EmptyEventsState(
                        hasActiveFilters = hasActiveFilters,
                        onClearFilters   = { clearAllFilters() }
                    )
                }
            } else {
                items(displayedEvents, key = { it.id }) { event ->
                    EventCard(
                        event       = event,
                        isFavorited = event.id in favoritedIds,
                        onFavorite  = {
                            val userId = idDoUsuarioAutenticado()
                            if (!isLoggedIn || userId == null) {
                                navController.navigate(Screen.Login.route)
                            } else {
                                favoritedIds = favoritos
                                    .setFavorite(userId, event.id, event.id !in favoritedIds)
                                    .toSet()
                            }
                        },
                        onClick      = { navController.navigate("eventDetail?eventId=${event.id}") },
                        modifier     = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        imageContent = {
                            EventCover(
                                imageUrl = event.imageUrl,
                                label    = event.title,
                                emoji    = eventsScreenEmoji(event.category),
                                gradient = eventsScreenGradient(event.category)
                            )
                        }
                    )
                }
            }
        }
    }

    // ── Bottom Sheet de Filtros Avançados ─────────────────────────────────────────
    if (showMoreFiltersSheet) {
        MoreFiltersBottomSheet(
            currentFilters = advancedFilters,
            onDismiss      = { showMoreFiltersSheet = false },
            onApply        = { newFilters ->
                advancedFilters = newFilters
                showMoreFiltersSheet = false
            },
            onClear        = {
                advancedFilters = AdvancedFiltersState()
                showMoreFiltersSheet = false
            }
        )
    }
}

// ─── Bottom Sheet de Filtros Avançados ────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreFiltersBottomSheet(
    currentFilters: AdvancedFiltersState,
    onDismiss:      () -> Unit,
    onApply:        (AdvancedFiltersState) -> Unit,
    onClear:        () -> Unit
) {
    // Estado local para permitir cancelar ou confirmar com "Aplicar"
    var tempCategory       by remember { mutableStateOf(currentFilters.category) }
    var tempStartDayText   by remember { mutableStateOf(currentFilters.startDay?.toString() ?: "") }
    var tempEndDayText     by remember { mutableStateOf(currentFilters.endDay?.toString() ?: "") }
    var tempPricing        by remember { mutableStateOf(currentFilters.pricing) }
    var tempRegion         by remember { mutableStateOf(currentFilters.region) }
    var tempAccessibility by remember { mutableStateOf(currentFilters.accessibilityOnly) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = CulturamaSurface,
        contentColor     = TextPrimary,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A3C))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Título e botão fechar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Mais filtros",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(
                    onClick  = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Fechar painel",
                        tint               = TextSecondary
                    )
                }
            }

            // ── 1. Período (Dia inicial e Dia final) ─────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Per\u00edodo (dias de Novembro)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dia inicial
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CulturamaCard)
                            .border(1.dp, Color(0xFF333338), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.DateRange,
                                contentDescription = null,
                                tint               = TextTertiary,
                                modifier           = Modifier.size(16.dp)
                            )
                            if (tempStartDayText.isEmpty()) {
                                Text(
                                    text  = "De (dia)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                            BasicTextField(
                                value         = tempStartDayText,
                                onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) tempStartDayText = it },
                                modifier      = Modifier.fillMaxWidth(),
                                singleLine    = true,
                                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                cursorBrush   = SolidColor(CulturamaOrange)
                            )
                        }
                    }

                    // Dia final
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CulturamaCard)
                            .border(1.dp, Color(0xFF333338), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.DateRange,
                                contentDescription = null,
                                tint               = TextTertiary,
                                modifier           = Modifier.size(16.dp)
                            )
                            if (tempEndDayText.isEmpty()) {
                                Text(
                                    text  = "At\u00e9 (dia)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                                )
                            }
                            BasicTextField(
                                value         = tempEndDayText,
                                onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) tempEndDayText = it },
                                modifier      = Modifier.fillMaxWidth(),
                                singleLine    = true,
                                textStyle     = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                cursorBrush   = SolidColor(CulturamaOrange)
                            )
                        }
                    }
                }
            }

            // ── 2. Entrada (Gratuito ou Pago) ─────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Entrada",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CulturamaCard)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Todas", "Gr\u00e1tis", "Pago").forEach { option ->
                        val isSelected = tempPricing == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) CulturamaOrange else Color.Transparent)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { tempPricing = option },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = option,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // ── 3. Cidade ou Região ───────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Cidade ou Regi\u00e3o",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    regionsList.forEach { reg ->
                        val isSelected = tempRegion == reg
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CulturamaOrange.copy(alpha = 0.2f) else CulturamaCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CulturamaOrange else Color(0xFF333338),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    tempRegion = if (tempRegion == reg) null else reg
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text  = reg,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = if (isSelected) CulturamaOrange else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── 4. Categoria do evento ────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = "Categoria",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = tempCategory == cat.label
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CulturamaOrange.copy(alpha = 0.2f) else CulturamaCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CulturamaOrange else Color(0xFF333338),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    tempCategory = if (tempCategory == cat.label) null else cat.label
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text  = "${cat.emoji} ${cat.label}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color      = if (isSelected) CulturamaOrange else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── 5. Acessibilidade ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CulturamaCard)
                    .clickable { tempAccessibility = !tempAccessibility }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "\u267F", fontSize = 20.sp)
                    Column {
                        Text(
                            text  = "Apenas eventos acess\u00edveis",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color      = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text  = "Com rampa, Libras ou espa\u00e7o adaptado",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Switch(
                    checked         = tempAccessibility,
                    onCheckedChange = { tempAccessibility = it },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CulturamaOrange,
                        uncheckedTrackColor = Color(0xFF2C2C30)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFF2A2A2E), thickness = 0.5.dp)

            // ── 6. Botões: Limpar filtros e Aplicar filtros ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onClear,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, Color(0xFF3A3A3E)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text(
                        text  = "Limpar filtros",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Button(
                    onClick  = {
                        val parsedStart = tempStartDayText.toIntOrNull()
                        val parsedEnd   = tempEndDayText.toIntOrNull()
                        onApply(
                            AdvancedFiltersState(
                                category          = tempCategory,
                                startDay          = parsedStart,
                                endDay            = parsedEnd,
                                pricing           = tempPricing,
                                region            = tempRegion,
                                accessibilityOnly = tempAccessibility
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = CulturamaOrange,
                        contentColor   = Color.White
                    )
                ) {
                    Text(
                        text  = "Aplicar filtros",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ─── Cabeçalho ───────────────────────────────────────────────────────────────

@Composable
private fun EventsHeader(
    searchQuery:    String,
    onSearchChange: (String) -> Unit,
    onClearSearch:  () -> Unit,
    isListView:     Boolean,
    onToggleView:   () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CulturamaSurface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título da tela
        Text(
            text  = "Eventos",
            style = MaterialTheme.typography.headlineLarge.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.Black
            )
        )

        // Campo de busca + botões de visualização
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CulturamaCard)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text  = "\uD83D\uDD0D",
                        fontSize = 14.sp
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text  = "Pesquisar por nome, categoria ou local\u2026",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextTertiary.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value           = searchQuery,
                            onValueChange   = onSearchChange,
                            modifier        = Modifier.fillMaxWidth(),
                            singleLine      = true,
                            textStyle       = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary
                            ),
                            cursorBrush     = SolidColor(CulturamaOrange),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            )
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.Clear,
                                contentDescription = "Limpar pesquisa",
                                tint               = TextTertiary,
                                modifier           = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            ViewToggleButton(
                isActive           = isListView,
                onClick            = { if (!isListView) onToggleView() },
                contentDescription = "Visualiza\u00e7\u00e3o em lista"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(if (idx == 0) 14.dp else if (idx == 1) 11.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isListView) CulturamaOrange else TextTertiary
                                )
                        )
                    }
                }
            }

            ViewToggleButton(
                isActive           = !isListView,
                onClick            = { if (isListView) onToggleView() },
                contentDescription = "Visualiza\u00e7\u00e3o em mapa"
            ) {
                Icon(
                    imageVector        = CulturamaIcons.MapPin,
                    contentDescription = null,
                    tint               = if (!isListView) CulturamaOrange else TextTertiary,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ViewToggleButton(
    isActive:          Boolean,
    onClick:           () -> Unit,
    contentDescription: String,
    content:           @Composable () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isActive) CulturamaOrange.copy(alpha = 0.15f) else CulturamaCard,
        animationSpec = tween(200),
        label         = "toggleBg"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ─── Filtros Rápidos ──────────────────────────────────────────────────────────

@Composable
private fun QuickFiltersRow(
    filters:             List<QuickFilter>,
    selectedFilter:      String,
    onFilterSelect:      (String) -> Unit,
    isAdvancedActive:    Boolean,
    onOpenMoreFilters:   () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { chip ->
            QuickFilterChip(
                label      = chip.label,
                isSelected = chip.label == selectedFilter,
                onClick    = { onFilterSelect(chip.label) }
            )
        }

        // Botão "Mais filtros" com clique funcional e indicador visual
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isAdvancedActive) CulturamaOrange.copy(alpha = 0.2f) else CulturamaCard)
                .border(
                    1.dp,
                    if (isAdvancedActive) CulturamaOrange else Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onOpenMoreFilters() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text  = "\u2699",
                style = MaterialTheme.typography.labelMedium.copy(
                    color      = if (isAdvancedActive) CulturamaOrange else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text  = "Mais filtros",
                style = MaterialTheme.typography.labelMedium.copy(
                    color      = if (isAdvancedActive) CulturamaOrange else TextSecondary,
                    fontWeight = if (isAdvancedActive) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        }
    }
}

@Composable
private fun QuickFilterChip(
    label:      String,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) CulturamaOrange else CulturamaCard,
        animationSpec = tween(200),
        label         = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(200),
        label         = "chipText"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color      = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        )
    }
}

// ─── Seção de Categorias ──────────────────────────────────────────────────────

@Composable
private fun CategoriesSection(
    categories:       List<CategoryItem>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        Text(
            text     = "Categorias",
            style    = MaterialTheme.typography.titleMedium.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { cat ->
                CategoryPill(
                    item       = cat,
                    isSelected = selectedCategory == cat.label,
                    onClick    = { onCategorySelect(cat.label) }
                )
            }
        }
    }
}

@Composable
private fun CategoryPill(
    item:       CategoryItem,
    isSelected: Boolean,
    onClick:    () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) CulturamaOrange else Color(0xFF252528),
        animationSpec = tween(220),
        label         = "pillBg"
    )
    val textColor by animateColorAsState(
        targetValue   = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(220),
        label         = "pillText"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = item.emoji, fontSize = 22.sp)
        Text(
            text     = item.label,
            style    = MaterialTheme.typography.labelSmall.copy(
                color      = textColor,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize   = 10.sp
            ),
            maxLines  = 2,
            overflow  = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier  = Modifier.widthIn(max = 70.dp)
        )
    }
}

// ─── Estado vazio ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyEventsState(
    hasActiveFilters: Boolean,
    onClearFilters:   () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "\uD83D\uDD0D", fontSize = 48.sp)
        Text(
            text  = "Nenhum evento encontrado",
            style = MaterialTheme.typography.titleMedium.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text      = "Nenhum evento corresponde aos crit\u00e9rios de pesquisa e filtros selecionados.",
            style     = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
            textAlign = TextAlign.Center
        )

        if (hasActiveFilters) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = onClearFilters,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = CulturamaOrange,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text  = "Limpar filtros e ver todos",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

// ─── Helpers de emoji/gradiente ──────────────────────────────────────────────

internal fun eventsScreenEmoji(category: String): String = when {
    category.contains("Hip-Hop",   ignoreCase = true) -> "\uD83C\uDFA4"
    category.contains("Samba",     ignoreCase = true) -> "\uD83E\uDD41"
    category.contains("Capoeira",  ignoreCase = true) -> "\uD83E\uDD4B"
    category.contains("Sarau",     ignoreCase = true) -> "\uD83D\uDCD6"
    category.contains("Feira",     ignoreCase = true) -> "\uD83C\uDFAA"
    category.contains("M\u00fasica",   ignoreCase = true) -> "\uD83C\uDFB8"
    category.contains("Esporte",   ignoreCase = true) -> "\u26BD"
    category.contains("Geek",      ignoreCase = true) -> "\uD83C\uDFAE"
    category.contains("Dan\u00e7a",    ignoreCase = true) -> "\uD83D\uDC83"
    category.contains("C\u00eanica",   ignoreCase = true) -> "\uD83C\uDFAD"
    category.contains("Arte",      ignoreCase = true) -> "\uD83C\uDFA8"
    category.contains("Oficina",   ignoreCase = true) -> "\uD83D\uDD27"
    else                                              -> "\uD83C\uDFAD"
}

internal fun eventsScreenGradient(category: String): List<Color> = when {
    category.contains("Hip-Hop",  ignoreCase = true) -> PlaceholderHipHop
    category.contains("Samba",    ignoreCase = true) -> PlaceholderSamba
    category.contains("Capoeira", ignoreCase = true) -> PlaceholderCapoeira
    category.contains("Sarau",    ignoreCase = true) -> PlaceholderSarau
    category.contains("Feira",    ignoreCase = true) -> PlaceholderFeira
    category.contains("M\u00fasica",  ignoreCase = true) -> PlaceholderFestival
    category.contains("Esporte",  ignoreCase = true) -> listOf(Color(0xFF0A1A0A), Color(0xFF1A4A1A))
    category.contains("Geek",     ignoreCase = true) -> listOf(Color(0xFF0A0A2A), Color(0xFF1A1A5A))
    category.contains("Dan\u00e7a",   ignoreCase = true) -> listOf(Color(0xFF1A001A), Color(0xFF4A004A))
    category.contains("C\u00eanica",  ignoreCase = true) -> listOf(Color(0xFF1A0A00), Color(0xFF5A2A00))
    else                                             -> PlaceholderBatalha
}
