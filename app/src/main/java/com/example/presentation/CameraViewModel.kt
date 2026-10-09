package com.example.presentation

import android.content.Context
import androidx.camera.core.CameraControl
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.camera.CameraProfile
import com.example.domain.camera.FramingGuide
import com.example.domain.camera.HardwareCapabilities
import com.example.domain.camera.HardwareCapabilityDetector
import com.example.domain.camera.SmoothZoomController
import com.example.domain.sensors.MotionSensorTracker
import com.example.domain.sensors.MotionTelemetry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraUiState(
    val capabilities: HardwareCapabilities? = null,
    val selectedProfile: CameraProfile = CameraProfile.CINEMATIC_LOG,
    val framingGuide: FramingGuide = FramingGuide.FULL,
    val currentZoomRatio: Float = 1.0f,
    val isRecording: Boolean = false,
    val recordedDurationSeconds: Int = 0,
    val lastSavedVideoPath: String? = null,
    val isAudioEnabled: Boolean = true,
    val isGridVisible: Boolean = true,
    val isHorizonLevelVisible: Boolean = true,
    val isTelemetryPanelExpanded: Boolean = false,
    val statusMessage: String? = null
)

class CameraViewModel(
    context: Context
) : ViewModel() {

    private val detector = HardwareCapabilityDetector(context)
    private val motionTracker = MotionSensorTracker(context)
    val zoomController = SmoothZoomController(viewModelScope)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val motionTelemetry: StateFlow<MotionTelemetry> = motionTracker.telemetry

    private var recordingTimerJob: Job? = null

    init {
        val detectedCaps = detector.detectBackCameraCapabilities()
        _uiState.value = _uiState.value.copy(capabilities = detectedCaps)
        zoomController.initialize(detectedCaps.minZoomRatio, detectedCaps.maxZoomRatio, null)

        viewModelScope.launch {
            zoomController.currentZoomRatio.collect { ratio ->
                _uiState.value = _uiState.value.copy(currentZoomRatio = ratio)
            }
        }

        motionTracker.startListening()
    }

    fun onCameraControlReady(control: CameraControl?) {
        zoomController.updateCameraControl(control)
    }

    fun selectProfile(profile: CameraProfile) {
        _uiState.value = _uiState.value.copy(selectedProfile = profile)
    }

    fun setFramingGuide(guide: FramingGuide) {
        _uiState.value = _uiState.value.copy(framingGuide = guide)
    }

    fun setZoom(targetRatio: Float) {
        zoomController.setTargetZoom(targetRatio)
    }

    fun toggleGrid() {
        _uiState.value = _uiState.value.copy(isGridVisible = !_uiState.value.isGridVisible)
    }

    fun toggleHorizonLevel() {
        _uiState.value = _uiState.value.copy(isHorizonLevelVisible = !_uiState.value.isHorizonLevelVisible)
    }

    fun toggleTelemetryPanel() {
        _uiState.value = _uiState.value.copy(isTelemetryPanelExpanded = !_uiState.value.isTelemetryPanelExpanded)
    }

    fun onRecordingStarted() {
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordedDurationSeconds = 0,
            statusMessage = "Recording active at ${_uiState.value.selectedProfile.targetBitrateMbps} Mbps"
        )
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    recordedDurationSeconds = _uiState.value.recordedDurationSeconds + 1
                )
            }
        }
    }

    fun onRecordingFinished(savedPath: String?) {
        recordingTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            lastSavedVideoPath = savedPath,
            statusMessage = if (savedPath != null) "Video master saved successfully" else "Recording saved"
        )
    }

    fun onRecordingError(message: String) {
        recordingTimerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            statusMessage = "Recording error: $message"
        )
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        motionTracker.stopListening()
        recordingTimerJob?.cancel()
    }
}
