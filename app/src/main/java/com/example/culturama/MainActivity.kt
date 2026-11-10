package com.example.culturama

import android.os.Bundle
import org.osmdroid.config.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.culturama.navigation.CulturamaNavGraph
import com.example.culturama.ui.theme.CulturamaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().apply {
            userAgentValue = packageName
            osmdroidBasePath = cacheDir
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
