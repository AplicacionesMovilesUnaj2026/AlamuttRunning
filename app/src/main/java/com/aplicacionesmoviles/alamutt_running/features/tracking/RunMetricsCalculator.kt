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

    // velocidad en m/s
    private val speedSamples = ArrayDeque<Double>()

    // ritmo actual suavizado (min/km)
    private var currentPace = 0.0

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    init {
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.let {
            currentAcceleration = sqrt(
                (
                        it[0] * it[0] +
                                it[1] * it[1] +
                                it[2] * it[2]
                        ).toDouble()
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun updateMetrics(newLocation: Location): RunMetrics {

        val previousLocation = lastLocation
        val previousTimestamp = lastTimestamp

        if (previousLocation != null) {

            val distance = previousLocation.distanceTo(newLocation).toDouble()

            val deltaTimeMillis =
                (newLocation.time - previousLocation.time).coerceAtLeast(0)

            val deltaTimeSeconds = deltaTimeMillis / 1000.0

            val calculatedSpeed =
                if (deltaTimeSeconds > 0)
                    distance / deltaTimeSeconds
                else
                    0.0

            val gpsValid =
                newLocation.accuracy <= 15f

            val realisticSpeed =
                calculatedSpeed in 0.5..8.5

            if (
                gpsValid &&
                distance >= 1.0 &&
                realisticSpeed
            ) {
                totalDistanceMeters += distance
            }

            val isMoving =
                currentAcceleration > 0.2 ||
                        calculatedSpeed > 0.8

            if (isMoving && deltaTimeMillis > 0) {
                movingTimeMillis += deltaTimeMillis
            }

            val gpsSpeed =
                if (
                    newLocation.hasSpeed() &&
                    newLocation.speed > 0.5f
                ) {
                    newLocation.speed.toDouble()
                } else {
                    calculatedSpeed
                }

            if (gpsSpeed in 0.5..8.5) {

                speedSamples.addLast(gpsSpeed)

                while (speedSamples.size > 15) {
                    speedSamples.removeFirst()
                }

                val avgSpeed = speedSamples.average()

                if (avgSpeed > 0.1) {

                    val paceMinPerKm =
                        1000.0 / (avgSpeed * 60.0)

                    currentPace =
                        if (currentPace == 0.0)
                            paceMinPerKm
                        else
                            currentPace * 0.85 +
                                    paceMinPerKm * 0.15
                }
            }
        }

        lastLocation = newLocation
        lastTimestamp = newLocation.time

        val averagePace =
            if (
                totalDistanceMeters >= 10 &&
                movingTimeMillis > 0
            ) {
                (movingTimeMillis / 60000.0) /
                        (totalDistanceMeters / 1000.0)
            } else {
                0.0
            }

        return RunMetrics(
            distanceMeters = totalDistanceMeters,
            currentPace = currentPace,
            averagePace = averagePace,
            movingTimeMillis = movingTimeMillis
        )
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