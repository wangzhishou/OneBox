package com.wanbaohe.textcard.presentation.editor.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.settings.di.FontCatalogEntryPoint
import com.t8rin.imagetoolbox.core.settings.domain.model.DomainFontFamily
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.settings.presentation.model.asUi
import com.t8rin.imagetoolbox.core.settings.presentation.model.toUiFont
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.PickFontFamilySheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSlider
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignJustify
import com.t8rin.imagetoolbox.core.resources.R as CoreR

/** 文本块标签:内容首行前 12 个字符(小卡两行可容纳),空内容兜底「文字 N」 */
@Composable
private fun TextBlock.displayLabel(): String {
    val firstLine = content.lineSequence().firstOrNull()?.take(12).orEmpty()
    return firstLine.ifEmpty { stringResource(R.string.textcard_block_fallback) }
}

/**
 * 文字设置面板(设计稿 04):作用于当前选中文本块(任意多块,按 id)。
 * 紧凑布局(对齐图片创作 TextEditDialog):标签在左的扁平设置行,
 * 滑杆/色板/分段均不带 container 包裹,行间距 8dp。
 * 顺序按常用优先:文本块切换 → 字体 → 字号 → 行距 → 字间距 → 不透明度 → 对齐 → B/I → 文字颜色
 */
