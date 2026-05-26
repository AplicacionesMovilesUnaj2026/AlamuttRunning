package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var roadManager: OSRMRoadManager? = null

    fun setupOsmdroid(context: Context) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "AlamuttRunningApp/1.0"
        val basePath = File(context.cacheDir, "osmdroid")
        val tilePath = File(basePath, "tiles")
        if (!tilePath.exists()) tilePath.mkdirs()
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = tilePath
        osmConfig.load(context, context.getSharedPreferences("osmdroid", 0))

        roadManager = OSRMRoadManager(context, "AlamuttRunningApp/1.0")
        roadManager?.setMean(OSRMRoadManager.MEAN_BY_FOOT)
    }

    @SuppressLint("MissingPermission")
    fun startTracking(context: Context, client: FusedLocationProviderClient) {
        this.fusedClient = client

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
            .setMinUpdateIntervalMillis(10000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    adjustToRoad(point)
                }
            }
        }

        client.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun adjustToRoad(point: GeoPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val destination = GeoPoint(point.latitude + 0.0001, point.longitude + 0.0001)
            val road = roadManager?.getRoad(arrayListOf(point, destination))

            val snapped = if (road != null && road.mStatus == Road.STATUS_OK && road.mRouteHigh.isNotEmpty()) {
                road.mRouteHigh[0]
            } else {
                point
            }

            withContext(Dispatchers.Main) {
                _userLocation.value = snapped
            }
        }
    }

    fun onMapRenderComplete() { _isMapFullyRendered.value = true }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
    }
}