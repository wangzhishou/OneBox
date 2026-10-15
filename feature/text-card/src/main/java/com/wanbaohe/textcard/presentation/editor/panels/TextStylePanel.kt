@file:OptIn(ExperimentalFoundationApi::class)

package com.wanbaohe.textcard.presentation.editor.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.logger.makeLog
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.effectiveColorAt
import com.wanbaohe.textcard.domain.model.effectiveSizeScaleAt
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyBold
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyItalic
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
 * 文字设置面板(设计稿 04):一套控件,作用目标随选中状态切换——
 * 默认作用于当前选中文字块(任意多块,按 id);就地编辑中长按选中块内
 * 局部文字时,字号/加粗/斜体/颜色改作用于选区(局部样式,叠加在块级之上),
 * 行间距/字间距/对齐为行级属性,置灰不可调,面板顶部提示「正在调整:选中的文字」
 * 并提供「清除样式」(移除选区全部局部样式)。取消选中即回到整块模式。
 * 紧凑布局(对齐图片创作 TextEditDialog):标签在左的扁平设置行,
 * 滑杆/色板/分段均不带 container 包裹,行间距 12dp。
 * 顺序按常用优先:文本块切换 → 字体 → 字号 → 文字颜色(高频) → 行距 → 字间距
 * → 不透明度 → 对齐 → B/I/U/S → 文字背景 → 描边(U/S、背景、描边为块级属性,选区激活时置灰)
 */
