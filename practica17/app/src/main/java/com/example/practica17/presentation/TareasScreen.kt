package com.example.practica17.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practica17.data.Tarea

@Composable
fun TareasScreen(
    viewModel: TareasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TareasContenido(
        uiState = uiState,
        onTextoCambio = viewModel::onTextoCambio,
        onAgregarTarea = viewModel::agregarTarea,
        onAlternarCompletada = viewModel::alternarCompletada,
        onEliminarTarea = viewModel::eliminarTarea,
        onCambiarFiltro = viewModel::cambiarFiltro,
        onLimpiarCompletadas = viewModel::limpiarCompletadas, // 🆕
        onRecargar = { viewModel.cargarTareas(simularError = false) },
        onSimularError = { viewModel.cargarTareas(simularError = true) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasContenido(
    uiState: TareasUiState,
    onTextoCambio: (String) -> Unit,
    onAgregarTarea: () -> Unit,
    onAlternarCompletada: (Int) -> Unit,
    onEliminarTarea: (Int) -> Unit,
    onCambiarFiltro: (FiltroTarea) -> Unit,
    onLimpiarCompletadas: () -> Unit,   // 🆕
    onRecargar: () -> Unit,
    onSimularError: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MVVM - Gestor de Tareas") },
                actions = {
                    IconButton(onClick = onSimularError) {
                        Icon(Icons.Default.Warning, contentDescription = "Simular error")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val estado = uiState.estadoCarga) {
                is EstadoCarga.Cargando -> PantallaCargando()
                is EstadoCarga.Error -> PantallaError(
                    mensaje = estado.mensaje,
                    onReintentar = onRecargar
                )
                is EstadoCarga.Exito -> PantallaExito(
                    uiState = uiState,
                    onTextoCambio = onTextoCambio,
                    onAgregarTarea = onAgregarTarea,
                    onAlternarCompletada = onAlternarCompletada,
                    onEliminarTarea = onEliminarTarea,
                    onCambiarFiltro = onCambiarFiltro,
                    onLimpiarCompletadas = onLimpiarCompletadas // 🆕
                )
            }
        }
    }
}

@Composable
private fun PantallaCargando() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.padding(8.dp))
        Text("Cargando tareas...")
    }
}

@Composable
private fun PantallaError(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.padding(8.dp))
        Text(text = mensaje, textAlign = TextAlign.Center)
        Spacer(Modifier.padding(12.dp))
        Button(onClick = onReintentar) { Text("Reintentar") }
    }
}

@Composable
private fun PantallaExito(
    uiState: TareasUiState,
    onTextoCambio: (String) -> Unit,
    onAgregarTarea: () -> Unit,
    onAlternarCompletada: (Int) -> Unit,
    onEliminarTarea: (Int) -> Unit,
    onCambiarFiltro: (FiltroTarea) -> Unit,
    onLimpiarCompletadas: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {

        // 🆕 Tarjeta de estadísticas derivadas
        CardEstadisticas(uiState = uiState)

        Spacer(Modifier.padding(8.dp))

        // Campo de texto + botón agregar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.textoNuevaTarea,
                onValueChange = onTextoCambio,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nueva tarea") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onAgregarTarea) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }

        Spacer(Modifier.padding(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FiltroTarea.entries.forEach { filtro ->
                FilterChip(
                    selected = uiState.filtroActivo == filtro,
                    onClick = { onCambiarFiltro(filtro) },
                    label = { Text(filtro.etiqueta) }
                )
            }
        }

        Spacer(Modifier.padding(4.dp))

        OutlinedButton(
            onClick = onLimpiarCompletadas,
            enabled = uiState.totalCompletadas > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Limpiar completadas (${uiState.totalCompletadas})")
        }

        Spacer(Modifier.padding(8.dp))

        if (uiState.tareasFiltradas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay tareas en este filtro",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.tareasFiltradas, key = { it.id }) { tarea ->
                    FilaTarea(
                        tarea = tarea,
                        onToggle = { onAlternarCompletada(tarea.id) },
                        onEliminar = { onEliminarTarea(tarea.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardEstadisticas(uiState: TareasUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EstadisticaItem(label = "Total", valor = uiState.totalTareas)
            EstadisticaItem(label = "Pendientes", valor = uiState.totalPendientes)
            EstadisticaItem(label = "Completadas", valor = uiState.totalCompletadas)
        }
    }
}

@Composable
private fun EstadisticaItem(label: String, valor: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun FilaTarea(
    tarea: Tarea,
    onToggle: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = tarea.completada,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = tarea.titulo,
            modifier = Modifier.weight(1f),
            textDecoration = if (tarea.completada) TextDecoration.LineThrough else TextDecoration.None
        )
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
        }
    }
}