package com.example.culturama.ui.component

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size as Tamanho
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import coil.transform.Transformation
import com.example.culturama.ui.theme.CulturamaCard
import kotlin.math.max
import kotlin.math.min

/**
 * Capa em destaque no retângulo fixo do card.
 * Fotos próximas do formato do card preenchem a área.
 * Cartaz vertical cresce até dominar a altura, com corte leve, e o que sobra
 * fica na mesma imagem suavizada. O desfoque não vai para a imagem principal.
 */
@Composable
fun CapaAdaptativa(
    imageUrl: String?,
    label: String,
    emoji: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    fundo: Color = CulturamaCard,
    velatura: Color = Color(0x66000000)
) {
    val url = imageUrl?.takeIf { it.isNotBlank() }
    val contexto = LocalContext.current
    val pedidoFundo = remember(url) {
        ImageRequest.Builder(contexto)
            .data(url)
            .size(80)
            .precision(Precision.INEXACT)
            .transformations(FundoSuave)
            .crossfade(180)
            .build()
    }
    val pedidoNitido = remember(url) {
        ImageRequest.Builder(contexto)
            .data(url)
            .scale(Scale.FILL)
            .crossfade(220)
            .build()
    }
    var falhou by remember(url) { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().background(fundo))
        if (url != null && !falhou) {
            AsyncImage(
                model = pedidoFundo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Low,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(velatura))
            AsyncImage(
                model = pedidoNitido,
                contentDescription = contentDescription,
                contentScale = PresencaDaCapa,
                alignment = Alignment.Center,
                filterQuality = FilterQuality.High,
                onError = { falhou = true },
                modifier = Modifier.fillMaxSize()
            )
        }
        if (url == null || falhou) {
            EventImagePlaceholder(
                label = label,
                emoji = emoji,
                gradient = gradient,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Preenche o card sem distorcer.
 * Foto horizontal ou próxima do retângulo entra como crop.
 * Cartaz mais alto que o card ocupa a altura inteira e avança na largura,
 * escondendo no máximo cerca de 28% da arte.
 */
private object PresencaDaCapa : ContentScale {
    override fun computeScaleFactor(srcSize: Tamanho, dstSize: Tamanho): ScaleFactor {
        if (srcSize.width <= 0f || srcSize.height <= 0f || dstSize.width <= 0f || dstSize.height <= 0f) {
            return ScaleFactor(1f, 1f)
        }
        val ajuste = ContentScale.Fit.computeScaleFactor(srcSize, dstSize).scaleX
        val preenchimento = ContentScale.Crop.computeScaleFactor(srcSize, dstSize).scaleX
        val proporcaoDaImagem = srcSize.width / srcSize.height
        val proporcaoDoCard = dstSize.width / dstSize.height
        val cartazVertical = proporcaoDaImagem < proporcaoDoCard * 0.9f
        if (!cartazVertical || preenchimento <= ajuste) {
            return ScaleFactor(preenchimento, preenchimento)
        }
        val coberturaNoAjuste = ajuste / preenchimento
        val paraPreencher = ajuste * (0.76f / coberturaNoAjuste)
        val paraPreservar = ajuste / 0.72f
        val escala = min(preenchimento, max(ajuste, min(paraPreencher, paraPreservar)))
        return ScaleFactor(escala, escala)
    }
}

/**
 * Roda uma vez por URL, fora da rolagem, e o Coil guarda o resultado.
 * A amostra fica pequena de propósito: o fundo só completa as laterais.
 */
private object FundoSuave : Transformation {
    override val cacheKey: String = "culturama-fundo-suave-v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val argb = input.copy(Bitmap.Config.ARGB_8888, false) ?: return input
        try {
            return suavizar(argb)
        } finally {
            if (!argb.isRecycled) argb.recycle()
        }
    }
}

private fun suavizar(origem: Bitmap): Bitmap {
    val largura = origem.width
    val altura = origem.height
    if (largura < 2 || altura < 2) {
        return origem.copy(Bitmap.Config.ARGB_8888, false)
            ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
    val pixels = IntArray(largura * altura)
    origem.getPixels(pixels, 0, largura, 0, 0, largura, altura)
    val raio = (minOf(largura, altura) / 8).coerceIn(2, 5)
    val apoio = IntArray(pixels.size)
    repeat(2) {
        borraoHorizontal(pixels, apoio, largura, altura, raio)
        borraoVertical(apoio, pixels, largura, altura, raio)
    }
    val borrada = Bitmap.createBitmap(pixels, largura, altura, Bitmap.Config.ARGB_8888)
    val maiorLado = maxOf(largura, altura)
    if (maiorLado >= 280) return borrada
    val fator = 280f / maiorLado
    val suave = Bitmap.createScaledBitmap(
        borrada,
        (largura * fator).toInt().coerceAtLeast(1),
        (altura * fator).toInt().coerceAtLeast(1),
        true
    )
    borrada.recycle()
    return suave
}

private fun borraoHorizontal(origem: IntArray, destino: IntArray, largura: Int, altura: Int, raio: Int) {
    val janela = raio * 2 + 1
    for (y in 0 until altura) {
        val linha = y * largura
        for (x in 0 until largura) {
            var alpha = 0
            var vermelho = 0
            var verde = 0
            var azul = 0
            for (k in -raio..raio) {
                val cor = origem[linha + (x + k).coerceIn(0, largura - 1)]
                alpha += cor ushr 24
                vermelho += (cor shr 16) and 0xFF
                verde += (cor shr 8) and 0xFF
                azul += cor and 0xFF
            }
            destino[linha + x] = (alpha / janela shl 24) or
                (vermelho / janela shl 16) or
                (verde / janela shl 8) or
                (azul / janela)
        }
    }
}

private fun borraoVertical(origem: IntArray, destino: IntArray, largura: Int, altura: Int, raio: Int) {
    val janela = raio * 2 + 1
    for (x in 0 until largura) {
        for (y in 0 until altura) {
            var alpha = 0
            var vermelho = 0
            var verde = 0
            var azul = 0
            for (k in -raio..raio) {
                val cor = origem[(y + k).coerceIn(0, altura - 1) * largura + x]
                alpha += cor ushr 24
                vermelho += (cor shr 16) and 0xFF
                verde += (cor shr 8) and 0xFF
                azul += cor and 0xFF
            }
            destino[y * largura + x] = (alpha / janela shl 24) or
                (vermelho / janela shl 16) or
                (verde / janela shl 8) or
                (azul / janela)
        }
    }
}
