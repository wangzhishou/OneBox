package com.shifenmiao.common.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
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
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave

// ==============================
// 1. Tab 数据模型
// ==============================

/**
 * 操作面板中单个 Tab 的定义。
 *
 * @param title   Tab 标签文字，支持含动态信息的字符串（如 "已选图片 (3)"）
 * @param icon    Tab 图标（可选），显示在标签文字上方
 * @param content Tab 内容区域的 Composable。
 *                **建议使用 `fillMaxWidth()` 而非 `fillMaxSize()`**，
 *                以便内容区自适应高度（配合 `panelTabHeight` 作为最大上限）。
 */
data class ImageWorkspaceTab(
    val title: String,
    val icon: ImageVector? = null,
    val content: @Composable () -> Unit,
)

// ==============================
// 2. 状态管理
// ==============================

/**
 * [ImageWorkspace] 工作台的状态持有者。
 *
 * 负责管理：
 * - 操作面板展开 / 折叠（[isPanelExpanded]）
 * - 当前选中 Tab（[selectedTabIndex]）
 * - 编程式 Tab 切换（[selectTab] / [navigateToTab] / [scrollToTab]）
 *
 * 通过 [rememberImageWorkspaceState] 创建，内部状态在配置变更后可恢复。
 *
 * ```kotlin
 * val state = rememberImageWorkspaceState()
 *
 * // 选图后自动跳到"样式"Tab
 * LaunchedEffect(images.size) {
 *     if (images.isNotEmpty()) state.navigateToTab(1)
 * }
 * ```
 */
@Stable
class ImageWorkspaceState(
    isPanelExpandedInitial: Boolean = true,
    selectedTabIndexInitial: Int = 0,
) {
    /** 操作面板是否展开 */
    var isPanelExpanded by mutableStateOf(isPanelExpandedInitial)
        private set

    /** 当前选中的 Tab 索引 */
    var selectedTabIndex by mutableStateOf(selectedTabIndexInitial)
        private set

    /** 当前选中的 Tab 索引（兼容旧 API） */
    val currentTabIndex: Int
        get() = selectedTabIndex

    /** 展开操作面板 */
    fun expandPanel() {
        isPanelExpanded = true
    }

    /** 折叠操作面板 */
    fun collapsePanel() {
        isPanelExpanded = false
    }

    /** 切换操作面板展开/折叠状态 */
    fun togglePanel() {
        isPanelExpanded = !isPanelExpanded
    }

    /**
     * 选中指定 Tab 并展开面板。
     */
    fun selectTab(index: Int) {
        selectedTabIndex = index
        if (!isPanelExpanded) isPanelExpanded = true
    }

    /**
     * 平滑切换到指定 Tab（兼容旧 API，内部直接调用 [selectTab]）。
     *
     * ```kotlin
     * LaunchedEffect(images.size) {
     *     if (images.isNotEmpty()) state.navigateToTab(1)
     * }
     * ```
     */
    suspend fun navigateToTab(index: Int) {
        selectTab(index)
    }

    /**
     * 立即跳转到指定 Tab（兼容旧 API，内部直接调用 [selectTab]）。
     */
    suspend fun scrollToTab(index: Int) {
        selectTab(index)
    }

    companion object {
        /** 用于 [rememberSaveable] 的 [Saver] */
        val Saver: Saver<ImageWorkspaceState, String> = Saver(
            save = { "${it.isPanelExpanded},${it.selectedTabIndex}" },
            restore = {
                val parts = it.split(",")
                ImageWorkspaceState(
                    isPanelExpandedInitial = parts.getOrNull(0)?.toBooleanStrictOrNull() ?: true,
                    selectedTabIndexInitial = parts.getOrNull(1)?.toIntOrNull() ?: 0,
                )
            },
        )
    }
}

/**
 * 创建并记住 [ImageWorkspaceState]，在配置变更（如屏幕旋转）后自动恢复。
 *
 * @param isPanelExpandedInitial 操作面板的初始展开状态，默认展开
 */
@Composable
fun rememberImageWorkspaceState(
    isPanelExpandedInitial: Boolean = true,
): ImageWorkspaceState = rememberSaveable(saver = ImageWorkspaceState.Saver) {
    ImageWorkspaceState(isPanelExpandedInitial)
}

// ==============================
// 3. 主屏幕 Composable
// ==============================

