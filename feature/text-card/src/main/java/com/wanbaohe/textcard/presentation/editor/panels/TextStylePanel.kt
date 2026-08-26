package com.wanbaohe.textcard.presentation.editor.panels

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButtonGroup
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
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
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic

/** 文本块标签:内容首行前 6 个字符,空内容兜底「文字 N」 */
@Composable
private fun TextBlock.displayLabel(): String {
    val firstLine = content.lineSequence().firstOrNull()?.take(6).orEmpty()
    return firstLine.ifEmpty { stringResource(R.string.textcard_block_fallback) }
}

/**
 * 文字设置面板(设计稿 04):作用于当前选中文本块(任意多块,按 id)。
 * 顺序按常用优先:文本块切换 → 字号 → 行距 → 字间距 → 对齐 → 加粗/斜体 → 不透明度 → 文字颜色;
 * 各分区统一 container() 包裹与 8dp 间距。
 */
@Composable
fun TextStylePanel(component: TextCardComponent) {
    val blocks = component.textBlocks
    val block = component.selectedTextBlock() ?: return
    val blockId = block.id

    PanelTitle(R.string.textcard_text_settings)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 文本块切换(标题/正文/新增块,按首行内容截断做标签);增删统一走「基础」面板与图层面板
        val labels = blocks.map { it.displayLabel() }
        EnhancedButtonGroup(
            items = labels,
            selectedIndex = blocks.indexOfFirst { it.id == blockId }.coerceAtLeast(0),
            onIndexChange = { index ->
                blocks.getOrNull(index)?.let { component.selectTextBlock(it.id) }
            },
            title = stringResource(R.string.textcard_text_target)
        )

        EnhancedSliderItem(
            value = block.sizeScale,
            title = stringResource(R.string.textcard_text_size),
            valueRange = 0.5f..2f,
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(sizeScale = value) }
            },
            internalStateTransformation = { (it * 36).roundToInt() }
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
            value = block.letterSpacingEm,
            title = stringResource(R.string.textcard_letter_spacing),
            valueRange = 0f..0.2f,
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(letterSpacingEm = value) }
            },
            internalStateTransformation = { (it * 50).roundToInt() }
        )

        // 字体:当前块字体名 + 预览,点击打开全局共享的字体选择器(能下载/能导入)
        FontPickerRow(
            component = component,
            block = block
        )

        // 对齐分段(设计稿 04 的四个图标按钮)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .container(shape = ShapeDefaults.default)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.textcard_alignment),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
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
                }
            )
        }

        // 字体样式:加粗 / 斜体开关
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .container(shape = ShapeDefaults.default)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.textcard_font_style),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
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
                GlassSwitch(
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
                GlassSwitch(
                    checked = block.isItalic,
                    onCheckedChange = { checked ->
                        component.updateTextBlock(blockId) { it.copy(isItalic = checked) }
                    }
                )
            }
        }

        // 元素级不透明度:每个文字块独立(背景透明度在「纸张背景」面板)
        EnhancedSliderItem(
            value = block.alpha,
            title = stringResource(R.string.textcard_opacity),
            valueRange = 0f..1f,
            onValueChange = { value ->
                component.updateTextBlock(blockId) { it.copy(alpha = value) }
            },
            internalStateTransformation = { (it * 100).roundToInt() },
            valueSuffix = "%"
        )

        // 文字颜色:与图片创作一致的横向滚动色板(首项支持自定义取色)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .container(shape = ShapeDefaults.default)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.textcard_text_color),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            ColorSelectionRow(
                value = Color(block.color),
                onValueChange = { color ->
                    component.updateTextBlock(blockId) {
                        it.copy(color = color.toArgb().toLong() and 0xFFFF_FFFFL)
                    }
                },
                allowAlpha = false
            )
        }
    }
}

/**
 * 「字体」行:显示当前块字体名,预览文案用当前字体渲染;点击打开全局共享的
 * [PickFontFamilySheet](默认/可下载/已导入)。导入/移除/导出走 LocalSettingsManager
 * (与系统字体切换同一数据源 SettingsState.customFonts)。
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
        null -> stringResource(com.t8rin.imagetoolbox.core.resources.R.string.system)
        is FontType.File -> fontCatalog?.fontForFile(font.path)
            ?.let { stringResource(it.nameRes) }
            ?: font.asUi().name
            ?: stringResource(com.t8rin.imagetoolbox.core.resources.R.string.system)
        is FontType.Resource -> font.asUi().name
            ?: stringResource(com.t8rin.imagetoolbox.core.resources.R.string.system)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .container(shape = ShapeDefaults.default)
            .clickable { showFontSheet = true }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.font),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = blockFontName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.font_preview_text),
                fontFamily = block.font.toUiFont().fontFamily,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
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
                        message = context.getString(
                            com.t8rin.imagetoolbox.core.resources.R.string.wrong_font
                        ),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText
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
