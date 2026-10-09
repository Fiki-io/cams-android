package com.example.domain.camera

enum class CameraProfile(
    val title: String,
    val description: String,
    val targetBitrateMbps: Int,
    val targetFps: Int
) {
    CINEMATIC_LOG(
        title = "Cinematic Flat",
        description = "High dynamic range tonemapping, natural sharpness, 24fps motion blur",
        targetBitrateMbps = 70,
        targetFps = 24
    ),
    PRO_VIDEO_HD(
        title = "Pro HD 60",
        description = "Full sensor readout, 60fps high bitrate fluid master",
        targetBitrateMbps = 80,
        targetFps = 60
    ),
    ULTRA_STABLE(
        title = "Ultra Steady",
        description = "Synchronized hardware OIS + EIS preview lock with gyro damping",
        targetBitrateMbps = 50,
        targetFps = 30
    )
}

enum class FramingGuide(val title: String, val aspectRatioNumerator: Float, val aspectRatioDenominator: Float) {
    FULL("Full", 16f, 9f),
    CINEMASCOPE("2.39:1 Cine", 2.39f, 1f),
    ACADEMY("1.85:1 Flat", 1.85f, 1f),
    SQUARE("1:1", 1f, 1f)
}
