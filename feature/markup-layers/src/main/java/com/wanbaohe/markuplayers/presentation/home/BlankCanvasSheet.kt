package com.wanbaohe.markuplayers.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R

/**
 * 空白画布创建面板:预设尺寸 chips(1:1 / 4:3 / 16:9 / 自定义宽高)
 * + 背景选择(滚动色板 [ColorSelectionRow] 取色 / 透明,默认透明)+ 创建。确认经 [onCreate] 交给
 * [com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent.startWithBlankCanvas],
 * backgroundColor 为 null 表示透明底。
 */
@Composable
fun BlankCanvasSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreate: (width: Int, height: Int, backgroundColor: Int?) -> Unit,
) {
    // selectedPreset == canvasPresets.size 时为自定义
    var selectedPreset by rememberSaveable { mutableStateOf(0) }
    var customWidth by rememberSaveable { mutableStateOf("1080") }
    var customHeight by rememberSaveable { mutableStateOf("1080") }
    // null 表示透明底,初始即透明(不选任何颜色);选了颜色才是纯色
    var backgroundColor by rememberSaveable { mutableStateOf<Int?>(null) }

    val isCustom = selectedPreset == canvasPresets.size
    val resolvedWidth: Int
    val resolvedHeight: Int
    if (isCustom) {
        resolvedWidth = customWidth.toIntOrNull() ?: 0
        resolvedHeight = customHeight.toIntOrNull() ?: 0
    } else {
        val preset = canvasPresets[selectedPreset]
        resolvedWidth = preset.width
        resolvedHeight = preset.height
    }

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.markup_blank_canvas_title),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = stringResource(R.string.markup_blank_canvas_size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    canvasPresets.forEachIndexed { index, preset ->
                        PresetChip(
                            label = preset.ratioLabel,
                            dimensions = stringResource(
                                R.string.markup_export_dimensions,
                                preset.width,
                                preset.height
                            ),
                            selected = selectedPreset == index,
                            onClick = { selectedPreset = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    PresetChip(
                        label = stringResource(R.string.markup_blank_canvas_custom),
                        dimensions = null,
                        selected = isCustom,
                        onClick = { selectedPreset = canvasPresets.size },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isCustom) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SizeField(
                            label = stringResource(R.string.markup_export_width),
                            value = customWidth,
                            onValueChange = { customWidth = it },
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SizeField(
                            label = stringResource(R.string.markup_export_height),
                            value = customHeight,
                            onValueChange = { customHeight = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.markup_blank_canvas_background),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 「透明」恢复入口;透明时滚动色板以透明值回显(自定义位显示棋盘格)
                BackgroundChip(
                    label = stringResource(R.string.markup_blank_canvas_bg_transparent),
                    selected = backgroundColor == null,
                    onClick = { backgroundColor = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(ShapeDefaults.small)
                            .transparencyChecker()
                    )
                }
                ColorSelectionRow(
                    value = backgroundColor?.let(::Color) ?: Color.Transparent,
                    onValueChange = { backgroundColor = it.toArgb() },
                    allowAlpha = false
                )

                // 实心主色确认按钮(M3 Button 不受全局玻璃样式影响)
                Button(
                    onClick = {
                        onCreate(
                            resolvedWidth,
                            resolvedHeight,
                            backgroundColor
                        )
                    },
                    enabled = resolvedWidth > 0 && resolvedHeight > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.markup_blank_canvas_create))
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    )
}

@Composable
private fun PresetChip(
    label: String,
    dimensions: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EnhancedChip(
        selected = selected,
        onClick = onClick,
        selectedColor = MaterialTheme.colorScheme.secondary,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, maxLines = 1)
            if (dimensions != null) {
                Text(
                    text = dimensions,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BackgroundChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    swatch: @Composable () -> Unit,
) {
    EnhancedChip(
        selected = selected,
        onClick = onClick,
        selectedColor = MaterialTheme.colorScheme.secondary,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            swatch()
            Spacer(Modifier.width(6.dp))
            Text(text = label, maxLines = 1)
        }
    }
}

@Composable
private fun SizeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private class CanvasPreset(
    val ratioLabel: String,
    val width: Int,
    val height: Int,
)

private val canvasPresets = listOf(
    CanvasPreset("1:1", 1080, 1080),
    CanvasPreset("4:3", 1440, 1080),
    CanvasPreset("16:9", 1920, 1080)
)
