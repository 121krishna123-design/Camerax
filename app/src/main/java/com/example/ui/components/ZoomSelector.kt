package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VivoGold
import java.util.Locale
import kotlin.math.abs

@Composable
fun ZoomSelector(
    currentZoom: Float,
    minZoom: Float = 0.6f,
    maxZoom: Float = 10.0f,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showContinuousSlider by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Continuous Zoom Slider (when activated)
        AnimatedVisibility(
            visible = showContinuousSlider,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = CameraSurfaceElevated.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.1f", currentZoom)}x",
                        color = VivoGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Slider(
                        value = currentZoom,
                        onValueChange = onZoomChange,
                        valueRange = minZoom..maxZoom,
                        colors = SliderDefaults.colors(
                            thumbColor = VivoGold,
                            activeTrackColor = VivoGold,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .size(width = 200.dp, height = 36.dp)
                            .testTag("zoom_slider")
                    )
                }
            }
        }

        // Quick Preset Pills (0.6x, 1x, 2x In-Sensor Lossless, 5x, 10x)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CameraGlass,
            modifier = Modifier.testTag("zoom_pill_container")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val zoomPills = listOf(
                    0.6f to ".6",
                    1.0f to "1x",
                    2.0f to "2x", // 2x Lossless In-Sensor Zoom on Vivo T3
                    5.0f to "5x",
                    10.0f to "10x"
                )

                zoomPills.forEach { (zoomValue, label) ->
                    val isSelected = abs(currentZoom - zoomValue) < 0.15f
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) VivoGold else Color.Transparent)
                            .clickable {
                                onZoomChange(zoomValue)
                                showContinuousSlider = !showContinuousSlider
                            }
                            .testTag("zoom_btn_$label"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
