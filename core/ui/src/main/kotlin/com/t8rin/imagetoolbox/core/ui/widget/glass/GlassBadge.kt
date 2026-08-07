package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/**
 * 毛玻璃风格 [Badge] —— 参数与 Material3 [Badge] 对齐。
 *
 * - **Glassmorphism 开启** → 容器背景由玻璃接管 ([containerColor] 作为玻璃底色)
 * - **Glassmorphism 关闭** → 退化为标准 M3 [Badge]
 *
 * @param style         毛玻璃浓度等级
 * @param containerColor 玻璃底色
 * @param contentColor  内容（数字/文字）颜色
 * @param shape         徽标形状，默认胶囊
 * @param borderWidth   描边宽度
 */
@Composable
fun GlassBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = BadgeDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(50),
    borderWidth: Dp = 0.5.dp,
    content: @Composable (RowScope.() -> Unit)? = null,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        Badge(
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            content = content,
        )
        return
    }

    if (content == null) {
        Badge(
            modifier = modifier,
            containerColor = Color.Transparent,
            contentColor = contentColor,
            content = null,
        )
        return
    }

    Row(
        modifier = modifier.glassControlStyle(
            style = style,
            backgroundAlpha = (style.backgroundAlpha + 0.08f).coerceAtMost(1f),
            shape = shape,
            color = containerColor,
            borderWidth = borderWidth,
        ),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * 毛玻璃风格 [BadgedBox] —— 参数与 Material3 [BadgedBox] 对齐。
 *
 * 包装一个内容组件并在右上角挂载 [badge]（默认走 [GlassBadge]）。
 *
 * @param style   毛玻璃浓度等级
 * @param badge   徽标内容；传 `null` 则不渲染徽标
 */
@Composable
fun GlassBadgedBox(
    badge: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    BadgedBox(
        modifier = modifier,
        badge = badge,
        content = content,
    )
}
