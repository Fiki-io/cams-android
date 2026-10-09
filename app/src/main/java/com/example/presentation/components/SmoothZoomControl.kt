package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentActiveState
import com.example.ui.theme.NeutralBlack
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary
import java.util.Locale

@Composable
fun SmoothZoomControl(
    currentZoom: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickPresets = listOf(1.0f, 2.0f, 5.0f).filter { it <= maxZoom }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick preset buttons (Pill badges)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            quickPresets.forEach { preset ->
                val isSelected = kotlin.math.abs(currentZoom - preset) < 0.15f
                Surface(
                    onClick = { onZoomChange(preset) },
                    shape = CircleShape,
                    color = if (isSelected) AccentActiveState else NeutralDarkSurface.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorder),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("zoom_preset_${preset.toInt()}x")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${preset.toInt()}x",
                            fontSize = 11.sp,
                            color = if (isSelected) NeutralBlack else NeutralTextPrimary
                        )
                    }
                }
            }
        }

        // Smooth Dial Slider (Drag gesture with spring feedback)
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = NeutralDarkSurface.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorder),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(44.dp)
                .testTag("smooth_zoom_slider")
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Sensitivity delta: 300px drag = 2.0x zoom change
                        val deltaRatio = -dragAmount.x / 180f
                        onZoomChange((currentZoom + deltaRatio).coerceIn(minZoom, maxZoom))
                    }
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format(Locale.US, "%.1fx", minZoom),
                    fontSize = 11.sp,
                    color = NeutralTextSecondary
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", currentZoom),
                    fontSize = 14.sp,
                    color = AccentActiveState,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = String.format(Locale.US, "%.1fx", maxZoom),
                    fontSize = 11.sp,
                    color = NeutralTextSecondary
                )
            }
        }
    }
}
