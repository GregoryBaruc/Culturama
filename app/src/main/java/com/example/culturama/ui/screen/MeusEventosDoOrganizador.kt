package com.example.culturama.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.culturama.data.acesso.EventoDoMural
import com.example.culturama.data.acesso.FiltroDosEventos
import com.example.culturama.data.acesso.filtrar
import com.example.culturama.data.acesso.rotuloDoStatus
import com.example.culturama.data.admin.lerInstante
import com.example.culturama.data.admin.textoDaData
import com.example.culturama.data.admin.textoDoDia
import com.example.culturama.data.admin.diaDoSeletor
import com.example.culturama.data.profile.CatalogoCultural
import com.example.culturama.data.profile.GrupoCultural
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary
import java.util.Calendar
import java.util.TimeZone

/**
 * Lista e formulário dos eventos da organização já autorizada.
 * A gravação não existe na API do aplicativo: o formulário não publica nem altera o servidor.
 */
@Composable
fun MeusEventosDoOrganizador(
    eventos: List<EventoDoMural>?,
    abrirFormulario: Boolean,
    onFormularioAberto: () -> Unit,
    onVoltar: () -> Unit,
    onAbrirDetalhe: (String) -> Unit
) {
    var criando by rememberSaveable { mutableStateOf(false) }
    var editandoId by rememberSaveable { mutableStateOf<String?>(null) }
    var filtro by rememberSaveable { mutableStateOf(FiltroDosEventos.TODOS.name) }
    var busca by rememberSaveable { mutableStateOf("") }
    var menuId by rememberSaveable { mutableStateOf<String?>(null) }
    val filtroAtual = FiltroDosEventos.entries.firstOrNull { it.name == filtro } ?: FiltroDosEventos.TODOS

    LaunchedEffect(abrirFormulario) {
        if (abrirFormulario) {
            criando = true
            editandoId = null
            onFormularioAberto()
        }
    }

    val editando = eventos?.firstOrNull { it.id == editandoId }
    if (criando || editandoId != null) {
        FormularioDeEvento(
            evento = if (criando) null else editando,
            onVoltar = {
                criando = false
                editandoId = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            Text(
                "Meus eventos",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (eventos == null) "A quantidade não pôde ser consultada." else quantidade(eventos.size),
                color = TextSecondary
            )
            Button(
                onClick = { criando = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
            ) { Text("Criar evento", fontWeight = FontWeight.Bold) }
            Campo(
                valor = busca,
                aoMudar = { busca = it },
                rotulo = "Pesquisar por nome"
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroDosEventos.entries.forEach { item ->
                    FilterChip(
                        selected = filtroAtual == item,
                        onClick = { filtro = item.name },
                        label = { Text(item.rotulo) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CulturamaOrange,
                            selectedLabelColor = Color.White,
                            containerColor = CulturamaCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
            when {
                eventos == null -> Caixa {
                    Text("Não foi possível consultar os eventos desta organização.", color = TextSecondary)
                }
                else -> {
                    val visiveis = eventos.filtrar(filtroAtual, busca, System.currentTimeMillis())
                    if (visiveis.isEmpty()) {
                        Caixa { Text("Nenhum evento neste filtro.", color = TextSecondary) }
                    } else {
                        visiveis.forEach { evento ->
                            CartaoDeEvento(
                                evento = evento,
                                menuAberto = menuId == evento.id,
                                onMenu = { menuId = if (menuId == evento.id) null else evento.id },
                                onFecharMenu = { menuId = null },
                                onDetalhe = {
                                    menuId = null
                                    onAbrirDetalhe(evento.id)
                                },
                                onEditar = {
                                    menuId = null
                                    editandoId = evento.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoDeEvento(
    evento: EventoDoMural,
    menuAberto: Boolean,
    onMenu: () -> Unit,
    onFecharMenu: () -> Unit,
    onDetalhe: () -> Unit,
    onEditar: () -> Unit
) {
    Caixa {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CulturamaOrange.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.DateRange, contentDescription = null, tint = CulturamaOrange)
                if (evento.imagemCapa != null) {
                    AsyncImage(
                        model = evento.imagemCapa,
                        contentDescription = "Imagem de ${evento.titulo}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    evento.titulo,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(evento.categoriaNome ?: "Sem categoria", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                val inicio = lerInstante(evento.dataHora)
                Text(
                    if (inicio == null) "Data não informada" else textoDaData(inicio),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    evento.endereco ?: "Local não informado",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(rotuloDoStatus(evento.status), color = CulturamaOrange, style = MaterialTheme.typography.labelLarge)
            }
            Box {
                IconButton(onClick = onMenu) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Ações de ${evento.titulo}", tint = TextPrimary)
                }
                DropdownMenu(expanded = menuAberto, onDismissRequest = onFecharMenu) {
                    DropdownMenuItem(text = { Text("Ver detalhe") }, onClick = onDetalhe)
                    DropdownMenuItem(text = { Text("Editar") }, onClick = onEditar)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioDeEvento(
    evento: EventoDoMural?,
    onVoltar: () -> Unit
) {
    val chave = evento?.id ?: "novo"
    var nome by rememberSaveable(chave) { mutableStateOf(evento?.titulo?.takeUnless { it == "Sem título" }.orEmpty()) }
    var descricao by rememberSaveable(chave) { mutableStateOf(evento?.descricao.orEmpty()) }
    var categoriaId by rememberSaveable(chave) { mutableStateOf(evento?.categoriaId) }
    var subcategoriaId by rememberSaveable(chave) { mutableStateOf<String?>(null) }
    var imagemLocal by rememberSaveable(chave) { mutableStateOf<String?>(null) }
    var diaInicio by rememberSaveable(chave) { mutableStateOf<Long?>(null) }
    var horaInicio by rememberSaveable(chave) { mutableStateOf<Int?>(null) }
    var minutoInicio by rememberSaveable(chave) { mutableStateOf<Int?>(null) }
    var diaFim by rememberSaveable(chave) { mutableStateOf<Long?>(null) }
    var horaFim by rememberSaveable(chave) { mutableStateOf<Int?>(null) }
    var minutoFim by rememberSaveable(chave) { mutableStateOf<Int?>(null) }
    var nomeLocal by rememberSaveable(chave) { mutableStateOf("") }
    var endereco by rememberSaveable(chave) { mutableStateOf(evento?.endereco.orEmpty()) }
    var cidade by rememberSaveable(chave) { mutableStateOf("") }
    var estado by rememberSaveable(chave) { mutableStateOf("") }
    var erroNome by rememberSaveable(chave) { mutableStateOf(false) }
    var erroData by rememberSaveable(chave) { mutableStateOf(false) }
    var aviso by rememberSaveable(chave) { mutableStateOf(false) }
    var grupos by remember(chave) { mutableStateOf<List<GrupoCultural>>(emptyList()) }
    var seletor by remember(chave) { mutableStateOf<String?>(null) }

    val escolherImagem = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imagemLocal = uri?.toString()
    }

    LaunchedEffect(chave) {
        grupos = runCatching { CatalogoCultural.grupos(ApiClient.api.categorias()) }.getOrDefault(emptyList())
        val grupo = grupos.firstOrNull { grupo ->
            grupo.id == evento?.categoriaId || grupo.opcoes.any { it.id == evento?.categoriaId }
        }
        if (grupo != null && grupo.opcoes.any { it.id == evento?.categoriaId && it.id != grupo.id }) {
            categoriaId = grupo.id
            subcategoriaId = evento?.categoriaId
        }
    }

    if (aviso) {
        AlertDialog(
            onDismissRequest = { aviso = false },
            title = { Text(if (evento == null) "Criar evento" else "Editar evento") },
            text = {
                Text("O servidor ainda não recebe a criação nem a edição de eventos pelo aplicativo. Nada foi publicado nem alterado.")
            },
            confirmButton = { TextButton(onClick = { aviso = false }) { Text("Entendi") } }
        )
    }

    val grupoEscolhido = grupos.firstOrNull { it.id == categoriaId }
    val temSubcategoria = grupoEscolhido?.opcoes?.any { it.id != grupoEscolhido.id } == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CulturamaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
            Text(
                if (evento == null) "Criar evento" else "Editar evento",
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloDeBloco("Informações principais")
            Campo(nome, { nome = it; erroNome = false }, "Nome do evento", erro = erroNome, apoio = if (erroNome) "Informe o nome." else null)
            Campo(descricao, { descricao = it }, "Descrição", linhas = 4)
            Seletor(
                rotulo = "Categoria",
                valor = grupoEscolhido?.nome ?: if (grupos.isEmpty()) "Categorias indisponíveis" else "Escolher",
                onClick = { if (grupos.isNotEmpty()) seletor = "categoria" }
            )
            if (temSubcategoria) {
                Seletor(
                    rotulo = "Subcategoria",
                    valor = grupoEscolhido?.opcoes?.firstOrNull { it.id == subcategoriaId }?.nome ?: "Escolher",
                    onClick = { seletor = "subcategoria" }
                )
            }
            Text("Imagem de divulgação", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            val previa = imagemLocal ?: evento?.imagemCapa
            if (previa != null) {
                AsyncImage(
                    model = previa,
                    contentDescription = "Prévia da imagem",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp))
                )
            }
            TextButton(onClick = { escolherImagem.launch("image/*") }) { Text("Escolher imagem") }
            Text(
                "A imagem escolhida fica neste aparelho. O servidor ainda não recebe o arquivo.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            TituloDeBloco("Data e horário")
            Seletor(
                "Data de início",
                rotuloDia(diaInicio, evento?.dataHora, "Escolher data"),
                { seletor = "diaInicio" },
                apoio = if (erroData) "Informe a data de início." else null
            )
            Seletor(
                "Horário de início",
                rotuloHora(horaInicio, minutoInicio, evento?.dataHora, "Escolher horário"),
                { seletor = "horaInicio" }
            )
            Seletor("Data de término", rotuloDia(diaFim, evento?.dataFim, "Opcional"), { seletor = "diaFim" })
            Seletor(
                "Horário de término",
                rotuloHora(horaFim, minutoFim, evento?.dataFim, "Opcional"),
                { seletor = "horaFim" }
            )

            TituloDeBloco("Localização")
            Campo(nomeLocal, { nomeLocal = it }, "Nome do local")
            Campo(endereco, { endereco = it }, "Endereço")
            Campo(cidade, { cidade = it }, "Cidade")
            Campo(estado, { estado = it }, "Estado")
            Text(
                "O servidor guarda o local em um único endereço. Nome do local, cidade e estado ainda não têm coluna própria e não serão gravados.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = {
                    erroNome = nome.isBlank()
                    erroData = diaInicio == null && lerInstante(evento?.dataHora) == null
                    if (!erroNome && !erroData) aviso = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CulturamaOrange)
            ) { Text("Salvar", fontWeight = FontWeight.Bold) }
        }
    }

    when (seletor) {
        "categoria" -> ListaDeOpcoes(
            titulo = "Categoria",
            opcoes = grupos.map { it.id to it.nome },
            onEscolher = {
                categoriaId = it
                subcategoriaId = null
                seletor = null
            },
            onFechar = { seletor = null }
        )
        "subcategoria" -> ListaDeOpcoes(
            titulo = "Subcategoria",
            opcoes = grupoEscolhido?.opcoes?.filter { it.id != grupoEscolhido.id }?.map { it.id to it.nome }.orEmpty(),
            onEscolher = { subcategoriaId = it; seletor = null },
            onFechar = { seletor = null }
        )
        "diaInicio" -> EscolherDia(diaInicio, { diaInicio = it; erroData = false; seletor = null }, { seletor = null })
        "diaFim" -> EscolherDia(diaFim, { diaFim = it; seletor = null }, { seletor = null })
        "horaInicio" -> EscolherHora(horaInicio, minutoInicio, { h, m -> horaInicio = h; minutoInicio = m; seletor = null }, { seletor = null })
        "horaFim" -> EscolherHora(horaFim, minutoFim, { h, m -> horaFim = h; minutoFim = m; seletor = null }, { seletor = null })
    }
}

@Composable
private fun Campo(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    linhas: Int = 1,
    erro: Boolean = false,
    apoio: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(rotulo) },
        minLines = linhas,
        isError = erro,
        supportingText = apoio?.let { { Text(it) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = CulturamaOrange,
            focusedBorderColor = CulturamaOrange,
            unfocusedBorderColor = TextTertiary,
            focusedLabelColor = CulturamaOrange,
            unfocusedLabelColor = TextSecondary,
            errorBorderColor = CulturamaOrange,
            errorLabelColor = CulturamaOrange
        )
    )
}

@Composable
private fun Seletor(rotulo: String, valor: String, onClick: () -> Unit, apoio: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(rotulo, color = TextSecondary, style = MaterialTheme.typography.labelLarge)
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(valor, modifier = Modifier.fillMaxWidth(), color = TextPrimary)
        }
        if (apoio != null) Text(apoio, color = CulturamaOrange, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ListaDeOpcoes(
    titulo: String,
    opcoes: List<Pair<String, String>>,
    onEscolher: (String) -> Unit,
    onFechar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = {
            Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                opcoes.forEach { (id, nome) ->
                    TextButton(onClick = { onEscolher(id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(nome, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onFechar) { Text("Fechar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscolherDia(atual: Long?, onConfirmar: (Long) -> Unit, onFechar: () -> Unit) {
    val estado = rememberDatePickerState(initialSelectedDateMillis = atual)
    DatePickerDialog(
        onDismissRequest = onFechar,
        confirmButton = {
            TextButton(onClick = { estado.selectedDateMillis?.let(onConfirmar) }) { Text("Ok") }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    ) { DatePicker(state = estado) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EscolherHora(
    hora: Int?,
    minuto: Int?,
    onConfirmar: (Int, Int) -> Unit,
    onFechar: () -> Unit
) {
    val estado = rememberTimePickerState(initialHour = hora ?: 19, initialMinute = minuto ?: 0, is24Hour = true)
    AlertDialog(
        onDismissRequest = onFechar,
        confirmButton = { TextButton(onClick = { onConfirmar(estado.hour, estado.minute) }) { Text("Ok") } },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } },
        text = { TimePicker(state = estado) },
        title = { Text("Horário") }
    )
}

@Composable
private fun TituloDeBloco(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
}

@Composable
private fun Caixa(conteudo: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CulturamaCard)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) { conteudo() }
}

private fun quantidade(total: Int): String = if (total == 1) "1 evento" else "$total eventos"

private fun horaDe(iso: String?): Int? = parte(iso, Calendar.HOUR_OF_DAY)
private fun minutoDe(iso: String?): Int? = parte(iso, Calendar.MINUTE)

private fun parte(iso: String?, campo: Int): Int? {
    val instante = lerInstante(iso) ?: return null
    return Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).apply { timeInMillis = instante }.get(campo)
}

private fun textoHorario(hora: Int?, minuto: Int?): String {
    val h = hora ?: return "Escolher horário"
    val min = minuto ?: 0
    return if (min == 0) "${h}h" else "${h}h%02d".format(min)
}

private fun rotuloDia(escolhido: Long?, iso: String?, vazio: String): String {
    if (escolhido != null) return textoDoDia(diaDoSeletor(escolhido))
    val instante = lerInstante(iso) ?: return vazio
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo")).apply { timeInMillis = instante }
    val dia = calendario.get(Calendar.DAY_OF_MONTH)
    val mes = calendario.get(Calendar.MONTH) + 1
    val ano = calendario.get(Calendar.YEAR)
    return "%02d/%02d/%04d".format(dia, mes, ano)
}

private fun rotuloHora(hora: Int?, minuto: Int?, iso: String?, vazio: String): String {
    if (hora != null) return textoHorario(hora, minuto)
    val existente = horaDe(iso) ?: return vazio
    return textoHorario(existente, minutoDe(iso))
}
