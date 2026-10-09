package com.example.domain.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build

data class HardwareCapabilities(
    val hasOis: Boolean,
    val hasStandardEis: Boolean,
    val hasPreviewStabilization: Boolean,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
    val hardwareLevel: String,
    val isLogicalMultiCamera: Boolean,
    val maxTonemapPoints: Int,
    val isNoiseReductionHighQualitySupported: Boolean,
    val isEdgeHighQualitySupported: Boolean
)

class HardwareCapabilityDetector(private val context: Context) {

    fun detectBackCameraCapabilities(): HardwareCapabilities {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return createDefaultCapabilities()

        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    val oisModes = characteristics.get(
                        CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION
                    ) ?: intArrayOf()
                    val hasOis = oisModes.contains(
                        CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON
                    )

                    val videoStabModes = characteristics.get(
                        CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES
                    ) ?: intArrayOf()
                    val hasStandardEis = videoStabModes.contains(
                        CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON
                    )
                    val hasPreviewStab = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        videoStabModes.contains(
                            CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION
                        )
                    } else false

                    val zoomRange = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        characteristics.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    } else null

                    val minZoom = zoomRange?.lower ?: 1.0f
                    val maxZoom = zoomRange?.upper ?: 10.0f

                    val hwLevelInt = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val hwLevel = when (hwLevelInt) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3"
                        else -> "EXTERNAL"
                    }

                    val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
                    ) ?: intArrayOf()
                    val isLogicalMulti = capabilities.contains(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
                    )

                    val maxTonemap = characteristics.get(
                        CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS
                    ) ?: 0

                    val nrModes = characteristics.get(
                        CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES
                    ) ?: intArrayOf()
                    val hasNrHq = nrModes.contains(CameraMetadata.NOISE_REDUCTION_MODE_HIGH_QUALITY)

                    val edgeModes = characteristics.get(
                        CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES
                    ) ?: intArrayOf()
                    val hasEdgeHq = edgeModes.contains(CameraMetadata.EDGE_MODE_HIGH_QUALITY)

                    return HardwareCapabilities(
                        hasOis = hasOis,
                        hasStandardEis = hasStandardEis,
                        hasPreviewStabilization = hasPreviewStab,
                        minZoomRatio = minZoom,
                        maxZoomRatio = maxZoom,
                        hardwareLevel = hwLevel,
                        isLogicalMultiCamera = isLogicalMulti,
                        maxTonemapPoints = maxTonemap,
                        isNoiseReductionHighQualitySupported = hasNrHq,
                        isEdgeHighQualitySupported = hasEdgeHq
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return createDefaultCapabilities()
    }

    private fun createDefaultCapabilities(): HardwareCapabilities {
        return HardwareCapabilities(
            hasOis = false,
            hasStandardEis = true,
            hasPreviewStabilization = false,
            minZoomRatio = 1.0f,
            maxZoomRatio = 10.0f,
            hardwareLevel = "UNKNOWN",
            isLogicalMultiCamera = false,
            maxTonemapPoints = 0,
            isNoiseReductionHighQualitySupported = true,
            isEdgeHighQualitySupported = true
        )
    }
}
