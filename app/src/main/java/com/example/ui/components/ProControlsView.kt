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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.model.MeteringMode
import com.example.model.ProSettings
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold
import java.util.Locale

enum class ProParam {
    EV,
    ISO,
    SHUTTER,
    WB,
    FOCUS,
    METERING,
    RAW
}

@Composable
fun ProControlsView(
    proSettings: ProSettings,
    onProSettingsChange: (ProSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeParam by remember { mutableStateOf(ProParam.EV) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active Parameter Control Slider / Wheel
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = CameraSurfaceElevated.copy(alpha = 0.92f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                when (activeParam) {
                    ProParam.EV -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "EV ${if (proSettings.ev > 0) "+" else ""}${String.format(Locale.US, "%.1f", proSettings.ev)}",
                                color = VivoGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.width(68.dp)
                            )
                            Slider(
                                value = proSettings.ev,
                                onValueChange = { onProSettingsChange(proSettings.copy(ev = (it * 10).toInt() / 10f)) },
                                valueRange = -3.0f..3.0f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = VivoGold,
                                    activeTrackColor = VivoGold,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("slider_pro_ev")
                            )
                        }
                    }

                    ProParam.ISO -> {
                        val isoValues = listOf(0, 50, 100, 200, 400, 800, 1600, 3200, 6400)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(isoValues) { iso ->
                                val isSelected = proSettings.iso == iso
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) VivoGold else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { onProSettingsChange(proSettings.copy(iso = iso)) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (iso == 0) "AUTO" else "$iso",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    ProParam.SHUTTER -> {
                        val shutterSpeeds = listOf("AUTO", "1/8000", "1/4000", "1/2000", "1/1000", "1/500", "1/250", "1/125", "1/60", "1/30", "1/15", "1/4", "1s", "2s", "8s", "30s")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(shutterSpeeds) { speed ->
                                val isSelected = proSettings.shutterSpeed == speed
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) VivoGold else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { onProSettingsChange(proSettings.copy(shutterSpeed = speed)) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = speed,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    ProParam.WB -> {
                        val wbValues = listOf(
                            0 to "AUTO",
                            2800 to "2800K Incandescent",
                            4000 to "4000K Fluorescent",
                            5500 to "5500K Daylight",
                            6500 to "6500K Cloudy",
                            7500 to "7500K Shade"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(wbValues) { (kelvin, label) ->
                                val isSelected = proSettings.wbKelvin == kelvin
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) VivoGold else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { onProSettingsChange(proSettings.copy(wbKelvin = kelvin)) }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    ProParam.FOCUS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!proSettings.isManualFocus) VivoGold else Color.Transparent,
                                modifier = Modifier
                                    .clickable { onProSettingsChange(proSettings.copy(isManualFocus = false, focusDistance = 0f)) }
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "AF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!proSettings.isManualFocus) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "MF ${if (proSettings.isManualFocus) String.format(Locale.US, "%.2f", proSettings.focusDistance) else ""}",
                                color = if (proSettings.isManualFocus) VivoGold else TextSecondary,
                                fontSize = 12.sp,
                                modifier = Modifier.width(60.dp)
                            )
                            Slider(
                                value = proSettings.focusDistance,
                                onValueChange = { onProSettingsChange(proSettings.copy(isManualFocus = true, focusDistance = it)) },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = VivoGold,
                                    activeTrackColor = VivoGold,
                                    inactiveTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    ProParam.METERING -> {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MeteringMode.values().forEach { mode ->
                                val isSelected = proSettings.meteringMode == mode
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) VivoGold else Color.Transparent,
                                    modifier = Modifier.clickable { onProSettingsChange(proSettings.copy(meteringMode = mode)) }
                                ) {
                                    Text(
                                        text = mode.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    ProParam.RAW -> {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!proSettings.isRaw) VivoGold else Color.Transparent,
                                modifier = Modifier
                                    .clickable { onProSettingsChange(proSettings.copy(isRaw = false)) }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "JPEG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!proSettings.isRaw) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (proSettings.isRaw) VivoGold else Color.Transparent,
                                modifier = Modifier
                                    .clickable { onProSettingsChange(proSettings.copy(isRaw = true)) }
                                    .padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "RAW + JPEG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (proSettings.isRaw) Color.Black else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pro Quick Tab Selector Buttons Row
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CameraGlass,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val params = listOf(
                    ProParam.EV to "EV",
                    ProParam.ISO to if (proSettings.iso == 0) "ISO" else "${proSettings.iso}",
                    ProParam.SHUTTER to if (proSettings.shutterSpeed == "AUTO") "S" else proSettings.shutterSpeed,
                    ProParam.WB to if (proSettings.wbKelvin == 0) "WB" else "${proSettings.wbKelvin}K",
                    ProParam.FOCUS to if (!proSettings.isManualFocus) "AF" else "MF",
                    ProParam.METERING to "METER",
                    ProParam.RAW to if (proSettings.isRaw) "RAW" else "JPG"
                )

                params.forEach { (param, label) ->
                    val isSelected = activeParam == param
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeParam = param }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) VivoGold else TextPrimary
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(width = 16.dp, height = 2.dp)
                                    .background(VivoGold, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
