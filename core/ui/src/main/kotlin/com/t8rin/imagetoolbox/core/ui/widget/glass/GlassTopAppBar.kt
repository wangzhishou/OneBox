package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/**
 * 毛玻璃风格 TopAppBar —— 参数与 Material3 [TopAppBar] 对齐。
 *
 * - **Glassmorphism 开启** → 容器背景由玻璃接管 ([color] 作为玻璃底色)；
 *   M3 [TopAppBar] 自身的 [TopAppBarColors] 全部置为透明，由文字/图标色 [contentColor] 主导内容
 * - **Glassmorphism 关闭** → 退化为标准 M3 [TopAppBar]
 *
 * @param style        毛玻璃浓度等级
 * @param color        玻璃底色
 * @param contentColor 内容（文字/图标）颜色
 * @param shape        容器形状
 * @param borderWidth  描边宽度
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    style: GlassStyle = GlassStyle.Regular,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(0.dp),
    borderWidth: Dp = 0.dp,
    height: Dp = 64.dp,
) {
    val settingsState = LocalSettingsState.current

    if (!settingsState.isGlassAlphaEnabled) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = windowInsets,
            colors = colors,
            scrollBehavior = scrollBehavior,
        )
        return
    }

    val transparentColors = colors.copy(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = Color.Unspecified,
        titleContentColor = Color.Unspecified,
        actionIconContentColor = Color.Unspecified,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .glassControlStyle(
                style = style,
                backgroundAlpha = (style.backgroundAlpha + 0.02f).coerceAtMost(1f),
                shape = shape,
                color = color,
                borderWidth = borderWidth,
            ),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            TopAppBar(
                title = title,
                modifier = Modifier.fillMaxWidth(),
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets,
                colors = transparentColors,
                scrollBehavior = scrollBehavior,
            )
        }
    }
}
