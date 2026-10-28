package com.example.culturama.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.ui.theme.*

/**
 * Componentes especializados para a tela "Para Organizadores" do Culturama.
 * Fiel às referências desktop e adaptado para a ergonomia mobile.
 */

// ═════════════════════════════════════════════════════════════════════════════
// 1. CABEÇALHO COMPACTO MOBILE
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerTopBar(
    onBackClick: () -> Unit,
    onPrimaryCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Botão voltar
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Logo textual com ponto laranja característico
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Culturama",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            )
            Box(
                modifier = Modifier
                    .padding(start = 4.dp, top = 2.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CulturamaOrange)
            )
        }

        // Acesso rápido CTA no topo
        Button(
            onClick = onPrimaryCtaClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = CulturamaOrange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = "Criar conta",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. SEÇÃO HERO (IMAGEM 2)
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerHeroSection(
    onPrimaryCtaClick: () -> Unit,
    onSecondaryCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animação sutil de pulso para a luz radial de fundo
    val infiniteTransition = rememberInfiniteTransition(label = "hero_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Lista de tags culturais da referência da Imagem 2
    val tags = remember {
        listOf(
            "Batalha de rima",
            "Roda de samba",
            "Encontro de carros",
            "Anime e games",
            "Breaking",
            "Slam",
            "Corrida de rua",
            "Eletrônica",
            "Sarau",
            "Skate"
        )
    }

    var selectedTags by remember { mutableStateOf(setOf("Batalha de rima", "Roda de samba")) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        // Brilho quente radial atmosférico (remetendo ao desktop)
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HeroGradientTop.copy(alpha = glowAlpha),
                            HeroGradientMid.copy(alpha = glowAlpha * 0.5f),
                            Color.Transparent
                        ),
                        radius = 450f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tag em pílula "PARA QUEM ORGANIZA"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E1715),
                border = BorderStroke(1.dp, CulturamaOrange.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "PARA QUEM ORGANIZA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            // Título Principal com destaque em "mapa"
            val annotatedTitle = buildAnnotatedString {
                append("Sua cena no ")
                withStyle(
                    SpanStyle(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Black
                    )
                ) {
                    append("mapa")
                }
                append(" da cidade.")
            }

            Text(
                text = annotatedTitle,
                style = MaterialTheme.typography.displayMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            // Texto descritivo
            Text(
                text = "A batalha, a roda, o encontro ou a festa que hoje só circula no grupo e no story aparece para quem procura o que fazer perto de casa. Cadastro grátis.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = TextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Botões de Ação
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botão Primário: "Criar conta grátis →"
                OrganizerActionButton(
                    text = "Criar conta grátis",
                    showArrow = true,
                    isPrimary = true,
                    onClick = onPrimaryCtaClick,
                    modifier = Modifier.fillMaxWidth()
                )

                // Botão Secundário: "Já tenho conta"
                OrganizerActionButton(
                    text = "Já tenho conta",
                    showArrow = false,
                    isPrimary = false,
                    onClick = onSecondaryCtaClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(28.dp))

            // Nuvem de Tags da Cultura Urbana / Cena Independente (Imagem 2)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    val isSelected = tag in selectedTags
                    OrganizerSceneTagChip(
                        text = tag,
                        isSelected = isSelected,
                        onClick = {
                            selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                        }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. SEÇÃO "O QUE VOCÊ GANHA" (IMAGEM 1)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerBenefitsSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "O que você ganha",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )

        Spacer(Modifier.height(16.dp))

        // 4 Benefícios da Imagem 1 organizados em cards mobile de alto contraste
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrganizerBenefitCard(
                icon = CulturamaIcons.MapPin,
                title = "Seu evento no mapa",
                description = "Quem abre o Culturama vê o pin, a data e o endereço, com rota de carro, metrô, Uber e 99."
            )

            OrganizerBenefitCard(
                icon = CulturamaIcons.Groups,
                title = "Público que já procura",
                description = "A lista e o mapa entregam o evento para quem filtra pela sua cena, pela data e pela distância."
            )

            OrganizerBenefitCard(
                icon = CulturamaIcons.VerifiedBadge,
                title = "Selo de verificado",
                description = "CNPJ ativo pode ser aprovado na hora; CPF passa pela equipe. O selo passa confiança e afasta golpe."
            )

            OrganizerBenefitCard(
                icon = CulturamaIcons.TagFree,
                title = "Sem custo para publicar",
                description = "Cadastro e publicação são grátis. Quem paga são as marcas que querem chegar no seu público."
            )
        }
    }
}

@Composable
private fun OrganizerBenefitCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF18181C),
        border = BorderStroke(1.dp, Color(0xFF282830)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Container do Ícone
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF261A17))
                    .border(1.dp, CulturamaOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CulturamaOrange,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Textos informativos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp
                    )
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. SEÇÃO "COMO FUNCIONA" (IMAGEM 1)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerHowItWorksSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Como funciona",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )

        Spacer(Modifier.height(16.dp))

        // 3 Etapas sequenciais da Imagem 1
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OrganizerStepCard(
                stepNumber = "01",
                icon = CulturamaIcons.AccountCreate,
                title = "Crie a conta",
                description = "E-mail e senha ou Google. Leva um minuto."
            )

            OrganizerStepCard(
                stepNumber = "02",
                icon = CulturamaIcons.OrganizerBadge,
                title = "Cadastre-se como organizador",
                description = "CPF ou CNPJ, nome do coletivo ou da empresa e um documento."
            )

            OrganizerStepCard(
                stepNumber = "03",
                icon = CulturamaIcons.PublishEvent,
                title = "Publique o evento",
                description = "Data, local, capa e link do ingresso. Depois da aprovação, ele entra no mapa e na lista."
            )
        }
    }
}

@Composable
private fun OrganizerStepCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF151518),
        border = BorderStroke(1.dp, Color(0xFF26262E)),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Número com destaque na identidade visual
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF201614))
                    .border(1.dp, CulturamaOrange.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CulturamaOrange,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.5.sp
                        )
                    )
                }

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. SEÇÃO COLETIVO & CTA FINAL (IMAGEM 3)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerCollectiveCtaSection(
    onPrimaryCtaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF171315),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    CulturamaOrange.copy(alpha = 0.5f),
                    Color(0xFF2A2A34)
                )
            )
        ),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HeroGradientTop.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        radius = 400f
                    )
                )
                .padding(vertical = 28.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Coletivo, assessoria ou clube com vários eventos?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        lineHeight = 27.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Um cadastro só publica todos os seus eventos, com o mesmo selo de verificado.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(6.dp))

                OrganizerActionButton(
                    text = "Criar conta grátis",
                    showArrow = true,
                    isPrimary = true,
                    onClick = onPrimaryCtaClick,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. CHIP DE CENA CULTURAL INTERATIVO
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerSceneTagChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tag_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2A1C18) else Color(0xFF1A1A1E),
        label = "tag_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CulturamaOrange.copy(alpha = 0.8f) else Color(0xFF2C2C35),
        label = "tag_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) CulturamaOrange else TextSecondary,
        label = "tag_text"
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(CulturamaOrange)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.5.sp
                )
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. BOTÕES DE AÇÃO REUTILIZÁVEIS COM FEEDBACK TÁTIL
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun OrganizerActionButton(
    text: String,
    showArrow: Boolean,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "button_scale"
    )

    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CulturamaOrange,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(26.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (showArrow) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = CulturamaIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .scale(scale)
                .height(52.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF202025),
                contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, Color(0xFF33333E)),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            )
        }
    }
}
