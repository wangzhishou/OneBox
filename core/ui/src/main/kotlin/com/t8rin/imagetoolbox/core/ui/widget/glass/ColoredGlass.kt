@file:Suppress("unused")

package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/**
 * 普通透明彩色玻璃。
 *
 * 设计目标：轻薄、清透、低干扰，适合卡片、标签、工具栏等大面积 UI。
 * 与全局液态玻璃开关解耦，始终使用普通玻璃质感。
 */
@Composable
fun Modifier.coloredGlass(
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 24.dp,
): Modifier {
    if (!LocalSettingsState.current.isGlassAlphaEnabled || style == GlassStyle.None) {
        return coloredGlassFallback(
            shape = shape,
            color = color,
            style = style,
        )
    }

    return glassSimpleStyle(
        style = style,
        shape = shape,
        color = color,
        borderWidth = borderWidth,
        blurRadius = blurRadius,
        liquidOverride = false,
    )
}

/**
 * 彩色液态玻璃。
 *
 * 设计目标：更明显的镜片边缘、彩色焦散和液态高光，适合浮层、强调卡片、选中态、底部面板。
 * 与全局液态玻璃开关解耦，始终使用液态玻璃质感。
 */
@Composable
fun Modifier.coloredLiquidGlass(
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    style: GlassStyle = GlassStyle.Medium,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 32.dp,
): Modifier {
    if (!LocalSettingsState.current.isGlassAlphaEnabled || style == GlassStyle.None) {
        return coloredGlassFallback(
            shape = shape,
            color = color,
            style = style,
        )
    }

    return glassSimpleStyle(
        style = style,
        shape = shape,
        color = color,
        borderWidth = borderWidth,
        blurRadius = blurRadius,
        liquidOverride = true,
    )
}

/**
 * 普通透明玻璃。默认不注入彩色色调，适合只需要背景透出和轻边缘的容器。
 */
@Composable
fun Modifier.transparentGlass(
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 24.dp,
): Modifier = coloredGlass(
    color = Color.Unspecified,
    style = style,
    shape = shape,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

@Composable
fun ColoredGlassSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 0.9.dp,
    blurRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.coloredGlass(
            color = color,
            style = style,
            shape = shape,
            borderWidth = borderWidth,
            blurRadius = blurRadius,
        ),
        propagateMinConstraints = true,
        content = content,
    )
}

@Composable
fun ColoredLiquidGlassSurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    style: GlassStyle = GlassStyle.Medium,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    blurRadius: Dp = 32.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.coloredLiquidGlass(
            color = color,
            style = style,
            shape = shape,
            borderWidth = borderWidth,
            blurRadius = blurRadius,
        ),
        propagateMinConstraints = true,
        content = content,
    )
}

@Composable
private fun Modifier.coloredGlassFallback(
    shape: Shape,
    color: Color,
    style: GlassStyle,
): Modifier {
    val glassBaseAlpha = LocalSettingsState.current.glassBaseAlpha
    val fallbackColor = when {
        style == GlassStyle.Transparent -> Color.Transparent
        color != Color.Unspecified -> color
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }.withGlassBaseAlpha(glassBaseAlpha)
    return clip(shape)
        .background(fallbackColor, shape)
}


