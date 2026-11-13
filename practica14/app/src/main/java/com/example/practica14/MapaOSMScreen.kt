package com.example.practica14

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap

fun drawableToBitmap(context: Context, drawableRes: Int): Bitmap {
    val d = ContextCompat.getDrawable(context, drawableRes)!!
    return d.toBitmap(
        width = d.intrinsicWidth.coerceAtLeast(1),
        height = d.intrinsicHeight.coerceAtLeast(1),
        config = Bitmap.Config.ARGB_8888
    )
}

@Composable
fun MapaOSMScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionGranted by remember { mutableStateOf(false) }

    val db = remember { AppDatabase.getInstance(context) }
    val dao = remember { db.marcadorDao() }

    val marcadores by dao.obtenerTodos()
        .collectAsState(initial = emptyList())

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var pendienteCentrar by remember { mutableStateOf<GeoPoint?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> permissionGranted = isGranted }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) permissionGranted = true
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(marcadores, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        // Quita solo los marcadores previos (no el overlay de ubicación)
        map.overlays.filterIsInstance<Marker>().forEach { map.overlays.remove(it) }

        marcadores.forEach { m ->
            val marker = Marker(map).apply {
                position = GeoPoint(m.latitud, m.longitud)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = m.titulo
                snippet = "Lat: ${m.latitud}, Lon: ${m.longitud}"
            }
            map.overlays.add(marker)
        }
        map.invalidate()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.OpenTopo)
                tileProvider.clearTileCache()
                setMultiTouchControls(true)
                controller.setZoom(17.0)
                controller.setCenter(GeoPoint(25.7905, -108.9875))

                // Long press → guardar marcador en Room
                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                    override fun longPressHelper(p: GeoPoint?): Boolean {
                        if (p != null) {
                            scope.launch {
                                dao.insertar(
                                    MarcadorEntity(
                                        latitud = p.latitude,
                                        longitud = p.longitude,
                                        titulo = "Marcador ${System.currentTimeMillis()}"
                                    )
                                )
                            }
                        }
                        return true
                    }
                }
                overlays.add(MapEventsOverlay(receiver))
            }.also { mapViewRef = it }
        },
        update = { mapView ->
            if (permissionGranted && mapView.overlays.none { it is MyLocationNewOverlay }) {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()

                // ✅ Fase 1 del reto: ícono rojo
                val gpsIcon = drawableToBitmap(context, R.drawable.ic_gps_red)
                locationOverlay.setPersonIcon(gpsIcon)
                locationOverlay.setDirectionArrow(gpsIcon, gpsIcon)

                locationOverlay.runOnFirstFix {
                    mapView.post {
                        val loc = locationOverlay.myLocation
                        if (loc != null) {
                            mapView.controller.setZoom(17.0)
                            mapView.controller.setCenter(loc)
                        }
                    }
                }
                mapView.overlays.add(locationOverlay)
                mapView.invalidate()
            }
        }
    )
}