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

