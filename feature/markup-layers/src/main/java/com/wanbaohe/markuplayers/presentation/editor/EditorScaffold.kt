package com.wanbaohe.markuplayers.presentation.editor

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveModeState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters.AddFiltersSheet
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.BackgroundColor
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.Dots
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomOut
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.tappable
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.presentation.components.EditBox
import com.wanbaohe.markuplayers.presentation.components.LayersFloatingPanel
import com.wanbaohe.markuplayers.presentation.components.LayersSheet
import com.wanbaohe.markuplayers.presentation.components.TextEditDialog
import com.wanbaohe.markuplayers.presentation.draw.BrushSettingsDialog
import com.wanbaohe.markuplayers.presentation.draw.DrawFloatingBar
import com.wanbaohe.markuplayers.presentation.draw.DrawOverlay
import com.wanbaohe.markuplayers.presentation.draw.DrawSessionState
import com.wanbaohe.markuplayers.presentation.export.ExportSettingsSheet
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.presentation.tools.EditorTool
import com.wanbaohe.markuplayers.presentation.tools.EditorTools
import com.wanbaohe.markuplayers.presentation.tools.adjust.AdjustToolSheet
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import com.wanbaohe.markuplayers.presentation.tools.ai.AiToolSheet
import com.wanbaohe.markuplayers.presentation.tools.crop.CropToolScreen
import com.wanbaohe.markuplayers.presentation.tools.shape.ShapeToolSheet
import com.wanbaohe.markuplayers.presentation.tools.sticker.StickerToolSheet
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * 主编辑界面(设计稿「图片创作」):基于 BaseScreen,背景槽为画布背景
 * (默认主题 surface 色,可切换纯色;底图自身透明区域由图片下的棋盘格标识),
 * 顶栏(返回/撤销/重做/更多菜单) + 画布区 + 底部主 Tab 栏与保存按钮。
 *
 * 左侧工具栏与右侧图层面板均为浮动卡片,可垂直拖动;底部「基础工具」Tab
 * 切换左侧工具栏显隐,「图层」Tab 切换浮动图层面板显隐(完整图层 Sheet 由面板内展开)。
 * 更多菜单可进沉浸预览,沉浸下单击画布或返回键退出。
 *
 * 文字与画笔均为画布内模式,不跳全屏页:文字经 [TextEditDialog] 编辑(会话式历史),
 * 画笔进入画布内绘制模式(画布右侧浮动竖条承载模式切换/撤销重做,
 * 画笔参数收进 [BrushSettingsDialog],底部 Tab 栏换成取消/完成操作条);
 * 裁剪是唯一保留的全屏工具页。
 */
