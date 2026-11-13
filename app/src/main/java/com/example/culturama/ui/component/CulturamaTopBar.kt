package com.example.culturama.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.ui.theme.*

/**
 * Cabeçalho do aplicativo Culturama.
 * Em telas compactas exibe logo + menu hambúrguer com menu expansível.
 * Os itens de navegação são passados como lambdas para manter o componente desacoplado.
 */
@Composable
fun CulturamaTopBar(
    onEventsClick: () -> Unit,
    onMapClick: () -> Unit,
    onOrganizersClick: () -> Unit,
    onCompaniesClick: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPreferencesClick: () -> Unit = {},
    onPlanosClick: () -> Unit = {},
    isLoggedIn: Boolean = false,
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Transparent)
    ) {
        // ── Barra principal ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CulturamaLogo(height = 28.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão Criar conta — só para quem ainda não entrou
                if (!isLoggedIn) {
                    Button(
                        onClick = onSignUpClick,
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = CulturamaOrange,
                            contentColor   = Color.White
                        ),
                        shape   = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text  = "Criar conta",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp)
                        )
                    }
                }

                // Menu hambúrguer
                IconButton(
                    onClick = { menuExpanded = !menuExpanded },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (menuExpanded) Icons.Default.Close else Icons.Default.Menu,
                        contentDescription = if (menuExpanded) "Fechar menu" else "Abrir menu",
                        tint   = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ── Menu expansível ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = menuExpanded,
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xE6161618), Color(0xF0161618))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isLoggedIn) {
                    NavMenuItem("Perfil", onProfileClick) { menuExpanded = false }
                    NavMenuItem("Meus Interesses", onPreferencesClick) { menuExpanded = false }
                }
                NavMenuItem("Eventos",            onEventsClick)     { menuExpanded = false }
                NavMenuItem("Mapa",               onMapClick)        { menuExpanded = false }
                NavMenuItem("Planos",             onPlanosClick)     { menuExpanded = false }
                NavMenuItem("Para organizadores", onOrganizersClick) { menuExpanded = false }
                NavMenuItem("Para empresas",      onCompaniesClick)  { menuExpanded = false }

                HorizontalDivider(
                    modifier  = Modifier.padding(vertical = 8.dp),
                    color     = TextTertiary.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )

                // Entrar ou Sair — estilo texto
                TextButton(
                    onClick  = {
                        if (isLoggedIn) onLogoutClick() else onLoginClick()
                        menuExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text  = if (isLoggedIn) "Sair" else "Entrar",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavMenuItem(
    label:     String,
    onClick:   () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(); onDismiss() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color      = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
