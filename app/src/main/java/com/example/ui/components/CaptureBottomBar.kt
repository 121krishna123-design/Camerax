package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.camera.CameraUiState
import com.example.model.CameraMode
import com.example.ui.theme.AccentRed
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VivoGold
import com.example.ui.theme.VivoGoldLight
import java.io.File

@Composable
fun CaptureBottomBar(
    uiState: CameraUiState,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shutterScale by animateFloatAsState(
        targetValue = if (isPressed || uiState.isCapturing) 0.88f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "shutter_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Gallery Thumbnail / Album button
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CameraGlass)
                .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
                .clickable { onGalleryClick() }
                .testTag("btn_gallery_thumbnail"),
            contentAlignment = Alignment.Center
        ) {
            val lastItem = uiState.lastCapturedItem
            if (lastItem != null && File(lastItem.filePath).exists()) {
                Image(
                    painter = rememberAsyncImagePainter(File(lastItem.filePath)),
                    contentDescription = "Last Captured Photo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Gallery",
                    tint = TextPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 2. Primary Shutter Trigger
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(shutterScale)
                .testTag("btn_camera_shutter")
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) { onShutterClick() },
            contentAlignment = Alignment.Center
        ) {
            // Outer golden ring
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(
                    3.5.dp,
                    if (uiState.currentMode == CameraMode.VIDEO && uiState.isRecordingVideo) AccentRed else VivoGold
                )
            ) {}

            // Inner button body
            if (uiState.countdownSeconds > 0) {
                // Countdown text display
                Text(
                    text = "${uiState.countdownSeconds}",
                    color = VivoGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (uiState.currentMode == CameraMode.VIDEO) {
                if (uiState.isRecordingVideo) {
                    // Recording Red Square
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = AccentRed
                    ) {}
                } else {
                    // Video Record Red Dot
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = AccentRed
                    ) {}
                }
            } else {
                // Photo Mode White + Gold core
                Surface(
                    modifier = Modifier.size(66.dp),
                    shape = CircleShape,
                    color = if (uiState.currentMode == CameraMode.NIGHT) VivoGoldLight else Color.White
                ) {}
            }
        }

        // 3. Flip Camera Sensor Button (Front / Rear)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(CameraGlass)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), CircleShape)
                .clickable { onFlipCameraClick() }
                .testTag("btn_flip_camera"),
            contentAlignment = Alignment.Center
        ) {
            val rotationAngle by animateFloatAsState(
                targetValue = if (uiState.isFrontCamera) 180f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "camera_flip_rotation"
            )
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = "Switch Camera",
                tint = TextPrimary,
                modifier = Modifier
                    .size(26.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}
