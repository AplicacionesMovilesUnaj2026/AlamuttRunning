package com.aplicacionesmoviles.alamutt_running.features.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterManager(context: Context, private val onStepsUpdated: (Int) -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var stepCount = 0
    private var initialCounterValue = -1

    fun start() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                stepCount++
                onStepsUpdated(stepCount)
            } else if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                val currentTotal = it.values[0].toInt()
                if (initialCounterValue == -1) initialCounterValue = currentTotal
                onStepsUpdated(currentTotal - initialCounterValue)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}