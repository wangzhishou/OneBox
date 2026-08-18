package com.shifenmiao.common.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.FixedHeightButton
import com.shifenmiao.common.R
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDownload
import com.t8rin.imagetoolbox.core.resources.icons.line.LineImage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

/**
 * 图片处理模块的基础屏幕框架
 *
 * ## 模块位置
 * - **模块**: `feature/common`
 * - **包路径**: `com.shifenmiao.common.ui`
 * - **文件**: `ImageBaseScreen.kt`
 *
 * ## 功能概述
 * 提供图片处理模块的通用 UI 结构：
 * - 空状态占位（选择图片）
 * - 图片预览区域
 * - 沉浸式模式支持
 * - 底部保存栏（可扩展）
 * - 通用对话框（加载、退出确认、保存位置选择）
 *
 * ## 组件列表
 * | 组件 | 说明 |
 * |------|------|
 * | `ImageBaseScreen` | 完整的图片处理屏幕框架 |
 * | `ImageEmptyPlaceholder` | 空状态占位组件 |
 * | `ImagePreviewBox` | 图片预览容器 |
 * | `ImageSaveBar` | 底部保存栏 |
 * | `ImageCollapsibleSaveBar` | 可折叠的保存栏 |
 * | `BatchSaveButton` | 批量保存按钮 |
 * | `rememberSafeImageBitmap()` | Bitmap 安全转换 |
 * | `rememberStandardImagePicker()` | 标准图片选择器 |
 *
 * ## 使用方式
 *
 * ### 方式一：使用完整的 ImageBaseScreen
 * ```kotlin
 * @Composable
 * fun MyImageScreen(component: MyImageComponent) {
 *     ImageBaseScreen(
 *         title = "我的图片处理",
 *         onGoBack = { /* 返回 */ },
 *         hasSelectedImages = component.hasSelectedImages,
 *         haveChanges = component.haveChanges,
 *         previewBitmap = component.previewBitmap,
 *         isImageLoading = component.isImageLoading,
 *         isSaving = component.isSaving,
 *         exportProgress = component.exportProgress,
 *         imageFormat = component.imageFormat,
 *         onPickImages = component::setSelectedUris,
 *         onSave = { uri -> component.saveBitmap(uri) { /* 完成 */ } },
 *         onCancelSaving = component::cancelSaving
 *     )
 * }
 * ```
 *
 * ### 方式二：组合使用子组件
 * ```kotlin
 * @Composable
 * fun MyCustomScreen(component: MyImageComponent) {
 *     BaseScreen(title = "自定义屏幕", onGoBack = { }) {
 *         if (!component.hasSelectedImages) {
 *             ImageEmptyPlaceholder(onPickImage = { /* 选择图片 */ })
 *         } else {
 *             // 自定义预览区域
 *             MyCustomPreview(bitmap = component.previewBitmap)
 *         }
 *     }
 *
 *     // 底部使用可折叠保存栏
 *     ImageCollapsibleSaveBar(
 *         hasSelectedImages = component.hasSelectedImages,
 *         isSaving = component.isSaving,
 *         exportProgress = component.exportProgress,
 *         exportFormat = component.imageFormat.title,
 *         isExpanded = isExpanded,
 *         onToggleExpand = { isExpanded = !isExpanded },
 *         onSave = { component.saveBitmap(null) { } },
 *         onSaveLongClick = { /* 选择保存位置 */ },
 *         expandableContent = {
 *             // 自定义展开内容
 *         }
 *     )
 * }
 * ```
 *
 * ## 配套组件
 * @see com.t8rin.imagetoolbox.core.ui.utils.ImageBaseComponent 配套的逻辑组件
 * @see BaseScreen 基础屏幕组件
 * @see ImmersiveModeState 沉浸式模式状态
 */

// ==================== 主屏幕组件 ====================

/**
 * 图片处理基础屏幕
 *
 * @param title 标题
 * @param onGoBack 返回回调
 * @param hasSelectedImages 是否有选中的图片
 * @param haveChanges 是否有未保存的更改
 * @param previewBitmap 预览 Bitmap
 * @param isImageLoading 图片是否正在加载
 * @param isSaving 是否正在保存
 * @param exportProgress 导出进度 (0-100)
 * @param done 已完成数量（用于批量保存进度显示）
 * @param left 剩余数量（用于批量保存进度显示）
 * @param imageFormat 导出格式
 * @param onPickImages 选择图片回调
 * @param onSave 保存回调
 * @param onCancelSaving 取消保存回调
 * @param immersiveModeState 沉浸式模式状态
 * @param actions TopAppBar 操作按钮
 * @param emptyPlaceholderTitle 空状态占位标题（使用默认占位时生效）
 * @param emptyPlaceholder 自定义空状态占位（为 null 时使用默认）
 * @param customBottomBar 自定义底部栏（为 null 时使用默认的 ImageSaveBar）
 * @param previewContent 自定义预览内容（覆盖默认预览）
 */
@Composable
fun ImageBaseScreen(
    title: String,
    onGoBack: () -> Unit,
    hasSelectedImages: Boolean,
    haveChanges: Boolean,
    previewBitmap: Bitmap?,
    isImageLoading: Boolean,
    isSaving: Boolean,
    exportProgress: Int,
    done: Int = 0,
    left: Int = 0,
    imageFormat: ImageFormat,
    onPickImages: (List<Uri>) -> Unit,
    onSave: (oneTimeSaveLocationUri: String?) -> Unit,
    onCancelSaving: () -> Unit,
    modifier: Modifier = Modifier,
    picker: Picker = Picker.Multiple,
    immersiveModeState: ImmersiveModeState = rememberImmersiveModeState(),
    colors: TopAppBarColors = topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.surface,
    ),
    actions: @Composable (RowScope.() -> Unit)? = null,
    emptyPlaceholderTitle: String = stringResource(R.string.common_select_image_to_start),
    emptyPlaceholder: @Composable (BoxScope.() -> Unit)? = null,
    customBottomBar: @Composable (ColumnScope.() -> Unit)? = null,
    previewContent: @Composable (BoxScope.(ImageBitmap?) -> Unit)? = null,
) {
    // 退出确认对话框状态
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // 保存位置选择对话框
    var showFolderSelectionDialog by rememberSaveable { mutableStateOf(false) }

    // 图片来源选择对话框状态
    var showOneTimeImagePickingDialog by rememberSaveable { mutableStateOf(false) }

    // 图片选择器
    val imagePicker = rememberImagePicker(picker) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onPickImages(uris)
        }
    }

    // 返回逻辑
    val onBack: () -> Unit = {
        if (hasSelectedImages && haveChanges) {
            showExitDialog = true
        } else {
            onGoBack()
        }
    }


    // 保存位置选择对话框
    OneTimeSaveLocationSelectionDialog(
        visible = showFolderSelectionDialog,
        onDismiss = { showFolderSelectionDialog = false },
        onSaveRequest = onSave,
        formatForFilenameSelection = imageFormat
    )

    BaseScreen(
        modifier = modifier,
        title = title,
        onGoBack = onBack,
        colors = colors,
        showNavigationBarsPadding = false,
        immersiveModeState = immersiveModeState,
        actions = actions,
        foreground = {
            // 底部操作区域使用沉浸式动画
            // 没有选择图片且 customBottomBar 为 null 时，不显示任何底部栏
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize()
            ) {
                ImmersiveBottomContent(visible = immersiveModeState.isUiVisible) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface
                            )
                            .navigationBarsPadding(),
                    ) {
                        if (customBottomBar != null) {
                            // 使用自定义底部栏
                            customBottomBar()
                        } else if (hasSelectedImages) {
                            // 只有选择了图片时才显示默认底部保存栏
                            ImageSaveBar(
                                hasSelectedImages = hasSelectedImages,
                                isSaving = isSaving,
                                exportFormat = imageFormat.title,
                                onSave = { onSave(null) },
                                onSaveLongClick = { showFolderSelectionDialog = true },
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        },
        background = {
            if (!hasSelectedImages) {
                if (emptyPlaceholder != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        emptyPlaceholder()
                    }
                } else {
                    ImageEmptyPlaceholder(
                        onPickImage = { imagePicker.pickImage() },
                        onLongClick = { showOneTimeImagePickingDialog = true },
                        title = emptyPlaceholderTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                val safeImageBitmap = rememberSafeImageBitmap(previewBitmap)
                if (previewContent != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        previewContent(safeImageBitmap)
                    }
                } else {
                    AnimatedContent(
                        targetState = safeImageBitmap,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "previewAnimation"
                    ) { imageBitmap ->
                        ImagePreviewBox(
                            imageBitmap = imageBitmap,
                            isLoading = isImageLoading,
                            modifier = Modifier.fillMaxSize(),
                            onClick = { immersiveModeState.toggle() }
                        )
                    }
                }
            }
        }
    )

    // 加载对话框（支持进度显示）
    if (left > 0) {
        // 批量保存时显示进度
        LoadingDialog(
            visible = isSaving,
            done = done,
            left = left,
            onCancelLoading = onCancelSaving
        )
    } else {
        // 单张保存或图片加载
        LoadingDialog(
            visible = isSaving || isImageLoading,
            onCancelLoading = onCancelSaving,
            canCancel = isSaving
        )
    }

    // 退出确认对话框
    ExitWithoutSavingDialog(
        onExit = onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    // 图片来源选择对话框
    OneTimeImagePickingDialog(
        onDismiss = { showOneTimeImagePickingDialog = false },
        picker = picker,
        imagePicker = imagePicker,
        visible = showOneTimeImagePickingDialog
    )
}

// ==================== 子组件 ====================

/**
 * 空状态占位组件
 * 支持点击选择图片，长按弹出图片来源选择对话框
 * 点击/长按整个区域都有效，体验更好
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageEmptyPlaceholder(
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    title: String = stringResource(R.string.common_select_image_to_start),
    buttonText: String = stringResource(R.string.common_select_image)
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier.clip(MaterialTheme.shapes.large)
                    .combinedClickable(
                        onClick = onPickImage,
                        onLongClick = onLongClick
                    )
                    .padding(96.dp, 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                GlassTonalButton(
                    onClick = onPickImage,
                    colors = AppTheme.colors.filledTonalButtonColors()
                ) {
                    Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineImage, contentDescription = null)
                    Text(text = buttonText, modifier = Modifier.padding(start = 8.dp))
                }
                // 如果支持长按，显示提示文字
                if (onLongClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.common_more_options),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

    }
}

/**
 * 图片预览容器（支持手势操作）
 * 支持：双指缩放、双击缩放、拖动平移
 */
@Composable
fun ImagePreviewBox(
    imageBitmap: ImageBitmap?,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 使用可缩放的图片预览组件
        ZoomableImagePreview(
            imageBitmap = imageBitmap,
            isLoading = isLoading,
            onTap = onClick,
            modifier = Modifier.fillMaxSize()
        )

        // 自定义内容
        content?.invoke(this)
    }
}

/**
 * 底部保存栏
 *
 * @param saveContent 自定义操作位内容(替换默认保存按钮,如美化的「应用/重置」),为 null 时用默认保存按钮
 */
@Composable
fun ImageSaveBar(
    hasSelectedImages: Boolean,
    isSaving: Boolean,
    exportFormat: String,
    onSave: () -> Unit,
    onSaveLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    saveContent: @Composable (RowScope.() -> Unit)? = null,
) {
    // 格式 + 保存按钮
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppTheme.dimens.paddingNormal,
                vertical = AppTheme.dimens.spaceSmall
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左侧内容
        leadingContent?.invoke(this)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.common_format),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = exportFormat.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (saveContent != null) {
            // 自定义操作位(如美化「应用/重置」),替换默认保存按钮
            saveContent.invoke(this)
        } else {
            FixedHeightButton(
                text = stringResource(R.string.common_save),
                onClick = onSave,
                enabled = hasSelectedImages && !isSaving,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                onLongClick = onSaveLongClick
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent.invoke(this)
        }
    }
}

/**
 * 可折叠的保存栏（带展开/收起功能）
 *
 * @param saveContent 自定义操作位内容(替换默认保存按钮),为 null 时用默认保存按钮
 */
@Composable
fun ImageCollapsibleSaveBar(
    hasSelectedImages: Boolean,
    isSaving: Boolean,
    exportFormat: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSave: () -> Unit,
    onSaveLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandableContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    saveContent: @Composable (RowScope.() -> Unit)? = null,
) {
    // 箭头旋转动画
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 180f,
        label = "arrowRotation"
    )

    Column(modifier = modifier) {
        // 可折叠内容
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
        ) {
            expandableContent?.invoke(this@Column)
        }

        // 保存栏
        ImageSaveBar(
            hasSelectedImages = hasSelectedImages,
            isSaving = isSaving,
            exportFormat = exportFormat,
            onSave = onSave,
            onSaveLongClick = onSaveLongClick,
            leadingContent = {
                // 折叠按钮
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                        contentDescription = if (isExpanded) stringResource(R.string.common_collapse) else stringResource(
                            R.string.common_expand
                        ),
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(arrowRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingContent = trailingContent,
            saveContent = saveContent
        )
    }
}

/**
 * 批量保存按钮
 */
@Composable
fun BatchSaveButton(
    count: Int,
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (count > 1) {
        FixedHeightButton(
            text = stringResource(R.string.common_save_all, count),
            onClick = onClick,
            enabled = !isSaving,
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDownload,
            modifier = modifier
        )
    }
}

// ==================== 工具函数 ====================

/**
 * 安全地管理 Bitmap 到 ImageBitmap 的转换
 * 创建一个独立的副本，避免原始 Bitmap 被 recycle 时导致崩溃
 *
 * @param bitmap 原始 Bitmap，可能随时被 recycle
 * @return 安全的 ImageBitmap 副本，或 null
 */
@Composable
fun rememberSafeImageBitmap(bitmap: Bitmap?): ImageBitmap? {
    return remember(bitmap) {
        if (bitmap == null) return@remember null

        try {
            if (bitmap.isRecycled) return@remember null

            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            copy?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * 计算预览区域的 Padding
 *
 * @param isTabSectionExpanded Tab 区域是否展开
 */
@Composable
fun rememberPreviewPaddingValues(
    isTabSectionExpanded: Boolean
): PaddingValues {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val padding = if (isTabSectionExpanded) 24.dp else 0.dp
    // TopAppBar 高度约 64.dp + 状态栏高度 + 额外留白
    val topPadding = statusBarHeight + TopAppBarDefaults.TopAppBarExpandedHeight + padding
    // 底部操作栏高度 + 导航栏高度 + 额外留白
    // 展开时底部栏约 160.dp，折叠时约 56.dp
    val bottomBarHeight = if (isTabSectionExpanded) 180.dp else 46.dp
    val bottomPadding = navigationBarHeight + bottomBarHeight + padding

    return PaddingValues(
        top = topPadding,
        bottom = bottomPadding
    )
}
