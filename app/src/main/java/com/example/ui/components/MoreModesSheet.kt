package com.example.ui.components

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CameraMode
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold

data class MoreModeItem(
    val mode: CameraMode,
    val icon: ImageVector,
    val title: String,
    val description: String
)

val MORE_MODES_LIST = listOf(
    MoreModeItem(CameraMode.DOC_SCAN, Icons.Default.Description, "Document", "Scan, straighten & boost text"),
    MoreModeItem(CameraMode.ASTRO, Icons.Default.NightsStay, "Astro Mode", "Starry sky & Milky Way capture"),
    MoreModeItem(CameraMode.SLOW_MO, Icons.Default.SlowMotionVideo, "Slow-Mo", "120/240 fps smooth motion"),
    MoreModeItem(CameraMode.TIME_LAPSE, Icons.Default.Timelapse, "Time-Lapse", "Speed up clouds & city lights"),
    MoreModeItem(CameraMode.DUAL_VIEW, Icons.Default.Videocam, "Dual-View", "Simultaneous front & rear video"),
    MoreModeItem(CameraMode.LIGHT_PAINTING, Icons.Default.Brush, "Light Trails", "Traffic & fireworks painting")
)

@Composable
fun MoreModesSheet(
    onModeSelect: (CameraMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CameraSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_more_modes")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VIVO CREATIVE MODES",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = VivoGold
                )
                Text(
                    text = "Powered by Sony IMX882 OIS & Imaging Engine",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(MORE_MODES_LIST) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF14161D),
                            modifier = Modifier
                                .clickable {
                                    onModeSelect(item.mode)
                                    onDismiss()
                                }
                                .testTag("more_mode_${item.mode.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(VivoGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = VivoGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