/**
 * 图片操作工作台 —— 新一代图片操作基础框架。
 *
 * 底部操作区采用毛玻璃风格：
 * - **Tab 内容区**：点击 Tab 后以动画展开，超过最大高度可滚动，使用 [GlassSurface] 样式
 * - **Tab 选择 + 保存栏**：Tab 按钮与保存按钮在同一个 [GlassSurface] 色块中
 *
 * ## 基本用法
 * ```kotlin
 * @Composable
 * fun MyImageScreen(component: MyComponent, appComponent: AppComponent) {
 *     val state = rememberImageWorkspaceState()
 *     val immersiveMode = rememberImmersiveModeState()
 *     val images by component.images.collectAsState()
 *
 *     // 选图后自动切到"样式"Tab
 *     LaunchedEffect(images.size) {
 *         if (images.isNotEmpty()) state.navigateToTab(1)
 *     }
 *
 *     ImageWorkspace(
 *         title = "我的功能",
 *         onGoBack = appComponent::onGoBack,
 *         state = state,
 *         immersiveModeState = immersiveMode,
 *         hasSelectedImages = images.isNotEmpty(),
 *         haveChanges = component.haveChanges,
 *         isSaving = component.isSaving,
 *         imageFormat = component.imageFormat,
 *         onPickImages = component::setImages,
 *         onSave = component::save,
 *         onCancelSaving = component::cancelSaving,
 *         topBarActions = {
 *             IconButton(onClick = { showSettings = true }) {
 *                 Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings, null)
 *             }
 *         },
 *         canvas = { contentPadding ->
 *             MyCanvas(
 *                 bitmap = component.bitmap,
 *                 modifier = Modifier.fillMaxSize(),
 *                 contentPadding = contentPadding,
 *             )
 *         },
 *         operationTabs = listOf(
 *             ImageWorkspaceTab("已选图片 (${images.size})") {
 *                 ImagePickerList(selectedUris = images, ...)
 *             },
 *             ImageWorkspaceTab("样式") {
 *                 MyStylePanel(...)
 *             },
 *         ),
 *     )
 * }
 * ```
 *
 * @param title 页面标题
 * @param onGoBack 返回回调（内部会判断是否有未保存更改再决定是否弹确认框）
 * @param hasSelectedImages 是否已选择了图片
 * @param haveChanges 是否有未保存的更改（影响退出时是否弹提示）
 * @param isSaving 是否正在保存（影响保存按钮和加载遮罩）
 * @param isImageLoading 图片是否加载中（影响加载遮罩）
 * @param exportProgress 导出进度 0–100（当前保留供扩展，暂未使用）
 * @param done 批量保存时已完成数量（>0 时显示批量进度弹窗）
 * @param left 批量保存时剩余数量
 * @param imageFormat 导出格式（用于保存位置选择对话框和保存栏文字）
 * @param onPickImages 用户通过空状态占位选图的回调
 * @param onSave 保存回调，参数为自定义保存路径 URI（null 使用默认路径）
 * @param onCancelSaving 取消保存回调
 * @param modifier 整体 Modifier
 * @param state 工作台状态，使用 [rememberImageWorkspaceState] 创建
 * @param immersiveModeState 沉浸式模式状态，使用 [rememberImmersiveModeState] 创建
 * @param picker 图片选择器模式（单选 / 多选），默认多选
 * @param topBarColors TopBar 配色方案
 * @param topBarActions TopBar 右侧图标按钮槽位
 * @param emptyPlaceholderTitle 空状态占位标题（使用内建占位时生效）
 * @param emptyPlaceholder 自定义空状态占位（null 时使用内建引导 UI）
 * @param canvas 画布 / 预览主区域；BoxScope 接收者可用于叠加内容，
 *   [PaddingValues] 参数表示应避开的顶底栏空间，确保核心内容不被遮挡
 * @param operationTabs 操作面板 Tab 列表（空列表时底部仅显示简单保存栏）
 * @param bottomBar 完全自定义的底部栏（非 null 时完全替换内建保存栏 + 操作面板）
 * @param saveBarLeadingContent 内建保存栏"折叠箭头"之后的左侧附加内容槽位
 * @param saveBarTrailingContent 内建保存栏"保存按钮"之后的右侧附加内容槽位
 * @param panelTabHeight 操作面板 Tab 内容区高度（不含 Tab 标题行），默认 80.dp
 * @param onTabChanged 切换 Tab 时的回调，参数为新 Tab 索引
 *
 * @see ImageWorkspaceState
 * @see ImageWorkspaceTab
 * @see rememberImageWorkspaceContentPadding
 */
