package com.example.culturama.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import kotlin.math.min

/** Posição do marcador selecionado, em pixels do mapa. */
data class PosicaoDoMarcador(
    val x: Float,
    val y: Float,
    val raioPx: Float
)

/** Cores dos marcadores por categoria real do app. */
internal fun corCategoriaMapa(categoria: String): Int {
    val c = categoria.lowercase()
    return when {
        "música" in c || "musica" in c || "samba" in c || "hip" in c || "rap" in c || "sarau" in c -> 0xFFE8835A.toInt()
        "geek" in c || "game" in c || "jog" in c -> 0xFF9B6BFF.toInt()
        "feira" in c || "gastro" in c || "comida" in c -> 0xFFF0C040.toInt()
        "arte" in c || "teatro" in c || "cênic" in c || "cenic" in c || "cinema" in c -> 0xFF4C8DFF.toInt()
        "grafite" in c || "oficina" in c || "exposição" in c || "exposicao" in c -> 0xFFFF5CA8.toInt()
        "esporte" in c || "skate" in c || "capoeira" in c || "dança" in c || "danca" in c -> 0xFF3DCC7A.toInt()
        "cultura" in c || "popular" in c -> 0xFFE85A5A.toInt()
        else -> 0xFFE8835A.toInt()
    }
}

/** Inicial da categoria. Eventos sem foto usam essa letra no círculo. */
internal fun letraDoMarcador(categoria: String): String {
    val caractere = categoria.trim().firstOrNull { it.isLetterOrDigit() }
    return (caractere ?: 'C').uppercaseChar().toString()
}

internal fun corDoSimbolo(categoria: String): Int = corLegivelNoFundo(corCategoriaMapa(categoria))

private fun corLegivelNoFundo(fundo: Int): Int {
    val vermelho = (fundo shr 16) and 0xFF
    val verde = (fundo shr 8) and 0xFF
    val azul = fundo and 0xFF
    val luminancia = 0.299 * vermelho + 0.587 * verde + 0.114 * azul
    return if (luminancia > 170) 0xFF1A1A1C.toInt() else 0xFFFFFFFF.toInt()
}

internal fun raioDoMarcadorSelecionado(density: Float): Float =
    medirMarcador(density, selecionado = true, compacto = false).raioTotal

internal fun iconeMarcador(
    density: Float,
    categoria: String,
    selecionado: Boolean,
    compacto: Boolean,
    foto: Bitmap?
): Drawable {
    val bmp = if (foto == null || foto.isRecycled || foto.width <= 0 || foto.height <= 0) {
        miniaturaPadrao(density, categoria, selecionado, compacto)
    } else {
        desenharMarcador(density, categoria, selecionado, compacto, foto)
    }
    bmp.density = Bitmap.DENSITY_NONE
    return BitmapDrawable(null, bmp).apply {
        setBounds(0, 0, bmp.width, bmp.height)
    }
}

/**
 * Fila curta de miniaturas. A imagem decodificada fica pequena e é
 * reaproveitada; o mapa não redesenha a foto a cada movimento.
 */
internal class FilaDeMiniaturas(context: Context) {
    private val app = context.applicationContext
    private val loader = app.imageLoader
    private val fotos = object : LruCache<String, Bitmap>(12 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }
    private val fila = ArrayDeque<String>()
    private val emVoo = HashSet<String>()
    private val falhas = HashSet<String>()
    private val tentativas = HashMap<String, Int>()
    private val ouvintes = HashMap<String, MutableMap<String, (Bitmap?) -> Unit>>()
    private var ativos = 0
    private var ativo = true

    fun pronta(url: String): Bitmap? = fotos.get(url)?.takeIf { !it.isRecycled }

    fun pedir(eventoId: String, url: String, onResultado: (Bitmap?) -> Unit) {
        if (!ativo || url in falhas) return
        val cacheada = pronta(url)
        if (cacheada != null) {
            onResultado(cacheada)
            return
        }
        ouvintes.getOrPut(url) { HashMap() }[eventoId] = onResultado
        if (url !in emVoo && fila.none { it == url }) fila.addLast(url)
        bombear()
    }

