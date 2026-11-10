package com.example.culturama.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.culturama.R

/**
 * Logo oficial do Culturama (PNG com canal alfa — sem caixa branca/bege).
 */
@Composable
fun CulturamaLogo(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp
) {
    Image(
        painter = painterResource(id = R.drawable.logo_culturama),
        contentDescription = "Culturama",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .widthIn(max = 160.dp)
    )
}
