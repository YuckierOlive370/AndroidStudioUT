package com.example.practica13

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurbujaScreen(sensorManager: SensorManager) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Factor de sensibilidad
    val sensibilidad = 50f

    // Limite: Se ajusta dependiendo del tamaño del circulo exterior (250dp / 2 = 125 aprox)
    val radioMaximo = 250f

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // 1. Obtenemos las coordenadas "crudas" del sensor
                    val rawX = -it.values[0] * sensibilidad
                    val rawY = it.values[1] * sensibilidad

                    // 2. Calculamos la distancia real desde el centro usando Teorema de Pitágoras
                    val distancia = sqrt((rawX * rawX) + (rawY * rawY))

                    // 3. Si la distancia intenta salirse de nuestro radio, la limitamos
                    if (distancia > radioMaximo) {
                        // Calculamos la proporción para mantenerla justo en la orilla
                        val proporcion = radioMaximo / distancia
                        offsetX = rawX * proporcion
                        offsetY = rawY * proporcion
                    } else {
                        // Si está dentro del circulo, se mueve libremente
                        offsetX = rawX
                        offsetY = rawY
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        val acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(listener, acelerometro, SensorManager.SENSOR_DELAY_GAME)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de Burbuja") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(50.dp).border(2.dp, Color.Red, CircleShape))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                        .size(40.dp)
                        .background(Color(0xFF00FF00), CircleShape)
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Eje X: ${"%.2f".format(offsetX / sensibilidad)}", color = Color.White, fontSize = 20.sp)
                Text("Eje Y: ${"%.2f".format(offsetY / sensibilidad)}", color = Color.White, fontSize = 20.sp)
            }
        }
    }
}