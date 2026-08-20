package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

/**
 * 毛玻璃风格卡片 —— 非点击版本，参数风格尽量与 Material3 [Card] 对齐。
 *
 * 额外参数：
 * - [containerAlpha]：玻璃底色透明度
 * - [borderWidth]：玻璃描边宽度；传入自定义 [border] 时会自动禁用内部玻璃描边
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = null,
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    containerAlpha: Float = GlassStyle.Regular.backgroundAlpha,
    borderWidth: Dp = 0.5.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val resolvedColors = colors ?: defaultGlassCardColors()

    if (!settingsState.isGlassAlphaEnabled) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = resolvedColors,
            elevation = elevation,
            border = border,
            content = content,
        )
        return
    }

    val glassModifier = modifier.glassSimpleStyle(
        style = GlassStyle.Regular,
        backgroundAlpha = containerAlpha,
        shape = shape,
        borderWidth = if (border == null) borderWidth else 0.dp,
        color = resolvedColors.containerColor,
    )

    Card(
        modifier = glassModifier,
        shape = shape,
        colors = glassTransparentCardColors(resolvedColors),
        elevation = elevation,
        border = border,
        content = content,
    )
}

/**
 * 毛玻璃风格卡片 —— 点击版本，参数风格尽量与 Material3 `Card(onClick = …)` 对齐。
 *
 * 额外参数：
 * - [containerAlpha]：玻璃底色透明度
 * - [borderWidth]：玻璃描边宽度；传入自定义 [border] 时会自动禁用内部玻璃描边
 */
@Composable
fun GlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors? = null,
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
    containerAlpha: Float = GlassStyle.Regular.backgroundAlpha,
    borderWidth: Dp = 0.5.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val resolvedColors = colors ?: defaultGlassCardColors()

    if (!settingsState.isGlassAlphaEnabled) {
        Card(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            colors = resolvedColors,
            elevation = elevation,
            border = border,
            interactionSource = interactionSource,
            content = content,
        )
        return
    }

    val glassModifier = modifier.glassSimpleStyle(
        style = GlassStyle.Regular,
        backgroundAlpha = containerAlpha,
        shape = shape,
        borderWidth = if (border == null) borderWidth else 0.dp,
        color = resolvedColors.containerColor,
    )

    Card(
        onClick = onClick,
        modifier = glassModifier,
        enabled = enabled,
        shape = shape,
        colors = glassTransparentCardColors(resolvedColors),
        elevation = elevation,
        border = border,
        interactionSource = interactionSource,
        content = content,
    )
}

/**
 * 兼容旧签名：允许通过 nullable [onClick] 同时覆盖点击/非点击两种用法。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = CardDefaults.shape,
    containerAlpha: Float = GlassStyle.Regular.backgroundAlpha,
    borderWidth: Dp = 0.5.dp,
    colors: CardColors? = null,
    border: BorderStroke? = null,
    elevation: CardElevation = CardDefaults.cardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        GlassCard(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            containerAlpha = containerAlpha,
            borderWidth = borderWidth,
            content = content,
        )
    } else {
        GlassCard(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            containerAlpha = containerAlpha,
            borderWidth = borderWidth,
            content = content,
        )
    }
}

@Composable
private fun defaultGlassCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
)

@Composable
private fun glassTransparentCardColors(colors: CardColors): CardColors = CardDefaults.cardColors(
    containerColor = Color.Transparent,
    contentColor = colors.contentColor,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = colors.disabledContentColor,
)

/** 玻璃填充(底+染色两层)对 tint 的近似覆盖率,用于估算混合后的有效底色 */
private const val TINTED_GLASS_EFFECTIVE_COVERAGE = 0.65f

/** 有效底色亮于此阈值时选用深色文字 */
private const val TINTED_GLASS_LIGHT_BG_THRESHOLD = 0.4f

/**
 * 彩色玻璃容器上的可读内容色。
 *
 * 玻璃管线会把 tint 以低透明度混合在页面底色上(并叠白色高光),
 * 直接使用与 tint 成对的 onColor(如 onTertiaryContainer)在部分主题下对比度不足。
 * 这里按 tint 混合到 surface 后的有效亮度选择深/浅文字色(取自当前主题,不硬编码),
 * 任何彩色玻璃容器都可用它兜底文字可读性。
 */
@Composable
fun tintedGlassContentColor(tint: Color): Color {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.surface.luminance() > 0.5f
    val effectiveBackground = tint
        .copy(alpha = TINTED_GLASS_EFFECTIVE_COVERAGE)
        .compositeOver(colorScheme.surface)
    val darkText = if (isLight) colorScheme.onSurface else colorScheme.inverseOnSurface
    val lightText = if (isLight) colorScheme.inverseOnSurface else colorScheme.onSurface
    return if (effectiveBackground.luminance() > TINTED_GLASS_LIGHT_BG_THRESHOLD) darkText else lightText
}

/** 彩色玻璃卡片配色:container 用 [tint],内容色按玻璃混合后的有效底色自动计算 */
@Composable
fun tintedGlassCardColors(tint: Color): CardColors = CardDefaults.cardColors(
    containerColor = tint,
    contentColor = tintedGlassContentColor(tint),
)

