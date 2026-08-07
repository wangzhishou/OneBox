package com.t8rin.imagetoolbox.core.ui.widget.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus

/**
 * 毛玻璃风格数字加减器 (Stepper)。
 *
 * 由 "-" 按钮 + 数值显示 + "+" 按钮组合而成，沿用 [GlassTonalIconButton] 实现两端按钮，
 * 中间数值文本走 [MaterialTheme.colorScheme.onSurface]。
 *
 * - **Glassmorphism 关闭** 时退化为 [androidx.compose.material3.IconButton] + Text。
 *
 * @param value          当前数值
 * @param onValueChange  数值变更回调（已 clamp 到 [valueRange]，步进 [step]）
 * @param valueRange     数值范围，默认 0..100
 * @param step           单次步进，默认 1
 * @param modifier       整体修饰符
 * @param buttonSize     两端按钮尺寸
 * @param buttonStyle    两端按钮玻璃浓度
 * @param buttonShape    两端按钮形状，默认圆形
 * @param minusIcon      "-" 按钮图标
 * @param plusIcon       "+" 按钮图标
 * @param enabled        是否可交互
 * @param label          数值显示文本样式
 */
@Composable
fun GlassStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 0..100,
    step: Int = 1,
    buttonSize: Dp = 36.dp,
    buttonStyle: GlassStyle = GlassStyle.Regular,
    buttonShape: Shape = CircleShape,
    minusIcon: @Composable () -> Unit = {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus,
            contentDescription = "decrease",
        )
    },
    plusIcon: @Composable () -> Unit = {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
            contentDescription = "increase",
        )
    },
    enabled: Boolean = true,
    label: TextStyle = MaterialTheme.typography.titleMedium,
) {
    val safeStep = step.coerceAtLeast(1)
    val canDecrement = enabled && value - safeStep >= valueRange.first
    val canIncrement = enabled && value + safeStep <= valueRange.last

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GlassTonalIconButton(
            onClick = { if (canDecrement) onValueChange((value - safeStep).coerceIn(valueRange)) },
            modifier = Modifier.size(buttonSize),
            enabled = canDecrement,
            shape = buttonShape,
            style = buttonStyle,
        ) {
            minusIcon()
        }
        Box(
            modifier = Modifier.width(buttonSize * 1.4f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value.toString(),
                style = label,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
        GlassTonalIconButton(
            onClick = { if (canIncrement) onValueChange((value + safeStep).coerceIn(valueRange)) },
            modifier = Modifier.size(buttonSize),
            enabled = canIncrement,
            shape = buttonShape,
            style = buttonStyle,
        ) {
            plusIcon()
        }
    }
}
