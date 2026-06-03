package com.aplicacionesmoviles.alamutt_running.util

import java.util.Locale

object UnitConverter {
    private const val MILES_PER_KM = 0.621371

    fun formatDistance(meters: Double, system: String): String {
        return if (system == "Metric") {
            String.format(Locale.US, "%.2f km", meters / 1000.0)
        } else {
            val miles = (meters / 1000.0) * MILES_PER_KM
            String.format(Locale.US, "%.2f mi", miles)
        }
    }

    fun formatDistanceKm(km: Double, system: String): String {
        return if (system == "Metric") {
            String.format(Locale.US, "%.2f km", km)
        } else {
            val miles = km * MILES_PER_KM
            String.format(Locale.US, "%.2f mi", miles)
        }
    }

    fun formatPace(paceMinPerKm: Double, system: String): String {
        if (paceMinPerKm <= 0.0 || paceMinPerKm.isNaN() || paceMinPerKm.isInfinite()) return "--:--"
        
        val convertedPace = if (system == "Metric") paceMinPerKm else paceMinPerKm / MILES_PER_KM
        val minutes = convertedPace.toInt()
        val seconds = ((convertedPace - minutes) * 60).toInt()
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
    
    fun getUnitLabel(system: String): String {
        return if (system == "Metric") "km" else "mi"
    }

    fun getWeightLabel(system: String): String {
        return if (system == "Metric") "kg" else "lb"
    }

    fun getHeightLabel(system: String): String {
        return if (system == "Metric") "cm" else "in"
    }
    
    fun getFullUnitLabel(system: String): String {
        return if (system == "Metric") "kilómetros" else "millas"
    }

    fun getPaceUnitLabel(system: String): String {
        return if (system == "Metric") "min/km" else "min/mi"
    }

    fun getPaceFullLabel(system: String): String {
        return if (system == "Metric") "por kilómetro" else "por milla"
    }
}
