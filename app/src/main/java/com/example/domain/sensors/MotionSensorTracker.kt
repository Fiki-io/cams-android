package com.example.domain.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class MotionTelemetry(
    val rollAngleDeg: Float = 0f,
    val pitchAngleDeg: Float = 0f,
    val angularVelocityRps: Float = 0f,
    val isLeveled: Boolean = false,
    val stabilityScorePct: Int = 100
)

class MotionSensorTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val _telemetry = MutableStateFlow(MotionTelemetry())
    val telemetry: StateFlow<MotionTelemetry> = _telemetry.asStateFlow()

    private var currentRoll = 0f
    private var currentPitch = 0f
    private var currentAngVel = 0f

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscope?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                // Low-pass filter for smooth horizon reading
                val alpha = 0.15f
                val targetRoll = (atan2(ax.toDouble(), ay.toDouble()) * (180.0 / Math.PI)).toFloat() - 90f
                val targetPitch = (atan2(-az.toDouble(), sqrt((ax * ax + ay * ay).toDouble())) * (180.0 / Math.PI)).toFloat()

                currentRoll += alpha * (targetRoll - currentRoll)
                currentPitch += alpha * (targetPitch - currentPitch)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                val magnitude = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()

                val alpha = 0.2f
                currentAngVel += alpha * (magnitude - currentAngVel)
            }
        }

        // Evaluate level (within 1.5 degree threshold)
        val isLeveled = abs(currentRoll) < 1.5f

        // Stability score based on rotational velocity (0 rps = 100%, >2.0 rps = low)
        val stability = ((1.0f - (currentAngVel / 2.0f).coerceIn(0f, 1f)) * 100f).roundToInt()

        _telemetry.value = MotionTelemetry(
            rollAngleDeg = currentRoll,
            pitchAngleDeg = currentPitch,
            angularVelocityRps = currentAngVel,
            isLeveled = isLeveled,
            stabilityScorePct = stability
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
