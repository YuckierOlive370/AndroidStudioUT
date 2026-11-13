package com.example.practica14

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.practica14.ui.theme.Practica14Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        org.osmdroid.config.Configuration.getInstance().load(ctx,
            ctx.getSharedPreferences("osmdroid", 0))
        org.osmdroid.config.Configuration.getInstance().userAgentValue = "Practica14_Gibran_UAS/1.0"
        setContent {
            MapaOSMScreen()
        }
    }
}
