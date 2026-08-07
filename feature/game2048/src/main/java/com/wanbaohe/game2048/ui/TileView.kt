package com.wanbaohe.game2048.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle

/**
 * 单个方块视图
 *
 * 根据数值选择不同配色和字号：
 * - 0（空格）：浅色占位背景
 * - 2/4：低对比度文字 + 浅背景
 * - 8~64：中等色调
 * - 128~1024：高亮色调
 * - 2048：渐变主题色 + 发光效果
 * - >2048：特殊强调色
 *
 * 出现时有缩放弹入动画。
 */
@Composable
fun TileView(
    value: Int,
    modifier: Modifier = Modifier,
) {
    // ── 弹入动画 ──
    val scale = remember(value) { Animatable(if (value == 0) 1f else 0.6f) }
    LaunchedEffect(value) {
        if (value != 0) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 150)
            )
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val (bgColor, textColor) = tileColors(value, colorScheme)
    val fontSize = tileFontSize(value)

    GlassSurface(
        modifier = modifier.scale(scale.value),
        style = if (value == 0) GlassStyle.Thin else GlassStyle.Regular,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        borderWidth = 0.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
        ) {
            if (value != 0) {
                Text(
                    text = value.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 根据方块数值返回 (背景色, 文字色) 对
 *
 * 配色遵循 Material 3 语义化色系，与主题颜色自动适配。
 */
@Composable
private fun tileColors(
    value: Int,
    colorScheme: androidx.compose.material3.ColorScheme
): Pair<Color, Color> {
    return when (value) {
        0 -> colorScheme.surfaceContainerHighest.copy(alpha = 0.4f) to Color.Transparent
        2 -> colorScheme.surfaceContainerLowest to colorScheme.onSurfaceVariant
        4 -> colorScheme.surfaceContainerLow to colorScheme.onSurfaceVariant
        8 -> colorScheme.secondaryContainer.copy(alpha = 0.5f) to colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        16 -> colorScheme.secondaryContainer to colorScheme.onSecondaryContainer
        32 -> colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
        64 -> colorScheme.tertiary to colorScheme.onTertiary
        128 -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        256 -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
        512 -> colorScheme.primary.copy(alpha = 0.8f) to colorScheme.onPrimary
        1024 -> colorScheme.primary to colorScheme.onPrimary
        2048 -> colorScheme.primary to colorScheme.onPrimary
        else -> colorScheme.error to colorScheme.onError // > 2048
    }
}

/** 根据数字位数动态调整字号 */
private fun tileFontSize(value: Int) = when {
    value == 0 -> 24.sp
    value < 100 -> 24.sp
    value < 1000 -> 20.sp
    value < 10000 -> 16.sp
    else -> 13.sp
}

