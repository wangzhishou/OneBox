package com.wanbaohe.markuplayers.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.shifenmiao.model.canvas.CreationCanvasSpec
import com.wanbaohe.base.ui.button.ConfirmButton
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassOutlinedTextField
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.components.SheetHeader

/** Dimensions are validated before allocating a bitmap; null background means transparency. */
@Composable
fun BlankCanvasSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onCreate: (width: Int, height: Int, backgroundColor: Int?) -> Unit,
    isCreating: Boolean = false,
    creationError: String? = null,
    maxCanvasSide: Int = 8192,
    maxCanvasPixels: Long = 24_000_000L,
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
        val preset = canvasPresets[selectedPreset.coerceIn(canvasPresets.indices)]
        resolvedWidth = preset.width
        resolvedHeight = preset.height
    }
    val validSides = resolvedWidth in 1..maxCanvasSide && resolvedHeight in 1..maxCanvasSide
    val validPixels = validSides && resolvedWidth.toLong() * resolvedHeight <= maxCanvasPixels

    EnhancedModalBottomSheet(
        visible = visible,
        dragHandle = {},
        onDismiss = { onDismiss() },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.markup_blank_canvas_title),
                onClose = onDismiss
            )
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
                    text = stringResource(R.string.markup_blank_canvas_size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                (0..canvasPresets.size).toList().chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { index ->
                            val preset = canvasPresets.getOrNull(index)
                            PresetChip(
                                label = preset?.ratioLabel
                                    ?: stringResource(R.string.markup_blank_canvas_custom),
                                dimensions = preset?.let {
                                    stringResource(R.string.markup_export_dimensions, it.width, it.height)
                                },
                                selected = selectedPreset == index,
                                onClick = { selectedPreset = index },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
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
                            text = stringResource(R.string.studio_dimension_separator),
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
                    when {
                        !validSides -> Text(
                            text = stringResource(R.string.studio_blank_invalid_size, maxCanvasSide),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        !validPixels -> Text(
                            text = stringResource(
                                R.string.studio_blank_pixel_limit,
                                maxCanvasPixels
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    creationError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
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

                // 创建为主操作,走项目封装的实心确认按钮
                ConfirmButton(
                    text = stringResource(
                        if (isCreating) R.string.studio_blank_creating
                        else R.string.markup_blank_canvas_create
                    ),
                    enabled = validPixels && !isCreating,
                    onClick = {
                        onCreate(
                            resolvedWidth,
                            resolvedHeight,
                            backgroundColor
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = modifier.heightIn(min = 48.dp)
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
        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
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
    GlassOutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(5)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

private val canvasPresets = listOf(
    CreationCanvasSpec.SQUARE,
    CreationCanvasSpec.LANDSCAPE,
    CreationCanvasSpec.WIDE,
    CreationCanvasSpec.PORTRAIT,
    CreationCanvasSpec.STORY,
)
