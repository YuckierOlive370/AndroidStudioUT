package com.example.practica10

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamaraScreen() {
    val context = LocalContext.current
    // Lista observable de imágenes capturadas
    var listaImagenes by remember {
        mutableStateOf(listOf<Bitmap>())
    }

    // Lanzador para abrir la cámara
    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->

        if (bitmap != null) {
            listaImagenes = listaImagenes + bitmap
        }
    }

    val permisoCamaraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { permisoConcedido ->

            if (permisoConcedido) {

                // Si el usuario acepta, abrir cámara
                camaraLauncher.launch(null)

            } else {

                Toast.makeText(
                    context,
                    "Permiso de cámara denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Cámara y Galería")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                    when {

                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {

                            // Ya tiene permiso
                            camaraLauncher.launch(null)
                        }

                        else -> {

                            // Solicitar permiso
                            permisoCamaraLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    }
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Tomar Foto"
                )
            }
        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp)
        ) {

            if (listaImagenes.isEmpty()) {

                Text("No hay fotos en la galería. ¡Toma una!")

            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {

                    items(listaImagenes) { bitmap ->
                        ImagenCapturadaItem(bitmap = bitmap)
                    }
                }
            }
        }
    }
}

@Composable
fun ImagenCapturadaItem(bitmap: Bitmap) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),

        shape = RoundedCornerShape(8.dp)
    ) {

        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto capturada",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}