package com.example.culturama

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.culturama.navigation.CulturamaNavGraph
import com.example.culturama.startup.AppStartup
import com.example.culturama.ui.screen.BrandRevealScreen
import com.example.culturama.ui.theme.CulturamaTheme
import org.osmdroid.config.Configuration
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val keepSystemSplash = AtomicBoolean(true)
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { keepSystemSplash.get() }
        // Remove o ícone padrão do sistema; a abertura Compose assume a tela.
        splash.setOnExitAnimationListener { provider -> provider.remove() }

        super.onCreate(savedInstanceState)

        // User-Agent identificável: obrigatório para baixar tiles (OSM/CARTO).
        val prefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(applicationContext, prefs)
            userAgentValue = "CulturamaAndroid/${BuildConfig.VERSION_NAME} (${packageName})"
            osmdroidBasePath = cacheDir.resolve("osmdroid").also { it.mkdirs() }
            osmdroidTileCache = cacheDir.resolve("osmdroid/tiles").also { it.mkdirs() }
        }

        enableEdgeToEdge()
        setContent {
            var showBrandReveal by remember {
                mutableStateOf(!AppStartup.brandRevealShownThisProcess)
            }

            LaunchedEffect(Unit) {
                keepSystemSplash.set(false)
            }

            CulturamaTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    // Home sobe por baixo: eventos e imagens seguem assíncronos.
                    CulturamaNavGraph(navController = navController)

                    if (showBrandReveal) {
                        BrandRevealScreen(
                            onFinished = {
                                AppStartup.brandRevealShownThisProcess = true
                                showBrandReveal = false
                            }
                        )
                    }
                }
            }
        }
    }
}
