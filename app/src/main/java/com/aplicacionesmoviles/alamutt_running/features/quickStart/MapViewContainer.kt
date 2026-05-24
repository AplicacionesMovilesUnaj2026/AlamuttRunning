package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewContainer(userLocation: GeoPoint, onMapReady: () -> Unit) {
    val context = LocalContext.current

    val iconSize = 40
    val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.RED
        isAntiAlias = true
    }
    canvas.drawCircle(iconSize / 2f, iconSize / 2f, 15f, paint)

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                setBuiltInZoomControls(false)

                isTilesScaledToDpi = true
                setClickable(false)
                setFocusable(false)
                setOnTouchListener { _, _ -> true }

                val matrix = ColorMatrix().apply {
                    setSaturation(0.2f)
                }
                val filter = ColorMatrixColorFilter(matrix)
                overlayManager.tilesOverlay.setColorFilter(filter)

                minZoomLevel = 18.0
                maxZoomLevel = 18.0

                val staticMarker = Marker(this).apply {
                    position = userLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    icon = android.graphics.drawable.BitmapDrawable(ctx.resources, bitmap)
                    infoWindow = null
                }
                overlays.add(staticMarker)

                post {
                    controller.setZoom(18.5)
                    controller.setCenter(userLocation)

                    val buffer = 0.001
                    val minLat = userLocation.latitude - buffer
                    val maxLat = userLocation.latitude + buffer
                    val minLon = userLocation.longitude - buffer
                    val maxLon = userLocation.longitude + buffer
                    setScrollableAreaLimitLatitude(maxLat, minLat, 0)
                    setScrollableAreaLimitLongitude(minLon, maxLon, 0)

                    invalidate()
                    onMapReady()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}