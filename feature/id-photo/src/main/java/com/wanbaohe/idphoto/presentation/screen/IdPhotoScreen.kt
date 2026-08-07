package com.wanbaohe.idphoto.presentation.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.common.ui.ImageBaseScreen
import com.shifenmiao.common.ui.ImageCollapsibleSaveBar
import com.shifenmiao.common.ui.ImagePickerList
import com.shifenmiao.common.ui.TabButton
import com.shifenmiao.common.ui.rememberAddImagePickerState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.common.ui.rememberPreviewPaddingValues
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalTopSheet
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoSize
import com.wanbaohe.idphoto.presentation.components.IdPhotoCropper
import com.wanbaohe.idphoto.presentation.components.IdPhotoExportConfigPanel
import com.wanbaohe.idphoto.presentation.components.SizeCustomizer
import com.wanbaohe.idphoto.presentation.components.SizeEditor
import com.wanbaohe.idphoto.presentation.components.SizeSelector
import com.wanbaohe.idphoto.presentation.screenLogic.IdPhotoComponent
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings

/**
 * 证件照制作主屏幕
 */
@Composable
fun IdPhotoScreen(
    component: IdPhotoComponent,
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
        stringResource(R.string.id_photo_tab_selected_images, selectedUris.size),
        stringResource(R.string.id_photo_tab_select_size)
    )

    // 选择图片后自动切换到 Tab 1
    LaunchedEffect(selectedUris.size) {
        if (selectedUris.isNotEmpty() && pagerState.currentPage != 1) {
            pagerState.animateScrollToPage(1)
        }
    }

    // Bottom Sheet 状态
    var showSizeCustomizer by remember { mutableStateOf(false) }
    var editingSize by remember { mutableStateOf<IdPhotoSize?>(null) }
    var showExportConfig by remember { mutableStateOf(false) }

    // 添加图片选择器状态（复用 common 模块的组件）
    val addImageState = rememberAddImagePickerState(
        picker = Picker.Multiple,
        onImagesPicked = { uris -> component.addUris(uris) }
    )

    var cropTrigger by remember { mutableLongStateOf(0L) }
    var pendingSaveToken by remember { mutableLongStateOf(0L) }
    var pendingSaveLocationUri by remember { mutableStateOf<String?>(null) }
    var isSavePending by remember { mutableStateOf(false) }

    val performSave: (oneTimeSaveLocationUri: String?) -> Unit = { uri ->
        component.saveBitmap(
            oneTimeSaveLocationUri = uri,
            onComplete = component::parseSaveResult
        )
    }

    val requestSave: (oneTimeSaveLocationUri: String?) -> Unit = requestSave@{ uri ->
        if (component.isSaving) return@requestSave
        if (selectedUris.isEmpty()) return@requestSave
        pendingSaveLocationUri = uri
        pendingSaveToken = System.currentTimeMillis()
        isSavePending = true
        cropTrigger = pendingSaveToken
    }

    // 分辨率提示（按当前语言生成）
    val resolutionTip = run {
        val imageSize = component.originalImageSize
        val targetSize = component.currentSize
        val isSufficient = component.isResolutionSufficient
        when {
            imageSize == null || isSufficient == null -> ""
            isSufficient -> stringResource(
                R.string.id_photo_resolution_sufficient,
                imageSize.first, imageSize.second
            )
            else -> stringResource(
                R.string.id_photo_resolution_insufficient,
                imageSize.first, imageSize.second, targetSize.widthPx, targetSize.heightPx
            )
        }
    }

    // 导出配置 Bottom Sheet
    if (showExportConfig) {
        EnhancedModalTopSheet(
            visible = showExportConfig,
            onDismiss = { showExportConfig = false },
            title = {
                Text(
                    text = stringResource(R.string.id_photo_export_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enableBackHandler = true
        ) {
            IdPhotoExportConfigPanel(
                config = component.exportConfig,
                onConfigChanged = component::updateExportConfig,
                currentBackground = component.currentBackground,
                onBackgroundSelected = component::setBackground,
                resolutionTip = resolutionTip,
                isResolutionSufficient = component.isResolutionSufficient,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 尺寸管理 Bottom Sheet
    if (showSizeCustomizer) {
        EnhancedModalBottomSheet(
            visible = showSizeCustomizer,
            onDismiss = { showSizeCustomizer = false },
            title = {
                Text(
                    text = stringResource(R.string.id_photo_size_template),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            SizeCustomizer(
                sizes = component.allSizes.collectAsState().value,
                onSaveSize = component::saveOrUpdateSize,
                onDeleteSize = component::deleteSize,
                onBatchDeleteSizes = component::batchDeleteSizes,
                onResetPresets = component::resetPresetsToDefaults,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }


    val imageListState = rememberLazyListState()
    val sizeListState = rememberLazyListState()

    // 编辑单个尺寸 Bottom Sheet
    editingSize?.let { size ->
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = { editingSize = null },
            title = {
                Text(
                    text = stringResource(R.string.id_photo_edit_size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            enableBackHandler = true,
            enableBottomContentWeight = false
        ) {
            SizeEditor(
                size = size,
                isNew = false,
                onSave = { updatedSize ->
                    component.saveOrUpdateSize(updatedSize)
                    editingSize = null
                },
                onCancel = { editingSize = null }
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

    // 主界面
    ImageBaseScreen(
        title = stringResource(R.string.id_photo_title),
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
        onSave = { uri -> requestSave(uri) },
        onCancelSaving = component::cancelSaving,
        immersiveModeState = immersiveModeState,
        actions = {
            IconButton(onClick = { showExportConfig = true }) {
                Icon(
                    com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.id_photo_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        customBottomBar = if (selectedUris.isEmpty()) {
            null
        } else {
            {
                // 自定义底部栏
                ImageCollapsibleSaveBar(
                    hasSelectedImages = selectedUris.isNotEmpty(),
                    isSaving = component.isSaving,
                    exportFormat = component.exportConfig.format.title,
                    isExpanded = isTabSectionExpanded,
                    onToggleExpand = { isTabSectionExpanded = !isTabSectionExpanded },
                    onSave = { requestSave(null) },
                    onSaveLongClick = { /* 由 ImageBaseScreen 内部处理 */ },
                    expandableContent = {
                        // Tab 切换区域
                        CollapsibleTabSection(
                            pagerState = pagerState,
                            tabTitles = tabTitles,
                            selectedUris = selectedUris,
                            currentIndex = component.currentIndex,
                            currentSize = component.currentSize,
                            sizes = component.allSizes.collectAsState().value,
                            onImageSelected = component::switchToIndex,
                            onAddClick = { addImageState.pickImage() },
                            onAddLongClick = { addImageState.onShowDialog() },
                            onRemoveClick = component::removeUri,
                            onSizeSelected = component::selectSize,
                            onSizeCustomize = { showSizeCustomizer = true },
                            onEditSize = { size -> editingSize = size },
                            imageListState = imageListState,
                            sizeListState = sizeListState
                        )
                    },
                    trailingContent = null
                )
            }
        },
        previewContent = { safeImageBitmap ->
            // 裁剪预览区
            val previewPadding = rememberPreviewPaddingValues(isTabSectionExpanded)

            IdPhotoCropper(
                bitmap = safeImageBitmap,
                cropProperties = component.cropProperties,
                backgroundColor = component.currentBackground.getColor(),
                onCropped = { croppedBitmap, token ->
                    component.setCroppedBitmap(croppedBitmap)
                    if (isSavePending && token == pendingSaveToken) {
                        isSavePending = false
                        val uri = pendingSaveLocationUri
                        pendingSaveLocationUri = null
                        pendingSaveToken = 0L
                        performSave(uri)
                    }
                },
                cropTrigger = cropTrigger,
                onTap = { immersiveModeState.toggle() },
                modifier = Modifier.fillMaxSize(),
                contentPadding = previewPadding.withHorizontal(16.dp)
            )
        }
    )
}

@Composable
private fun PaddingValues.withHorizontal(horizontal: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = this.calculateLeftPadding(layoutDirection) + horizontal,
        top = this.calculateTopPadding(),
        end = this.calculateRightPadding(layoutDirection) + horizontal,
        bottom = this.calculateBottomPadding()
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
    currentSize: IdPhotoSize,
    sizes: List<IdPhotoSize>,
    onImageSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onAddLongClick: () -> Unit,
    onRemoveClick: (Uri) -> Unit,
    onSizeSelected: (IdPhotoSize) -> Unit,
    onSizeCustomize: () -> Unit,
    onEditSize: (IdPhotoSize) -> Unit = {},
    imageListState: LazyListState,
    sizeListState: LazyListState,
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
        // Tab 标题栏 - 左对齐
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            tabTitles.forEachIndexed { index, title ->
                TabButton(
                    text = title,
                    isSelected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
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
                    // 已选图片列表
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
                    // 尺寸选择
                    SizeSelector(
                        sizes = sizes,
                        selectedSize = currentSize,
                        onSizeSelected = onSizeSelected,
                        onSizeCustomize = onSizeCustomize,
                        onEditSize = onEditSize,
                        listState = sizeListState
                    )
                }
            }
        }
    }
}
