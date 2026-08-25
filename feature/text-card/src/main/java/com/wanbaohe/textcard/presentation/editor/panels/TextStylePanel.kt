package com.wanbaohe.textcard.presentation.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorPickerSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButtonGroup
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSwitch
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignJustify
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic

/** 文本块标签:内容首行前 6 个字符,空内容兜底「文字 N」 */
@Composable
private fun TextBlock.displayLabel(): String {
    val firstLine = content.lineSequence().firstOrNull()?.take(6).orEmpty()
    return firstLine.ifEmpty { stringResource(R.string.textcard_block_fallback) }
}

/**
 * 文字设置面板(设计稿 04):作用于当前选中文本块(任意多块,按 id)的
 * 字号/字间距/行间距滑杆 + 对齐分段 + 粗/斜开关 + 文字颜色;另含背景透明度。
 * 顶部「添加文字块」入口新增正文样式块。
 */
@Composable
fun TextStylePanel(component: TextCardComponent) {
    val blocks = component.textBlocks
    val block = component.selectedTextBlock() ?: return
    val blockId = block.id
    var showTextColorPicker by remember { mutableStateOf(false) }

    PanelTitle(R.string.textcard_text_settings)

    // 文本块切换(标题/正文/新增块,按首行内容截断做标签);增删统一走「基础」面板与图层面板
    val labels = blocks.map { it.displayLabel() }
    EnhancedButtonGroup(
        items = labels,
        selectedIndex = blocks.indexOfFirst { it.id == blockId }.coerceAtLeast(0),
        onIndexChange = { index ->
            blocks.getOrNull(index)?.let { component.selectTextBlock(it.id) }
        },
        title = stringResource(R.string.textcard_text_target),
        modifier = Modifier.padding(top = 8.dp)
    )

    EnhancedSliderItem(
        value = block.sizeScale,
        title = stringResource(R.string.textcard_text_size),
        valueRange = 0.5f..2f,
        onValueChange = { value ->
            component.updateTextBlock(blockId) { it.copy(sizeScale = value) }
        },
        internalStateTransformation = { (it * 36).roundToInt() },
        modifier = Modifier.padding(top = 4.dp)
    )
    EnhancedSliderItem(
        value = block.letterSpacingEm,
        title = stringResource(R.string.textcard_letter_spacing),
        valueRange = 0f..0.2f,
        onValueChange = { value ->
            component.updateTextBlock(blockId) { it.copy(letterSpacingEm = value) }
        },
        internalStateTransformation = { (it * 50).roundToInt() }
    )
    EnhancedSliderItem(
        value = block.lineSpacingMultiplier,
        title = stringResource(R.string.textcard_line_spacing),
        valueRange = 1f..2f,
        onValueChange = { value ->
            component.updateTextBlock(blockId) { it.copy(lineSpacingMultiplier = value) }
        },
        internalStateTransformation = { ((it - 1f) * 40).roundToInt() }
    )
    EnhancedSliderItem(
        value = component.backgroundOpacity,
        title = stringResource(R.string.textcard_background_opacity),
        valueRange = 0f..1f,
        onValueChange = component::updateBackgroundOpacity,
        internalStateTransformation = { (it * 100).roundToInt() },
        valueSuffix = "%"
    )

    // 对齐分段(设计稿 04 的四个图标按钮)
    val alignments = CardTextAlignment.entries
    Text(
        text = stringResource(R.string.textcard_alignment),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp)
    )
    EnhancedButtonGroup(
        itemCount = alignments.size,
        selectedIndex = alignments.indexOf(block.alignment),
        onIndexChange = { index ->
            component.updateTextBlock(blockId) {
                it.copy(alignment = alignments[index])
            }
        },
        itemContent = { index ->
            Icon(
                imageVector = when (alignments[index]) {
                    CardTextAlignment.Left -> MaterialIcons.AutoMirrored.Outlined.FormatAlignLeft
                    CardTextAlignment.Center -> MaterialIcons.Outlined.FormatAlignCenter
                    CardTextAlignment.Right -> MaterialIcons.AutoMirrored.Outlined.FormatAlignRight
                    CardTextAlignment.Justify -> MaterialIcons.Outlined.FormatAlignJustify
                },
                contentDescription = null
            )
        }
    )

    // 字体样式:加粗 / 斜体开关 + 文字颜色
    Text(
        text = stringResource(R.string.textcard_font_style),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = MaterialIcons.Outlined.FormatBold,
            contentDescription = null,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = stringResource(R.string.textcard_bold),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        EnhancedSwitch(
            checked = block.isBold,
            onCheckedChange = { checked ->
                component.updateTextBlock(blockId) { it.copy(isBold = checked) }
            }
        )
        Icon(
            imageVector = MaterialIcons.Outlined.FormatItalic,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp, end = 4.dp)
        )
        Text(
            text = stringResource(R.string.textcard_italic),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        EnhancedSwitch(
            checked = block.isItalic,
            onCheckedChange = { checked ->
                component.updateTextBlock(blockId) { it.copy(isItalic = checked) }
            }
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .container(shape = ShapeDefaults.large)
            .clickable(onClick = { showTextColorPicker = true })
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = stringResource(R.string.textcard_text_color),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(block.color))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(6.dp)
                )
        )
    }

    ColorPickerSheet(
        visible = showTextColorPicker,
        onDismiss = { showTextColorPicker = false },
        color = Color(block.color),
        onColorSelected = { color ->
            component.updateTextBlock(blockId) {
                it.copy(color = color.toArgb().toLong() and 0xFFFF_FFFFL)
            }
            showTextColorPicker = false
        },
        allowAlpha = false
    )
}
