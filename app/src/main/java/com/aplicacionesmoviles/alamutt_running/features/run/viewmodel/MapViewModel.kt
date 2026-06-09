package com.aplicacionesmoviles.alamutt_running.features.run.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
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

    private val _isGpsActive = MutableStateFlow(false)
    val isGpsActive: StateFlow<Boolean> = _isGpsActive

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var roadManager: OSRMRoadManager? = null
    private var isTracking = false
    private var adjustmentJob: kotlinx.coroutines.Job? = null

    fun setupOsmdroid(context: Context) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = "AlamuttRunningApp/1.0"

        osmConfig.tileDownloadThreads = 12 // Más hilos para descargar imágenes.
        osmConfig.cacheMapTileCount = 200
        osmConfig.tileFileSystemCacheMaxBytes = 500L * 1024 * 1024 // 500MB de cache en disco, para poder visualizar mejor el mapa
        
        val basePath = File(context.cacheDir, "osmdroid")
        val tilePath = File(basePath, "tiles")
        if (!tilePath.exists()) tilePath.mkdirs()
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = tilePath
        osmConfig.load(context, context.getSharedPreferences("osmdroid", 0))

        // Cargar última ubicación conocida de la sesión anterior
        val prefs = context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        val lat = prefs.getFloat("last_lat", 40.4167f)
        val lon = prefs.getFloat("last_lon", -3.7038f)
        if (_userLocation.value == null) {
            _userLocation.value = GeoPoint(lat.toDouble(), lon.toDouble())
        }

        roadManager = OSRMRoadManager(context, "AlamuttRunningApp/1.0")
        roadManager?.setMean(OSRMRoadManager.MEAN_BY_FOOT)
    }

    @SuppressLint("MissingPermission")
    fun startTracking(client: FusedLocationProviderClient, context: Context) {
        if (isTracking) return
        isTracking = true
        this.fusedClient = client

        // 1.priorizamos Red / Wi-Fi
        client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    _userLocation.value = point
                }
            }

        // 2. revisamos last location
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null && _userLocation.value == null) {
                _userLocation.value = GeoPoint(loc.latitude, loc.longitude)
            }
        }

        // 3. usamos Seguimiento GPS de alta precisión
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
            .setMinUpdateIntervalMillis(1000L)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val point = GeoPoint(loc.latitude, loc.longitude)
                    _isGpsActive.value = true
                    adjustToRoad(point)
                    
                    // con esto guardamos la ubi para que carge instantaneamente.
                    context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE).edit()
                        .putFloat("last_lat", loc.latitude.toFloat())
                        .putFloat("last_lon", loc.longitude.toFloat())
                        .apply()
                }
            }
        }

        client.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun adjustToRoad(point: GeoPoint) {
        // Si es la primera ubicación, la mostramos inmediatamente
        if (_userLocation.value == null) {
            _userLocation.value = point
        }

        adjustmentJob?.cancel()
        adjustmentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Usamos OSRM para encontrar el punto más cercano en una carretera/sendero
                val roadPoints = arrayListOf(point, point)
                val road = roadManager?.getRoad(roadPoints)

                val snapped = if (road != null && road.mStatus == Road.STATUS_OK && road.mRouteHigh.isNotEmpty()) {
                    road.mRouteHigh[0]
                } else {
                    point
                }

                withContext(Dispatchers.Main) {
                    _userLocation.value = snapped
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error ajustando a carretera", e)
                withContext(Dispatchers.Main) {
                    _userLocation.value = point
                }
            }
        }
    }

    fun onMapRenderComplete() { _isMapFullyRendered.value = true }

    override fun onCleared() {
        super.onCleared()
        locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
        isTracking = false
    }
}
