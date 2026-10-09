package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.camera.CameraProfile
import com.example.domain.camera.HardwareCapabilities
import com.example.domain.sensors.MotionTelemetry
import com.example.ui.theme.AccentActiveState
import com.example.ui.theme.AccentLevelBalanced
import com.example.ui.theme.AccentRecordingActive
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import java.util.Locale

@Composable
fun TopTelemetryBar(
    profile: CameraProfile,
    capabilities: HardwareCapabilities?,
    telemetry: MotionTelemetry,
    isRecording: Boolean,
    durationSeconds: Int,
    onToggleDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NeutralDarkSurface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, NeutralBorder),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("top_telemetry_bar"),
        onClick = onToggleDetails
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Stabilization Badge (OIS / EIS state)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val oisLabel = if (capabilities?.hasOis == true) "OIS ON" else "EIS DAMP"
                val stabColor = if (telemetry.stabilityScorePct > 75) AccentLevelBalanced else AccentActiveState
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(stabColor, CircleShape)
                )
                Text(
                    text = oisLabel,
                    fontSize = 11.sp,
                    color = NeutralTextPrimary
                )
                Text(
                    text = "| ${telemetry.stabilityScorePct}% STAB",
                    fontSize = 10.sp,
                    color = NeutralTextSecondary
                )
            }

            // Center: Recording timer or Profile Bitrate
            if (isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AccentRecordingActive, CircleShape)
                    )
                    val minutes = durationSeconds / 60
                    val seconds = durationSeconds % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d REC", minutes, seconds),
                        fontSize = 12.sp,
                        color = NeutralTextPrimary
                    )
                }
            } else {
                Text(
                    text = "${profile.targetFps} FPS | ${profile.targetBitrateMbps} Mbps",
                    fontSize = 11.sp,
                    color = AccentActiveState
                )
            }

            // Right: Horizon tilt
            Text(
                text = String.format(Locale.US, "%.1f° TILT", telemetry.rollAngleDeg),
                fontSize = 10.sp,
                color = if (telemetry.isLeveled) AccentLevelBalanced else NeutralTextSecondary
            )
        }
    }
}
