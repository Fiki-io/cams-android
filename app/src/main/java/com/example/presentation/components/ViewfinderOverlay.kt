package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.domain.camera.FramingGuide
import com.example.domain.sensors.MotionTelemetry
import com.example.ui.theme.AccentLevelBalanced
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralTextMuted
import com.example.ui.theme.NeutralTextSecondary

@Composable
fun ViewfinderOverlay(
    framingGuide: FramingGuide,
    isGridVisible: Boolean,
    isHorizonLevelVisible: Boolean,
    motionTelemetry: MotionTelemetry,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Framing Guide Letterbox (Matte mask)
            if (framingGuide != FramingGuide.FULL) {
                val targetRatio = framingGuide.aspectRatioNumerator / framingGuide.aspectRatioDenominator
                val currentRatio = width / height

                if (currentRatio < targetRatio) {
                    // Fit width, crop height
                    val targetHeight = width / targetRatio
                    val barHeight = (height - targetHeight) / 2f
                    // Top letterbox
                    drawRect(
                        color = Color.Black.copy(alpha = 0.75f),
                        topLeft = Offset(0f, 0f),
                        size = Size(width, barHeight)
                    )
                    // Bottom letterbox
                    drawRect(
                        color = Color.Black.copy(alpha = 0.75f),
                        topLeft = Offset(0f, height - barHeight),
                        size = Size(width, barHeight)
                    )
                } else {
                    // Fit height, pillarbox width
                    val targetWidth = height * targetRatio
                    val barWidth = (width - targetWidth) / 2f
                    // Left
                    drawRect(
                        color = Color.Black.copy(alpha = 0.75f),
                        topLeft = Offset(0f, 0f),
                        size = Size(barWidth, height)
                    )
                    // Right
                    drawRect(
                        color = Color.Black.copy(alpha = 0.75f),
                        topLeft = Offset(width - barWidth, 0f),
                        size = Size(barWidth, height)
                    )
                }
            }

            // 2. Rule of Thirds Cine Grid
            if (isGridVisible) {
                val col1 = width / 3f
                val col2 = width * 2f / 3f
                val row1 = height / 3f
                val row2 = height * 2f / 3f

                val gridColor = Color.White.copy(alpha = 0.18f)
                val strokeW = 1.5f

                drawLine(gridColor, Offset(col1, 0f), Offset(col1, height), strokeWidth = strokeW)
                drawLine(gridColor, Offset(col2, 0f), Offset(col2, height), strokeWidth = strokeW)
                drawLine(gridColor, Offset(0f, row1), Offset(width, row1), strokeWidth = strokeW)
                drawLine(gridColor, Offset(0f, row2), Offset(width, row2), strokeWidth = strokeW)
            }

            // 3. Virtual Gyroscope Horizon Leveler
            if (isHorizonLevelVisible) {
                val centerX = width / 2f
                val centerY = height / 2f
                val levelColor = if (motionTelemetry.isLeveled) AccentLevelBalanced else Color.White.copy(alpha = 0.65f)

                // Center crosshair reference
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = 4f,
                    center = Offset(centerX, centerY)
                )

                // Rotating dynamic horizon line
                rotate(degrees = -motionTelemetry.rollAngleDeg, pivot = Offset(centerX, centerY)) {
                    val lineLength = 120f
                    val gap = 20f

                    // Left wing
                    drawLine(
                        color = levelColor,
                        start = Offset(centerX - lineLength, centerY),
                        end = Offset(centerX - gap, centerY),
                        strokeWidth = 2.5f
                    )
                    // Right wing
                    drawLine(
                        color = levelColor,
                        start = Offset(centerX + gap, centerY),
                        end = Offset(centerX + lineLength, centerY),
                        strokeWidth = 2.5f
                    )
                }
            }
        }
    }
}