@Composable
fun TextStylePanel(component: TextCardComponent) {
    val blocks = component.textBlocks
    val block = component.selectedTextBlock() ?: return
    val blockId = block.id

    PanelTitle(R.string.textcard_text_settings)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 文本块切换:固定宽小卡单行横滑,卡内文字最多两行(增删走「基础」面板与图层面板)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = blocks,
                key = { it.id }
            ) { item ->
                val selected = item.id == blockId
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(72.dp)
                        .height(48.dp)
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
                        .clickable { component.selectTextBlock(item.id) }
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = item.displayLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // 字体:当前块字体名(该字体渲染),点击打开全局共享的字体选择器(能下载/能导入)
        FontPickerRow(
            component = component,
            block = block
        )

        PanelSliderRow(
            label = stringResource(R.string.textcard_text_size),
            value = block.sizeScale,
            valueRange = 0.5f..2f,
            valueText = "${(block.sizeScale * 36).roundToInt()}",
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(sizeScale = value) }
            }
        )
        PanelSliderRow(
            label = stringResource(R.string.textcard_line_spacing),
            value = block.lineSpacingMultiplier,
            valueRange = 1f..2f,
            valueText = formatDecimal((block.lineSpacingMultiplier * 10).roundToInt() / 10f),
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(lineSpacingMultiplier = value) }
            }
        )
        PanelSliderRow(
            label = stringResource(R.string.textcard_letter_spacing),
            value = block.letterSpacingEm,
            valueRange = 0f..0.2f,
            valueText = formatDecimal((block.letterSpacingEm * 100).roundToInt() / 100f),
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(letterSpacingEm = value) }
            }
        )
        // 元素级不透明度:每个文字块独立(背景透明度在「纸张背景」面板)
        PanelSliderRow(
            label = stringResource(R.string.textcard_opacity),
            value = block.alpha,
            valueRange = 0f..1f,
            valueText = "${(block.alpha * 100).roundToInt()}%",
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(alpha = value) }
            }
        )

        // 对齐分段(左/居中/右/两端四个图标按钮)
        PanelSettingRow(label = stringResource(R.string.textcard_alignment)) {
            GlassSegmentedButtonRow(
                options = CardTextAlignment.entries,
                selectedOption = block.alignment,
                onOptionSelected = { alignment ->
                    component.updateTextBlock(blockId) { it.copy(alignment = alignment) }
                },
                label = { alignment ->
                    Icon(
                        imageVector = when (alignment) {
                            CardTextAlignment.Left -> MaterialIcons.AutoMirrored.Outlined.FormatAlignLeft
                            CardTextAlignment.Center -> MaterialIcons.Outlined.FormatAlignCenter
                            CardTextAlignment.Right -> MaterialIcons.AutoMirrored.Outlined.FormatAlignRight
                            CardTextAlignment.Justify -> MaterialIcons.Outlined.FormatAlignJustify
                        },
                        contentDescription = null
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        // 字体样式:B 加粗 / I 斜体切换 chip(同图片创作 DecorationRow)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StyleChip(
                glyph = "B",
                labelRes = R.string.textcard_bold,
                selected = block.isBold,
                onClick = {
                    component.updateTextBlock(blockId) { it.copy(isBold = !it.isBold) }
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StyleChip(
                glyph = "I",
                labelRes = R.string.textcard_italic,
                selected = block.isItalic,
                onClick = {
                    component.updateTextBlock(blockId) { it.copy(isItalic = !it.isItalic) }
                },
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
        }

        // 文字颜色:与图片创作一致的横向滚动色板(首项支持自定义取色)
        PanelSettingRow(label = stringResource(R.string.textcard_text_color)) {
            ColorSelectionRow(
                value = Color(block.color),
                onValueChange = { color ->
                    component.updateTextBlock(blockId) {
                        it.copy(color = color.toArgb().toLong() and 0xFFFF_FFFFL)
                    }
                },
                allowAlpha = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 标签在左的设置行(同图片创作 SettingRow) */
@Composable
private fun PanelSettingRow(
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
private fun PanelSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    PanelSettingRow(label = label) {
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

/** 样式切换 chip:字形 + 文案,选中态中性灰底加深色文字(不用主色,避免整行变黄) */
@Composable
private fun StyleChip(
    glyph: String,
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
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
                fontStyle = fontStyle
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

/**
 * 「字体」紧凑行:标签 + 当前块字体名(该字体渲染即预览)+ 下拉箭头;
 * 点击打开全局共享的 [PickFontFamilySheet](默认/可下载/已导入)。
 * 导入/移除/导出走 LocalSettingsManager(与系统字体切换同一数据源 SettingsState.customFonts)。
 */
@Composable
private fun FontPickerRow(
    component: TextCardComponent,
    block: TextBlock,
) {
    var showFontSheet by remember { mutableStateOf(false) }
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 命中可下载清单(File 路径)时显示本地化名称,否则回退字体内部名/系统默认
    val fontCatalog = remember {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                FontCatalogEntryPoint::class.java
            ).fontCatalog
        }.getOrNull()
    }
    val blockFontName = when (val font = block.font) {
        null -> stringResource(CoreR.string.system)
        is FontType.File -> fontCatalog?.fontForFile(font.path)
            ?.let { stringResource(it.nameRes) }
            ?: font.asUi().name
            ?: stringResource(CoreR.string.system)
        is FontType.Resource -> font.asUi().name
            ?: stringResource(CoreR.string.system)
    }

    PanelSettingRow(label = stringResource(CoreR.string.font)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(ShapeDefaults.default)
                .clickable { showFontSheet = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = blockFontName,
                fontFamily = block.font.toUiFont().fontFamily,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.LineKeyboardArrowDown,
                contentDescription = stringResource(CoreR.string.font),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

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
                }.onFailure { it.makeLog("TextCardExportFonts") }
            }
        }
    )

    PickFontFamilySheet(
        visible = showFontSheet,
        onDismiss = { showFontSheet = false },
        onFontSelected = { font ->
            // UiFontFamily.System 的 type 为 null = 默认字体
            component.applyFont(font.type)
            showFontSheet = false
        },
        onAddFont = { uri ->
            scope.launch {
                val imported = settingsManager.importCustomFont(uri.toString())
                if (imported != null) {
                    component.applyFont(FontType.File(imported.filePath))
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

/** 小数展示:去掉尾随的 ".0"(0.0 → "0",1.2 → "1.2") */
private fun formatDecimal(value: Float): String = value.toString().removeSuffix(".0")
