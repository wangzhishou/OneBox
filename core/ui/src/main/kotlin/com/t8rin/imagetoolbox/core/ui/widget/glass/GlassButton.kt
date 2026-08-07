package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格按钮(Glass Button) —— 两模式自动适配：
 *
 * 1. **Glassmorphism 开启** → 半透明底色 + 渐变描边（Liquid Glass 增强由内部自动处理）
 * 2. **均关闭** → 退化为标准 M3 [Button]
 *
 * @param onClick        点击回调
 * @param modifier       可选修饰符
 * @param enabled        是否可用
 * @param shape          按钮形状，默认 24 dp 圆角（胶囊感）
 * @param color           玻璃色调
 * @param contentColor   内容（文字/图标）颜色
 * @param containerAlpha Glassmorphism 模式下容器透明度，默认 0.35f
 * @param borderWidth    描边宽度，默认 0.75 dp
 * @param colors         完全自定义 [ButtonColors]；传入后 [color]/[contentColor]/[containerAlpha] 将被忽略
 * @param border         完全自定义 [BorderStroke]
 * @param elevation      按钮阴影
 * @param contentPadding 内容内边距
 * @param interactionSource 交互源
 * @param content        按钮内容（RowScope）
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerAlpha: Float = 0.34f,
    borderWidth: Dp = 0.dp,
    colors: ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    // ── 毛玻璃关闭 → 标准 M3 Button ──
    if (!settingsState.isGlassAlphaEnabled) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = colors ?: ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = contentColor,
            ),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    // ── 毛玻璃开启 → 统一玻璃样式（内部自动增强 Liquid Glass） ──
    val resolvedContainerColor = colors.containerColor(enabled = enabled).takeIfSpecifiedOrElse(color)
    val resolvedContentColor = colors.contentColor(enabled = enabled).takeIfSpecifiedOrElse(contentColor)
    val disabledContentColor = colors.contentColor(enabled = false).takeIfSpecifiedOrElse(
        contentColor.copy(alpha = 0.38f),
    )

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        Button(
            onClick = onClick,
            modifier = modifier.glassControlStyle(
                style = GlassStyle.Regular,
                backgroundAlpha = containerAlpha,
                shape = shape,
                borderWidth = borderWidth,
                color = resolvedContainerColor,
                enabled = enabled,
            ),
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = resolvedContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = disabledContentColor,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            border = null,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Stable
private fun ButtonColors?.containerColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> containerColor
    else -> disabledContainerColor
}

@Stable
private fun ButtonColors?.contentColor(enabled: Boolean): Color = when {
    this == null -> Color.Unspecified
    enabled -> contentColor
    else -> disabledContentColor
}

private fun Color.takeIfSpecifiedOrElse(fallback: Color): Color {
    return if (this != Color.Unspecified && this != Color.Transparent) this else fallback
}
