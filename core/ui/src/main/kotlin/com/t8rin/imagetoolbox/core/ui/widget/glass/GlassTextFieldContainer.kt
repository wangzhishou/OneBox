package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState

// ──────────────────────────────────────────────────────────────
//  毛玻璃输入容器 — 用于包裹全透明 TextField
// ──────────────────────────────────────────────────────────────

/**
 * 毛玻璃输入容器 —— 用于包裹全透明 `TextField` / `BasicTextField` 的玻璃效果 Row 容器。
 *
 * 两模式自动适配：
 *
 * | 全局开关状态                | 渲染方式                                            |
 * |---------------------------|---------------------------------------------------|
 * | `isGlassmorphismEnabled`  | 半透明底色 + 渐变描边（Liquid Glass 增强由内部自动处理）  |
 * | 均关闭                     | 退化为纯色背景                                       |
 *
 * @param modifier              外部修饰符
 * @param isFocused             内部输入框是否处于聚焦状态
 * @param style                 毛玻璃浓度等级
 * @param shape                 容器形状，默认全圆角胶囊
 * @param color                 叠加色调
 * @param borderWidth           描边宽度
 * @param blurRadius            Liquid Glass 模式下的高斯模糊半径
 * @param contentPadding        内容区域内边距
 * @param horizontalArrangement Row 水平排列方式
 * @param verticalAlignment     Row 垂直对齐方式
 * @param content               内容插槽（RowScope）
 */
@Composable
fun GlassTextFieldContainer(
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    style: GlassStyle = GlassStyle.Medium,
    shape: Shape = RoundedCornerShape(50),
    color: Color = Color.Unspecified,
    borderWidth: Dp = 0.5.dp,
    blurRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    val settingsState = LocalSettingsState.current

    // 底色
    val resolvedColor = if (color != Color.Unspecified) color
    else MaterialTheme.colorScheme.surfaceContainerLowest

    // ── 毛玻璃关闭 → 纯色降级 ──
    if (!settingsState.isGlassAlphaEnabled) {
        Row(
            modifier = modifier
                .clip(shape)
                .background(resolvedColor.withGlassBaseAlpha(settingsState.glassBaseAlpha), shape)
                .padding(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content,
        )
        return
    }

    // ── Focus 过渡动画 ──
    val focusProgress by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "GlassTextFieldFocusProgress",
    )
    val animatedAlpha = (style.backgroundAlpha + focusProgress * 0.20f).coerceAtMost(1f)

    // ── 毛玻璃开启 → 统一玻璃样式（内部自动增强 Liquid Glass） ──
    Row(
        modifier = modifier
            .glassSimpleStyle(
                style = style,
                backgroundAlpha = animatedAlpha,
                shape = shape,
                color = resolvedColor,
                borderWidth = borderWidth,
                blurRadius = blurRadius,
            )
            .padding(contentPadding),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content,
    )
}
