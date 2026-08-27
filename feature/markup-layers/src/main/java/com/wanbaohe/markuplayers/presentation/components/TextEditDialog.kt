package com.wanbaohe.markuplayers.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.t8rin.imagetoolbox.core.domain.model.Outline
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.ui.theme.ProvideTypography
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.PickFontFamilySheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.logger.makeLog
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import com.t8rin.imagetoolbox.core.resources.R as CoreR

/**
 * 文字编辑 Dialog(替代原全屏文字页):
 * 顶部输入框(打开自动聚焦弹键盘)+ 可滚动样式区(字体/字号/颜色/对齐/行距/
 * 字间距/B I U S/背景/描边)+ 底部取消/确认。
 *
 * 打开期间所有修改经 [MarkupLayersComponent.updateLayerInEditSession] 实时落到画布
 * 图层上预览;确认经 [MarkupLayersComponent.commitLayerEditSession] 压成一次历史快照,
 * 取消经 [MarkupLayersComponent.cancelLayerEditSession] 整体还原(语义同原全屏页)。
 * 点外部/返回键等同取消。
 *
 * 由 EditorScaffold 常驻组合,可见性完全由 [MarkupLayersComponent.editSessionLayerId]
 * 驱动:会话一开始(新建或编辑已有文字图层)Dialog 即弹出。
 */
@Composable
internal fun TextEditDialog(
    component: MarkupLayersComponent,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val layerId = component.editSessionLayerId ?: return
    val layer = component.layers.firstOrNull { it.id == layerId }
    val text = layer?.type as? LayerType.Text
    if (text == null) {
        // 防御:会话图层被外部操作(如 undo)移除,兜底取消会话
        LaunchedEffect(layerId) { onCancel() }
        return
    }

    val updateText: ((LayerType.Text) -> LayerType.Text) -> Unit = { transform ->
        component.updateLayerInEditSession(layerId) { current ->
            (current.type as? LayerType.Text)
                ?.let { current.copy(type = transform(it)) }
                ?: current
        }
    }

    // 字体行点击 → 全局共享字体选择器(能下载/能导入);弹层期间隐藏 Dialog,
    // 关闭后恢复(会话状态在组件里,不丢)
    var showFontSheet by rememberSaveable { mutableStateOf(false) }

    EnhancedAlertDialog(
        visible = !showFontSheet,
        onDismissRequest = onCancel,
        // 键盘弹出时整体上移,输入框不被遮挡
        modifier = Modifier.imePadding(),
        title = {
            Text(
                text = stringResource(
                    if (component.isEditSessionNewLayer) {
                        R.string.markup_add_text
                    } else R.string.markup_edit_text
                )
            )
        },
        text = {
            TextEditContent(
                text = text,
                onTextChange = updateText,
                onPickFont = { showFontSheet = true }
            )
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = onCancel
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = onConfirm
            )
        }
    )

    TextEditFontSheet(
        visible = showFontSheet,
        text = text,
        updateText = updateText,
        onDismiss = { showFontSheet = false }
    )
}

