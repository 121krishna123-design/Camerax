package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiScene
import com.example.model.GridType
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.CameraGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VivoGold
import kotlin.math.roundToInt

@Composable
fun GridAndFocusOverlay(
    gridType: GridType,
    showLevelMeter: Boolean,
    tiltAngle: Float,
    isLevel: Boolean,
    focusPoint: Pair<Float, Float>?,
    isFocusLocked: Boolean,
    exposureCompensation: Float,
    onExposureChange: (Float) -> Unit,
    aiScene: AiScene,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Composition Grid Lines
        if (gridType != GridType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 1.dp.toPx())
                val lineColor = Color.White.copy(alpha = 0.3f)

                when (gridType) {
                    GridType.RULE_OF_THIRDS -> {
                        // 2 horizontal, 2 vertical
                        val oneThirdW = size.width / 3f
                        val twoThirdsW = size.width * 2f / 3f
                        val oneThirdH = size.height / 3f
                        val twoThirdsH = size.height * 2f / 3f

                        drawLine(lineColor, Offset(oneThirdW, 0f), Offset(oneThirdW, size.height), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(twoThirdsW, 0f), Offset(twoThirdsW, size.height), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(0f, oneThirdH), Offset(size.width, oneThirdH), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(0f, twoThirdsH), Offset(size.width, twoThirdsH), strokeWidth = stroke.width)
                    }

                    GridType.GOLDEN_RATIO -> {
                        val phi = 0.618f
                        val w1 = size.width * (1f - phi)
                        val w2 = size.width * phi
                        val h1 = size.height * (1f - phi)
                        val h2 = size.height * phi

                        drawLine(lineColor, Offset(w1, 0f), Offset(w1, size.height), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(w2, 0f), Offset(w2, size.height), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(0f, h1), Offset(size.width, h1), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(0f, h2), Offset(size.width, h2), strokeWidth = stroke.width)
                    }

                    GridType.CROSSHAIR -> {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val arm = 24.dp.toPx()

                        drawLine(lineColor, Offset(centerX - arm, centerY), Offset(centerX + arm, centerY), strokeWidth = stroke.width)
                        drawLine(lineColor, Offset(centerX, centerY - arm), Offset(centerX, centerY + arm), strokeWidth = stroke.width)
                        drawCircle(lineColor, radius = 6.dp.toPx(), center = Offset(centerX, centerY), style = stroke)
                    }

                    GridType.SPIRAL -> {
                        val path = Path()
                        val w = size.width
                        val h = size.height
                        path.moveTo(0f, h)
                        path.cubicTo(w * 0.8f, h, w, h * 0.8f, w, h * 0.5f)
                        path.cubicTo(w, h * 0.2f, w * 0.7f, 0f, w * 0.5f, 0f)
                        path.cubicTo(w * 0.3f, 0f, w * 0.2f, h * 0.3f, w * 0.3f, h * 0.5f)
                        drawPath(path, lineColor, style = stroke)
                    }

                    else -> {}
                }
            }
        }

        // 2. Electronic Horizon Leveler
        if (showLevelMeter) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                val levelColor = if (isLevel) AccentGreen else Color.White.copy(alpha = 0.5f)
                val transition = rememberInfiniteTransition(label = "level_pulse")
                val pulseAlpha by transition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "pulse_alpha"
                )

                Canvas(
                    modifier = Modifier
                        .size(width = 160.dp, height = 30.dp)
                        .rotate(-tiltAngle)
                ) {
                    val midY = size.height / 2f
                    // Left and right horizon lines with center dot
                    drawLine(
                        color = levelColor.copy(alpha = if (isLevel) pulseAlpha else 0.5f),
                        start = Offset(0f, midY),
                        end = Offset(size.width * 0.4f, midY),
                        strokeWidth = if (isLevel) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                    drawLine(
                        color = levelColor.copy(alpha = if (isLevel) pulseAlpha else 0.5f),
                        start = Offset(size.width * 0.6f, midY),
                        end = Offset(size.width, midY),
                        strokeWidth = if (isLevel) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                    drawCircle(
                        color = levelColor,
                        radius = 3.dp.toPx(),
                        center = Offset(size.width / 2f, midY)
                    )
                }
            }
        }

        // 3. Tap to Focus Animated Golden Reticle with EV Slider
        if (focusPoint != null && isFocusLocked) {
            val density = LocalDensity.current
            val xDp = with(density) { focusPoint.first.toDp() }
            val yDp = with(density) { focusPoint.second.toDp() }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (focusPoint.first - 40.dp.toPx()).roundToInt(),
                            (focusPoint.second - 40.dp.toPx()).roundToInt()
                        )
                    }
                    .size(80.dp)
                    .border(1.5.dp, VivoGold, RoundedCornerShape(4.dp))
                    .testTag("focus_reticle"),
                contentAlignment = Alignment.Center
            ) {
                // Focus center dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(VivoGold, RoundedCornerShape(2.dp))
                )

                // Sun icon on side with drag to adjust exposure
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Exposure slider",
                    tint = VivoGold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 24.dp)
                        .size(20.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                val delta = -dragAmount / 100f
                                onExposureChange(exposureCompensation + delta)
                            }
                        }
                )
            }
        }

        // 4. Vivo Smart AI Scene Badge (Floating pill)
        AnimatedVisibility(
            visible = aiScene != AiScene.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CameraGlass,
                modifier = Modifier.testTag("ai_scene_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Scene",
                        tint = VivoGold,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 6.dp)
                    )
                    Text(
                        text = "AI • ${aiScene.label}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
