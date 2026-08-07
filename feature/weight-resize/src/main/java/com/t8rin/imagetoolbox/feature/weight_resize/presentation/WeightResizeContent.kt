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

package com.t8rin.imagetoolbox.feature.weight_resize.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.ImagePickerList
import com.shifenmiao.common.ui.ImageWorkspace
import com.shifenmiao.common.ui.ImageWorkspaceTab
import com.shifenmiao.common.ui.ZoomableImagePreview
import com.shifenmiao.common.ui.rememberImageWorkspaceState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.common.ui.rememberSafeImageBitmap
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormatGroup
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.ImageUtils.rememberHumanFileSize
import com.t8rin.imagetoolbox.core.ui.widget.controls.SaveExifWidget
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageFormatSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ScaleModeSelector
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.image.AutoFilePicker
import com.t8rin.imagetoolbox.core.ui.widget.modifier.detectSwipes
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import com.t8rin.imagetoolbox.core.ui.widget.utils.AutoContentBasedColors
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.components.CompressControlPanel
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.components.ImageFormatAlert
import com.t8rin.imagetoolbox.feature.weight_resize.presentation.screenLogic.WeightResizeComponent
import com.shifenmiao.common.R as CommonR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload

/**
 * 图片压缩屏幕 —— 基于 [ImageWorkspace] 重构。
 *
 * ## 布局结构
 * ```
 * ┌──────────────────────────────────────────────┐
 * │  TopBar: 标题  ·  ShareBtn                   │
 * ├──────────────────────────────────────────────┤
 * │                                              │
 * │   Canvas（画布区）                            │
 * │   · 可缩放 / 拖动 / 双击的图片预览            │
 * │   · 左右滑动切换多图                           │
 * │   · 点击画布切换沉浸模式                       │
 * │   · 底部悬浮 FileSizeChip（压缩后大小）        │
 * │                                              │
 * ├──────── 操作面板（可折叠）──────────────────┤
 * │  [已选图片] [压缩方式] [图片格式] [缩放模式] [设置]│
 * │  ──────────────────────────────────────────  │
 * │  已选图片: 横向图片选择列表 + 添加按钮         │
 * │  压缩方式: 预设百分比芯片 ↔ 手动KB输入 并排显示│
 * │  图片格式: 格式选择（ImageFormatSelector）    │
 * │  缩放模式: 算法 + 色彩空间（ScaleModeSelector）│
 * │  设置:    保留 EXIF 等选项                    │
 * ├──────────────────────────────────────────────┤
 * │  BottomBar: ······ [保存 ▼]                 │
 * └──────────────────────────────────────────────┘
 * ```
 *
 * @see ImageWorkspace
 * @see ImageWorkspaceTab
 */