@Composable
fun EditorScaffold(
    component: MarkupLayersComponent
) {
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var showStickerSheet by rememberSaveable { mutableStateOf(false) }
    var showLayersSheet by rememberSaveable { mutableStateOf(false) }
    var showExportSheet by rememberSaveable { mutableStateOf(false) }
    var showShapeSheet by rememberSaveable { mutableStateOf(false) }
    var showAiSheet by rememberSaveable { mutableStateOf(false) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showAdjustSheet by rememberSaveable { mutableStateOf(false) }
    var showCanvasBackgroundSheet by rememberSaveable { mutableStateOf(false) }
    // 左侧工具栏 / 右侧浮动图层面板显隐,由底部「基础工具」「图层」Tab 切换
    var sideBarVisible by rememberSaveable { mutableStateOf(true) }
    var layersPanelVisible by rememberSaveable { mutableStateOf(true) }
    // 「下一个新形状」的默认样式,由形状面板维护,关闭面板后保留
    var shapeDefaultSpec by remember { mutableStateOf(ShapeSpec.default(ShapeKind.Rectangle)) }
    val immersiveModeState = rememberImmersiveModeState()

    val onToolClick: (EditorTool) -> Unit = { tool ->
        when (tool.id) {
            // 「基础工具」Tab = 左侧工具栏显隐开关;「图层」Tab = 浮动图层面板显隐开关
            EditorTools.ID_BASIC -> sideBarVisible = !sideBarVisible
            EditorTools.ID_LAYERS -> layersPanelVisible = !layersPanelVisible
            else -> {
                component.setActiveTool(tool.id)
                when (tool.id) {
                    EditorTools.ID_SELECT -> component.selectLayer(null)
                    // 文字为画布内联工具:中心新增文字图层并选中,同时弹出编辑 Dialog
                    EditorTools.ID_TEXT -> component.beginTextEditSession()
                    // 画笔为画布内绘制模式:取消图层选中,避免选中框手势与绘制冲突
                    EditorTools.ID_DRAW -> component.selectLayer(null)
                    EditorTools.ID_STICKER -> showStickerSheet = true
                    EditorTools.ID_SHAPE -> showShapeSheet = true
                    EditorTools.ID_AI -> showAiSheet = true
                    EditorTools.ID_FILTER -> showFilterSheet = true
                    EditorTools.ID_ADJUST -> showAdjustSheet = true
                    // FullScreen 工具(裁剪)由 activeToolId 驱动,下方直接切换全屏页
                }
            }
        }
    }

    val imageLayerPicker = rememberImagePicker { uri: Uri ->
        component.addLayer(
            MarkupLayer(type = LayerType.Image(imageData = uri))
        )
    }

    // 画笔画布内绘制模式:会话随模式进出创建/销毁,完成时才落成 Draw 图层
    val isDrawMode = component.activeToolId == EditorTools.ID_DRAW
    val drawSession = remember(isDrawMode) { if (isDrawMode) DrawSessionState() else null }
    // 画笔参数 Dialog 开关,随绘制模式进出复位
    var showBrushSettings by remember(isDrawMode) { mutableStateOf(false) }
    val onDrawConfirm: () -> Unit = {
        drawSession?.let { session ->
            if (session.strokes.isNotEmpty()) {
                component.addLayer(
                    MarkupLayer(type = LayerType.Draw(strokes = session.strokes))
                )
            }
        }
        component.setActiveTool(null)
    }
    val onDrawCancel: () -> Unit = { component.setActiveTool(null) }

    // 返回键由 BaseScreen 处理(沉浸态先退沉浸):绘制模式下返回 = 取消绘制,否则按原逻辑退出
    val onBack = {
        when {
            isDrawMode -> onDrawCancel()
            component.haveChanges -> showExitDialog = true
            else -> component.resetState()
        }
    }

    // 全屏工具页(仅裁剪):覆盖主编辑界面,关闭时由工具页自行 setActiveTool(null)
    val fullScreenTool = component.activeToolId
        ?.let(EditorTools::byId)
        ?.takeIf { it.mode == EditorTool.Mode.FullScreen }
    when (fullScreenTool?.id) {
        EditorTools.ID_CROP -> {
            CropToolScreen(component = component)
            return
        }
    }

    // 滤镜面板开关同步给组件:打开期间画布切换为合成预览(底图+图层合成后过滤镜)
    LaunchedEffect(showFilterSheet) {
        component.setFilterSheetOpen(showFilterSheet)
    }

    BaseScreen(
        title = stringResource(R.string.markup_editor_title),
        onGoBack = onBack,
        background = {
            // 操作台背景:默认透出主题 surface 底色,可切换纯色(画布背景设置)
            when (val bg = component.canvasBackground) {
                CanvasBackground.Default -> Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                )

                is CanvasBackground.Solid -> Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(bg.color))
                )
            }
        },
        navigationIcon = {
            EnhancedIconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.markup_back)
                )
            }
        },
        actions = {
            EditorTopBarActions(
                component = component,
                immersiveModeState = immersiveModeState,
                onOpenCanvasBackground = { showCanvasBackgroundSheet = true }
            )
        },
        // 底部 Tab 栏自带 navigationBarsPadding,避免叠加
        showNavigationBarsPadding = false,
        // 顶栏不跟随全局玻璃效果,保持不透明 surface 底色,避免透出画布背景
        supportGlassEffect = false,
        immersiveModeState = immersiveModeState,
        content = {
            EditorCanvas(
                component = component,
                immersiveModeState = immersiveModeState,
                drawSession = drawSession,
                sideBarVisible = sideBarVisible,
                layersPanelVisible = layersPanelVisible,
                onEditTextLayer = { layerId ->
                    component.beginTextEditSession(layerId)
                    component.setActiveTool(EditorTools.ID_TEXT)
                },
                onToolClick = onToolClick,
                onOpenBrushSettings = { showBrushSettings = true },
                onAddImageLayer = { imageLayerPicker.pickImage() },
                onExpandLayers = { showLayersSheet = true },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            AnimatedVisibility(visible = immersiveModeState.isUiVisible) {
                if (drawSession != null) {
                    // 绘制模式:底部 Tab 栏换成取消/完成操作条,画笔操作由画布右侧浮动竖条承载
                    DrawModeActionBar(
                        onCancel = onDrawCancel,
                        onConfirm = onDrawConfirm
                    )
                } else {
                    EditorBottomBar(
                        component = component,
                        sideBarVisible = sideBarVisible,
                        layersPanelVisible = layersPanelVisible,
                        onToolClick = onToolClick,
                        onSaveClick = { showExportSheet = true }
                    )
                }
            }
        }
    )

    StickerToolSheet(
        visible = showStickerSheet,
        component = component,
        onDismiss = { showStickerSheet = false }
    )

    LayersSheet(
        visible = showLayersSheet,
        component = component,
        onDismiss = { showLayersSheet = false },
        onAddText = {
            showLayersSheet = false
            component.beginTextEditSession()
            component.setActiveTool(EditorTools.ID_TEXT)
        },
        onAddSticker = {
            showLayersSheet = false
            showStickerSheet = true
        },
        onAddImage = {
            showLayersSheet = false
            imageLayerPicker.pickImage()
        }
    )

    ShapeToolSheet(
        visible = showShapeSheet,
        component = component,
        defaultSpec = shapeDefaultSpec,
        onDefaultSpecChange = { shapeDefaultSpec = it },
        onDismiss = { showShapeSheet = false }
    )

    AdjustToolSheet(
        visible = showAdjustSheet,
        component = component,
        onDismiss = { showAdjustSheet = false }
    )

    AddFiltersSheet(
        component = component.addFiltersSheetComponent,
        filterTemplateCreationSheetComponent = component.filterTemplateCreationSheetComponent,
        visible = showFilterSheet,
        onDismiss = { showFilterSheet = false },
        previewBitmap = component.displayBitmap,
        onFilterPicked = component::selectFilter,
        onFilterPickedWithParams = { filter ->
            // TODO: 带参数滤镜暂未做参数编辑页,本期按默认参数直接应用
            component.selectFilter(filter)
        },
        canAddTemplates = false
    )

    AiToolSheet(
        visible = showAiSheet,
        onDismiss = { showAiSheet = false }
    )

    CanvasBackgroundSheet(
        visible = showCanvasBackgroundSheet,
        background = component.canvasBackground,
        onBackgroundChange = component::setCanvasBackground,
        onDismiss = { showCanvasBackgroundSheet = false }
    )

    ExportSettingsSheet(
        visible = showExportSheet,
        settings = component.exportSettings,
        sourceSize = component.sourceSize,
        onDismiss = { showExportSheet = false },
        onSettingsChange = component::updateExportSettings,
        onSave = {
            showExportSheet = false
            component.saveBitmap(
                oneTimeSaveLocationUri = null,
                onComplete = component::parseSaveResult
            )
        }
    )

    // 文字编辑 Dialog:由 Component 的文字编辑会话驱动(新建/点按已选中文字图层)
    TextEditDialog(
        component = component,
        onConfirm = {
            component.commitLayerEditSession()
            component.setActiveTool(null)
        },
        onCancel = {
            component.cancelLayerEditSession()
            component.setActiveTool(null)
        }
    )

    // 画笔参数 Dialog:即改即生效,取消/确认均只关窗
    drawSession?.let { session ->
        BrushSettingsDialog(
            visible = showBrushSettings,
            session = session,
            onDismiss = { showBrushSettings = false }
        )
    }

    ExitWithoutSavingDialog(
        onExit = { component.resetState() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}

@Composable
private fun EditorTopBarActions(
    component: MarkupLayersComponent,
    immersiveModeState: ImmersiveModeState,
    onOpenCanvasBackground: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    EnhancedIconButton(
        onClick = component::undo,
        enabled = component.canUndo
    ) {
        Icon(
            imageVector = Icons.Outlined.LineUndo,
            contentDescription = stringResource(R.string.markup_undo)
        )
    }
    EnhancedIconButton(
        onClick = component::redo,
        enabled = component.canRedo
    ) {
        Icon(
            imageVector = Icons.Outlined.LineRedo,
            contentDescription = stringResource(R.string.markup_redo)
        )
    }
    Box {
        EnhancedIconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Rounded.Dots,
                contentDescription = stringResource(R.string.markup_more_options)
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_immersive_preview)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Fullscreen, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    immersiveModeState.enterImmersive()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_canvas_background)) },
                leadingIcon = {
                    Icon(Icons.Outlined.BackgroundColor, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    onOpenCanvasBackground()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_share)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Share, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    component.shareBitmap(AppToastHost::showConfetti)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_copy)) },
                leadingIcon = {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    component.cacheCurrentImage(Clipboard::copy)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_clear_layers)) },
                leadingIcon = {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    component.clearLayers()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_clear_filter)) },
                leadingIcon = {
                    Icon(Icons.Outlined.LineFilters, contentDescription = null)
                },
                enabled = component.selectedFilter != null,
                onClick = {
                    menuExpanded = false
                    component.selectFilter(null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.markup_menu_save_exif)) },
                trailingIcon = {
                    Switch(
                        checked = component.saveExif,
                        onCheckedChange = component::setSaveExif
                    )
                },
                onClick = {
                    component.setSaveExif(!component.saveExif)
                }
            )
        }
    }
}

