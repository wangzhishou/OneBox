/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.core.ui.widget.controls.selection

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.FileExport
import com.t8rin.imagetoolbox.core.resources.icons.FileImport
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExtension
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFont
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRadioChecked
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRadioUnchecked
import com.t8rin.imagetoolbox.core.settings.di.FontCatalogEntryPoint
import com.t8rin.imagetoolbox.core.settings.domain.FontCatalog
import com.t8rin.imagetoolbox.core.settings.domain.model.DownloadableFont
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiFontFamily
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.core.ui.theme.takeColorFromScheme
import com.wanbaohe.core.ui.theme.ProvideTypography
import com.wanbaohe.core.ui.utils.content_pickers.rememberFilePicker
import com.wanbaohe.core.ui.utils.provider.SafeLocalContainerColor
import com.wanbaohe.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.other.GradientEdge
import com.wanbaohe.core.ui.widget.other.InfoContainer
import com.wanbaohe.core.ui.widget.preferences.PreferenceItemDefaults
import com.wanbaohe.core.ui.widget.preferences.PreferenceItemOverload
import com.wanbaohe.core.ui.widget.preferences.PreferenceRow
import com.wanbaohe.core.ui.widget.system.OneBoxDesignSystem
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import com.wanbaohe.core.ui.widget.text.TitleItem
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.t8rin.logger.makeLog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.CloudDownload

/**
 * 全局字体选择弹层(自 feature/settings 上移 core/ui,新增「可下载字体」区):
 * 默认字体 → 可下载字体(未下载/下载中/已下载三态,下载完成立即可选)→ 已导入字体,
 * 顶部导入/导出。下载状态管理内聚在本 Sheet:经 EntryPoint 取 [FontCatalog] 单例,
 * 查询/触发下载,下载成功即回调 onFontSelected(FontType.File 包装的 Custom)。
 */
@Composable
fun PickFontFamilySheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onFontSelected: (UiFontFamily) -> Unit,
    onAddFont: (Uri) -> Unit,
    onRemoveFont: (UiFontFamily.Custom) -> Unit,
    onExportFonts: () -> Unit
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        },
        sheetContent = {
            val defaultEntries = UiFontFamily.defaultEntries
            val customEntries = UiFontFamily.customEntries

            LazyColumn(
                contentPadding = PaddingValues(OneBoxDesignSystem.screenPadding),
                verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val result = measurable.measure(
                                    constraints.copy(
                                        maxWidth = constraints.maxWidth + 32.dp.roundToPx()
                                    )
                                )
                                layout(
                                    result.measuredWidth,
                                    result.measuredHeight
                                ) {
                                    result.place(0, 0)
                                }
                            }
                            .glassBackground(
                                style = GlassStyle.Medium,
                                color = EnhancedBottomSheetDefaults.containerColor,
                                borderWidth = 0.dp,
                            )
                            .padding(horizontal = OneBoxDesignSystem.screenPadding)
                    ) {
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.microSpacing),
                            modifier = Modifier.height(IntrinsicSize.Max)
                        ) {
                            val pickFileLauncher = rememberFilePicker(
                                mimeType = MimeType.Font,
                                onSuccess = onAddFont
                            )
                            PreferenceRow(
                                title = stringResource(R.string.import_font),
                                onClick = pickFileLauncher::pickFile,
                                shape = ShapeDefaults.start,
                                titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered,
                                startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.FileImport,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )

                            val canExport = customEntries.isNotEmpty()

                            PreferenceRow(
                                title = stringResource(R.string.export_fonts),
                                onClick = onExportFonts,
                                shape = ShapeDefaults.end,
                                enabled = canExport,
                                startIcon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.FileExport,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = takeColorFromScheme {
                                    if (canExport) primaryContainer
                                    else surfaceVariant
                                },
                                titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                            )
                        }
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                    }
                    GradientEdge(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        startColor = EnhancedBottomSheetDefaults.containerColor,
                        endColor = Color.Transparent
                    )
                }

                items(
                    items = defaultEntries,
                    key = { it.name ?: "sys" }
                ) { font ->
                    FontItem(
                        font = font,
                        onFontSelected = onFontSelected,
                        onRemoveFont = onRemoveFont
                    )
                }

                // 可下载字体区(默认与已导入之间)
                item {
                    InfoContainer(
                        text = stringResource(R.string.downloadable_fonts),
                        icon = MaterialIcons.Outlined.CloudDownload,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(OneBoxDesignSystem.compactSpacing),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.4f)
                    )
                }
                downloadableFontItems(onFontSelected = onFontSelected)

                item {
                    InfoContainer(
                        text = stringResource(R.string.imported_fonts),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExtension,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(OneBoxDesignSystem.compactSpacing),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.4f)
                    )
                }
                items(
                    items = customEntries,
                    key = { it.name ?: "sys" }
                ) { font ->
                    FontItem(
                        font = font,
                        onFontSelected = onFontSelected,
                        onRemoveFont = onRemoveFont
                    )
                }
            }
        },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                OneSecondaryButton(
                    text = stringResource(R.string.close),
                    onClick = onDismiss
                )
            }
        },
        title = stringResource(R.string.font),
    )
}

/** 可下载字体行状态 */
private sealed interface FontDownloadUiState {
    data object NotDownloaded : FontDownloadUiState
    data class Downloading(val progress: Float) : FontDownloadUiState
    data object Downloaded : FontDownloadUiState
}

/** 可下载字体区:状态与下载动作内聚,经 EntryPoint 取 FontCatalog 单例 */
private fun androidx.compose.foundation.lazy.LazyListScope.downloadableFontItems(
    onFontSelected: (UiFontFamily) -> Unit,
) {
    item(key = "downloadable_font_list") {
        DownloadableFontList(onFontSelected = onFontSelected)
    }
}

