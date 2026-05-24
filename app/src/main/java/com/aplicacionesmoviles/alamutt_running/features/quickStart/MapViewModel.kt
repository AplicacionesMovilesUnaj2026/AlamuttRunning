package com.aplicacionesmoviles.alamutt_running.features.quickStart

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class MapViewModel : ViewModel() {
    private val _userLocation = MutableStateFlow<GeoPoint?>(null)
    val userLocation: StateFlow<GeoPoint?> = _userLocation

    private val _isMapFullyRendered = MutableStateFlow(false)
    val isMapFullyRendered: StateFlow<Boolean> = _isMapFullyRendered

    private var locationCallback: LocationCallback? = null

    fun setupOsmdroid(context: Context) {
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = context.packageName

        val basePath = File(context.cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = File(basePath, "tiles")

        osmConfig.load(context, context.getSharedPreferences("osmdroid", 0))
    }

    fun onMapRenderComplete() {
        _isMapFullyRendered.value = true
    }

    fun tryFetchLocation(context: Context, client: FusedLocationProviderClient) {
        if (_userLocation.value != null) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _userLocation.value == null) {
                    _userLocation.value = GeoPoint(loc.latitude, loc.longitude)
                }
            }

            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdates(1)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLoc = locationResult.lastLocation
                    if (lastLoc != null && _userLocation.value == null) {
                        _userLocation.value = GeoPoint(lastLoc.latitude, lastLoc.longitude)
                        client.removeLocationUpdates(this)
                    }
                }
            }

            client.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationCallback = null
    }
}