@Composable
fun WeightResizeContent(
    component: WeightResizeComponent
) {
    // 主题色随图片内容自适应
    AutoContentBasedColors(component.bitmap)

    // ── 外部图片选择器（专供 AutoFilePicker 自动弹出系统选图）──
    val autoImagePicker = rememberImagePicker { uris: List<Uri> ->
        component.updateUris(uris = uris, onFailure = AppToastHost::showFailureToast)
    }
    AutoFilePicker(
        onAutoPick = autoImagePicker::pickImage,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    // ── Workspace 状态 ──
    val workspaceState = rememberImageWorkspaceState()
    val immersiveModeState = rememberImmersiveModeState()

    // ── 跨模块编辑跳转 Sheet ──
    var editSheetData by remember { mutableStateOf(listOf<Uri>()) }
    ProcessImagesPreferenceSheet(
        uris = editSheetData,
        visible = editSheetData.isNotEmpty(),
        onDismiss = { editSheetData = emptyList() },
        onNavigate = component.onNavigate
    )

    // ── 安全 Bitmap（防 recycle 崩溃）──
    val safeImageBitmap = rememberSafeImageBitmap(component.previewBitmap)

    // ── 当前预览图在 uris 列表中的索引 ──
    val currentImageIndex = remember(component.uris, component.selectedUri) {
        component.uris?.indexOf(component.selectedUri)?.takeIf { it >= 0 } ?: 0
    }

    // ── 已选图片列表滚动状态（"已选图片" Tab 专用）──
    val imageListState = rememberLazyListState()

    // ── 选图后自动跳到"压缩方式"Tab（index=1），提示用户开始配置 ──
    LaunchedEffect(component.uris?.size) {
        if (!component.uris.isNullOrEmpty() && workspaceState.currentTabIndex == 0) {
            workspaceState.navigateToTab(1)
        }
        // 图片全部删除 —— 跳回"已选图片"Tab，引导用户重新选图
        if (component.uris.isNullOrEmpty()) {
            workspaceState.scrollToTab(0)
        }
    }

    // ── 操作面板 Tab 内容区可见高度 ──
    val panelTabHeight = 220.dp

    // ══════════════════════════════════════════════════
    //  ImageWorkspace — 新一代图片操作基础框架
    // ══════════════════════════════════════════════════
    ImageWorkspace(
        // ── 基础配置 ──────────────────────────────────
        title = stringResource(R.string.by_bytes_resize),
        onGoBack = component.onGoBack,
        hasSelectedImages = component.bitmap != null,
        haveChanges = component.haveChanges,
        isSaving = component.isSaving,
        isImageLoading = component.isImageLoading,
        done = component.done,
        left = component.uris?.size ?: 0,
        imageFormat = component.imageFormat,
        onPickImages = { uris ->
            component.updateUris(uris = uris, onFailure = AppToastHost::showFailureToast)
        },
        onSave = { uri ->
            component.saveBitmaps(
                oneTimeSaveLocationUri = uri,
                onResult = component::parseSaveResults
            )
        },
        onCancelSaving = component::cancelSaving,
        state = workspaceState,
        immersiveModeState = immersiveModeState,
        picker = Picker.Multiple,

        // ── TopBar 右侧操作按钮槽 ────────────────────
        topBarActions = {
            TopAppBarEmoji()
        },

        // ── 画布区域槽 ────────────────────────────────
        canvas = { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .detectSwipes(
                        onSwipeRight = component::selectLeftUri,
                        onSwipeLeft = component::selectRightUri
                    )
            ) {
                ZoomableImagePreview(
                    imageBitmap = safeImageBitmap,
                    isLoading = component.isImageLoading,
                    isImmersive = immersiveModeState.isImmersive,
                    contentPadding = contentPadding,
                    onTap = immersiveModeState::toggle,
                    modifier = Modifier.fillMaxSize()
                )

                // ── 压缩后文件大小悬浮 Chip ──
                AnimatedVisibility(
                    visible = component.imageSize > 0 && !immersiveModeState.isImmersive,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = contentPadding.calculateBottomPadding() + 8.dp)
                ) {
                    FileSizeChip(
                        imageSize = component.imageSize,
                        isLoading = component.isImageLoading
                    )
                }
            }
        },

        // ── 操作面板 Tabs 槽 ──────────────────────────
        operationTabs = listOf(

            // ─────────────────────────────────────────
            // Tab 0 · 已选图片
            // · 已选图片缩略图横向列表
            // · 点击缩略图切换预览；点击 + 号追加图片
            // · 点击 × 删除单张图
            // ─────────────────────────────────────────
            ImageWorkspaceTab(
                title = if ((component.uris?.size ?: 0) > 0)
                    stringResource(CommonR.string.common_selected_images_count, component.uris!!.size)
                else
                    stringResource(CommonR.string.common_selected_images)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ImagePickerList(
                        selectedUris = component.uris ?: emptyList(),
                        currentIndex = currentImageIndex,
                        onImageSelected = { index ->
                            component.uris?.getOrNull(index)?.let { uri ->
                                component.updateSelectedUri(
                                    uri = uri,
                                    onFailure = AppToastHost::showFailureToast
                                )
                            }
                        },
                        onAddClick = { autoImagePicker.pickImage() },
                        onAddLongClick = { autoImagePicker.pickImage() },
                        onRemoveClick = { uri ->
                            component.updateUrisSilently(removedUri = uri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        listState = imageListState,
                    )
                }
            },

            // ─────────────────────────────────────────
            // Tab 1 · 压缩方式
            // · 左：预设百分比芯片（可横向滚动）
            // · 右：手动输入目标 KB 上限
            // · 两种控件并排展示，无需切换按钮
            // ─────────────────────────────────────────
            ImageWorkspaceTab(title = stringResource(R.string.compression_type)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    CompressControlPanel(
                        enabled = component.bitmap != null,
                        maxBytes = component.maxBytes,
                        presetSelected = component.presetSelected,
                        onMaxBytesChange = component::updateMaxBytes,
                        onPresetChange = component::selectPreset,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },

            // ─────────────────────────────────────────
            // Tab 2 · 图片格式
            // · 排除有损 PNG，保留无损 PNG 选项
            // · title = {} 避免 Tab 标题与控件标题重复
            // ─────────────────────────────────────────
            ImageWorkspaceTab(title = stringResource(R.string.image_format)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AnimatedVisibility(
                        visible = !component.imageFormat.canChangeCompressionValue
                    ) {
                        ImageFormatAlert(
                            format = component.imageFormat,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ImageFormatSelector(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.imageFormat,
                        onValueChange = component::setImageFormat,
                        // 隐藏控件内部标题，避免与 Tab 标题 "图片格式" 重复
                        title = {},
                        entries = remember {
                            ImageFormatGroup.entries
                                .minus(ImageFormatGroup.Png)
                                .plus(
                                    ImageFormatGroup.Custom(
                                        title = "PNG Lossless",
                                        formats = listOf(ImageFormat.Png.Lossless)
                                    )
                                )
                        }
                    )
                }
            },

            // ─────────────────────────────────────────
            // Tab 3 · 缩放模式（含色彩空间）
            // · title = {} 避免与 Tab 标题 "缩放模式" 重复
            // ─────────────────────────────────────────
            ImageWorkspaceTab(title = stringResource(R.string.scale_mode)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    ScaleModeSelector(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        value = component.imageScaleMode,
                        onValueChange = component::setImageScaleMode,
                        title = {},
                    )
                }
            },

            // ─────────────────────────────────────────
            // Tab 4 · 设置
            // · 保留 EXIF 元数据开关
            // ─────────────────────────────────────────
            ImageWorkspaceTab(title = stringResource(R.string.settings)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    SaveExifWidget(
                        modifier = Modifier.fillMaxWidth(),
                        imageFormat = component.imageFormat,
                        checked = component.keepExif,
                        onCheckedChange = component::setKeepExif
                    )
                }
            }
        ),

        panelTabHeight = panelTabHeight,
    )
}

// ══════════════════════════════════════════════════
//  内部组件
// ══════════════════════════════════════════════════

/**
 * 画布区悬浮文件大小 Chip。
 */
@Composable
private fun FileSizeChip(
    imageSize: Long,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val humanSize = rememberHumanFileSize(imageSize)

    GlassSurface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        style = GlassStyle.Thin,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.88f),
        borderWidth = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            } else {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                text = if (isLoading) stringResource(R.string.loading) else humanSize,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
