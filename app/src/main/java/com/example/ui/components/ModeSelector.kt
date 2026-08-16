package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VivoGold

val PRIMARY_CAMERA_MODES = listOf(
    CameraMode.NIGHT,
    CameraMode.PORTRAIT,
    CameraMode.PHOTO,
    CameraMode.HIGH_RES_50MP,
    CameraMode.VIDEO,
    CameraMode.PRO
)

@Composable
fun ModeSelector(
    currentMode: CameraMode,
    onModeSelect: (CameraMode) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentMode) {
        val index = PRIMARY_CAMERA_MODES.indexOf(currentMode)
        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("mode_selector_carousel"),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(PRIMARY_CAMERA_MODES) { mode ->
            val isSelected = currentMode == mode
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .clickable { onModeSelect(mode) }
                    .testTag("mode_${mode.name.lowercase()}")
            ) {
                Text(
                    text = mode.title,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 1.2.sp,
                    color = if (isSelected) VivoGold else TextTertiary
                )
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = VivoGold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(4.dp)
                    ) {}
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .clickable { onMoreClick() }
                    .testTag("mode_more")
            ) {
                Text(
                    text = "MORE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp,
                    color = TextTertiary
                )
            }
        }
    }
}
