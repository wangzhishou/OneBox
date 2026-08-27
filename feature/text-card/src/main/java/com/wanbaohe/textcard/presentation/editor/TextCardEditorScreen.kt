package com.wanbaohe.textcard.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.FreeDraw
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.TextFields
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStickerEmoji
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.editor.EditorRailTool
import com.t8rin.imagetoolbox.core.ui.widget.editor.EditorToolRail
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerToolSheet
import com.t8rin.imagetoolbox.core.ui.widget.editor.VerticalDraggable
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedSliderItem
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.textcard.R
import kotlin.math.roundToInt
import com.wanbaohe.textcard.presentation.editor.panels.BackgroundPanel
import com.wanbaohe.textcard.presentation.editor.panels.LayersPanel
import com.wanbaohe.textcard.presentation.editor.panels.TextStylePanel
import com.wanbaohe.textcard.presentation.screenLogic.EditorPanel
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Tune

/**
 * 编辑首页(设计稿 01):画布预览 + 底部常驻栏(面板 Tab + 保存按钮,
 * 结构对齐图片创作 EditorBottomBar:玻璃容器 + Tab 区窄屏横滑 + 保存固定右端)。
 * Tab 点击后以 [EnhancedModalBottomSheet] 弹出对应面板(标题栏 + 关闭按钮,
 * 模式参考 DemoScreen);文字块就地编辑:点选 → 再点进入编辑态(自动弹键盘),
 * 点空白/返回键/完成键提交退出;元素选中后支持拖动/缩放/旋转(手势在
 * [CardCanvasPreview] 内)。
 */
