package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.camera.HardwareCapabilities
import com.example.domain.sensors.MotionTelemetry
import com.example.ui.theme.AccentActiveState
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import java.util.Locale

@Composable
fun HardwareInspectionModal(
    capabilities: HardwareCapabilities?,
    telemetry: MotionTelemetry,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NeutralDarkSurface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, NeutralBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("hardware_inspection_modal")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hardware Inspection Report",
                    fontSize = 14.sp,
                    color = AccentActiveState
                )
                Text(
                    text = "Close",
                    fontSize = 12.sp,
                    color = NeutralTextSecondary,
                    modifier = Modifier.testTag("close_inspection_button")
                )
            }

            Text(
                text = "Target Device HAL: Infinix Note 60 5G ISP Pipeline",
                fontSize = 11.sp,
                color = NeutralTextSecondary
            )

            // Parameter checklist
            InfoRow("Camera2 Level", capabilities?.hardwareLevel ?: "DETECTION IN PROGRESS")
            InfoRow("Lens Optical Stabilization (OIS)", if (capabilities?.hasOis == true) "SUPPORTED" else "NO PHYSICAL OIS (FALLBACK EIS)")
            InfoRow("Video Stabilization (EIS)", if (capabilities?.hasStandardEis == true) "AVAILABLE" else "UNAVAILABLE")
            InfoRow("Preview Stabilization (Android 13+)", if (capabilities?.hasPreviewStabilization == true) "ACTIVE" else "STANDARD EIS MODE")
            InfoRow("Optical/Digital Zoom Range", "${capabilities?.minZoomRatio}x - ${capabilities?.maxZoomRatio}x")
            InfoRow("High Quality Noise Reduction", if (capabilities?.isNoiseReductionHighQualitySupported == true) "ENABLED" else "STANDARD")
            InfoRow("High Quality Edge Mode", if (capabilities?.isEdgeHighQualitySupported == true) "ENABLED" else "STANDARD")
            InfoRow("Angular Rotational Velocity", String.format(Locale.US, "%.2f rad/s", telemetry.angularVelocityRps))
            InfoRow("Pitch & Roll Inclination", String.format(Locale.US, "P: %.1f° | R: %.1f°", telemetry.pitchAngleDeg, telemetry.rollAngleDeg))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = NeutralTextSecondary)
        Text(text = value, fontSize = 11.sp, color = NeutralTextPrimary)
    }
}
