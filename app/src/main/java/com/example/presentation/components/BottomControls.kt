package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.camera.FramingGuide
import com.example.ui.theme.AccentActiveState
import com.example.ui.theme.AccentRecordingActive
import com.example.ui.theme.NeutralBlack
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary

@Composable
fun BottomControls(
    isRecording: Boolean,
    onRecordToggle: () -> Unit,
    isGridVisible: Boolean,
    onToggleGrid: () -> Unit,
    isHorizonLevelVisible: Boolean,
    onToggleLevel: () -> Unit,
    framingGuide: FramingGuide,
    onCycleFramingGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left auxiliary: Guide cycle
        Surface(
            onClick = onCycleFramingGuide,
            shape = RoundedCornerShape(12.dp),
            color = NeutralDarkSurface.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, NeutralBorder),
            modifier = Modifier
                .size(46.dp)
                .testTag("framing_guide_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = when (framingGuide) {
                        FramingGuide.FULL -> "16:9"
                        FramingGuide.CINEMASCOPE -> "2.39"
                        FramingGuide.ACADEMY -> "1.85"
                        FramingGuide.SQUARE -> "1:1"
                    },
                    fontSize = 11.sp,
                    color = NeutralTextPrimary
                )
            }
        }

        // Secondary: Grid toggle
        IconButton(
            onClick = onToggleGrid,
            modifier = Modifier
                .size(42.dp)
                .testTag("toggle_grid_button")
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Toggle Grid",
                tint = if (isGridVisible) AccentActiveState else NeutralTextSecondary.copy(alpha = 0.5f)
            )
        }

        // Center: Heavy Pro Record Button (Physical-feel shutter)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(76.dp)
        ) {
            // Outer bezel ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
            )
            Surface(
                onClick = onRecordToggle,
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(3.dp, if (isRecording) AccentRecordingActive else AccentActiveState),
                modifier = Modifier
                    .size(76.dp)
                    .testTag("record_shutter_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isRecording) {
                        // Square stop icon
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AccentRecordingActive)
                        )
                    } else {
                        // Solid inner trigger circle
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AccentRecordingActive)
                        )
                    }
                }
            }
        }

        // Secondary: Horizon level toggle
        IconButton(
            onClick = onToggleLevel,
            modifier = Modifier
                .size(42.dp)
                .testTag("toggle_level_button")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Toggle Horizon Level",
                tint = if (isHorizonLevelVisible) AccentActiveState else NeutralTextSecondary.copy(alpha = 0.5f)
            )
        }

        // Empty spacer to balance layout
        Spacer(modifier = Modifier.size(46.dp))
    }
}
