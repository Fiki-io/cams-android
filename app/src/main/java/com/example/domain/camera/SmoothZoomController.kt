package com.example.domain.camera

import androidx.camera.core.CameraControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class SmoothZoomController(
    private val scope: CoroutineScope
) {
    private val _currentZoomRatio = MutableStateFlow(1.0f)
    val currentZoomRatio: StateFlow<Float> = _currentZoomRatio.asStateFlow()

    private var targetZoomRatio = 1.0f
    private var minRatio = 1.0f
    private var maxRatio = 10.0f
    private var zoomAnimationJob: Job? = null
    private var cameraControl: CameraControl? = null

    fun initialize(min: Float, max: Float, control: CameraControl?) {
        minRatio = min.coerceAtLeast(1.0f)
        maxRatio = max.coerceAtMost(10.0f).coerceAtLeast(minRatio)
        targetZoomRatio = 1.0f
        _currentZoomRatio.value = 1.0f
        cameraControl = control
        cameraControl?.setZoomRatio(1.0f)
    }

    fun updateCameraControl(control: CameraControl?) {
        cameraControl = control
    }

    fun setTargetZoom(target: Float) {
        val clampedTarget = target.coerceIn(minRatio, maxRatio)
        targetZoomRatio = clampedTarget

        if (zoomAnimationJob?.isActive != true) {
            startSmoothZoomLoop()
        }
    }

    fun stepZoom(delta: Float) {
        setTargetZoom(targetZoomRatio + delta)
    }

    private fun startSmoothZoomLoop() {
        zoomAnimationJob = scope.launch {
            // 60 Hz smooth damping loop (16ms per step)
            val dampingFactor = 0.22f // Exponential spring damping
            while (abs(_currentZoomRatio.value - targetZoomRatio) > 0.01f) {
                val current = _currentZoomRatio.value
                val next = current + (targetZoomRatio - current) * dampingFactor
                _currentZoomRatio.value = next
                cameraControl?.setZoomRatio(next)
                delay(16L)
            }
            // Settle on final exact target
            _currentZoomRatio.value = targetZoomRatio
            cameraControl?.setZoomRatio(targetZoomRatio)
        }
    }
}
