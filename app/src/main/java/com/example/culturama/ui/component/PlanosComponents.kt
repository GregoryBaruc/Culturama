package com.example.culturama.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.culturama.data.planos.AcaoDoPlano
import com.example.culturama.data.planos.DestaqueDoPlano
import com.example.culturama.data.planos.LinhaComparacao
import com.example.culturama.data.planos.PlanoApresentacao
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.CulturamaOrangeDeep
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import com.example.culturama.ui.theme.TextTertiary

@Composable
fun CartaoDePlano(
    plano: PlanoApresentacao,
    onAcao: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(18.dp)
    val borda = when (plano.destaque) {
        DestaqueDoPlano.SIMPLES -> Color(0xFF2A2A32)
        DestaqueDoPlano.INTERMEDIARIO -> CulturamaOrange.copy(alpha = 0.55f)
        DestaqueDoPlano.PREMIUM -> CulturamaOrange
    }
    val fundo = when (plano.destaque) {
        DestaqueDoPlano.SIMPLES -> CulturamaCard
        DestaqueDoPlano.INTERMEDIARIO -> Color(0xFF1E1814)
        DestaqueDoPlano.PREMIUM -> Color(0xFF241812)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(fundo)
            .border(width = if (plano.destaque == DestaqueDoPlano.PREMIUM) 1.5.dp else 1.dp, color = borda, shape = forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = plano.categoria.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CulturamaOrange,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            )
            if (plano.destaque == DestaqueDoPlano.PREMIUM) {
                Text(
                    text = "DESTAQUE",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CulturamaOrange.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Text(
            text = plano.nome,
            style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = plano.preco,
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (plano.preco == "R$ 0") CulturamaOrange else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            text = plano.publico,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            plano.beneficios.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = CulturamaIcons.Checkmark,
                        contentDescription = null,
                        tint = CulturamaOrange,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(14.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary,
                            lineHeight = 19.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        plano.nota?.let { aviso ->
            Text(
                text = aviso,
                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, lineHeight = 17.sp)
            )
        }

        when (plano.acao) {
            AcaoDoPlano.EM_BREVE -> {
                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = CulturamaOrangeDeep.copy(alpha = 0.35f),
                        disabledContentColor = Color.White.copy(alpha = 0.85f)
                    )
                ) {
                    Text(plano.botao, fontWeight = FontWeight.Bold)
                }
            }
            AcaoDoPlano.CADASTRO_ORGANIZADOR,
            AcaoDoPlano.CADASTRO_EMPRESA -> {
                Button(
                    onClick = onAcao,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CulturamaOrange,
                        contentColor = Color.White
                    )
                ) {
                    Text(plano.botao, fontWeight = FontWeight.Bold)
                }
            }
            AcaoDoPlano.CONHECER_EMPRESAS -> {
                OutlinedButton(
                    onClick = onAcao,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(plano.botao, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SecaoComparacaoDePlanos(
    titulo: String,
    nomes: List<String>,
    linhas: List<LinhaComparacao>,
    rodape: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        linhas.forEach { linha ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = linha.criterio,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = CulturamaOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    linha.valores.forEachIndexed { indice, valor ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF141418))
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = nomes.getOrElse(indice) { "" },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextTertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = valor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
        rodape?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall.copy(color = TextTertiary, lineHeight = 16.sp)
            )
        }
    }
}

@Composable
fun CartaoHubDePlanos(
    titulo: String,
    resumo: String,
    icone: ImageVector,
    cor: Color,
    rotuloBotao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CulturamaCard)
            .border(1.dp, cor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(28.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge.copy(color = cor, fontWeight = FontWeight.Bold)
        )
        Text(
            text = resumo,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, lineHeight = 20.sp)
        )
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cor, contentColor = Color.White)
        ) {
            Text(rotuloBotao, fontWeight = FontWeight.Bold)
        }
    }
}
