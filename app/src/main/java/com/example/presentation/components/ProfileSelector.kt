package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.camera.CameraProfile
import com.example.ui.theme.AccentActiveState
import com.example.ui.theme.NeutralBlack
import com.example.ui.theme.NeutralBorder
import com.example.ui.theme.NeutralDarkSurface
import com.example.ui.theme.NeutralTextPrimary
import com.example.ui.theme.NeutralTextSecondary

@Composable
fun ProfileSelector(
    selectedProfile: CameraProfile,
    onProfileSelected: (CameraProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val profiles = CameraProfile.values()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        profiles.forEach { profile ->
            val isSelected = profile == selectedProfile
            Surface(
                onClick = { onProfileSelected(profile) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) AccentActiveState else NeutralDarkSurface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, NeutralBorder),
                modifier = Modifier.testTag("profile_pill_${profile.name.lowercase()}")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = profile.title,
                        fontSize = 12.sp,
                        color = if (isSelected) NeutralBlack else NeutralTextPrimary
                    )
                    Text(
                        text = "${profile.targetFps} fps / ${profile.targetBitrateMbps} Mbps",
                        fontSize = 9.sp,
                        color = if (isSelected) NeutralBlack.copy(alpha = 0.75f) else NeutralTextSecondary
                    )
                }
            }
        }
    }
}
