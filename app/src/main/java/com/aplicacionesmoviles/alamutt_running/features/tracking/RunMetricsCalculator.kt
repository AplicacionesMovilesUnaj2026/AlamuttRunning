package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import kotlin.math.sqrt

class RunMetricsCalculator(
    context: Context
) : SensorEventListener {

    private var totalDistanceMeters = 0.0
    private var movingTimeMillis = 0L
    private var lastLocation: Location? = null
    private var lastTimestamp: Long? = null
    private var currentAcceleration = 0.0
    private val speedSamples = ArrayDeque<Double>()
    private var currentPace = 0.0

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let {
            currentAcceleration = sqrt((it[0] * it[0] + it[1] * it[1] + it[2] * it[2]).toDouble())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun updateMetrics(newLocation: Location): RunMetrics {
        val previousLocation = lastLocation

        if (previousLocation != null) {
            val distance = previousLocation.distanceTo(newLocation).toDouble()
            val deltaTimeMillis = (newLocation.time - previousLocation.time).coerceAtLeast(0)
            val deltaTimeSeconds = deltaTimeMillis / 1000.0

            val calculatedSpeed = if (deltaTimeSeconds > 0) distance / deltaTimeSeconds else 0.0

            val gpsValid = newLocation.accuracy <= 10f
            val isVehicle = (newLocation.hasSpeed() && newLocation.speed > 5.5f) || calculatedSpeed > 5.5f

            if (gpsValid && distance >= 1.0 && !isVehicle) {
                totalDistanceMeters += distance

                if (currentAcceleration > 0.5 && currentAcceleration < 15.0) {
                    movingTimeMillis += deltaTimeMillis
                }
            }

            if (!isVehicle && calculatedSpeed in 0.5..6.0) {
                speedSamples.addLast(calculatedSpeed)
                if (speedSamples.size > 15) speedSamples.removeFirst()

                val avgSpeed = speedSamples.average()
                if (avgSpeed > 0.1) {
                    val paceMinPerKm = 1000.0 / (avgSpeed * 60.0)
                    currentPace = if (currentPace == 0.0) paceMinPerKm else currentPace * 0.85 + paceMinPerKm * 0.15
                }
            }
        }

        lastLocation = newLocation
        lastTimestamp = newLocation.time

        val averagePace = if (totalDistanceMeters >= 10 && movingTimeMillis > 0) {
            (movingTimeMillis / 60000.0) / (totalDistanceMeters / 1000.0)
        } else {
            0.0
        }

        return RunMetrics(totalDistanceMeters, currentPace, averagePace, movingTimeMillis)
    }

    fun reset() {
        totalDistanceMeters = 0.0
        movingTimeMillis = 0
        lastLocation = null
        lastTimestamp = null
        speedSamples.clear()
        currentPace = 0.0
        currentAcceleration = 0.0
    }

    fun release() {
        sensorManager.unregisterListener(this)
    }
}