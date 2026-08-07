package com.wanbaohe.camera.watermark.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.common.ui.BatchSaveButton
import com.shifenmiao.common.ui.ImageBaseScreen
import com.shifenmiao.common.ui.ImageCollapsibleSaveBar
import com.shifenmiao.common.ui.ImagePickerList
import com.shifenmiao.common.ui.TabButton
import com.shifenmiao.common.ui.rememberAddImagePickerState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.common.ui.rememberPreviewPaddingValues
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.WatermarkStyle
import com.wanbaohe.camera.watermark.presentation.components.ExportConfigPanel
import com.wanbaohe.camera.watermark.presentation.components.StyleCustomizer
import com.wanbaohe.camera.watermark.presentation.components.StyleSelector
import com.wanbaohe.camera.watermark.presentation.components.TemplateEditor
import com.wanbaohe.camera.watermark.presentation.components.WatermarkPreview
import com.wanbaohe.camera.watermark.presentation.screenLogic.CameraWatermarkComponent
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings

/**
 * 相机水印主屏幕
 * 使用 ImageBaseScreen 作为基础框架
 */
@Composable
fun CameraWatermarkScreen(
    component: CameraWatermarkComponent,
    appComponent: AppComponent
) {
    // 沉浸式模式状态
    val immersiveModeState = rememberImmersiveModeState()

    // Tab 区域展开状态
    var isTabSectionExpanded by rememberSaveable { mutableStateOf(true) }

    // 状态
    val selectedUris by component.selectedUris.collectAsState()

    // Tab 状态
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabTitles = listOf(
        stringResource(R.string.camera_watermark_tab_selected_images, selectedUris.size),
        stringResource(R.string.camera_watermark_tab_select_style)
    )

    // 图片列表和样式列表的滚动状态（在父组件保存，避免折叠时丢失）
    val imageListState = rememberLazyListState()
    val styleListState = rememberLazyListState()

    // 选择图片后自动切换到 Tab 1（开始工作了）
    LaunchedEffect(selectedUris.size) {
        if (selectedUris.isNotEmpty() && pagerState.currentPage != 1) {
            pagerState.animateScrollToPage(1)
        }
    }

    // Bottom Sheet 状态
    var showExportConfig by remember { mutableStateOf(false) }
    var showStyleCustomizer by remember { mutableStateOf(false) }
    var editingStyle by remember { mutableStateOf<WatermarkStyle?>(null) }

    // 添加图片选择器状态（复用 common 模块的组件）
    val addImageState = rememberAddImagePickerState(
        picker = Picker.Multiple,
        onImagesPicked = { uris -> component.addUris(uris) }
    )

    // 保存函数
    val saveBitmap: (oneTimeSaveLocationUri: String?) -> Unit = { uri ->
        component.saveBitmap(
            oneTimeSaveLocationUri = uri,
            onComplete = component::parseSaveResult
        )
    }

    // 导出配置 Bottom Sheet
    if (showExportConfig) {
        EnhancedModalTopSheet(
            visible = showExportConfig,
            title = {
                Text(
                    text = stringResource(R.string.camera_watermark_export_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            onDismiss = { showExportConfig = false },
        ) {
            ExportConfigPanel(
                config = component.exportConfig,
                onConfigChanged = component::updateExportConfig,
                authorSignature = component.authorSignature,
                onAuthorSignatureChanged = component::setAuthorSignature,
                keepOriginalExif = component.keepOriginalExif,
                onKeepOriginalExifChanged = component::setKeepOriginalExif,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // 样式自定义 Bottom Sheet
    EnhancedModalBottomSheet(
        visible = showStyleCustomizer,
        onDismiss = { showStyleCustomizer = false },
        title = {
            Text(
                text = stringResource(R.string.camera_watermark_template),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        enableBackHandler = true,
        enableBottomContentWeight = false
    ) {
        StyleCustomizer(
            style = component.currentStyle,
            onStyleChanged = component::setStyle,
            metadata = component.metadata,
            templates = component.allTemplates.collectAsState().value,
            onSaveTemplate = component::saveOrUpdateTemplate,
            onDeleteTemplate = component::deleteTemplate,
            onBatchDeleteTemplates = component::batchDeleteTemplates,
            onResetPresets = component::resetPresetsToDefaults,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }

    // 编辑单个样式 Bottom Sheet
    editingStyle?.let { style ->
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = { editingStyle = null },
            title = {
                Text(
                    text = stringResource(R.string.camera_watermark_edit_template),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            TemplateEditor(
                template = style,
                metadata = component.metadata,
                isNew = false,
                onSave = { updatedStyle ->
                    component.saveOrUpdateTemplate(updatedStyle)
                    editingStyle = null
                },
                onCancel = { editingStyle = null }
            )
        }
    }

    // 添加图片对话框（复用 common 模块的组件）
    AddImagePickingDialogWithPicker(
        visible = addImageState.showDialog,
        onDismiss = addImageState.onDismissDialog,
        picker = Picker.Multiple,
        onImagesPicked = { uris -> component.addUris(uris) }
    )

    // 使用 ImageBaseScreen 作为基础框架
    ImageBaseScreen(
        title = stringResource(R.string.camera_watermark_title),
        onGoBack = { appComponent.onGoBack() },
        hasSelectedImages = selectedUris.isNotEmpty(),
        haveChanges = component.haveChanges,
        previewBitmap = component.previewBitmap,
        isImageLoading = component.isImageLoading,
        isSaving = component.isSaving,
        exportProgress = component.exportProgress,
        done = component.done,
        left = component.left,
        imageFormat = component.exportConfig.format,
        onPickImages = component::setSelectedUris,
        onSave = saveBitmap,
        onCancelSaving = component::cancelSaving,
        immersiveModeState = immersiveModeState,
        actions = {
            // 导出设置
            IconButton(onClick = { showExportConfig = true }) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.camera_watermark_settings)
                )
            }
        },
        emptyPlaceholderTitle = stringResource(R.string.camera_watermark_empty_placeholder),
        customBottomBar = if (selectedUris.isEmpty()) {
            // 没有选择图片时不显示底部栏
            null
        } else {
            {
                // 自定义底部栏：可折叠的 Tab + 保存栏
                ImageCollapsibleSaveBar(
                    hasSelectedImages = selectedUris.isNotEmpty(),
                    isSaving = component.isSaving,
                    exportFormat = component.exportConfig.format.title,
                    isExpanded = isTabSectionExpanded,
                    onToggleExpand = { isTabSectionExpanded = !isTabSectionExpanded },
                    onSave = { saveBitmap(null) },
                    onSaveLongClick = { /* 由 ImageBaseScreen 内部处理 */ },
                    expandableContent = {
                        // Tab 切换区域
                        CollapsibleTabSection(
                            pagerState = pagerState,
                            tabTitles = tabTitles,
                            selectedUris = selectedUris,
                            currentIndex = component.currentIndex,
                            currentStyle = component.currentStyle,
                            templates = component.allTemplates.collectAsState().value,
                            onImageSelected = component::switchToIndex,
                            onAddClick = { addImageState.pickImage() },
                            onAddLongClick = { addImageState.onShowDialog() },
                            onRemoveClick = component::removeUri,
                            onStyleSelected = component::selectPreset,
                            onStyleCustomize = { showStyleCustomizer = true },
                            onEditStyle = { style -> editingStyle = style },
                            imageListState = imageListState,
                            styleListState = styleListState
                        )
                    },
                    trailingContent = if (selectedUris.size > 1) {
                        {
                            val context = LocalContext.current
                            // 批量保存按钮（仅多张图片时显示）
                            BatchSaveButton(
                                count = selectedUris.size,
                                isSaving = component.isSaving,
                                onClick = {
                                    component.saveAllBitmaps(
                                        onProgress = { _, _ -> },
                                        onComplete = { results ->
                                            val successCount =
                                                results.count { it is SaveResult.Success }
                                            AppToastHost.showToast(
                                                context.getString(
                                                    R.string.camera_watermark_batch_save_result,
                                                    successCount,
                                                    results.size
                                                )
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    } else null
                )
            }
        },
        previewContent = { safeImageBitmap ->
            // 自定义预览内容：使用 WatermarkPreview
            val previewPadding = rememberPreviewPaddingValues(isTabSectionExpanded)

            AnimatedContent(
                targetState = safeImageBitmap,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "previewAnimation"
            ) { imageBitmap ->
                WatermarkPreview(
                    imageBitmap = imageBitmap,
                    isLoading = component.isImageLoading,
                    isImmersive = immersiveModeState.isImmersive,
                    contentPadding = previewPadding,
                    modifier = Modifier.fillMaxSize(),
                    onClick = { immersiveModeState.toggle() }
                )
            }
        }
    )
}


/**
 * 可折叠的 Tab 区域
 */
@Composable
private fun CollapsibleTabSection(
    pagerState: PagerState,
    tabTitles: List<String>,
    selectedUris: List<Uri>,
    currentIndex: Int,
    currentStyle: WatermarkStyle,
    templates: List<WatermarkStyle>,
    onImageSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onAddLongClick: () -> Unit,
    onRemoveClick: (Uri) -> Unit,
    onStyleSelected: (WatermarkStyle) -> Unit,
    onStyleCustomize: () -> Unit,
    onEditStyle: (WatermarkStyle) -> Unit = {},
    imageListState: LazyListState,
    styleListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = AppTheme.dimens.spaceNormal,
                horizontal = AppTheme.dimens.paddingNormal
            )
            .padding(bottom = 0.dp)
    ) {
        // Tab 标题栏
        // Tab 按钮 - 左对齐
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            tabTitles.forEachIndexed { index, title ->
                TabButton(
                    text = title,
                    isSelected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            }
        }

        // 内容区域
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = if (pagerState.currentPage == 0) {
                        MaterialTheme.shapes.medium.copy(
                            topStart = CornerSize(0.dp)
                        )
                    } else {
                        MaterialTheme.shapes.medium
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.spaceNormal)
                .height(80.dp)
        ) { page ->
            when (page) {
                0 -> {
                    // 已选图片 Tab
                    ImagePickerList(
                        selectedUris = selectedUris,
                        currentIndex = currentIndex,
                        onImageSelected = onImageSelected,
                        onAddClick = onAddClick,
                        onAddLongClick = onAddLongClick,
                        onRemoveClick = onRemoveClick,
                        modifier = Modifier.fillMaxSize(),
                        listState = imageListState
                    )
                }

                1 -> {
                    // 选择样式 Tab
                    StyleSelector(
                        presets = templates,
                        selectedStyle = currentStyle,
                        onStyleSelected = onStyleSelected,
                        onStyleCustomize = onStyleCustomize,
                        onEditStyle = onEditStyle,
                        listState = styleListState
                    )
                }
            }
        }
    }
}
