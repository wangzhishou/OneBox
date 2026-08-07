@file:Suppress("unused", "Unused")

package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
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

// ──────────────────────────────────────────────────────────────
//  Liquid Glass — 兼容层
//
//  重构后所有玻璃渲染已统一收敛到 GlassModifier.kt 的 glassSimpleStyle()，
//  当 isLiquidGlassEnabled == true 时自动增强 Liquid Glass 效果。
//
//  本文件保留公开 API 以维持源兼容，内部全部委托到统一实现。
// ──────────────────────────────────────────────────────────────

// ──────────────────────────────────────────────────────────────
//  核心 Modifier（委托到统一实现）
// ──────────────────────────────────────────────────────────────

/**
 * Liquid Glass Modifier —— 受 iOS Liquid Glass 和 AndroidLiquidGlass 库启发。
 *
 * 重构后委托到 [glassBackground]，由 [glassSimpleStyle] 统一渲染。
 * 当 `isLiquidGlassEnabled == true` 时自动应用高斯模糊 + 径向光斑 + 内阴影等增强效果。
 *
 * @param style       效果浓度等级，见 [GlassStyle]
 * @param shape       形状，默认 16 dp 圆角
 * @param blurRadius  高斯模糊半径（仅 API 31+ 生效），默认 24 dp
 * @param color        叠加色调（传入不透明原色，alpha 由 style 控制）；
 *                    传入 [Color.Unspecified] 时不叠加
 * @param borderWidth 高光边框宽度，默认 0.75 dp
 */
@Composable
@Suppress("unused", "Unused")
fun Modifier.liquidGlass(
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 24.dp,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.75.dp,
): Modifier = glassBackground(
    style = style,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

// ──────────────────────────────────────────────────────────────
//  常用快捷方法
// ──────────────────────────────────────────────────────────────

/**
 * 轻薄 Liquid Glass — 图标背景圈、小徽标。
 */
@Composable
@Suppress("unused", "Unused")
fun Modifier.liquidGlassThin(
    shape: Shape = CircleShape,
    color: Color = Color.Unspecified,
    blurRadius: Dp = 16.dp,
    borderWidth: Dp = 0.dp,
): Modifier = glassBackground(
    style = GlassStyle.Thin,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

/**
 * 常规 Liquid Glass — 卡片、标签、选中态胶囊。
 */
@Composable
@Suppress("unused", "Unused")
fun Modifier.liquidGlassRegular(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    blurRadius: Dp = 24.dp,
    borderWidth: Dp = 0.75.dp,
): Modifier = glassBackground(
    style = GlassStyle.Regular,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

/**
 * 中等 Liquid Glass — 导航栏、工具栏、浮动面板。
 */
@Composable
@Suppress("unused", "Unused")
fun Modifier.liquidGlassMedium(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    blurRadius: Dp = 28.dp,
    borderWidth: Dp = 0.75.dp,
): Modifier = glassBackground(
    style = GlassStyle.Medium,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

/**
 * 浓厚 Liquid Glass — 弹窗蒙层、底部弹出面板。
 */
@Composable
@Suppress("unused", "Unused")
fun Modifier.liquidGlassThick(
    shape: Shape = RoundedCornerShape(16.dp),
    color: Color = Color.Unspecified,
    blurRadius: Dp = 32.dp,
    borderWidth: Dp = 0.75.dp,
): Modifier = glassBackground(
    style = GlassStyle.Thick,
    shape = shape,
    color = color,
    borderWidth = borderWidth,
    blurRadius = blurRadius,
)

// ──────────────────────────────────────────────────────────────
//  Composable 容器
// ──────────────────────────────────────────────────────────────

/**
 * Liquid Glass 表面容器 —— 轻量级 Box 版本。
 *
 * 重构后委托到 [glassBackground]，由 [glassSimpleStyle] 统一渲染。
 *
 * @param modifier    外部修饰符
 * @param style       效果浓度，见 [GlassStyle]
 * @param shape       容器形状
 * @param blurRadius  高斯模糊半径（API 31+）
 * @param color        叠加色调
 * @param borderWidth 高光边框宽度
 * @param content     内容插槽
 */
@Composable
@Suppress("unused", "Unused")
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    style: GlassStyle = GlassStyle.Regular,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 24.dp,
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.75.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current
    val isEnabled = settingsState.isGlassAlphaEnabled

    if (!isEnabled) {
        val fallback = if (color != Color.Unspecified) color
        else MaterialTheme.colorScheme.surfaceContainerLow
        Box(
            modifier = modifier.clip(shape).background(
                color = fallback.withGlassBaseAlpha(settingsState.glassBaseAlpha),
                shape = shape,
            ),
            propagateMinConstraints = true,
            content = content,
        )
        return
    }

    Box(
        modifier = modifier.glassBackground(
            style = style,
            shape = shape,
            color = color,
            borderWidth = borderWidth,
            blurRadius = blurRadius,
        ),
        propagateMinConstraints = true,
        content = content,
    )
}
