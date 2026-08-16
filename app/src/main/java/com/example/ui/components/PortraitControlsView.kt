package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.BokehShape
import com.example.model.PortraitSettings
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VivoGold
import java.util.Locale

@Composable
fun PortraitControlsView(
    portraitSettings: PortraitSettings,
    onPortraitSettingsChange: (PortraitSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Aperture f-stop slider (f/0.95 - f/16)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CameraSurfaceElevated.copy(alpha = 0.9f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aperture Blur",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "f/${String.format(Locale.US, "%.1f", portraitSettings.apertureFStop)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VivoGold
                    )
                }

                Slider(
                    value = portraitSettings.apertureFStop,
                    onValueChange = { onPortraitSettingsChange(portraitSettings.copy(apertureFStop = it)) },
                    valueRange = 0.95f..16.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = VivoGold,
                        activeTrackColor = VivoGold,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slider_portrait_aperture")
                )
            }
        }

        // 2. Bokeh Flare Shapes Picker (Vivo Signature Feature)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CameraGlass,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BokehShape.values().forEach { shape ->
                    val isSelected = portraitSettings.bokehShape == shape
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) VivoGold.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onPortraitSettingsChange(portraitSettings.copy(bokehShape = shape)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("bokeh_${shape.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = when (shape) {
                                BokehShape.CIRCLE -> Icons.Default.Face
                                BokehShape.STAR -> Icons.Default.Star
                                BokehShape.HEART -> Icons.Default.Favorite
                                BokehShape.BUTTERFLY -> Icons.Default.AutoAwesome
                                BokehShape.HEXAGON -> Icons.Default.Face
                            },
                            contentDescription = shape.label,
                            tint = if (isSelected) VivoGold else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = shape.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) VivoGold else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
