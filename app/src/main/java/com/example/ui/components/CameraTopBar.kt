package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HdrAuto
import androidx.compose.material.icons.filled.HdrOff
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraUiState
import com.example.model.AspectRatioMode
import com.example.model.CameraMode
import com.example.model.FlashMode
import com.example.model.HdrMode
import com.example.model.TimerMode
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold

@Composable
fun CameraTopBar(
    uiState: CameraUiState,
    onFlashToggle: () -> Unit,
    onFlashSelect: (FlashMode) -> Unit,
    onHdrToggle: () -> Unit,
    on50MpToggle: () -> Unit,
    onAspectRatioSelect: (AspectRatioMode) -> Unit,
    onTimerSelect: (TimerMode) -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleExpandFlash: () -> Unit,
    onToggleExpandRatio: () -> Unit,
    onToggleExpandTimer: () -> Unit,
    expandedFlash: Boolean,
    expandedRatio: Boolean,
    expandedTimer: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Quick Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Button
            IconButton(
                onClick = onToggleExpandFlash,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_flash_toggle")
            ) {
                Icon(
                    imageVector = when (uiState.flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.TORCH -> Icons.Default.Highlight
                        FlashMode.AURA_LIGHT -> Icons.Default.AutoAwesome
                    },
                    contentDescription = "Flash mode",
                    tint = if (uiState.flashMode != FlashMode.OFF) VivoGold else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // HDR Button
            IconButton(
                onClick = onHdrToggle,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_hdr_toggle")
            ) {
                Icon(
                    imageVector = when (uiState.hdrMode) {
                        HdrMode.OFF -> Icons.Default.HdrOff
                        HdrMode.AUTO -> Icons.Default.HdrAuto
                        HdrMode.ON -> Icons.Default.HdrOn
                    },
                    contentDescription = "HDR mode",
                    tint = if (uiState.hdrMode != HdrMode.OFF) VivoGold else TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 50MP Ultra HD Badge (Vivo T3 Sony IMX882)
            Surface(
                onClick = on50MpToggle,
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.currentMode == CameraMode.HIGH_RES_50MP) VivoGold else CameraGlass,
                modifier = Modifier
                    .height(28.dp)
                    .testTag("badge_50mp")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "50MP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.currentMode == CameraMode.HIGH_RES_50MP) Color.Black else TextGold
                    )
                }
            }

            // Aspect Ratio Button
            Surface(
                onClick = onToggleExpandRatio,
                shape = RoundedCornerShape(12.dp),
                color = CameraGlass,
                modifier = Modifier
                    .height(28.dp)
                    .testTag("btn_aspect_ratio")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.aspectRatio.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // Timer Button
            IconButton(
                onClick = onToggleExpandTimer,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_timer_toggle")
            ) {
                Icon(
                    imageVector = when (uiState.timerMode) {
                        TimerMode.OFF -> Icons.Default.TimerOff
                        TimerMode.S3 -> Icons.Default.Timer3
                        TimerMode.S5 -> Icons.Default.Timer
                        TimerMode.S10 -> Icons.Default.Timer10
                    },
                    contentDescription = "Timer",
                    tint = if (uiState.timerMode != TimerMode.OFF) VivoGold else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Filter Button
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_filter_palette")
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Color Filters",
                    tint = if (uiState.currentFilter != com.example.model.FilterType.ORIGINAL) VivoGold else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Settings Button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("btn_camera_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Camera Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Expandable Flash Selector Row
        AnimatedVisibility(
            visible = expandedFlash,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = CameraSurfaceElevated.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlashMode.values().forEach { mode ->
                        val isSelected = uiState.flashMode == mode
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onFlashSelect(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    FlashMode.OFF -> Icons.Default.FlashOff
                                    FlashMode.AUTO -> Icons.Default.FlashAuto
                                    FlashMode.ON -> Icons.Default.FlashOn
                                    FlashMode.TORCH -> Icons.Default.Highlight
                                    FlashMode.AURA_LIGHT -> Icons.Default.AutoAwesome
                                },
                                contentDescription = mode.name,
                                tint = if (isSelected) VivoGold else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = when (mode) {
                                    FlashMode.OFF -> "Off"
                                    FlashMode.AUTO -> "Auto"
                                    FlashMode.ON -> "On"
                                    FlashMode.TORCH -> "Torch"
                                    FlashMode.AURA_LIGHT -> "Aura"
                                },
                                fontSize = 10.sp,
                                color = if (isSelected) VivoGold else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Expandable Aspect Ratio Selector Row
        AnimatedVisibility(
            visible = expandedRatio,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = CameraSurfaceElevated.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AspectRatioMode.values().forEach { ratio ->
                        val isSelected = uiState.aspectRatio == ratio
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VivoGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onAspectRatioSelect(ratio) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = ratio.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VivoGold else TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Expandable Timer Selector Row
        AnimatedVisibility(
            visible = expandedTimer,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = CameraSurfaceElevated.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimerMode.values().forEach { timer ->
                        val isSelected = uiState.timerMode == timer
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) VivoGold.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { onTimerSelect(timer) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = timer.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) VivoGold else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