@Composable
private fun DownloadableFontList(
    onFontSelected: (UiFontFamily) -> Unit,
) {
    val context = LocalContext.current
    val fontCatalog = remember {
        runCatching {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                FontCatalogEntryPoint::class.java
            ).fontCatalog
        }.onFailure { it.makeLog("FontCatalogEntryPoint") }.getOrNull()
    } ?: return
    val scope = rememberCoroutineScope()

    // 下载态:内存覆盖 + 落盘清单兜底
    var downloadStates by remember {
        mutableStateOf(
            fontCatalog.fonts.associate { font ->
                font.id to if (fontCatalog.downloadedFont(font) != null) {
                    FontDownloadUiState.Downloaded
                } else FontDownloadUiState.NotDownloaded
            }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.compactSpacing)) {
        fontCatalog.fonts.forEach { font ->
            DownloadableFontRow(
                font = font,
                state = downloadStates[font.id] ?: FontDownloadUiState.NotDownloaded,
                fontCatalog = fontCatalog,
                onFontSelected = onFontSelected,
                onStartDownload = {
                    if (downloadStates[font.id] !is FontDownloadUiState.Downloading) {
                        scope.launch {
                            downloadStates = downloadStates + (font.id to FontDownloadUiState.Downloading(0f))
                            fontCatalog.download(font) { progress ->
                                downloadStates =
                                    downloadStates + (font.id to FontDownloadUiState.Downloading(progress))
                            }.onSuccess { fontType ->
                                downloadStates = downloadStates + (font.id to FontDownloadUiState.Downloaded)
                                // 选中时带上清单的本地化名称,避免 Custom.name 为空回退成字体内部名
                                onFontSelected(
                                    UiFontFamily.Custom(
                                        name = context.getString(font.nameRes),
                                        filePath = fontType.path
                                    )
                                )
                                AppToastHost.showToast(R.string.font_downloaded)
                            }.onFailure { failure ->
                                failure.makeLog("FontDownload")
                                downloadStates =
                                    downloadStates + (font.id to FontDownloadUiState.NotDownloaded)
                                AppToastHost.showFailureToast(R.string.font_download_failed)
                            }
                        }
                    }
                }
            )
        }
    }
}

/** 单个可下载字体行。尾部状态图标与其余分区统一(全图标无文字):
 * 未下载=云朵(点击下载)、下载中=进度、已下载=单选圈(选中态与其他分区一致),
 * 选中行同样给 secondaryContainer 底色 + 描边。已下载后用实际字体渲染整行。
 * 组件与 FontSelectionItem 同为 PreferenceItemOverload,保证各分区卡片 padding 完全一致 */
@Composable
private fun DownloadableFontRow(
    font: DownloadableFont,
    state: FontDownloadUiState,
    fontCatalog: FontCatalog,
    onFontSelected: (UiFontFamily) -> Unit,
    onStartDownload: () -> Unit,
) {
    val context = LocalContext.current
    val settingsState = LocalSettingsState.current
    val downloadedPath = if (state == FontDownloadUiState.Downloaded) {
        fontCatalog.downloadedFont(font)?.path
    } else null
    val isSelected = downloadedPath != null &&
        settingsState.font == UiFontFamily.Custom(name = null, filePath = downloadedPath)
    // 体积文案:小于 1MB 显示 "<1",否则四舍五入取整
    val sizeText = if (font.approxSizeMb < 1f) "<1" else font.approxSizeMb.roundToInt().toString()
    val row: @Composable () -> Unit = {
        PreferenceItemOverload(
        title = stringResource(font.nameRes),
        // 已下载放大成两行样张(与已导入字体同款),未下载保持单行例句+体积
        subtitle = if (downloadedPath != null) {
            stringResource(R.string.font_preview_text) + "\n" +
                stringResource(R.string.alphabet_and_numbers)
        } else {
            stringResource(R.string.font_preview_text) + " · ${sizeText}MB"
        },
        // 与 FontItem(FontSelectionItem)一致的卡片样式与选中高亮
        color = takeColorFromScheme {
            if (isSelected) secondaryContainer
            else SafeLocalContainerColor
        },
        shape = ShapeDefaults.default,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = settingsState.borderWidth,
                color = animateColorAsState(
                    if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    } else Color.Transparent
                ).value,
                shape = ShapeDefaults.default
            ),
        onClick = when (state) {
            FontDownloadUiState.Downloaded -> {
                {
                    fontCatalog.downloadedFont(font)?.let { fontType ->
                        onFontSelected(
                            UiFontFamily.Custom(
                                name = context.getString(font.nameRes),
                                filePath = fontType.path
                            )
                        )
                    }
                    Unit
                }
            }

            FontDownloadUiState.NotDownloaded -> onStartDownload
            is FontDownloadUiState.Downloading -> null
        },
        endIcon = {
            // 尾部状态全图标:云朵下载 / 进度 / 单选圈,与其余分区语义一致
            when (state) {
                is FontDownloadUiState.Downloading -> if (state.progress > 0f) {
                    Text(
                        text = "${(state.progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // 无 Content-Length 时只有 0/1 回调,转不确定圈
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }

                FontDownloadUiState.Downloaded -> Icon(
                    imageVector = if (isSelected) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRadioChecked
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRadioUnchecked
                    },
                    contentDescription = stringResource(
                        if (isSelected) R.string.font_downloaded else R.string.font_download
                    ),
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                FontDownloadUiState.NotDownloaded -> Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudDownload,
                    contentDescription = stringResource(R.string.font_download),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        )
    }
    if (downloadedPath != null) {
        ProvideTypography(UiFontFamily.Custom(name = null, filePath = downloadedPath)) {
            row()
        }
    } else {
        row()
    }
}