@Composable
fun TextCardEditorScreen(
    component: TextCardComponent,
) {
    var showDecorationSheet by rememberSaveable { mutableStateOf(false) }
    var showShapeSheet by rememberSaveable { mutableStateOf(false) }
    var showGenerateImageSheet by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // 编辑页返回(顶栏 + 系统返回键/手势):有未保存变更先弹退出确认,选择页不拦截
    val onBack = {
        if (component.haveChanges) {
            showExitDialog = true
        } else {
            component.backToSelection()
        }
    }

    BaseScreen(
        title = stringResource(R.string.textcard_title),
        onGoBack = onBack,
        // 顶栏透明,透出画布背景,与选择画布页观感一致
        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors().copy(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        showNavigationBarsPadding = false,
        content = {
            // 就地编辑态下返回键 = 提交退出编辑(优先于页面返回)
            BackHandler(enabled = component.editingTextBlockId != null) {
                component.endTextEdit()
            }
            // 绘制态下返回键 = 取消绘制
            BackHandler(enabled = component.isDrawing) {
                component.cancelDrawMode()
            }
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                component.renderState()?.let { state ->
                    CardCanvasPreview(
                        state = state,
                        selectedElementId = component.selectedElementId,
                        editingTextBlockId = component.editingTextBlockId,
                        onElementTap = { id ->
                            // 编辑别的块时点其他元素 = 先提交当前编辑
                            val editingId = component.editingTextBlockId
                            if (editingId != null && editingId != id) {
                                component.endTextEdit()
                            }
                            // 再点已选中的文字块 = 进入就地编辑;否则仅选中
                            if (component.editingTextBlockId == null &&
                                component.selectedElementId == id &&
                                component.textBlocks.any { it.id == id }
                            ) {
                                component.beginTextEdit(id)
                            } else {
                                component.selectElement(id)
                            }
                        },
                        onElementTransform = component::setElementTransform,
                        onElementDelete = component::removeElement,
                        onTextBoxResize = component::setTextBlockBounds,
                        onTextChange = { id, text ->
                            component.updateTextBlock(id) { it.copy(content = text) }
                        },
                        onTextEditCommit = component::endTextEdit,
                        onCanvasTap = {
                            // 编辑中点空白 = 提交并退出编辑态
                            component.endTextEdit()
                            component.selectElement(null)
                        },
                        onBackgroundDrag = component::updateBackgroundImageOffset,
                        isDrawing = component.isDrawing,
                        drawSessionStrokes = component.drawStrokes,
                        drawBrushColorArgb = component.drawBrushColor,
                        drawBrushWidthRatio = component.drawBrushWidthRatio,
                        onDrawStroke = component::addSessionStroke,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 「基础」Tab = 左侧浮动工具竖栏开关(图片创作式):
                // 添加文字 / 添加装饰 / AI 生成图片 / 删除选中;可垂直拖动
                val railVisible = component.activePanel == EditorPanel.Basic
                androidx.compose.animation.AnimatedVisibility(
                    visible = railVisible,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    VerticalDraggable(
                        containerHeightPx = constraints.maxHeight.toFloat(),
                        consumeTap = true
                    ) {
                        EditorToolRail(
                            tools = basicRailTools(component),
                            activeId = null,
                            onToolClick = { tool ->
                                when (tool.id) {
                                    RAIL_ADD_TEXT -> component.addTextBlock()
                                    RAIL_ADD_DECORATION -> showDecorationSheet = true
                                    RAIL_ADD_SHAPE -> showShapeSheet = true
                                    RAIL_DRAW -> component.startDrawMode()
                                    RAIL_AI_GENERATE -> showGenerateImageSheet = true
                                    RAIL_DELETE -> component.selectedElementId
                                        ?.let(component::removeElement)
                                }
                            }
                        )
                    }
                }
            }

            // 绘制模式:底部 Tab 栏换成取消/完成操作条(对齐图片创作)
            if (component.isDrawing) {
                DrawModeActionBar(
                    component = component,
                    onCancel = component::cancelDrawMode,
                    onConfirm = component::finishDrawMode
                )
            } else {
                EditorBottomBar(
                    component = component,
                    onSaveClick = component::saveCard
                )
            }
        }
    )

    EditorPanelSheet(component = component)

    // 贴纸共享弹层(与图片创作同款):emoji + assets/stickers 素材,确认落装饰元素
    StickerToolSheet(
        visible = showDecorationSheet,
        onDismiss = { showDecorationSheet = false },
        onStickerClick = component::addStickerDecoration
    )

    ShapePickerSheet(
        visible = showShapeSheet,
        component = component,
        onDismiss = { showShapeSheet = false }
    )

    GenerateImageSheet(
        visible = showGenerateImageSheet,
        component = component,
        onDismiss = { showGenerateImageSheet = false }
    )

    ExitWithoutSavingDialog(
        onExit = { component.backToSelection() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    LoadingDialog(
        visible = component.isSaving,
        onCancelLoading = component::cancelSaving,
        canCancel = true
    )
}

/** 面板底部弹层:标题栏(居中标题 + 关闭按钮) + 对应面板内容。
 * 「基础」Tab 不走弹层(左侧浮动工具竖栏,见编辑页画布区)。 */
@Composable
private fun EditorPanelSheet(
    component: TextCardComponent,
) {
    val activePanel = component.activePanel
    EnhancedModalBottomSheet(
        visible = activePanel != null && activePanel != EditorPanel.Basic,
        dragHandle = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { component.setActivePanel(null) }) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = activePanel?.let { stringResource(it.labelRes()) }.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            )
        },
        onDismiss = { component.setActivePanel(null) },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // 导航栏留白挂在滚动区外侧(同 markup-layers 各 Sheet 的成熟用法):
                    // 滚动视口整体抬到手势条上方,内容滚到任意位置都不会被遮挡
                    .navigationBarsPadding()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when (activePanel) {
                    EditorPanel.Background -> BackgroundPanel(component)
                    EditorPanel.TextStyle -> TextStylePanel(component)
                    EditorPanel.Layers -> LayersPanel(component = component)

                    // Basic 走左侧浮动工具竖栏,不进弹层
                    else -> Unit
                }
            }
        }
    )
}

/**
 * 底部常驻栏(对齐图片创作 EditorBottomBar):玻璃容器(顶部圆角)+
 * Tab 区窄屏横滑/宽屏均分 + 「保存」固定右端。
 */
@Composable
private fun EditorBottomBar(
    component: TextCardComponent,
    onSaveClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val tabs = EditorPanel.entries
            // 每项规定最小宽度:放得下就均分填满,放不下收窄到最小宽度并可横滑
            val itemWidth = maxOf(TAB_ITEM_MIN_WIDTH, maxWidth / tabs.size)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                tabs.forEach { panel ->
                    BottomTab(
                        panel = panel,
                        active = component.activePanel == panel,
                        onClick = { component.togglePanel(panel) },
                        modifier = Modifier.width(itemWidth)
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // 「保存」固定右端:实心主按钮 + 保存图标(同 ConfirmButton 样式,图标换 LineSave)
        PrimaryButton(
            text = stringResource(R.string.textcard_save),
            onClick = onSaveClick,
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.width(16.dp)
                )
            }
        )
    }
}

