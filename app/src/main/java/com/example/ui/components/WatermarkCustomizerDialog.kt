package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.WatermarkSettings
import com.example.model.WatermarkStyle
import com.example.ui.theme.CameraBackground
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatermarkCustomizerDialog(
    currentSettings: WatermarkSettings,
    onSave: (WatermarkSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var enabled by remember { mutableStateOf(currentSettings.enabled) }
    var selectedStyle by remember { mutableStateOf(currentSettings.style) }
    var customAuthor by remember { mutableStateOf(currentSettings.customAuthor) }
    var showDateTime by remember { mutableStateOf(currentSettings.showDateTime) }
    var showOisBadge by remember { mutableStateOf(currentSettings.showOisBadge) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CameraSurfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("dialog_watermark")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "VIVO WATERMARK",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = VivoGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2C3E50), Color(0xFF131A26))
                                )
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        if (enabled) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Vivo round golden badge
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(VivoGold),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("v", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (selectedStyle) {
                                            WatermarkStyle.CLASSIC_VIVO -> "vivo T3 5G | 50MP OIS Camera"
                                            WatermarkStyle.ZEISS_STYLE -> "vivo T3 • 50MP SONY IMX882"
                                            WatermarkStyle.MINIMALIST -> "vivo T3 5G | OIS"
                                            WatermarkStyle.FILM_BORDER -> "VIVO FILM 50MP"
                                            WatermarkStyle.CUSTOM_AUTHOR -> customAuthor.ifBlank { "vivo Photography" }
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                if (showDateTime) {
                                    Text(
                                        text = "${if (customAuthor.isNotBlank() && selectedStyle != WatermarkStyle.CUSTOM_AUTHOR) "$customAuthor  •  " else ""}${SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())}",
                                        fontSize = 9.sp,
                                        color = Color(0xCCFFFFFF),
                                        modifier = Modifier.padding(start = 22.dp, top = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Watermark Disabled",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Add Watermark to Photos", color = TextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = VivoGold
                        )
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Style Selector
                    Text(
                        text = "Watermark Style",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WatermarkStyle.values().forEach { style ->
                            val isSelected = selectedStyle == style
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) VivoGold.copy(alpha = 0.2f) else Color(0xFF14161C),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStyle = style }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) VivoGold else Color.DarkGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = style.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) VivoGold else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Custom Author Text Field
                    OutlinedTextField(
                        value = customAuthor,
                        onValueChange = { customAuthor = it },
                        label = { Text("Custom Author / Signature", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VivoGold,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedLabelColor = VivoGold,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_watermark_author")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Show Date/Time Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Include Date & Time", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = showDateTime,
                            onCheckedChange = { showDateTime = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = VivoGold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282C35)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            onSave(
                                WatermarkSettings(
                                    enabled = enabled,
                                    style = selectedStyle,
                                    customAuthor = customAuthor,
                                    showDateTime = showDateTime,
                                    showOisBadge = showOisBadge
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VivoGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
