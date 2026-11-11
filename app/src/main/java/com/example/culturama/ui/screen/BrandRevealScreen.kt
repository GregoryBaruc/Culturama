package com.example.culturama.ui.screen

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.provider.Settings
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.culturama.R
import com.example.culturama.startup.StartupBootstrap
import com.example.culturama.ui.theme.CulturamaOrange
import com.example.culturama.ui.theme.TextPrimary
import com.example.culturama.ui.theme.TextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_OPENING_MILLIS = 6_500L
private const val EXIT_FADE_MILLIS = 280
private val SplashBlack = Color(0xFF000000)

/**
 * Abertura com o MP4 oficial em tela cheia (sem áudio).
 * Bootstrap real roda em paralelo; fallback para logo estática se o vídeo falhar.
 */
@Composable
fun BrandRevealScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reduceMotion = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        }.getOrDefault(false)
    }

    var useStaticFallback by remember { mutableStateOf(reduceMotion) }
    var exitStarted by remember { mutableStateOf(false) }
    var loadProgress by remember { mutableFloatStateOf(0.08f) }
    var showLoading by remember { mutableStateOf(false) }
    var videoEnded by remember { mutableStateOf(false) }

    val contentAlpha = remember { Animatable(1f) }
    val logoAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val logoScale = remember { Animatable(if (reduceMotion) 1f else 0.92f) }
    val loadingAlpha = remember { Animatable(0f) }

    fun finish() {
        if (exitStarted) return
        exitStarted = true
    }

    LaunchedEffect(exitStarted) {
        if (!exitStarted) return@LaunchedEffect
        contentAlpha.animateTo(0f, tween(EXIT_FADE_MILLIS, easing = FastOutSlowInEasing))
        onFinished()
    }

    LaunchedEffect(reduceMotion, useStaticFallback) {
        coroutineScope {
            val bootstrap = async {
                StartupBootstrap.prepararEssenciais { p ->
                    loadProgress = p.coerceIn(0.08f, 1f)
                }
            }

            if (reduceMotion || useStaticFallback) {
                showLoading = true
                launch { logoAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing)) }
                launch { logoScale.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
                loadingAlpha.animateTo(1f, tween(320))
                bootstrap.await()
                loadProgress = 1f
                delay(220)
                finish()
                return@coroutineScope
            }

            // Vídeo em tela cheia; loading aparece perto do fim / após o clip.
            launch {
                delay(2_200)
                showLoading = true
                loadingAlpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
            }

            // Espera o fim do vídeo (sinalizado pelo player) com teto de segurança.
            val waitVideo = async {
                var waited = 0
                while (!videoEnded && waited < 5_800) {
                    delay(50)
                    waited += 50
                }
            }

            waitVideo.await()
            bootstrap.await()
            loadProgress = 1f
            delay(180)
            finish()
        }
    }

    LaunchedEffect(Unit) {
        delay(MAX_OPENING_MILLIS)
        finish()
    }

    val shimmer = rememberInfiniteTransition(label = "loadingShimmer")
    val shimmerX by shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(contentAlpha.value)
            .background(SplashBlack)
    ) {
        if (useStaticFallback) {
            Image(
                painter = painterResource(id = R.drawable.logo_culturama),
                contentDescription = stringResource(R.string.app_name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(64.dp)
                    .widthIn(max = 280.dp)
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
            )
        } else {
            BrandRevealVideo(
                modifier = Modifier.fillMaxSize(),
                onFailed = {
                    useStaticFallback = true
                    videoEnded = true
                },
                onCompleted = { videoEnded = true }
            )
        }

        if (showLoading) {
            OpeningLoader(
                progress = loadProgress,
                shimmer = shimmerX,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 36.dp, vertical = 32.dp)
                    .alpha(loadingAlpha.value)
            )
        }
    }
}

@Composable
private fun BrandRevealVideo(
    onFailed: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val player = remember { MediaPlayer() }
    var released by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            released = true
            runCatching {
                if (player.isPlaying) player.stop()
                player.reset()
                player.release()
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        if (released) return
                        try {
                            player.setSurface(Surface(surface))
                            val fd = ctx.resources.openRawResourceFd(R.raw.culturama_brand_reveal)
                            player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                            fd.close()
                            player.isLooping = false
                            player.setVolume(0f, 0f)
                            player.setOnVideoSizeChangedListener { _, videoW, videoH ->
                                applyCenterCrop(this@apply, videoW, videoH)
                            }
                            player.setOnCompletionListener {
                                if (!released) onCompleted()
                            }
                            player.setOnErrorListener { _, _, _ ->
                                if (!released) onFailed()
                                true
                            }
                            player.setOnPreparedListener { prepared ->
                                if (released) return@setOnPreparedListener
                                applyCenterCrop(
                                    this@apply,
                                    prepared.videoWidth,
                                    prepared.videoHeight
                                )
                                prepared.start()
                            }
                            player.prepareAsync()
                        } catch (_: Exception) {
                            if (!released) onFailed()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surface: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        if (released) return
                        runCatching {
                            applyCenterCrop(this@apply, player.videoWidth, player.videoHeight)
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
                }
            }
        }
    )
}

private fun applyCenterCrop(view: TextureView, videoW: Int, videoH: Int) {
    if (videoW <= 0 || videoH <= 0 || view.width == 0 || view.height == 0) return
    val viewRatio = view.width.toFloat() / view.height
    val videoRatio = videoW.toFloat() / videoH
    val scaleX: Float
    val scaleY: Float
    if (videoRatio > viewRatio) {
        scaleX = videoRatio / viewRatio
        scaleY = 1f
    } else {
        scaleX = 1f
        scaleY = viewRatio / videoRatio
    }
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, view.width / 2f, view.height / 2f)
    view.setTransform(matrix)
}

@Composable
private fun OpeningLoader(
    progress: Float,
    shimmer: Float,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.splash_loading),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            val track = TextPrimary.copy(alpha = 0.12f)
            val filled = size.width * progress.coerceIn(0f, 1f)
            val radius = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(color = track, cornerRadius = radius)
            if (filled > 0f) {
                drawRoundRect(
                    color = CulturamaOrange,
                    size = Size(filled, size.height),
                    cornerRadius = radius
                )
                val glowW = size.width * 0.18f
                val x = (filled - glowW).coerceAtLeast(0f) * shimmer
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        startX = x,
                        endX = x + glowW
                    ),
                    size = Size(filled, size.height)
                )
            }
        }
    }
}