/** 底部 Tab 单项的最小宽度,窄屏低于该宽度时 Tab 区进入横滑 */
private val TAB_ITEM_MIN_WIDTH = 64.dp

/** Tab 单项:图标 + 小字 + 选中态着色(参考 markup-layers EditorToolItem) */
@Composable
private fun BottomTab(
    panel: EditorPanel,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (active) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (active) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = panel.icon(),
            contentDescription = stringResource(panel.labelRes()),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(panel.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}

private fun EditorPanel.icon(): ImageVector = when (this) {
    EditorPanel.Basic -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentCut
    EditorPanel.Background -> MaterialIcons.Outlined.CropSquare
    EditorPanel.TextStyle -> MaterialIcons.Outlined.Tune
    EditorPanel.Layers -> MaterialIcons.Outlined.Layers
}

private fun EditorPanel.labelRes(): Int = when (this) {
    EditorPanel.Basic -> R.string.textcard_tab_basic
    EditorPanel.Background -> R.string.textcard_tab_background
    EditorPanel.TextStyle -> R.string.textcard_tab_text_style
    EditorPanel.Layers -> R.string.textcard_tab_layers
}

// ---------------- 「基础」侧栏 ----------------

private const val RAIL_ADD_TEXT = "add_text"
private const val RAIL_ADD_DECORATION = "add_decoration"
private const val RAIL_ADD_SHAPE = "add_shape"
private const val RAIL_DRAW = "draw"
private const val RAIL_AI_GENERATE = "ai_generate"
private const val RAIL_DELETE = "delete_selected"

/** 「基础」侧栏项(与图片创作侧栏同款),固定顺序:文字、贴纸、形状、画笔、AI、删除 */
@Composable
private fun basicRailTools(component: TextCardComponent): List<EditorRailTool> = listOf(
    EditorRailTool(
        id = RAIL_ADD_TEXT,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.TextFields,
        label = stringResource(R.string.textcard_add_text)
    ),
    EditorRailTool(
        id = RAIL_ADD_DECORATION,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStickerEmoji,
        label = stringResource(R.string.textcard_add_decoration)
    ),
    EditorRailTool(
        id = RAIL_ADD_SHAPE,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Star,
        label = stringResource(R.string.textcard_add_shape)
    ),
    EditorRailTool(
        id = RAIL_DRAW,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.FreeDraw,
        label = stringResource(R.string.textcard_draw)
    ),
    EditorRailTool(
        id = RAIL_AI_GENERATE,
        icon = MaterialIcons.Outlined.AutoAwesome,
        label = stringResource(R.string.textcard_rail_ai)
    ),
    EditorRailTool(
        id = RAIL_DELETE,
        icon = MaterialIcons.Outlined.Delete,
        label = stringResource(R.string.textcard_delete_selected),
        enabled = component.selectedElementId != null
    )
)

/**
 * 绘制模式底部操作条(对齐图片创作):画笔设置(颜色色板 + 粗细滑杆)
 * + 左「取消」丢弃退出,右「完成」落成画笔图层。
 */
@Composable
private fun DrawModeActionBar(
    component: TextCardComponent,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 画笔设置(复用图片创作画笔的最小选项集:颜色 + 粗细)
        ColorSelectionRow(
            value = Color(component.drawBrushColor),
            onValueChange = { component.updateDrawBrushColor(it.toArgb().toLong() and 0xFFFF_FFFFL) },
            allowAlpha = false
        )
        EnhancedSliderItem(
            value = component.drawBrushWidthRatio,
            title = stringResource(R.string.textcard_draw_width),
            valueRange = 0.004f..0.04f,
            onValueChange = component::updateDrawBrushWidth,
            internalStateTransformation = { (it * 1000).roundToInt() }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CancelButton(
                onClick = onCancel
            )
            Spacer(Modifier.weight(1f))
            ConfirmButton(
                text = stringResource(R.string.textcard_draw_done),
                onClick = onConfirm
            )
        }
    }
}
