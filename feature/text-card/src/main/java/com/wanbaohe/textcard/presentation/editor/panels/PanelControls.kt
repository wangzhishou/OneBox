package com.wanbaohe.textcard.presentation.editor.panels

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanbaohe.core.ui.widget.enhanced.EnhancedSlider
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

/**
 * 文字设置面板的共享控件(块级块作用域与选区作用域,即 TextStylePanel 及其
 * SelectionStyleSection 共用,保证两种作用域同一套操作心智):
 * 标签在左的扁平设置行 / 紧凑滑杆行 / 样式切换 chip。
 */

/** 标签在左的设置行(同图片创作 SettingRow) */
@Composable
internal fun PanelSettingRow(
    label: String,
    control: @Composable RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 52.dp)
        )
        Spacer(Modifier.width(8.dp))
        control()
    }
}

/** 紧凑滑杆行:标签 + 无容器滑杆 + 数值(同图片创作 TextSliderRow) */
@Composable
internal fun PanelSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    enabled: Boolean = true,
    onValueChangeFinished: () -> Unit = {},
) {
    PanelSettingRow(label = label) {
        EnhancedSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            // 滑杆不带背景容器,直接排布
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (enabled) 1f else 0.38f
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp)
        )
    }
}

/**
 * 置灰包装:内容降透明度 + 透明遮罩吞掉点击(用于选区激活时禁用行级属性行,
 * GlassSegmentedButtonRow 等无 enabled 参数的控件统一走这里)。
 */
@Composable
internal fun PanelDisabledWrapper(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        Box(Modifier.alpha(if (enabled) 1f else 0.38f)) {
            content()
        }
        if (!enabled) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
            )
        }
    }
}

/** 样式切换 chip:字形 + 文案;选中态玻璃背景区分(玻璃关闭回退 M3 FilterChip),不做描边 */
@Composable
internal fun StyleChip(
    glyph: String,
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
) {
    GlassFilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = ShapeDefaults.default,
        contentPadding = PaddingValues(horizontal = 4.dp),
        // 不用 Outline:主色系背景区分选中
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        glassContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        glassSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = glyph,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = fontWeight,
                        fontStyle = fontStyle,
                        textDecoration = textDecoration
                    ),
                    maxLines = 1
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 7.5.sp,
                        maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
                        stepSize = 0.25.sp,
                    )
                )
            }
        }
    )
}

/** 小数展示:去掉尾随的 ".0"(0.0 → "0",1.2 → "1.2") */
internal fun formatDecimal(value: Float): String = value.toString().removeSuffix(".0")
