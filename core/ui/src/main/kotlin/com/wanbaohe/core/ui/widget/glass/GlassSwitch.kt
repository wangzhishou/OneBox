package com.wanbaohe.core.ui.widget.glass

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.utils.helper.ProvidesValue

/**
 * 毛玻璃风格 Switch —— 两模式自动适配：
 *
 * 1. **Glassmorphism 开启** → Track 透明 + 统一玻璃背景（支持 Liquid Glass 增强）
 * 2. **Glassmorphism 关闭** → 退化为标准 M3 [Switch]
 *
 * 玻璃开启时：
 * - Track / Border 颜色强制置为透明
 * - Thumb / Icon 颜色保持原样（由 [colors] 控制）
 * - 背景颜色根据 [checked] 状态自动选择 [checkedGlassColor] / [uncheckedGlassColor]
 * - 取消最小交互尺寸限制，使玻璃背景与 Switch 实际 track 区域（52×32 dp）对齐
 *
 * 开启时滑块里放对勾。关闭和禁用不画轨道描边，避免空心边框。
 *
 * @param style              毛玻璃浓度等级，默认 [GlassStyle.Regular]
 * @param shape              玻璃背景形状，默认全圆角胶囊
 * @param checkedGlassColor  选中时的玻璃底色
 * @param uncheckedGlassColor 未选中时的玻璃底色
 * @param glassBorderWidth   开启且可用时的玻璃描边宽度；关闭和禁用固定为 0
 */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = AppTheme.colors.switchColors(),
    interactionSource: MutableInteractionSource? = null,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(50),
    checkedGlassColor: Color = MaterialTheme.colorScheme.primaryContainer,
    uncheckedGlassColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    glassBorderWidth: Dp = 0.9.dp,
) {
    val settingsState = LocalSettingsState.current
    val resolvedThumb: @Composable () -> Unit = {
        if (thumbContent != null) {
            thumbContent()
        } else if (checked) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        }
    }
    // 关闭、禁用都是实心轨道。描边只留给开启且可用，避免淡轨道上多一圈框。
    val resolvedBorderWidth = if (enabled && checked) glassBorderWidth else 0.dp

    // 调用方若没覆盖禁用色，Material 默认会给关闭/禁用态画一圈描边。这里统一抹掉。
    val borderlessColors = SwitchColors(
        checkedThumbColor = colors.checkedThumbColor,
        checkedTrackColor = colors.checkedTrackColor,
        checkedBorderColor = Color.Transparent,
        checkedIconColor = colors.checkedIconColor,
        uncheckedThumbColor = colors.uncheckedThumbColor,
        uncheckedTrackColor = colors.uncheckedTrackColor,
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = colors.uncheckedIconColor,
        disabledCheckedThumbColor = colors.disabledCheckedThumbColor,
        disabledCheckedTrackColor = colors.disabledCheckedTrackColor,
        disabledCheckedBorderColor = Color.Transparent,
        disabledCheckedIconColor = colors.disabledCheckedIconColor,
        disabledUncheckedThumbColor = colors.disabledUncheckedThumbColor,
        disabledUncheckedTrackColor = colors.disabledUncheckedTrackColor,
        disabledUncheckedBorderColor = Color.Transparent,
        disabledUncheckedIconColor = colors.disabledUncheckedIconColor,
    )

    // ── 毛玻璃关闭 → 标准 M3 Switch ──
    if (!settingsState.isGlassAlphaEnabled) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            thumbContent = resolvedThumb,
            enabled = enabled,
            colors = borderlessColors,
            interactionSource = interactionSource,
        )
        return
    }

    // ── 毛玻璃开启 → Track/Border 透明，由玻璃背景接管 ──
    val transparentTrackColors = SwitchColors(
        checkedThumbColor = borderlessColors.checkedThumbColor,
        checkedTrackColor = Color.Transparent,
        checkedBorderColor = Color.Transparent,
        checkedIconColor = borderlessColors.checkedIconColor,
        uncheckedThumbColor = borderlessColors.uncheckedThumbColor,
        uncheckedTrackColor = Color.Transparent,
        uncheckedBorderColor = Color.Transparent,
        uncheckedIconColor = borderlessColors.uncheckedIconColor,
        disabledCheckedThumbColor = borderlessColors.disabledCheckedThumbColor,
        disabledCheckedTrackColor = Color.Transparent,
        disabledCheckedBorderColor = Color.Transparent,
        disabledCheckedIconColor = borderlessColors.disabledCheckedIconColor,
        disabledUncheckedThumbColor = borderlessColors.disabledUncheckedThumbColor,
        disabledUncheckedTrackColor = Color.Transparent,
        disabledUncheckedBorderColor = Color.Transparent,
        disabledUncheckedIconColor = borderlessColors.disabledUncheckedIconColor,
    )

    val glassColor = if (checked) checkedGlassColor else uncheckedGlassColor

    LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier.glassControlStyle(
                style = style,
                backgroundAlpha = style.backgroundAlpha,
                shape = shape,
                color = glassColor,
                borderWidth = resolvedBorderWidth,
                enabled = enabled,
            ),
            thumbContent = resolvedThumb,
            enabled = enabled,
            colors = transparentTrackColors,
            interactionSource = interactionSource,
        )
    }
}

