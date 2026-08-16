package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuraLightTemp
import com.example.ui.theme.AuraLightCool
import com.example.ui.theme.AuraLightNatural
import com.example.ui.theme.AuraLightWarm
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VivoGold

@Composable
fun AuraLightScreen(
    auraTemp: AuraLightTemp,
    onTempSelect: (AuraLightTemp) -> Unit,
    modifier: Modifier = Modifier
) {
    val glowColor = when (auraTemp) {
        AuraLightTemp.WARM -> AuraLightWarm
        AuraLightTemp.NATURAL -> AuraLightNatural
        AuraLightTemp.COOL -> AuraLightCool
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .border(width = 38.dp, color = glowColor.copy(alpha = 0.95f))
    ) {
        // Temperature selector floating at top
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CameraGlass,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .testTag("aura_temp_selector")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuraLightTemp.values().forEach { temp ->
                    val isSelected = auraTemp == temp
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) VivoGold else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onTempSelect(temp) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${temp.label} (${temp.kelvin})",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