    /** Descarta pedidos que saíram da área visível, sem cancelar o que já está baixando. */
    fun manterSomente(urls: Set<String>) {
        fila.removeAll { it !in urls }
        val iterador = ouvintes.iterator()
        while (iterador.hasNext()) {
            val entrada = iterador.next()
            if (entrada.key !in urls && entrada.key !in emVoo) iterador.remove()
        }
    }

    fun encerrar() {
        ativo = false
        fila.clear()
        ouvintes.clear()
    }

    private fun bombear() {
        if (!ativo) return
        while (ativos < MAX_SIMULTANEOS && fila.isNotEmpty()) {
            val url = fila.removeFirst()
            val cacheada = pronta(url)
            if (cacheada != null) {
                entregar(url, cacheada)
                continue
            }
            if (url in falhas) {
                entregar(url, null)
                continue
            }
            emVoo.add(url)
            ativos++
            val pedido = ImageRequest.Builder(app)
                .data(url)
                .size(192)
                .precision(Precision.INEXACT)
                .allowHardware(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .memoryCacheKey("culturama-mapa-$url")
                .diskCacheKey("culturama-mapa-$url")
                .target(
                    onSuccess = { drawable -> concluir(url, copiarMiniatura(drawable)) },
                    onError = { concluir(url, null) }
                )
                .build()
            loader.enqueue(pedido)
        }
    }

    private fun concluir(url: String, bitmap: Bitmap?) {
        if (!ativo) return
        ativos = (ativos - 1).coerceAtLeast(0)
        emVoo.remove(url)
        if (bitmap != null) {
            fotos.put(url, bitmap)
            entregar(url, bitmap)
        } else {
            val tentativa = (tentativas[url] ?: 0) + 1
            tentativas[url] = tentativa
            if (tentativa < 2) {
                fila.addFirst(url)
            } else {
                falhas.add(url)
                entregar(url, null)
            }
        }
        bombear()
    }

    private fun entregar(url: String, bitmap: Bitmap?) {
        val lista = ouvintes.remove(url) ?: return
        lista.values.forEach { it(bitmap) }
    }

    private companion object {
        const val MAX_SIMULTANEOS = 3
    }
}

private val miniaturasPadrao = LruCache<String, Bitmap>(48)

private fun miniaturaPadrao(
    density: Float,
    categoria: String,
    selecionado: Boolean,
    compacto: Boolean
): Bitmap {
    val letra = letraDoMarcador(categoria)
    val cor = corCategoriaMapa(categoria)
    val chave = "${(density * 100).toInt()}|$compacto|$selecionado|$cor|$letra"
    miniaturasPadrao.get(chave)?.let { return it }
    val desenhada = desenharMarcador(density, categoria, selecionado, compacto, foto = null)
    miniaturasPadrao.put(chave, desenhada)
    return desenhada
}

private data class MedidasDoMarcador(
    val cx: Float,
    val cy: Float,
    val raioMiolo: Float,
    val raioTotal: Float,
    val anelBranco: Float,
    val anelExterno: Float,
    val lado: Int
)

private fun medirMarcador(density: Float, selecionado: Boolean, compacto: Boolean): MedidasDoMarcador {
    val baseDp = when {
        selecionado -> 46f
        compacto -> 32f
        else -> 40f
    }
    val diametro = baseDp * density
    val anelBranco = (if (selecionado) 2.2f else 1.8f) * density
    val anelExterno = (if (selecionado) 3f else 1.35f) * density
    val halo = if (selecionado) 4f * density else 0f
    val sombra = 2.5f * density
    val raioMiolo = diametro / 2f
    val raioTotal = raioMiolo + anelBranco + anelExterno + halo
    val lado = ((raioTotal + sombra + density) * 2f).toInt().coerceAtLeast(1)
    val centro = lado / 2f
    return MedidasDoMarcador(centro, centro, raioMiolo, raioTotal, anelBranco, anelExterno, lado)
}

private fun desenharMarcador(
    density: Float,
    categoria: String,
    selecionado: Boolean,
    compacto: Boolean,
    foto: Bitmap?
): Bitmap {
    val medidas = medirMarcador(density, selecionado, compacto)
    val bitmap = Bitmap.createBitmap(medidas.lado, medidas.lado, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val sombra = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x38000000 }
    canvas.drawCircle(
        medidas.cx,
        medidas.cy + 1.4f * density,
        medidas.raioMiolo + medidas.anelBranco * 0.35f,
        sombra
    )
    if (selecionado) {
        val halo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x55E8835A }
        canvas.drawCircle(
            medidas.cx,
            medidas.cy,
            medidas.raioMiolo + medidas.anelBranco + medidas.anelExterno + 2.2f * density,
            halo
        )
    }
    val externo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (selecionado) 0xFFE8835A.toInt() else 0xCC141416.toInt()
        style = Paint.Style.STROKE
        strokeWidth = medidas.anelExterno
    }
    canvas.drawCircle(
        medidas.cx,
        medidas.cy,
        medidas.raioMiolo + medidas.anelBranco + medidas.anelExterno / 2f,
        externo
    )
    val branco = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = medidas.anelBranco
    }
    canvas.drawCircle(
        medidas.cx,
        medidas.cy,
        medidas.raioMiolo + medidas.anelBranco / 2f,
        branco
    )
    val desenhouFoto = foto != null && runCatching { pintarFoto(canvas, medidas, foto) }.isSuccess
    if (!desenhouFoto) {
        val fundo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = corCategoriaMapa(categoria) }
        canvas.drawCircle(medidas.cx, medidas.cy, medidas.raioMiolo, fundo)
        val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = corLegivelNoFundo(corCategoriaMapa(categoria))
            textAlign = Paint.Align.CENTER
            textSize = medidas.raioMiolo * 0.92f
            typeface = Typeface.DEFAULT_BOLD
        }
        val y = medidas.cy - (texto.descent() + texto.ascent()) / 2f
        canvas.drawText(letraDoMarcador(categoria), medidas.cx, y, texto)
    }
    return bitmap
}

