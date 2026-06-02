package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.aplicacionesmoviles.alamutt_running.ui.theme.AccentRed
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@SuppressLint("ClickableViewAccessibility")
@Composable
fun MapViewContainer(userLocation: GeoPoint, onMapReady: () -> Unit) {
    var pulseRadius by remember { mutableFloatStateOf(20f) }
    var pulseAlpha by remember { mutableFloatStateOf(255f) }

    val primaryColor = AccentRed.toArgb()

    LaunchedEffect(Unit) {
        while (true) {
            val startTime = System.currentTimeMillis()
            val duration = 1800L
            while (System.currentTimeMillis() - startTime < duration) {
                val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
                pulseRadius = 20f + (progress * 20f)
                pulseAlpha = 150f * (1f - progress)
                delay(16)
            }
        }
    }

    val pulseOverlay = remember {
        object : org.osmdroid.views.overlay.Overlay() {
            var radius = 20f
            var alphaVal = 255f
            var currentLocation: GeoPoint = userLocation

            override fun draw(canvas: Canvas, map: MapView, shadow: Boolean) {
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

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(false)
                setBuiltInZoomControls(false)
                setOnTouchListener { _, _ -> true }
                controller.setZoom(19.5)
                controller.setCenter(userLocation)
                overlays.add(pulseOverlay)
                onMapReady()
            }
        },
        update = { mapView ->
            pulseOverlay.radius = pulseRadius
            pulseOverlay.alphaVal = pulseAlpha
            pulseOverlay.currentLocation = userLocation
            mapView.controller.setCenter(userLocation)
            mapView.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}