package com.example.practica16

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.asFlow
import androidx.work.*
import kotlinx.coroutines.flow.flowOf
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkManagerScreen() {
    val context = LocalContext.current
    val workManager = remember { WorkManager.getInstance(context) }

    var workId by remember { mutableStateOf<UUID?>(null) }
    var origenTarea by remember { mutableStateOf("") }  // "UNICA" o "PERIODICA"
    var requiereInternet by remember { mutableStateOf(false) }
    // RETO
    var requiereCarga by remember { mutableStateOf(false) }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val estado = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )
            if (estado != PackageManager.PERMISSION_GRANTED) {
                permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val workInfo by remember(workId) {
        if (workId != null) {
            workManager.getWorkInfoByIdLiveData(workId!!).asFlow()
        } else {
            flowOf(null)
        }
    }.collectAsState(initial = null)

    val estadoTexto = when (workInfo?.state) {
        WorkInfo.State.ENQUEUED -> "EN ESPERA (EN COLA)"
        WorkInfo.State.RUNNING -> "EN EJECUCIÓN ..."
        WorkInfo.State.SUCCEEDED -> "COMPLETADO CON ÉXITO"
        WorkInfo.State.FAILED -> "FALLÓ LA TAREA"
        WorkInfo.State.BLOCKED -> "BLOQUEADO"
        WorkInfo.State.CANCELLED -> "CANCELADO"
        null -> "SIN TAREA ACTIVA"
    }
    val progreso = if (workInfo?.state == WorkInfo.State.SUCCEEDED) {
        100
    } else {
        workInfo?.progress?.getInt("PROGRESO", 0) ?: 0
    }
    val mensajeResultado = workInfo?.outputData?.getString("RESULTADO")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkManager - Respaldo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Estado de la tarea: $estadoTexto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { progreso / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Progreso: $progreso%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (mensajeResultado != null) {
                        Text(
                            text = mensajeResultado,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Requerir conexión a Internet",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = requiereInternet,
                    onCheckedChange = { requiereInternet = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Solo mientras está cargando",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = requiereCarga,
                    onCheckedChange = { requiereCarga = it }
                )
            }

            HorizontalDivider()

            // ---------- Botón 1: Tarea única ----------
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    workId = null                       // limpia el flow anterior
                    origenTarea = "UNICA"
                    val restricciones = Constraints.Builder()
                        .setRequiredNetworkType(
                            if (requiereInternet) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED
                        )
                        .setRequiresCharging(requiereCarga)   // 🔥 RETO aplicado
                        .build()

                    val datosEntrada = workDataOf("NOMBRE_ARCHIVO" to "fotos_practica16.zip")

                    val solicitudUnica = OneTimeWorkRequestBuilder<RespaldoWorker>()
                        .setConstraints(restricciones)
                        .setInputData(datosEntrada)
                        .addTag("respaldo_unico")
                        .build()

                    workManager.enqueue(solicitudUnica)
                    workId = solicitudUnica.id
                }
            ) {
                Text("Iniciar Respaldo Único (OneTime)")
            }
            
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    workId = null                       // limpia el flow anterior
                    origenTarea = "UNICA"
                    val restricciones = Constraints.Builder()
                        .setRequiredNetworkType(
                            if (requiereInternet) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED
                        )
                        .setRequiresCharging(requiereCarga)   // RETO aplicado
                        .build()

                    val datosEntrada = workDataOf("NOMBRE_ARCHIVO" to "respaldo_automatico.db")

                    val solicitudPeriodica = PeriodicWorkRequestBuilder<RespaldoWorker>(
                        15, TimeUnit.MINUTES
                    )
                        .setConstraints(restricciones)
                        .setInputData(datosEntrada)
                        .build()

                    workManager.enqueueUniquePeriodicWork(
                        "trabajo_respaldo_periodico",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        solicitudPeriodica
                    )
                    workId = solicitudPeriodica.id
                }
            ) {
                Text("Programar Respaldo Periódico (15 min)")
            }

            TextButton(
                onClick = { workManager.cancelAllWork() }
            ) {
                Text(
                    text = "Cancelar Todas las Tareas",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}