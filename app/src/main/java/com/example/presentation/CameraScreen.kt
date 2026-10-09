package com.example.presentation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.camera.FramingGuide
import com.example.presentation.components.BottomControls
import com.example.presentation.components.HardwareInspectionModal
import com.example.presentation.components.ProfileSelector
import com.example.presentation.components.SmoothZoomControl
import com.example.presentation.components.TopTelemetryBar
import com.example.presentation.components.ViewfinderOverlay
import com.example.ui.theme.NeutralBlack
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()
    val telemetry by viewModel.motionTelemetry.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }

    // Active recording handle
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] == true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NeutralBlack,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                // Live Camera Viewfinder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            // High-quality recorder target
                            val recorder = Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                                .build()
                            val vCapture = VideoCapture.withOutput(recorder)
                            videoCapture = vCapture

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    vCapture
                                )
                                viewModel.onCameraControlReady(camera.cameraControl)
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay 1: Framing Mask, Horizon Leveler, Thirds Grid
                ViewfinderOverlay(
                    framingGuide = uiState.framingGuide,
                    isGridVisible = uiState.isGridVisible,
                    isHorizonLevelVisible = uiState.isHorizonLevelVisible,
                    motionTelemetry = telemetry,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay 2: Top Status Bar
                TopTelemetryBar(
                    profile = uiState.selectedProfile,
                    capabilities = uiState.capabilities,
                    telemetry = telemetry,
                    isRecording = uiState.isRecording,
                    durationSeconds = uiState.recordedDurationSeconds,
                    onToggleDetails = { viewModel.toggleTelemetryPanel() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                // Overlay 3: Bottom Control Stack
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    // Smooth Zoom dial
                    SmoothZoomControl(
                        currentZoom = uiState.currentZoomRatio,
                        minZoom = uiState.capabilities?.minZoomRatio ?: 1.0f,
                        maxZoom = uiState.capabilities?.maxZoomRatio ?: 10.0f,
                        onZoomChange = { viewModel.setZoom(it) }
                    )

                    // Profile selector pills
                    ProfileSelector(
                        selectedProfile = uiState.selectedProfile,
                        onProfileSelected = { viewModel.selectProfile(it) },
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Physical Record Shutter and quick controls
                    BottomControls(
                        isRecording = uiState.isRecording,
                        onRecordToggle = {
                            if (uiState.isRecording) {
                                activeRecording?.stop()
                                activeRecording = null
                            } else {
                                startVideoRecording(
                                    context = context,
                                    videoCapture = videoCapture,
                                    hasAudioPermission = hasAudioPermission,
                                    onStarted = { recording ->
                                        activeRecording = recording
                                        viewModel.onRecordingStarted()
                                    },
                                    onFinished = { savedPath ->
                                        viewModel.onRecordingFinished(savedPath)
                                    },
                                    onError = { error ->
                                        viewModel.onRecordingError(error)
                                    }
                                )
                            }
                        },
                        isGridVisible = uiState.isGridVisible,
                        onToggleGrid = { viewModel.toggleGrid() },
                        isHorizonLevelVisible = uiState.isHorizonLevelVisible,
                        onToggleLevel = { viewModel.toggleHorizonLevel() },
                        framingGuide = uiState.framingGuide,
                        onCycleFramingGuide = {
                            val next = when (uiState.framingGuide) {
                                FramingGuide.FULL -> FramingGuide.CINEMASCOPE
                                FramingGuide.CINEMASCOPE -> FramingGuide.ACADEMY
                                FramingGuide.ACADEMY -> FramingGuide.SQUARE
                                FramingGuide.SQUARE -> FramingGuide.FULL
                            }
                            viewModel.setFramingGuide(next)
                        }
                    )
                }

                // Modal: Hardware inspection
                if (uiState.isTelemetryPanelExpanded) {
                    HardwareInspectionModal(
                        capabilities = uiState.capabilities,
                        telemetry = telemetry,
                        onClose = { viewModel.toggleTelemetryPanel() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                // Permission Fallback Screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Camera & Audio permissions required for direct ISP video capture.",
                        color = NeutralTextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

private fun startVideoRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>?,
    hasAudioPermission: Boolean,
    onStarted: (Recording) -> Unit,
    onFinished: (String?) -> Unit,
    onError: (String) -> Unit
) {
    val capture = videoCapture ?: run {
        onError("Camera capture engine not ready")
        return
    }

    val name = "CAMS_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Cams")
        }
    }

    val mediaStoreOutput = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues).build()

    val pendingRecording = capture.output
        .prepareRecording(context, mediaStoreOutput)

    val recordingWithAudio = if (hasAudioPermission) {
        try {
            pendingRecording.withAudioEnabled()
        } catch (e: SecurityException) {
            pendingRecording
        }
    } else pendingRecording

    val recording = recordingWithAudio.start(ContextCompat.getMainExecutor(context)) { event ->
        when (event) {
            is VideoRecordEvent.Start -> {
                // Recording actively running
            }
            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    onError("Recording error code: ${event.error}")
                } else {
                    onFinished(event.outputResults.outputUri.toString())
                }
            }
        }
    }

    onStarted(recording)
}