/** Dialog 正文:输入框(自动聚焦)+ 可滚动样式区 */
@Composable
private fun TextEditContent(
    text: LayerType.Text,
    onTextChange: ((LayerType.Text) -> LayerType.Text) -> Unit,
    onPickFont: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Column(modifier = Modifier.fillMaxWidth()) {
        GlassOutlinedTextField(
            value = text.text,
            onValueChange = { value -> onTextChange { it.copy(text = value) } },
            placeholder = { Text(stringResource(R.string.markup_text_input_hint)) },
            shape = ShapeDefaults.default,
            minLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Spacer(Modifier.height(12.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            FontRow(
                selectedFont = text.font,
                onPickFont = onPickFont
            )

            TextSliderRow(
                label = stringResource(R.string.markup_text_font_size),
                value = text.fontSizeRatio,
                valueRange = 0.01f..0.3f,
                valueText = "${(text.fontSizeRatio * 100).roundToInt()}%",
                onValueChange = { value -> onTextChange { it.copy(fontSizeRatio = value) } }
            )

            SettingRow(label = stringResource(R.string.markup_text_color)) {
                ColorSelectionRow(
                    value = Color(text.color),
                    onValueChange = { color ->
                        onTextChange { it.copy(color = color.toArgb()) }
                    },
                    allowAlpha = false,
                    modifier = Modifier.weight(1f)
                )
            }

            AlignmentRow(
                alignment = text.alignment,
                onAlignmentChange = { alignment ->
                    onTextChange { it.copy(alignment = alignment) }
                }
            )

            TextSliderRow(
                label = stringResource(R.string.markup_text_line_height),
                value = text.lineHeight,
                valueRange = 1f..2f,
                valueText = formatDecimal((text.lineHeight * 10).roundToInt() / 10f),
                onValueChange = { value -> onTextChange { it.copy(lineHeight = value) } }
            )
            TextSliderRow(
                label = stringResource(R.string.markup_text_letter_spacing),
                value = text.letterSpacingEm,
                valueRange = -0.05f..0.3f,
                valueText = formatDecimal((text.letterSpacingEm * 100).roundToInt() / 100f),
                onValueChange = { value -> onTextChange { it.copy(letterSpacingEm = value) } }
            )

            DecorationRow(
                decorations = text.decorations,
                onToggle = { decoration ->
                    onTextChange {
                        it.copy(
                            decorations = if (decoration in it.decorations) {
                                it.decorations - decoration
                            } else it.decorations + decoration
                        )
                    }
                }
            )

            BackgroundRow(
                backgroundColor = text.backgroundColor,
                onBackgroundChange = { color ->
                    onTextChange { it.copy(backgroundColor = color) }
                }
            )

            OutlineRow(
                outline = text.outline,
                onOutlineChange = { outline ->
                    onTextChange { it.copy(outline = outline) }
                }
            )
            Spacer(Modifier.height(4.dp))
        }
    }
    // 打开即聚焦输入框弹键盘
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

/** 字体行:当前字体名(该字体渲染),点击打开全局共享字体选择器(能下载/能导入) */
@Composable
private fun FontRow(
    selectedFont: FontType?,
    onPickFont: () -> Unit,
) {
    val selected = selectedFont.toUiFont()
    SettingRow(label = stringResource(R.string.markup_text_font)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(ShapeDefaults.default)
                .clickable(onClick = onPickFont)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            ProvideTypography(selected) {
                Text(
                    text = selected.name ?: stringResource(CoreR.string.system),
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
            Icon(
                imageVector = Icons.Outlined.LineKeyboardArrowDown,
                contentDescription = stringResource(R.string.markup_text_more_fonts)
            )
        }
    }
}

/** 字体选择:共享 PickFontFamilySheet(core/ui),选中/导入/移除/导出经 SettingsManager 落地 */
@Composable
private fun TextEditFontSheet(
    visible: Boolean,
    text: LayerType.Text,
    updateText: ((LayerType.Text) -> LayerType.Text) -> Unit,
    onDismiss: () -> Unit,
) {
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val exportFontsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                runCatching {
                    val cache = settingsManager.createCustomFontsExport() ?: return@runCatching
                    context.contentResolver.openInputStream(cache.toUri())?.use { input ->
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                }.onFailure { it.makeLog("MarkupExportFonts") }
            }
        }
    )
    PickFontFamilySheet(
        visible = visible,
        onDismiss = onDismiss,
        onFontSelected = { font ->
            updateText { it.copy(font = font.type) }
            onDismiss()
        },
        onAddFont = { uri ->
            scope.launch {
                val imported = settingsManager.importCustomFont(uri.toString())
                if (imported != null) {
                    updateText { it.copy(font = FontType.File(imported.filePath)) }
                    AppToastHost.showConfetti()
                } else {
                    AppToastHost.showToast(
                        message = context.getString(CoreR.string.wrong_font),
                        icon = Icons.Outlined.LineText
                    )
                }
            }
        },
        onRemoveFont = { font ->
            scope.launch {
                settingsManager.removeCustomFont(font.asDomain() as DomainFontFamily.Custom)
            }
        },
        onExportFonts = {
            runCatching {
                val timeStamp = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm-ss",
                    Locale.getDefault()
                ).format(Date())
                exportFontsLauncher.launch("FONTS_EXPORT_$timeStamp.zip")
            }.onFailure {
                AppToastHost.showActivateFilesToast()
            }
        }
    )
}

/** 对齐行:左/居中/右/两端 4 分段按钮(图标为手绘对齐线段) */
@Composable
private fun AlignmentRow(
    alignment: LayerType.Text.TextAlignment,
    onAlignmentChange: (LayerType.Text.TextAlignment) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_alignment)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .clip(ShapeDefaults.default)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp)
        ) {
            LayerType.Text.TextAlignment.entries.forEach { item ->
                val isSelected = item == alignment
                val contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.onSurfaceVariant
                val description = stringResource(
                    when (item) {
                        LayerType.Text.TextAlignment.Left -> R.string.markup_text_align_left
                        LayerType.Text.TextAlignment.Center -> R.string.markup_text_align_center
                        LayerType.Text.TextAlignment.Right -> R.string.markup_text_align_right
                        LayerType.Text.TextAlignment.Justify -> R.string.markup_text_align_justify
                    }
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(ShapeDefaults.small)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                        .clickable { onAlignmentChange(item) }
                        .semantics { contentDescription = description }
                        .padding(vertical = 8.dp)
                ) {
                    AlignmentGlyph(
                        alignment = item,
                        tint = contentColor
                    )
                }
            }
        }
    }
}

