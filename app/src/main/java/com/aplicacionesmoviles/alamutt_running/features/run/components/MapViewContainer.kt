package com.aplicacionesmoviles.alamutt_running.features.run.components

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.aplicacionesmoviles.alamutt_running.core.ui.theme.AccentRed
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapViewContainer(
    userLocation: GeoPoint, 
    mapStyle: String = "Standard",
    onMapReady: () -> Unit
) {
    val primaryColor = AccentRed.toArgb()

    val pulseOverlay = remember {
        object : org.osmdroid.views.overlay.Overlay() {
            var radius = 20f
            var alphaVal = 255f
            var currentLocation: GeoPoint = userLocation

            override fun draw(canvas: Canvas, map: MapView, shadow: Boolean) {
                if (shadow) return
                val point = map.projection.toPixels(currentLocation, null)

                val haloPaint = Paint().apply {
                    color = primaryColor
                    alpha = alphaVal.toInt()
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), radius, haloPaint)

                val centerPaint = Paint().apply {
                    color = primaryColor
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 15f, centerPaint)
            }
        }
    }

    var mapViewInternal by remember { mutableStateOf<MapView?>(null) }


    LaunchedEffect(mapViewInternal) {
        val mv = mapViewInternal ?: return@LaunchedEffect
        while (true) {
            val duration = 1800L
            val startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() - startTime < duration) {
                val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
                pulseOverlay.radius = 20f + (progress * 20f)
                pulseOverlay.alphaVal = 150f * (1f - progress)
                mv.postInvalidate()
                delay(32)
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(false)
                setBuiltInZoomControls(false)
                setOnTouchListener { _, _ -> true }
                controller.setZoom(19.5)
                controller.setCenter(userLocation)
                overlays.add(pulseOverlay)
                mapViewInternal = this
                onMapReady()
            }
        },
        update = { mapView ->
            pulseOverlay.currentLocation = userLocation

            val newTileSource = when (mapStyle) {
                "Satellite" -> TileSourceFactory.USGS_SAT
                "Terrain" -> TileSourceFactory.USGS_TOPO
                else -> TileSourceFactory.MAPNIK
            }
            if (mapView.tileProvider.tileSource != newTileSource) {
                mapView.setTileSource(newTileSource)
            }

            val currentCenter = mapView.mapCenter
            val latDiff = Math.abs(currentCenter.latitude - userLocation.latitude)
            val lonDiff = Math.abs(currentCenter.longitude - userLocation.longitude)
            
            if (latDiff > 1e-6 || lonDiff > 1e-6) {
                mapView.controller.setCenter(userLocation)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
