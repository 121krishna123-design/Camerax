package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.GridType
import com.example.model.VideoQuality
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold

@Composable
fun CameraSettingsDialog(
    gridType: GridType,
    onGridTypeChange: (GridType) -> Unit,
    videoQuality: VideoQuality,
    onVideoQualityChange: (VideoQuality) -> Unit,
    onWatermarkClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var shutterSound by remember { mutableStateOf(true) }
    var mirrorSelfie by remember { mutableStateOf(true) }
    var levelMeterEnabled by remember { mutableStateOf(true) }
    var antiFlicker by remember { mutableStateOf("Auto (50Hz/60Hz)") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CameraSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .testTag("dialog_camera_settings")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VIVO T3 CAMERA SETTINGS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = VivoGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Watermark Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWatermarkClick() }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BrandingWatermark, contentDescription = null, tint = VivoGold, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text("Watermark", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("vivo T3 5G | 50MP OIS", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                // Grid Type Setting
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = VivoGold, modifier = Modifier.size(20.dp))
                        Text("Composition Framing Lines", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GridType.values().forEach { type ->
                            val isSelected = gridType == type
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) VivoGold else Color(0xFF161820),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onGridTypeChange(type) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (type) {
                                        GridType.NONE -> "Off"
                                        GridType.RULE_OF_THIRDS -> "3x3"
                                        GridType.GOLDEN_RATIO -> "Ratio"
                                        GridType.SPIRAL -> "Spiral"
                                        GridType.CROSSHAIR -> "Cross"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                // Video Resolution
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Videocam, contentDescription = null, tint = VivoGold, modifier = Modifier.size(20.dp))
                        Text("Video Resolution", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VideoQuality.values().forEach { quality ->
                            val isSelected = videoQuality == quality
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) VivoGold else Color(0xFF161820),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onVideoQualityChange(quality) }
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = quality.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                // Electronic Leveler switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Straighten, contentDescription = null, tint = VivoGold, modifier = Modifier.size(20.dp))
                        Text("Horizon Level Meter", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                    }
                    Switch(
                        checked = levelMeterEnabled,
                        onCheckedChange = { levelMeterEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VivoGold
                        )
                    )
                }

                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))

                // Shutter sound switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = VivoGold, modifier = Modifier.size(20.dp))
                        Text("Shutter Sound", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                    }
                    Switch(
                        checked = shutterSound,
                        onCheckedChange = { shutterSound = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VivoGold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VivoGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
