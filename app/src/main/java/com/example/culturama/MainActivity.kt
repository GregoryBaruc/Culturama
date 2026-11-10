package com.example.culturama

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.culturama.navigation.CulturamaNavGraph
import com.example.culturama.ui.theme.CulturamaTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // User-Agent identificável: obrigatório para baixar tiles (OSM/CARTO).
        // Sem isso o mapa fica em branco ou com tiles de erro.
        val prefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().apply {
            load(applicationContext, prefs)
            userAgentValue = "CulturamaAndroid/${BuildConfig.VERSION_NAME} (${packageName})"
            osmdroidBasePath = cacheDir.resolve("osmdroid").also { it.mkdirs() }
            osmdroidTileCache = cacheDir.resolve("osmdroid/tiles").also { it.mkdirs() }
        }
        enableEdgeToEdge()
        setContent {
            CulturamaTheme {
                val navController = rememberNavController()
                CulturamaNavGraph(navController = navController)
            }
        }
    }
}
