package com.example.practica08.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.practica08.data.Usuario
import com.example.practica08.domain.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Instancia de DB y DAO
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = db.usuarioDao()

    // Variable para corrutinas (inserción/eliminación)
    val coroutineScope = rememberCoroutineScope()

    // Flujo reactivo convertido a Estado de Compose
    val usuarios by dao.obtenerTodos().collectAsState(initial = emptyList())
    var nuevoNombre by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp)
    ) {
        OutlinedTextField(
            value = nuevoNombre,
            onValueChange = { nuevoNombre = it },
            label = { Text("Nombre del usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (nuevoNombre.isNotBlank()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        // Inserción en segundo plano
                        dao.insertar(Usuario(nombre = nuevoNombre))
                        withContext(Dispatchers.Main) {
                            nuevoNombre = ""
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Usuario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lista de Usuarios Registrados:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(usuarios) { usuario ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ID: ${usuario.id} | Nombre: ${usuario.nombre}")

                        // Botón para eliminar (Cumplimiento del Reto)
                        IconButton(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                dao.eliminar(usuario)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar usuario",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}