@Composable
fun ImageWorkspace(
    title: String,
    onGoBack: () -> Unit,
    hasSelectedImages: Boolean,
    haveChanges: Boolean,
    isSaving: Boolean,
    isImageLoading: Boolean = false,
    exportProgress: Int = 0,
    done: Int = 0,
    left: Int = 0,
    imageFormat: ImageFormat,
    onPickImages: (List<Uri>) -> Unit,
    onSave: (oneTimeSaveLocationUri: String?) -> Unit,
    onCancelSaving: () -> Unit,
    modifier: Modifier = Modifier,
    state: ImageWorkspaceState = rememberImageWorkspaceState(),
    immersiveModeState: ImmersiveModeState = rememberImmersiveModeState(),
    picker: Picker = Picker.Multiple,
    topBarColors: TopAppBarColors = topAppBarColors().copy(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
    topBarActions: (@Composable RowScope.() -> Unit)? = null,
    emptyPlaceholderTitle: String = stringResource(R.string.common_select_image_to_start),
    emptyPlaceholder: (@Composable BoxScope.() -> Unit)? = null,
    canvas: @Composable BoxScope.(contentPadding: PaddingValues) -> Unit = {},
    operationTabs: List<ImageWorkspaceTab> = emptyList(),
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    saveBarLeadingContent: (@Composable RowScope.() -> Unit)? = null,
    saveBarTrailingContent: (@Composable RowScope.() -> Unit)? = null,
    panelTabHeight: Dp = 80.dp,
    onTabChanged: ((Int) -> Unit)? = null,
) {
    // ---- 对话框可见状态 ----
    var showFolderSelectionDialog by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showOneTimeImagePickingDialog by rememberSaveable { mutableStateOf(false) }

    // ---- 图片选择器 ----
    val imagePicker = rememberImagePicker(picker) { uris: List<Uri> ->
        if (uris.isNotEmpty()) onPickImages(uris)
    }

    // ---- 返回逻辑（有未保存更改时弹确认框）----
    val onBack: () -> Unit = {
        if (hasSelectedImages && haveChanges) {
            showExitDialog = true
        } else {
            onGoBack()
        }
    }

    // ---- Tab 管理（不再使用 PagerState）----
    val hasTabs = operationTabs.isNotEmpty()
    val showBuiltinPanel = hasSelectedImages && hasTabs && bottomBar == null

    // Tab 切换回调
    if (hasTabs && onTabChanged != null) {
        LaunchedEffect(state.selectedTabIndex) {
            onTabChanged(state.selectedTabIndex)
        }
    }

    // ---- 为 canvas 计算内容 padding ----
    val contentPadding = rememberImageWorkspaceContentPadding(
        isPanelExpanded = state.isPanelExpanded,
        hasOperationPanel = showBuiltinPanel,
        panelTabHeight = panelTabHeight,
    )

    // ==================== 对话框 ====================

    OneTimeSaveLocationSelectionDialog(
        visible = showFolderSelectionDialog,
        onDismiss = { showFolderSelectionDialog = false },
        onSaveRequest = onSave,
        formatForFilenameSelection = imageFormat,
    )

    ExitWithoutSavingDialog(
        onExit = onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
    )

    OneTimeImagePickingDialog(
        onDismiss = { showOneTimeImagePickingDialog = false },
        picker = picker,
        imagePicker = imagePicker,
        visible = showOneTimeImagePickingDialog,
    )

    if (left > 0) {
        LoadingDialog(
            visible = isSaving,
            done = done,
            left = left,
            onCancelLoading = onCancelSaving,
        )
    } else {
        LoadingDialog(
            visible = isSaving || isImageLoading,
            onCancelLoading = onCancelSaving,
            canCancel = isSaving,
        )
    }

    // ==================== 主体布局 ====================

    BaseScreen(
        modifier = modifier,
        title = title,
        onGoBack = onBack,
        colors = topBarColors,
        showNavigationBarsPadding = false,
        immersiveModeState = immersiveModeState,
        actions = topBarActions,
        // 背景层：画布区域 or 空状态占位
        background = {
            if (!hasSelectedImages) {
                Box(modifier = Modifier.fillMaxSize()) {
                    emptyPlaceholder?.invoke(this) ?: ImageEmptyPlaceholder(
                        onPickImage = { imagePicker.pickImage() },
                        onLongClick = { showOneTimeImagePickingDialog = true },
                        title = emptyPlaceholderTitle,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    canvas(this, contentPadding)
                }
            }
        },
        // 前景层：底部操作区（浮于画布之上，支持沉浸式动画）
        foreground = {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxSize(),
            ) {
                ImmersiveBottomContent(visible = immersiveModeState.isUiVisible) {
                    // Fix#1: 消费点击事件，防止穿透到画布触发沉浸式切换
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { /* 消费点击，不传递到下层 */ }
                            )
                            .navigationBarsPadding()
                            .padding(bottom = AppTheme.dimens.spaceSmall),
                    ) {
                        when {
                            // 优先使用调用方完全自定义的底部栏
                            bottomBar != null -> {
                                bottomBar()
                            }

                            // 有操作面板 Tab：显示可折叠内容区 + 玻璃风格 Tab 栏 + 保存栏
                            showBuiltinPanel -> {
                                // ── Tab 内容区（上方）：动画展开/折叠 ──
                                AnimatedVisibility(
                                    visible = state.isPanelExpanded,
                                    enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                                ) {
                                    // Fix#3: wrapContentHeight 使面板自适应内容高度，
                                    //        heightIn(max=) 限制最大高度
                                    GlassSurface(
                                        style = GlassStyle.Medium,
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = AppTheme.dimens.paddingNormal)
                                            .wrapContentHeight()
                                            .heightIn(max = panelTabHeight),
                                    ) {
                                        Crossfade(
                                            targetState = state.selectedTabIndex,
                                            label = "tabContentCrossfade",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight(),
                                        ) { targetIndex ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .padding(AppTheme.dimens.spaceNormal),
                                            ) {
                                                operationTabs.getOrNull(targetIndex)
                                                    ?.content
                                                    ?.invoke()
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // ── Tab 选择 + 保存栏（下方）：同一个玻璃色块 ──
                                GlassSurface(
                                    style = GlassStyle.Medium,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AppTheme.dimens.paddingNormal),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                horizontal = AppTheme.dimens.spaceSmall,
                                                vertical = AppTheme.dimens.spaceSmall,
                                            ),
                                    ) {
                                        // ── 第一行：Tab 按钮（可横向滚动，选中自动居中）──
                                        val tabScrollState = rememberScrollState()
                                        val tabOffsets = remember {
                                            mutableMapOf<Int, Pair<Int, Int>>()
                                        }

                                        // 选中 Tab 后自动平滑滚动至视口居中位置
                                        LaunchedEffect(state.selectedTabIndex) {
                                            val (offset, width) =
                                                tabOffsets[state.selectedTabIndex]
                                                    ?: return@LaunchedEffect
                                            val viewport = tabScrollState.viewportSize
                                            if (viewport > 0) {
                                                val target =
                                                    offset - (viewport - width) / 2
                                                tabScrollState.animateScrollTo(
                                                    target.coerceIn(
                                                        0,
                                                        tabScrollState.maxValue
                                                    )
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(tabScrollState),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            operationTabs.forEachIndexed { index, tab ->
                                                GlassTabButton(
                                                    text = tab.title,
                                                    icon = tab.icon,
                                                    // 面板折叠时不显示选中态
                                                    isSelected = state.isPanelExpanded
                                                            && state.selectedTabIndex == index,
                                                    onClick = {
                                                        if (state.selectedTabIndex == index) {
                                                            state.togglePanel()
                                                        } else {
                                                            state.selectTab(index)
                                                        }
                                                    },
                                                    modifier = Modifier.onGloballyPositioned { coordinates ->
                                                        tabOffsets[index] = Pair(
                                                            coordinates.positionInParent().x.roundToInt(),
                                                            coordinates.size.width,
                                                        )
                                                    },
                                                )
                                                if (index < operationTabs.lastIndex) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // ── 第二行：保存栏（含折叠箭头）──
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // 折叠/展开箭头
                                            val arrowRotation by animateFloatAsState(
                                                targetValue = if (state.isPanelExpanded) 0f else 180f,
                                                label = "panelArrowRotation",
                                            )
                                            IconButton(
                                                onClick = { state.togglePanel() },
                                                modifier = Modifier.size(32.dp),
                                            ) {
                                                Icon(
                                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKeyboardArrowDown,
                                                    contentDescription = if (state.isPanelExpanded) {
                                                        stringResource(R.string.common_collapse)
                                                    } else {
                                                        stringResource(R.string.common_expand)
                                                    },
                                                    modifier = Modifier.rotate(arrowRotation),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }

                                            // 调用方附加的左侧内容
                                            saveBarLeadingContent?.invoke(this)

                                            Spacer(modifier = Modifier.weight(1f))

                                            Text(
                                                text = stringResource(R.string.common_format),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = imageFormat.title.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))

                                            FixedHeightButton(
                                                text = stringResource(R.string.common_save),
                                                onClick = { onSave(null) },
                                                enabled = hasSelectedImages && !isSaving,
                                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                                                onLongClick = { showFolderSelectionDialog = true },
                                            )

                                            if (saveBarTrailingContent != null) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                saveBarTrailingContent.invoke(this)
                                            }
                                        }
                                    }
                                }
                            }

                            // 已选图片但无操作面板：简单保存栏（玻璃风格）
                            hasSelectedImages -> {
                                GlassSurface(
                                    style = GlassStyle.Medium,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = AppTheme.dimens.paddingNormal),
                                ) {
                                    ImageSaveBar(
                                        hasSelectedImages = hasSelectedImages,
                                        isSaving = isSaving,
                                        exportFormat = imageFormat.title,
                                        onSave = { onSave(null) },
                                        onSaveLongClick = { showFolderSelectionDialog = true },
                                        leadingContent = saveBarLeadingContent,
                                        trailingContent = saveBarTrailingContent,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}

// ==============================
// 4. 子组件
// ==============================

/**
 * 玻璃风格 Tab 按钮 —— 选中时显示毛玻璃背景，未选中时透明。
 * 支持图标 + 文字的上下排列布局（参考 STYLE / COLOR / SHAPE / LOGO 风格）。
 *
 * @param text       Tab 标签文字
 * @param isSelected 是否选中
 * @param onClick    点击回调
 * @param modifier   可选 Modifier
 * @param icon       可选图标，显示在文字上方
 */
@Composable
private fun GlassTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier.glassBackground(
                        style = GlassStyle.Medium,
                        shape = shape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = text,
                style = if (isSelected) {
                    MaterialTheme.typography.titleSmall
                } else {
                    MaterialTheme.typography.titleSmall
                },
                color = contentColor,
            )
        }
    }
}

// ==============================
// 5. 工具函数
// ==============================

/**
 * 计算 [ImageWorkspace] 画布区域的内容 [PaddingValues]。
 *
 * 确保画布核心内容（如裁剪框、预览图片）不被顶部 TopBar 和底部操作栏遮挡。
 *
 * 计算规则：
 * - **顶部**：状态栏 + TopAppBar 高度 + 额外边距（24.dp）
 * - **底部（无操作面板）**：导航栏 + 玻璃保存栏 + 额外边距
 * - **底部（面板折叠）**：同上
 * - **底部（面板展开）**：导航栏 + 玻璃保存栏 + 间距 + 内容区高度 + 额外边距
 *
 * @param isPanelExpanded 操作面板当前是否处于展开状态
 * @param hasOperationPanel 是否有操作面板（false 时只有保存栏）
 * @param panelTabHeight Tab 内容区最大高度
 */
@Composable
fun rememberImageWorkspaceContentPadding(
    isPanelExpanded: Boolean,
    hasOperationPanel: Boolean,
    panelTabHeight: Dp = 80.dp,
): PaddingValues {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // TopBar 高度 + 状态栏 + 上方额外留白
    val topPadding = statusBarHeight + TopAppBarDefaults.TopAppBarExpandedHeight + 24.dp

    // 底部高度估算：
    //   玻璃底栏(两行):
    //     Tab行:   button(32dp) ≈ 32dp
    //     间距:    6dp
    //     保存行:  button(32dp) ≈ 32dp
    //     上下padding: spaceSmall(≈8dp)*2 = 16dp
    //     合计: 32 + 6 + 32 + 16 ≈ 86dp
    //   内容区与栏间距:    8dp
    //   内容区:           panelTabHeight + padding(spaceNormal≈11dp)*2 ≈ panelTabHeight + 22dp
    val glassBarHeight = 86.dp
    val gapHeight = 8.dp
    val contentPadding = 22.dp

    val bottomBarHeight = when {
        !hasOperationPanel -> glassBarHeight
        isPanelExpanded -> glassBarHeight + gapHeight + panelTabHeight + contentPadding
        else -> glassBarHeight
    }

    return PaddingValues(
        top = topPadding,
        bottom = navigationBarHeight + bottomBarHeight + 24.dp,
    )
}