/** Recorte quadrado central, sem esticar a foto. */
private fun pintarFoto(canvas: Canvas, medidas: MedidasDoMarcador, foto: Bitmap) {
    if (foto.isRecycled || foto.width <= 0 || foto.height <= 0) error("miniatura inválida")
    val shader = android.graphics.BitmapShader(foto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val lado = min(foto.width, foto.height).toFloat()
    val escala = (medidas.raioMiolo * 2f) / lado
    val esquerda = (foto.width - lado) / 2f
    val topo = (foto.height - lado) / 2f
    val matriz = Matrix()
    matriz.setScale(escala, escala)
    matriz.postTranslate(
        medidas.cx - medidas.raioMiolo - esquerda * escala,
        medidas.cy - medidas.raioMiolo - topo * escala
    )
    shader.setLocalMatrix(matriz)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        this.shader = shader
    }
    canvas.drawCircle(medidas.cx, medidas.cy, medidas.raioMiolo, paint)
}

private fun copiarMiniatura(drawable: Drawable): Bitmap? {
    val original = (drawable as? BitmapDrawable)?.bitmap
    val base = when {
        original != null && !original.isRecycled && original.width > 0 && original.height > 0 -> original
        else -> runCatching { drawable.toBitmap(192, 192) }.getOrNull()?.takeIf { !it.isRecycled }
    } ?: return null
    if (base.isRecycled || base.width <= 0 || base.height <= 0) return null
    return runCatching { base.copy(Bitmap.Config.ARGB_8888, false) }.getOrNull()
}
