package com.example.culturama.ui.component

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.culturama.model.EventModel
import com.example.culturama.ui.theme.*

/**
 * Bottom Sheet de detalhes do evento.
 * Exibe imagem, nome, categoria, data, horário, local e preço do evento selecionado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailBottomSheet(
    event:           EventModel,
    onDismiss:       () -> Unit,
    imageContent:    @Composable BoxScope.() -> Unit
) {
    val context    = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val abrirLink: (String) -> Unit = { url -> runCatching { uriHandler.openUri(url) } }
    val linkPrincipal = event.ticketUrl ?: event.moreInfoUrl ?: event.siteUrl

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = CulturamaSurface,
        contentColor     = TextPrimary,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextTertiary.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Imagem de capa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
                content = imageContent
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Categoria
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CulturamaOrange.copy(alpha = 0.15f)
                ) {
                    Text(
                        text     = event.category,
                        style    = MaterialTheme.typography.labelMedium.copy(
                            color      = CulturamaOrange,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Título
                Text(
                    text  = event.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                )

                // Descrição
                if (event.description.isNotBlank()) {
                    Text(
                        text  = event.description,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                } else if (!event.detailsLoaded) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color    = CulturamaOrange
                    )
                }

                HorizontalDivider(color = TextTertiary.copy(alpha = 0.2f))

                // Data e hora
                InfoRow(
                    icon  = Icons.Default.DateRange,
                    label = "Data e hora",
                    value = "${event.dayOfWeek}, ${event.dayOfMonth} de ${event.monthAbbr} · ${event.time}"
                )

                // Local
                InfoRow(
                    icon  = Icons.Default.LocationOn,
                    label = "Local",
                    value = event.venueName
                )

                // Preço
                HorizontalDivider(color = TextTertiary.copy(alpha = 0.2f))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text  = "Ingresso",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (event.isFree) Color(0xFF1A3A1A) else CulturamaCard
                    ) {
                        Text(
                            text  = if (event.isFree) "Gratuito" else event.price,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color      = if (event.isFree) Color(0xFF4CAF50) else TextPrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                if (event.organizerName.isNotBlank()) {
                    Text(
                        text  = "Organizado por ${event.organizerName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary)
                    )
                }

                // Crédito da fonte: combinado com a Prefeitura de Santo André no CulturAZ
                if (!event.sourceName.isNullOrBlank()) {
                    val link = event.sourceUrl
                    Text(
                        text  = "Fonte: ${event.sourceName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color          = if (link != null) CulturamaOrange else TextTertiary,
                            textDecoration = if (link != null) TextDecoration.Underline else null
                        ),
                        modifier = if (link != null) {
                            Modifier.clickable(onClickLabel = "Abrir evento original") { abrirLink(link) }
                        } else {
                            Modifier
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (linkPrincipal != null) {
                    Button(
                        onClick = { abrirLink(linkPrincipal) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CulturamaOrange,
                            contentColor   = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text  = if (event.ticketUrl != null) "Ver ingressos" else "Saiba mais",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                event.siteUrl?.let { url ->
                    TextButton(
                        onClick = {
                            val envio = Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, url)
                            context.startActivity(Intent.createChooser(envio, "Compartilhar evento"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text  = "Compartilhar",
                            style = MaterialTheme.typography.labelLarge.copy(color = CulturamaOrange)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon:  androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = CulturamaOrange,
            modifier           = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextTertiary)
            )
            Text(
                text  = value,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
            )
        }
    }
}