@Composable
fun TextStylePanel(
    component: TextCardComponent,
    editingState: TextFieldState? = null,
    editingBlock: TextBlock? = null,
    onEditingSync: () -> Unit = {},
) {
    // 有活选区(就地编辑中选中了块内局部文字)时,面板控件切换到选区作用域;
    // 选区写在 TextFieldState 里,snapshot 感知,选中变化即重组
    val selectionCtx = editingState?.let { state ->
        editingBlock?.let { blk ->
            state.selection.takeUnless { it.collapsed }?.let { Triple(state, blk, it) }
        }
    }

    val blocks = component.textBlocks
    val block = component.selectedTextBlock() ?: return
    val blockId = block.id

    PanelTitle(R.string.textcard_text_settings)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 选区激活提示:告诉用户现在调的是选中的局部文字;「清除样式」移除选区全部局部样式
        if (selectionCtx != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.textcard_editing_selection),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.textcard_clear_selection_style),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .clip(ShapeDefaults.default)
                        .clickable { clearSelectionStyles(selectionCtx.first, onEditingSync) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

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
                        // 选中态用玻璃背景区分(玻璃关闭时回退纯色),不做描边
                        .glassDense(
                            shape = ShapeDefaults.default,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else MaterialTheme.colorScheme.surfaceContainer
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

        // 字号:选区激活时为相对块级的倍率(em 写入样式层,100% = 块级字号),否则整块字号
        PanelSliderRow(
            label = stringResource(R.string.textcard_text_size),
            value = selectionCtx?.let { (_, blk, sel) ->
                blk.effectiveSizeScaleAt(sel.min, sel.max)
            } ?: block.sizeScale,
            valueRange = if (selectionCtx != null) {
                MIN_SELECTION_SIZE_SCALE..MAX_SELECTION_SIZE_SCALE
            } else 0.5f..4f,
            valueText = selectionCtx?.let { (_, blk, sel) ->
                "${(blk.effectiveSizeScaleAt(sel.min, sel.max) * 100).roundToInt()}%"
            } ?: "${(block.sizeScale * 36).roundToInt()}",
            onValueChange = { value ->
                val ctx = selectionCtx
                if (ctx != null) {
                    applySpanStyle(
                        state = ctx.first,
                        style = SpanStyle(fontSize = value.em),
                        onChanged = onEditingSync
                    )
                } else {
                    component.updateTextBlock(blockId) { it.copy(sizeScale = value) }
                }
            }
        )
        // 文字颜色(高频操作,紧跟字号):与图片创作一致的横向滚动色板(首项支持自定义取色);
        // 选区激活时给选中文字上色(恢复跟随整块用顶部「清除样式」)
        PanelSettingRow(label = stringResource(R.string.textcard_text_color)) {
            ColorSelectionRow(
                value = selectionCtx?.let { (_, blk, sel) ->
                    Color(blk.effectiveColorAt(sel.min, sel.max))
                } ?: Color(block.color),
                onValueChange = { color ->
                    val ctx = selectionCtx
                    if (ctx != null) {
                        applySpanStyle(
                            state = ctx.first,
                            style = SpanStyle(color = color),
                            onChanged = onEditingSync
                        )
                    } else {
                        component.updateTextBlock(blockId) {
                            it.copy(color = color.toArgb().toLong() and 0xFFFF_FFFFL)
                        }
                    }
                },
                allowAlpha = false,
                modifier = Modifier.weight(1f)
            )
        }
        // 行间距/字间距是行级属性,对局部选区无意义:选区激活时置灰
        PanelSliderRow(
            label = stringResource(R.string.textcard_line_spacing),
            value = block.lineSpacingMultiplier,
            valueRange = 1f..3f,
            valueText = formatDecimal((block.lineSpacingMultiplier * 10).roundToInt() / 10f),
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(lineSpacingMultiplier = value) }
            },
            enabled = selectionCtx == null
        )
        PanelSliderRow(
            label = stringResource(R.string.textcard_letter_spacing),
            value = block.letterSpacingEm,
            valueRange = 0f..1f,
            valueText = formatDecimal((block.letterSpacingEm * 100).roundToInt() / 100f),
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(letterSpacingEm = value) }
            },
            enabled = selectionCtx == null
        )
        // 元素级不透明度:每个文字块独立(背景透明度在「纸张背景」面板);整体属性,选区激活时仍可用
        PanelSliderRow(
            label = stringResource(R.string.textcard_opacity),
            value = block.alpha,
            valueRange = 0f..1f,
            valueText = "${(block.alpha * 100).roundToInt()}%",
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(alpha = value) }
            }
        )

        // 对齐分段(左/居中/右/两端四个图标按钮):行级属性,选区激活时置灰
        PanelSettingRow(label = stringResource(R.string.textcard_alignment)) {
            PanelDisabledWrapper(
                enabled = selectionCtx == null,
                modifier = Modifier.weight(1f)
            ) {
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 字体样式:B 加粗 / I 斜体 / U 下划线 / S 删除线切换 chip(对齐图片创作);
        // B/I 选区激活时切换选区(叠加/反转块级);U/S 为块级属性,选区激活时置灰
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val selBold = selectionCtx?.let { (_, blk, sel) ->
                blk.isRangeEffectivelyBold(sel.min, sel.max)
            }
            val selItalic = selectionCtx?.let { (_, blk, sel) ->
                blk.isRangeEffectivelyItalic(sel.min, sel.max)
            }
            StyleChip(
                glyph = "B",
                labelRes = R.string.textcard_bold,
                selected = selBold ?: block.isBold,
                onClick = {
                    val ctx = selectionCtx
                    if (ctx != null) {
                        val (_, blk, sel) = ctx
                        applySpanStyle(
                            state = ctx.first,
                            style = SpanStyle(
                                fontWeight = if (blk.isRangeEffectivelyBold(sel.min, sel.max)) {
                                    FontWeight.Normal
                                } else FontWeight.Bold
                            ),
                            onChanged = onEditingSync
                        )
                    } else {
                        component.updateTextBlock(blockId) { it.copy(isBold = !it.isBold) }
                    }
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            StyleChip(
                glyph = "I",
                labelRes = R.string.textcard_italic,
                selected = selItalic ?: block.isItalic,
                onClick = {
                    val ctx = selectionCtx
                    if (ctx != null) {
                        val (_, blk, sel) = ctx
                        applySpanStyle(
                            state = ctx.first,
                            style = SpanStyle(
                                fontStyle = if (blk.isRangeEffectivelyItalic(sel.min, sel.max)) {
                                    FontStyle.Normal
                                } else FontStyle.Italic
                            ),
                            onChanged = onEditingSync
                        )
                    } else {
                        component.updateTextBlock(blockId) { it.copy(isItalic = !it.isItalic) }
                    }
                },
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            PanelDisabledWrapper(
                enabled = selectionCtx == null,
                modifier = Modifier.weight(1f)
            ) {
                StyleChip(
                    glyph = "U",
                    labelRes = R.string.textcard_underline,
                    selected = block.isUnderline,
                    onClick = {
                        component.updateTextBlock(blockId) {
                            it.copy(isUnderline = !it.isUnderline)
                        }
                    },
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            PanelDisabledWrapper(
                enabled = selectionCtx == null,
                modifier = Modifier.weight(1f)
            ) {
                StyleChip(
                    glyph = "S",
                    labelRes = R.string.textcard_strikethrough,
                    selected = block.isStrikethrough,
                    onClick = {
                        component.updateTextBlock(blockId) {
                            it.copy(isStrikethrough = !it.isStrikethrough)
                        }
                    },
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 文字背景(逐行高亮底色):开关 + 颜色行(支持透明);块级属性,选区激活时置灰
        PanelDisabledWrapper(enabled = selectionCtx == null) {
            Column {
                PanelSettingRow(label = stringResource(R.string.textcard_text_background)) {
                    Spacer(Modifier.weight(1f))
                    GlassSwitch(
                        checked = block.backgroundColor != 0L,
                        onCheckedChange = { checked ->
                            component.updateTextBlock(blockId) {
                                it.copy(backgroundColor = if (checked) 0x66000000 else 0)
                            }
                        }
                    )
                }
                if (block.backgroundColor != 0L) {
                    ColorSelectionRow(
                        value = Color(block.backgroundColor),
                        onValueChange = { color ->
                            component.updateTextBlock(blockId) {
                                it.copy(
                                    backgroundColor = color.toArgb().toLong() and 0xFFFF_FFFFL
                                )
                            }
                        },
                        allowAlpha = true
                    )
                }
            }
        }

        // 描边:开关 + 颜色行 + 粗细滑杆(粗细为相对画布宽的比例);块级属性,选区激活时置灰
        PanelDisabledWrapper(enabled = selectionCtx == null) {
            Column {
                PanelSettingRow(label = stringResource(R.string.textcard_text_outline)) {
                    Spacer(Modifier.weight(1f))
                    GlassSwitch(
                        checked = block.outlineWidthRatio > 0f,
                        onCheckedChange = { checked ->
                            component.updateTextBlock(blockId) {
                                it.copy(outlineWidthRatio = if (checked) 0.004f else 0f)
                            }
                        }
                    )
                }
                if (block.outlineWidthRatio > 0f) {
                    ColorSelectionRow(
                        value = Color(block.outlineColor),
                        onValueChange = { color ->
                            component.updateTextBlock(blockId) {
                                it.copy(outlineColor = color.toArgb().toLong() and 0xFFFF_FFFFL)
                            }
                        },
                        allowAlpha = false
                    )
                    PanelSliderRow(
                        label = stringResource(R.string.textcard_outline_width),
                        value = block.outlineWidthRatio,
                        valueRange = 0.001f..0.02f,
                        valueText = "${formatDecimal((block.outlineWidthRatio * 1000).roundToInt() / 10f)}%",
                        onValueChange = { value ->
                            component.updateTextBlock(blockId) {
                                it.copy(outlineWidthRatio = value)
                            }
                        }
                    )
                }
            }
        }
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

