package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterType
import com.example.ui.theme.CameraSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VivoGold

@Composable
fun FilterPickerBar(
    currentFilter: FilterType,
    onFilterSelect: (FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = CameraSurfaceElevated.copy(alpha = 0.95f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = "VIVO LUT FILM PROFILES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = VivoGold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(FilterType.values()) { filter ->
                    val isSelected = currentFilter == filter
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onFilterSelect(filter) }
                            .padding(4.dp)
                            .testTag("filter_${filter.name.lowercase()}")
                    ) {
                        // Filter Color Swatch Circle
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(getFilterGradient(filter))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) VivoGold else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = filter.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) VivoGold else TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getFilterGradient(filter: FilterType): Brush {
    return when (filter) {
        FilterType.ORIGINAL -> Brush.linearGradient(listOf(Color(0xFF4A5568), Color(0xFF2D3748)))
        FilterType.VIVO_VIVID -> Brush.linearGradient(listOf(Color(0xFFFF5252), Color(0xFFFFB74D)))
        FilterType.VIVO_TEXTURED -> Brush.linearGradient(listOf(Color(0xFF37474F), Color(0xFF78909C)))
        FilterType.CYBERPUNK -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFD500F9)))
        FilterType.BLACK_GOLD -> Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF212121)))
        FilterType.VINTAGE_FILM -> Brush.linearGradient(listOf(Color(0xFFBCAAA4), Color(0xFF8D6E63)))
        FilterType.FRENCH_RETRO -> Brush.linearGradient(listOf(Color(0xFFFFCCBC), Color(0xFFD7CCC8)))
        FilterType.CINE_TEAL_ORANGE -> Brush.linearGradient(listOf(Color(0xFF00B4D8), Color(0xFFF77F00)))
        FilterType.NOIR_BW -> Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFF000000)))
    }
}
