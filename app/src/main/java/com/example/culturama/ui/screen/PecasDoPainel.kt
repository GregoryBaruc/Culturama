package com.example.culturama.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.culturama.ui.theme.CulturamaBackground
import com.example.culturama.ui.theme.CulturamaCard
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextSecondary

@Composable
internal fun ChipDoPainel(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Text(
        text = texto,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selecionado) CulturamaOrange else CulturamaCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelLarge.copy(
            color = if (selecionado) CulturamaBackground else TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
internal fun AvisoCurto(
    texto: String,
    onFechar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CulturamaCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = texto, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        if (onFechar != null) {
            TextButton(onClick = onFechar) { Text("Fechar", color = CulturamaOrange) }
        }
    }
}
