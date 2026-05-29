package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location

class RunMetricsCalculator(context: Context) : SensorEventListener {
    private var totalDistance: Double = 0.0
    private var lastLocation: Location? = null
    private val speedHistory = mutableListOf<Double>()
    private var lastValidPace: Double = 0.0
    private var currentAcceleration: Double = 0.0

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    init {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let {
            currentAcceleration = kotlin.math.sqrt((it[0] * it[0] + it[1] * it[1] + it[2] * it[2]).toDouble())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateMetrics(newLocation: Location): RunMetrics {
        val isMoving = newLocation.hasSpeed() && newLocation.speed > 0.5 && currentAcceleration > 0.2

        lastLocation?.let { last ->
            if (isMoving) {
                val rawDistance = last.distanceTo(newLocation).toDouble()
                if (rawDistance > 0.5 && rawDistance < 50.0) {
                    totalDistance += rawDistance
                }
            }
        }
        lastLocation = newLocation

        if (isMoving) {
            val currentSpeed = newLocation.speed.toDouble()
            speedHistory.add(currentSpeed)

            if (speedHistory.size > 35) {
                speedHistory.removeAt(0)
            }

            val avgSpeed = speedHistory.average()
            if (avgSpeed > 0.3) {
                val calculatedPace = (1000.0 / (avgSpeed * 60.0))
                lastValidPace = (lastValidPace * 0.90) + (calculatedPace * 0.10)
            }
        }

        val displayPace = if (lastValidPace == 0.0) 0.0 else lastValidPace.coerceIn(4.0, 25.0)

        return RunMetrics(totalDistance, displayPace, (totalDistance / 1000 * 60).toInt())
    }

    fun reset() {
        totalDistance = 0.0
        lastLocation = null
        speedHistory.clear()
        lastValidPace = 0.0
    }
}