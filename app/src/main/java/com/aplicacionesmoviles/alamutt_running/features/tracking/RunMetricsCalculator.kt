package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.location.Location
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context

class RunMetricsCalculator(context: Context) : SensorEventListener {
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null
    private val speedHistory = mutableListOf<Double>()

    private var isActuallyMoving = false
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Si la aceleración es muy baja (cerca de 0), el usuario está quieto
        val acceleration = event?.values?.let { Math.sqrt((it[0]*it[0] + it[1]*it[1] + it[2]*it[2]).toDouble()) } ?: 0.0
        isActuallyMoving = acceleration > 0.5
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateMetrics(newLocation: Location): RunMetrics {
        // Solo procesamos si el GPS dice que se movió Y el acelerómetro confirma movimiento
        if (isActuallyMoving && newLocation.speed > 0.5) {
            lastLocation?.let { last ->
                val distanceSegment = last.distanceTo(newLocation).toDouble()
                if (distanceSegment < 50.0) { // Filtro anti-saltos bruscos
                    totalDistance += distanceSegment
                }
            }
            lastLocation = newLocation
            speedHistory.add(newLocation.speed.toDouble())
            if (speedHistory.size > 10) speedHistory.removeAt(0)
        }

        val avgSpeed = if (speedHistory.isNotEmpty()) speedHistory.average() else 0.0
        val pace = if (isActuallyMoving && avgSpeed > 0.5) (1000.0 / (avgSpeed * 60.0)) else 0.0
        val calories = (totalDistance / 1000 * 60).toInt()

        return RunMetrics(totalDistance, pace, calories)
    }

    fun reset() {
        totalDistance = 0.0
        lastLocation = null
        speedHistory.clear()
        sensorManager.unregisterListener(this)
    }
}