/** 对齐示意图标:三条横线按对齐方式排布(两端对齐为三条等长) */
@Composable
private fun AlignmentGlyph(
    alignment: LayerType.Text.TextAlignment,
    tint: Color,
) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.dp.toPx()
        val widthFractions = when (alignment) {
            LayerType.Text.TextAlignment.Justify -> listOf(1f, 1f, 1f)
            else -> listOf(1f, 0.7f, 0.85f)
        }
        val yFractions = listOf(0.2f, 0.5f, 0.8f)
        widthFractions.forEachIndexed { index, widthFraction ->
            val lineWidth = size.width * widthFraction
            val startX = when (alignment) {
                LayerType.Text.TextAlignment.Left,
                LayerType.Text.TextAlignment.Justify -> 0f
                LayerType.Text.TextAlignment.Center -> (size.width - lineWidth) / 2f
                LayerType.Text.TextAlignment.Right -> size.width - lineWidth
            }
            val y = size.height * yFractions[index]
            drawLine(
                color = tint,
                start = Offset(startX, y),
                end = Offset(startX + lineWidth, y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/** 样式按钮行:B 加粗 / I 斜体 / U 下划线 / S 删除线(toggle) */
@Composable
private fun DecorationRow(
    decorations: Set<LayerType.Text.Decoration>,
    onToggle: (LayerType.Text.Decoration) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        DecorationChip(
            glyph = "B",
            labelRes = R.string.markup_text_bold,
            selected = LayerType.Text.Decoration.Bold in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Bold) },
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "I",
            labelRes = R.string.markup_text_italic,
            selected = LayerType.Text.Decoration.Italic in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Italic) },
            fontStyle = FontStyle.Italic,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "U",
            labelRes = R.string.markup_text_underline,
            selected = LayerType.Text.Decoration.Underline in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.Underline) },
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.weight(1f)
        )
        DecorationChip(
            glyph = "S",
            labelRes = R.string.markup_text_strikethrough,
            selected = LayerType.Text.Decoration.LineThrough in decorations,
            onClick = { onToggle(LayerType.Text.Decoration.LineThrough) },
            textDecoration = TextDecoration.LineThrough,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DecorationChip(
    glyph: String,
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = ShapeDefaults.default
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                textDecoration = textDecoration
            ),
            color = contentColor,
            maxLines = 1
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1
        )
    }
}

/** 文字背景色:开关 + 颜色行(支持透明);完全透明视为无背景 */
@Composable
private fun BackgroundRow(
    backgroundColor: Int,
    onBackgroundChange: (Int) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_background)) {
        Spacer(Modifier.weight(1f))
        Switch(
            checked = backgroundColor != 0,
            onCheckedChange = { checked ->
                onBackgroundChange(if (checked) 0x66000000.toInt() else 0)
            }
        )
    }
    if (backgroundColor != 0) {
        ColorSelectionRow(
            value = Color(backgroundColor),
            onValueChange = { onBackgroundChange(it.toArgb()) },
            allowAlpha = true
        )
    }
}

/** 描边:开关 + 颜色行 + 粗细滑杆(粗细为相对底图宽的比例) */
@Composable
private fun OutlineRow(
    outline: Outline?,
    onOutlineChange: (Outline?) -> Unit,
) {
    SettingRow(label = stringResource(R.string.markup_text_outline)) {
        Spacer(Modifier.weight(1f))
        Switch(
            checked = outline != null,
            onCheckedChange = { checked ->
                onOutlineChange(
                    if (checked) {
                        Outline(color = 0xFFFFFFFF.toInt(), width = 0.004f)
                    } else null
                )
            }
        )
    }
    if (outline != null) {
        ColorSelectionRow(
            value = Color(outline.color),
            onValueChange = { onOutlineChange(outline.copy(color = it.toArgb())) },
            allowAlpha = false
        )
        TextSliderRow(
            label = stringResource(R.string.markup_shape_stroke_width),
            value = outline.width,
            valueRange = 0.001f..0.02f,
            valueText = "${formatDecimal((outline.width * 1000).roundToInt() / 10f)}%",
            onValueChange = { onOutlineChange(outline.copy(width = it)) }
        )
    }
}

/** 标签在左的设置行 */
@Composable
private fun SettingRow(
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

@Composable
private fun TextSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    SettingRow(label = label) {
        EnhancedSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            // 滑杆不带背景容器,直接排布
            drawContainer = false,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 40.dp)
        )
    }
}

/** 小数展示:去掉尾随的 ".0"(0.0 → "0",1.2 → "1.2") */
private fun formatDecimal(value: Float): String = value.toString().removeSuffix(".0")