@Composable
private fun EditorCanvas(
    component: MarkupLayersComponent,
    immersiveModeState: ImmersiveModeState,
    drawSession: DrawSessionState?,
    sideBarVisible: Boolean,
    layersPanelVisible: Boolean,
    onEditTextLayer: (String) -> Unit,
    onToolClick: (EditorTool) -> Unit,
    onOpenBrushSettings: () -> Unit,
    onAddImageLayer: () -> Unit,
    onExpandLayers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberZoomState(maxScale = 10f)
    val drawMode = drawSession != null
    // 有选中(非锁定)图层且非滤镜合成预览时:画布手势作用于选中图层本身,画布缩放/平移让位
    val gestureLayer = component.layers
        .firstOrNull { it.id == component.selectedLayerId }
        ?.takeIf {
            !drawMode && component.filterCompositeBitmap == null && !it.transform.locked
        }
    val transformSelection = gestureLayer != null

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .tappable {
                // 沉浸态下单击画布任意处退出沉浸;平时点击空白处取消图层选中
                if (immersiveModeState.isImmersive) {
                    immersiveModeState.exitImmersive()
                } else {
                    component.selectLayer(null)
                }
            }
    ) {
        val containerHeightPx = constraints.maxHeight.toFloat()

        Box(
            modifier = Modifier
                .fillMaxSize()
                // 绘制模式下单指让位给笔画采集;浏览模式(pan)才恢复缩放/平移;
                // 选中图层时缩放/平移让位给图层手势(下方 SelectedLayerGestureBox)
                .zoomable(
                    zoomState = zoomState,
                    zoomEnabled = (drawSession == null || drawSession.isPanMode) && !transformSelection
                ),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = component.bitmap
            if (bitmap != null) {
                // 滤镜面板打开期间展示合成预览(底图+图层烘焙后过滤镜),图层不再单独渲染;
                // 否则底图滤镜预览(displayBitmap)+ 图层实时渲染
                val filterComposite = component.filterCompositeBitmap
                val display = filterComposite ?: component.displayBitmap ?: bitmap
                val imageBitmap = remember(display) { display.asImageBitmap() }
                val adjustments = component.baseAdjustments
                val baseColorFilter = remember(adjustments) {
                    if (adjustments.isNeutral) null
                    else ColorFilter.colorMatrix(ColorMatrix(adjustments.toColorMatrixValues()))
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fitScale = min(
                        constraints.maxWidth / bitmap.width.toFloat(),
                        constraints.maxHeight / bitmap.height.toFloat()
                    )
                    val canvasWidthPx = bitmap.width * fitScale
                    val canvasHeightPx = bitmap.height * fitScale
                    val density = LocalDensity.current

                    Box(
                        modifier = Modifier
                            .size(
                                width = with(density) { canvasWidthPx.toDp() },
                                height = with(density) { canvasHeightPx.toDp() }
                            )
                            // 调色作用于「底图+图层」整个画布:离屏合成后整体过 colorFilter,与导出一致
                            .then(
                                if (baseColorFilter != null) {
                                    Modifier.graphicsLayer {
                                        compositingStrategy = CompositingStrategy.Offscreen
                                        colorFilter = baseColorFilter
                                    }
                                } else Modifier
                            )
                    ) {
                        Picture(
                            model = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .matchParentSize()
                                // 底图自身的透明棋盘格:透明图片/透明空白画布仍能看出透明区域
                                .transparencyChecker()
                                .clipToBounds()
                        )
                        // 选中图层的画布级手势层:置于图层之下,手指不在图层上也能变换选中图层
                        if (gestureLayer != null) {
                            SelectedLayerGestureBox(
                                layerId = gestureLayer.id,
                                transform = gestureLayer.transform,
                                canvasWidthPx = canvasWidthPx,
                                canvasHeightPx = canvasHeightPx,
                                onTransformStart = component::beginLayerTransformChange,
                                onTransformChange = {
                                    component.updateLayerTransformTransient(gestureLayer.id, it)
                                }
                            )
                        }
                        if (filterComposite == null) {
                            BoxWithConstraints(
                                modifier = Modifier.matchParentSize()
                            ) {
                                val layerCanvasWidth = constraints.maxWidth.toFloat()
                                val layerCanvasHeight = constraints.maxHeight.toFloat()
                                component.layers.forEach { layer ->
                                    key(layer.id) {
                                        val onEditRequest = (layer.type as? LayerType.Text)
                                            ?.let { { onEditTextLayer(layer.id) } }
                                        EditBox(
                                            transform = layer.transform,
                                            isSelected = component.selectedLayerId == layer.id,
                                            onSelect = { component.selectLayer(layer.id) },
                                            onEditRequest = onEditRequest,
                                            onTransformEnd = { newTransform ->
                                                component.updateLayerTransform(layer.id, newTransform)
                                            },
                                            // 绘制模式下图层不响应手势(纯静态渲染)
                                            tapSelectable = !drawMode && layer.type !is LayerType.Draw
                                        ) {
                                            LayerPreviewRenderers.Content(
                                                layer = layer,
                                                canvasWidthPx = layerCanvasWidth,
                                                canvasHeightPx = layerCanvasHeight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // 绘制模式:会话笔画覆盖层(含进行中笔画,橡皮擦离屏隔离)
                        if (drawSession != null) {
                            DrawOverlay(
                                session = drawSession,
                                canvasWidthPx = canvasWidthPx,
                                canvasHeightPx = canvasHeightPx
                            )
                        }
                    }
                }
            }
        }

        // 缩放胶囊:仅缩放比例 ≠100% 且画布手势未被选中图层接管时显示,绘制/沉浸模式下不显示
        ZoomCapsule(
            zoomState = zoomState,
            enabled = !drawMode && !immersiveModeState.isImmersive && !transformSelection,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // 绘制模式:画布右侧精简浮动竖条(画笔设置/橡皮擦/撤销重做/浏览模式)
        if (drawSession != null) {
            DrawFloatingBar(
                session = drawSession,
                onOpenSettings = onOpenBrushSettings,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }

        // 左侧工具栏:浮动卡片,可垂直拖动;「基础工具」Tab 控制显隐
        AnimatedVisibility(
            visible = immersiveModeState.isUiVisible && !drawMode && sideBarVisible,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            VerticalDraggable(containerHeightPx = containerHeightPx) {
                EditorSideBar(
                    component = component,
                    onToolClick = onToolClick,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // 右侧浮动图层面板:浮动卡片,可垂直拖动;「图层」Tab 控制显隐
        AnimatedVisibility(
            visible = immersiveModeState.isUiVisible && !drawMode && layersPanelVisible,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            VerticalDraggable(
                containerHeightPx = containerHeightPx,
                consumeTap = true
            ) {
                LayersFloatingPanel(
                    component = component,
                    onAddText = { EditorTools.byId(EditorTools.ID_TEXT)?.let(onToolClick) },
                    onAddSticker = { EditorTools.byId(EditorTools.ID_STICKER)?.let(onToolClick) },
                    onAddImage = onAddImageLayer,
                    onExpand = onExpandLayers,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

/**
 * 选中图层的画布级手势兜底层:铺满画布、置于图层之下(z 序),
 * 双指捏合 = 缩放选中图层、双指旋转 = 旋转、单指拖动 = 移动(即便手指不在图层上)。
 * 手指直接落在选中图层上时由 EditBox 自身手势接管(事件被消费后这里即退出)。
 * 首次越过 touchSlop 时经 [onTransformStart] 记一次历史快照,手势期间经
 * [onTransformChange] transient 更新,整段手势 = 一步 undo。
 */
@Composable
private fun SelectedLayerGestureBox(
    layerId: String,
    transform: LayerTransform,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onTransformStart: () -> Unit,
    onTransformChange: (LayerTransform) -> Unit,
) {
    val currentTransform by rememberUpdatedState(transform)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(layerId, canvasWidthPx, canvasHeightPx) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    // 与 EditBox 同款手动 transform 检测:越过 touchSlop 后开始应用增量
                    var pastTouchSlop = false
                    var pan = Offset.Zero
                    var zoom = 1f
                    var rotation = 0f
                    var local = currentTransform
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) break
                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()
                        if (!pastTouchSlop) {
                            zoom *= zoomChange
                            rotation += rotationChange
                            pan += panChange
                            val centroidSize = event.calculateCentroidSize(useCurrent = false)
                            val zoomMotion = abs(1 - zoom) * centroidSize
                            val rotationMotion =
                                abs(rotation * Math.PI.toFloat() * centroidSize / 180f)
                            val panMotion = pan.getDistance()
                            if (zoomMotion > viewConfiguration.touchSlop ||
                                rotationMotion > viewConfiguration.touchSlop ||
                                panMotion > viewConfiguration.touchSlop
                            ) {
                                pastTouchSlop = true
                                onTransformStart()
                            }
                        }
                        if (pastTouchSlop &&
                            (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero)
                        ) {
                            // 本层不做缩放/旋转,pan 已在画布坐标系,直接归一化累加
                            local = local.copy(
                                centerX = (local.centerX + panChange.x / canvasWidthPx)
                                    .coerceIn(0f, 1f),
                                centerY = (local.centerY + panChange.y / canvasHeightPx)
                                    .coerceIn(0f, 1f),
                                scale = (local.scale * zoomChange).coerceIn(0.1f, 10f),
                                rotation = local.rotation + rotationChange
                            )
                            onTransformChange(local)
                            event.changes.forEach {
                                if (it.position != it.previousPosition) it.consume()
                            }
                        }
                        if (event.changes.none { it.pressed }) break
                    }
                }
            }
    )
}

/** 垂直可拖动的浮动卡片容器:本地 offset,钳制在画布区域内,会话级 remember */
@Composable
private fun VerticalDraggable(
    containerHeightPx: Float,
    modifier: Modifier = Modifier,
    consumeTap: Boolean = false,
    content: @Composable () -> Unit,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var contentHeightPx by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .onSizeChanged { contentHeightPx = it.height }
            .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
            // consumeTap:吞掉落在卡片空白处的点按,避免穿透成「点空白取消选择」
            .pointerInput(consumeTap) {
                if (consumeTap) detectTapGestures { }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val limit = ((containerHeightPx - contentHeightPx) / 2f).coerceAtLeast(0f)
                    offsetY = (offsetY + dragAmount.y).coerceIn(-limit, limit)
                }
            }
    ) {
        content()
    }
}

/**
 * 缩放胶囊:只在缩放比例 ≠100% 时显示(AnimatedVisibility),
 * 「−」「+」按固定步进缩放,点百分比数字动画复位到 100%。
 */
@Composable
private fun ZoomCapsule(
    zoomState: ZoomState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale = zoomState.scale
    val scope = rememberCoroutineScope()
    val step: (Float) -> Unit = { target ->
        scope.launch { zoomState.changeScale(target, Offset.Zero) }
    }
    AnimatedVisibility(
        visible = enabled && abs(scale - 1f) > 0.001f,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(ShapeDefaults.circle)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            EnhancedIconButton(
                onClick = { step((scale - ZOOM_STEP).coerceAtLeast(1f)) },
                enabled = scale > 1f
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineZoomOut,
                    contentDescription = stringResource(R.string.markup_zoom_out),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "${(scale * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(ShapeDefaults.small)
                    .clickable { step(1f) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            )
            EnhancedIconButton(
                onClick = { step((scale + ZOOM_STEP).coerceAtMost(zoomState.maxScale)) },
                enabled = scale < zoomState.maxScale
            ) {
                Icon(
                    imageVector = Icons.Outlined.LineZoomIn,
                    contentDescription = stringResource(R.string.markup_zoom_in),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** 缩放胶囊「−」「+」的单次步进 */
private const val ZOOM_STEP = 0.5f

@Composable
private fun EditorSideBar(
    component: MarkupLayersComponent,
    onToolClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = modifier
            .clip(ShapeDefaults.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        EditorTools.sideBar.forEach { tool ->
            EditorToolItem(
                tool = tool,
                isActive = component.activeToolId == tool.id,
                onClick = { onToolClick(tool) }
            )
        }
    }
}

/** 底部主 Tab 栏:Tab 区窄屏可横滑、宽屏按可用宽度均分;「保存」固定右端 */
@Composable
private fun EditorBottomBar(
    component: MarkupLayersComponent,
    sideBarVisible: Boolean,
    layersPanelVisible: Boolean,
    onToolClick: (EditorTool) -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // 不透明容器背景(顶部圆角),避免透出画布背景,与顶栏风格协调
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val tabs = EditorTools.bottomTab
            // 每项规定最小宽度:放得下就均分填满,放不下收窄到最小宽度并可横滑
            val itemWidth = maxOf(TAB_ITEM_MIN_WIDTH, maxWidth / tabs.size)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                tabs.forEach { tool ->
                    EditorToolItem(
                        tool = tool,
                        isActive = when (tool.id) {
                            EditorTools.ID_BASIC -> sideBarVisible
                            EditorTools.ID_LAYERS -> layersPanelVisible
                            else -> component.activeToolId == tool.id
                        },
                        onClick = { onToolClick(tool) },
                        modifier = Modifier.width(itemWidth)
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // 「保存」主操作走项目封装的实心确认按钮
        ConfirmButton(
            text = stringResource(R.string.markup_save),
            onClick = onSaveClick
        )
    }
}

/** 底部 Tab 单项的最小宽度,窄屏低于该宽度时 Tab 区进入横滑 */
private val TAB_ITEM_MIN_WIDTH = 64.dp

/** 绘制模式底部操作条:左「取消」丢弃退出,右「完成」落成 Draw 图层;与底部 Tab 栏同款容器风格 */
@Composable
private fun DrawModeActionBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CancelButton(
            text = stringResource(R.string.markup_cancel),
            onClick = onCancel
        )
        Spacer(Modifier.weight(1f))
        ConfirmButton(
            text = stringResource(R.string.markup_draw_done),
            onClick = onConfirm
        )
    }
}

@Composable
private fun EditorToolItem(
    tool: EditorTool,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(ShapeDefaults.default)
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                } else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = tool.icon,
            contentDescription = stringResource(tool.titleRes),
            tint = contentColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(tool.titleRes),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1
        )
    }
}
