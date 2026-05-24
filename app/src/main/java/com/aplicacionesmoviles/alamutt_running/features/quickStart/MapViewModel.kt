package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.File

class MapViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation
    private val _isMapFullyRendered = MutableStateFlow(false)
    val isMapFullyRendered: StateFlow<Boolean> = _isMapFullyRendered

    private var trackingJob: Job? = null

    fun setupOsmdroid(context: Context) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "AlamuttRunningApp/1.0"
        val basePath = File(context.cacheDir, "osmdroid")
        val tilePath = File(basePath, "tiles")
        if (!tilePath.exists()) tilePath.mkdirs()
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = tilePath
        osmConfig.load(context, context.getSharedPreferences("osmdroid", 0))
    }

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context, client: FusedLocationProviderClient) {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val loc = client.lastLocation.await()
                        if (loc != null) {
                            val point = GeoPoint(loc.latitude, loc.longitude)
                            val roadManager = OSRMRoadManager(context, "AlamuttRunningApp/1.0")
                            roadManager.setMean(OSRMRoadManager.MEAN_BY_FOOT)

                            val point2 = GeoPoint(point.latitude + 0.0001, point.longitude + 0.0001)
                            val road = roadManager.getRoad(arrayListOf(point, point2))

                            val snapped = if (road.mStatus == Road.STATUS_OK && road.mRouteHigh.isNotEmpty()) {
                                road.mRouteHigh[0]
                            } else point

                            withContext(Dispatchers.Main) { _userLocation.value = snapped }
                        }
                    } catch (e: Exception) {
                    }
                }
                delay(5000)
            }
        }
    }

    fun onMapRenderComplete() { _isMapFullyRendered.value = true }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }
}