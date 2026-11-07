package com.example.practica13

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.practica13.ui.theme.Practica13Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Obtenemos el SensorManager del sistema
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {
            Practica13Theme {
                // Llamamos a la pantalla pasándole el sensorManager
                BurbujaScreen(sensorManager = sensorManager)
            }
        }
    }
}