package com.wanbaohe.idphoto.presentation.screen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.ui.button.FixedHeightButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.AddImagePickingDialogWithPicker
import com.shifenmiao.common.ui.ImageBaseScreen
import com.shifenmiao.common.ui.ImageCollapsibleSaveBar
import com.shifenmiao.common.ui.ImagePickerList
import com.shifenmiao.common.ui.TabButton
import com.shifenmiao.common.ui.rememberAddImagePickerState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.shifenmiao.common.ui.rememberPreviewPaddingValues
import com.shifenmiao.model.imageprocess.RetouchParams
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTextButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.BEAUTY_PARAM_CATALOG
import com.wanbaohe.idphoto.domain.BeautyLevel
import com.wanbaohe.idphoto.domain.BeautyParamSpec
import com.wanbaohe.idphoto.domain.IdPhotoBackground
import com.wanbaohe.idphoto.domain.IdPhotoSize
import com.wanbaohe.idphoto.presentation.components.BackgroundPanel
import com.wanbaohe.idphoto.presentation.components.BeautyGroupPanel
import com.wanbaohe.idphoto.presentation.components.BeautyPanel
import com.wanbaohe.idphoto.presentation.components.IdPhotoCropper
import com.wanbaohe.idphoto.presentation.components.IdPhotoExportConfigPanel
import com.wanbaohe.idphoto.presentation.components.IdPhotoExportInfoRow
import com.wanbaohe.idphoto.presentation.components.SizeCustomizer
import com.wanbaohe.idphoto.presentation.components.SizeEditor
import com.wanbaohe.idphoto.presentation.components.SizeSelector
import com.wanbaohe.idphoto.presentation.screenLogic.IdPhotoComponent
import kotlinx.coroutines.launch

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
    val sizes by component.allSizes.collectAsState()

    // Tab 状态:已选图片 / 选择尺寸 / 背景 / 一键美化 / 各美化参数分组
    val pagerState = rememberPagerState(pageCount = { PAGE_GROUP_START + BEAUTY_PARAM_CATALOG.size })
    val tabTitles = listOf(
        stringResource(R.string.id_photo_tab_selected_images, selectedUris.size),
        stringResource(R.string.id_photo_tab_select_size),
        stringResource(R.string.id_photo_background_color),
        stringResource(R.string.id_photo_tab_beauty)
    ) + BEAUTY_PARAM_CATALOG.map { stringResource(it.titleRes) }

    // 选择图片后自动切换到尺寸 Tab(页数较多,直接跳转避免长动画)
    LaunchedEffect(selectedUris.size) {
        if (selectedUris.isNotEmpty() && pagerState.currentPage != PAGE_SIZE) {
            pagerState.scrollToPage(PAGE_SIZE)
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

    // 导出设置确认后:触发裁剪,裁剪完成回调(onCropped)里执行真正保存
    val startCropSave: () -> Unit = {
        pendingSaveToken = System.currentTimeMillis()
        isSavePending = true
        cropTrigger = pendingSaveToken
    }

    // 保存入口:先弹导出设置(参考图片创作),确认后再裁剪导出
    val requestSave: (oneTimeSaveLocationUri: String?) -> Unit = requestSave@{ uri ->
        if (component.isSaving) return@requestSave
        if (selectedUris.isEmpty()) return@requestSave
        pendingSaveLocationUri = uri
        showExportConfig = true
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

    // 导出设置(保存时弹出,参考图片创作):底部弹层,左「取消」右「保存」,确认后开始裁剪导出
    if (showExportConfig) {
        val dismissExportConfig = {
            showExportConfig = false
            pendingSaveLocationUri = null
        }
        EnhancedModalBottomSheet(
            visible = showExportConfig,
            onDismiss = { dismissExportConfig() },
            title = {
                // 底栏 Row 仅自带 end padding,左侧这里补齐,与右侧对称
                CancelButton(
                    text = stringResource(CommonR.string.common_cancel),
                    onClick = dismissExportConfig,
                    modifier = Modifier.padding(start = 16.dp)
                )
            },
            confirmButton = {
                ConfirmButton(
                    text = stringResource(CommonR.string.common_save),
                    onClick = {
                        showExportConfig = false
                        startCropSave()
                    }
                )
            },
            enableBackHandler = true
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.id_photo_export_settings),
                    style = MaterialTheme.typography.titleLarge
                )
                IdPhotoExportInfoRow(
                    targetWidth = component.currentSize.widthPx,
                    targetHeight = component.currentSize.heightPx,
                    sourceSize = component.originalImageSize,
                    estimateBitmap = component.previewBitmap,
                    format = component.exportConfig.format,
                    quality = component.exportConfig.quality
                )
                IdPhotoExportConfigPanel(
                    config = component.exportConfig,
                    onConfigChanged = component::updateExportConfig,
                    resolutionTip = resolutionTip,
                    isResolutionSufficient = component.isResolutionSufficient
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
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
                sizes = sizes,
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
        customBottomBar = if (selectedUris.isEmpty()) {
            null
        } else {
            {
                val isAiProcessing = component.isBeautifying || component.isBgProcessing
                // 草稿与已生效参数不一致(任意 tab 有未应用的修改)时,保存位动画切换为「重置/应用」
                val beautyDirty = component.beautyDraft != component.beautyParams

                // 自定义底部栏
                ImageCollapsibleSaveBar(
                    hasSelectedImages = selectedUris.isNotEmpty(),
                    isSaving = component.isSaving,
                    exportFormat = component.exportConfig.format.title,
                    isExpanded = isTabSectionExpanded,
                    onToggleExpand = { isTabSectionExpanded = !isTabSectionExpanded },
                    onSave = { requestSave(null) },
                    onSaveLongClick = { /* 由 ImageBaseScreen 内部处理 */ },
                    saveContent = {
                        AnimatedContent(
                            targetState = beautyDirty,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(180)) + scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(180)
                                )).togetherWith(
                                    fadeOut(animationSpec = tween(120)) + scaleOut(
                                        targetScale = 0.92f,
                                        animationSpec = tween(120)
                                    )
                                )
                            },
                            label = "saveBarAction"
                        ) { dirty ->
                            if (dirty) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // 「取消」放弃未应用的草稿修改,回到已生效参数(底部栏恢复保存状态)
                                    GlassTextButton(
                                        onClick = component::discardBeautyDraft,
                                        enabled = !isAiProcessing,
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(CommonR.string.common_cancel),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    GlassTonalButton(
                                        onClick = {
                                            val draft = component.beautyDraft
                                            if (draft.isEmpty()) {
                                                // 草稿为空即回到默认:清除已生效的美化
                                                component.restoreBeauty()
                                            } else {
                                                ActionUtils.ensureLoginAndCheckPoints(
                                                    source = AI_POINTS_SOURCE,
                                                    point = aiImageProcessPointsCost()
                                                ) {
                                                    component.applyBeauty(draft)
                                                }
                                            }
                                        },
                                        enabled = !isAiProcessing,
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.id_photo_beauty_apply),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            } else {
                                FixedHeightButton(
                                    text = stringResource(CommonR.string.common_save),
                                    onClick = { requestSave(null) },
                                    enabled = !component.isSaving,
                                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave
                                )
                            }
                        }
                    },
                    expandableContent = {
                        // Tab 切换区域
                        CollapsibleTabSection(
                            pagerState = pagerState,
                            tabTitles = tabTitles,
                            selectedUris = selectedUris,
                            currentIndex = component.currentIndex,
                            currentSize = component.currentSize,
                            sizes = sizes,
                            onImageSelected = component::switchToIndex,
                            onAddClick = { addImageState.pickImage() },
                            onAddLongClick = { addImageState.onShowDialog() },
                            onRemoveClick = component::removeUri,
                            onSizeSelected = component::selectSize,
                            onSizeCustomize = { showSizeCustomizer = true },
                            onEditSize = { size -> editingSize = size },
                            currentBackground = component.currentBackground,
                            onBackgroundSelected = { background ->
                                val applyBackground = {
                                    component.setBackground(background)
                                    // 透明背景导出自动切 PNG,避免 JPG 丢失透明通道
                                    if (background.isTransparent &&
                                        component.exportConfig.format == ImageFormat.Jpg
                                    ) {
                                        component.updateExportConfig(
                                            component.exportConfig.copy(format = ImageFormat.Png.Lossy)
                                        )
                                    }
                                }
                                // 透明/真实颜色且当前基底未抠过图时,先做登录+积分预检再触发 AI 抠图
                                if (component.requiresSegment(background)) {
                                    ActionUtils.ensureLoginAndCheckPoints(
                                        source = AI_POINTS_SOURCE,
                                        point = aiImageProcessPointsCost()
                                    ) {
                                        applyBackground()
                                    }
                                } else {
                                    applyBackground()
                                }
                            },
                            beautyDraft = component.beautyDraft,
                            isAiProcessing = isAiProcessing,
                            onBeautyLevelSelected = { level ->
                                // 档位只写入草稿,由底部操作栏「应用」统一发起(积分在应用时预检)
                                component.setBeautyDraft(level.params)
                            },
                            onBeautyDraftChange = component::setBeautyDraftValue,
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

            // 「原图」「透明」时预览蒙版用中性色;真实底色已合成进预览图
            val cropperBackground = if (component.currentBackground.isOriginal ||
                component.currentBackground.isTransparent
            ) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                component.currentBackground.getColor()
            }

            IdPhotoCropper(
                bitmap = safeImageBitmap,
                cropProperties = component.cropProperties,
                backgroundColor = cropperBackground,
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

    // AI 美化/抠图换底色进行中(可取消)
    LoadingDialog(
        visible = component.isBeautifying || component.isBgProcessing,
        onCancelLoading = component::cancelAiProcessing,
        isForSaving = false
    )
}

/** AI 处理(美化/换底色)积分来源标识 */
private const val AI_POINTS_SOURCE = "id_photo_ai"

private const val PAGE_IMAGES = 0
private const val PAGE_SIZE = 1

/** 背景(原图/透明/纯色)选择页,位于「一键美化」之前 */
private const val PAGE_BACKGROUND = 2
private const val PAGE_BEAUTY = 3

/** 美化参数分组 tab 的起始页码 */
private const val PAGE_GROUP_START = 4

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
    currentBackground: IdPhotoBackground,
    onBackgroundSelected: (IdPhotoBackground) -> Unit,
    beautyDraft: RetouchParams,
    isAiProcessing: Boolean,
    onBeautyLevelSelected: (BeautyLevel) -> Unit,
    onBeautyDraftChange: (BeautyParamSpec, Float?) -> Unit,
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
        // Tab 标题栏 - 左对齐,可横向滚动(tab 较多)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
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
                    },
                    selectedTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    unselectedTextStyle = MaterialTheme.typography.titleSmall
                )
            }
        }

        // 内容高度按页切换:图片/尺寸页为单行列表,一键美化页为档位+提示,分组页为参数卡片行
        val contentHeight by animateDpAsState(
            targetValue = when {
                pagerState.currentPage == PAGE_BEAUTY -> 96.dp
                pagerState.currentPage >= PAGE_GROUP_START -> 116.dp
                else -> 80.dp
            },
            label = "tabContentHeight"
        )

        // 内容区域(Glass 容器)
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.dimens.spaceNormal)
                .height(contentHeight),
            shape = if (pagerState.currentPage == PAGE_IMAGES) {
                MaterialTheme.shapes.medium.copy(
                    topStart = CornerSize(0.dp)
                )
            } else {
                MaterialTheme.shapes.medium
            }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    PAGE_IMAGES -> {
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

                    PAGE_SIZE -> {
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

                    PAGE_BACKGROUND -> {
                        // 背景(原图/透明/纯色)
                        BackgroundPanel(
                            currentBackground = currentBackground,
                            onBackgroundSelected = onBackgroundSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    PAGE_BEAUTY -> {
                        // 一键美化(档位预设,写入草稿)
                        BeautyPanel(
                            beautyDraft = beautyDraft,
                            isProcessing = isAiProcessing,
                            pointsCost = aiImageProcessPointsCost(),
                            onLevelSelected = onBeautyLevelSelected,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        // 美化参数分组(横向滚动,+/- 步进调节草稿)
                        BeautyGroupPanel(
                            group = BEAUTY_PARAM_CATALOG[page - PAGE_GROUP_START],
                            draft = beautyDraft,
                            isProcessing = isAiProcessing,
                            onValueChange = onBeautyDraftChange,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
