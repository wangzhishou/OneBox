package com.wanbaohe.markuplayers.presentation.editor

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.button.CancelButton
import com.shifenmiao.base.ui.button.ConfirmButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ImmersiveModeState
import com.shifenmiao.common.ui.rememberImmersiveModeState
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters.AddFiltersSheet
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.BackgroundColor
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomOut
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiEditImage
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiGenerateImageSheet
import com.t8rin.imagetoolbox.core.ui.widget.editor.AiStickerGeneration
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.tappable
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import com.t8rin.imagetoolbox.core.ui.widget.editor.BoxResizeConfig
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.presentation.components.EditBox
import com.wanbaohe.markuplayers.presentation.components.LayersFloatingPanel
import com.wanbaohe.markuplayers.presentation.components.LayersSheet
import com.wanbaohe.markuplayers.presentation.components.layerDisplayName
import com.wanbaohe.markuplayers.presentation.components.TextEditDialog
import com.wanbaohe.markuplayers.presentation.draw.BrushSettingsDialog
import com.wanbaohe.markuplayers.presentation.draw.DrawFloatingBar
import com.wanbaohe.markuplayers.presentation.draw.DrawOverlay
import com.wanbaohe.markuplayers.presentation.draw.DrawSessionState
import com.wanbaohe.markuplayers.presentation.export.ExportSettingsSheet
import com.wanbaohe.markuplayers.presentation.recipe.RecipeBatchSheet
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.presentation.tools.EditorTool
import com.wanbaohe.markuplayers.presentation.tools.EditorTools
import com.wanbaohe.markuplayers.presentation.tools.adjust.AdjustToolSheet
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import com.wanbaohe.markuplayers.domain.model.AiImageOp
import com.wanbaohe.markuplayers.domain.model.AiGenerationTarget
import com.shifenmiao.base.utils.aiImageProcessPointsCost
import com.wanbaohe.markuplayers.presentation.tools.ai.AiRectSelectOverlay
import com.wanbaohe.markuplayers.presentation.tools.ai.AiToolSheet
import com.wanbaohe.markuplayers.presentation.tools.crop.CropToolScreen
import com.wanbaohe.markuplayers.presentation.tools.filter.FilterPanel
import com.wanbaohe.markuplayers.presentation.tools.effects.EffectsToolSheet
import com.wanbaohe.markuplayers.presentation.tools.mask.MaskToolSheet
import com.wanbaohe.markuplayers.presentation.tools.shape.ShapeToolSheet
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerToolSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedDropdownMenu
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ZoomOutMap

/**
 * 主编辑界面(设计稿「图片创作」):基于 BaseScreen,背景槽为画布背景
 * (默认主题 surface 色,可切换纯色;底图自身透明区域由图片下的棋盘格标识),
 * 顶栏(返回/保存/更多菜单) + 画布区(顶部浮动控制条:缩放/撤销/重做/对比原图)
 * + 底部主 Tab 栏与保存按钮。
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
    component: MarkupLayersComponent,
) {
    var showStickerSheet by rememberSaveable { mutableStateOf(false) }
    var showLayersSheet by rememberSaveable { mutableStateOf(false) }
    var showExportSheet by rememberSaveable { mutableStateOf(false) }
    var showShapeSheet by rememberSaveable { mutableStateOf(false) }
    var showFullFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showEffectsSheet by rememberSaveable { mutableStateOf(false) }
    var showMaskSheet by rememberSaveable { mutableStateOf(false) }
    var showCanvasBackgroundSheet by rememberSaveable { mutableStateOf(false) }
    var showRecipeSheet by rememberSaveable { mutableStateOf(false) }
    // AI 生成图片弹层(共享 AiGenerateImageSheet,宿主薄壳负责预检)
    var showAiGenerateSheet by rememberSaveable { mutableStateOf(false) }
    var aiTargetAction by remember { mutableStateOf<AiTargetAction?>(null) }
    var aiCandidateLayerId by remember { mutableStateOf<String?>(null) }
    // 图像修复框选模式:true 时画布叠加框选层(矩形相对底图归一化),底部换成取消/确认操作条
    var aiRectSelect by rememberSaveable { mutableStateOf(false) }
    var aiRect by remember { mutableStateOf(DEFAULT_AI_RECT) }
    // Non-region AI operations confirm the chosen target before processing.
    var pendingAiOp by remember { mutableStateOf<AiImageOp?>(null) }
    // 文字/形状图层选滤镜的待确认项:图层 id + 待应用滤镜,确认后光栅化为图片图层再应用
    var pendingRasterize by remember { mutableStateOf<RasterizeRequest?>(null) }
    // 底部 Tab 单一工作态:任一时刻至多一个 Tab 高亮(高亮即「当前工作态」)。
    // basic=左侧工具栏、layers=浮动图层面板、filter=滤镜横滚面板、adjust/ai=对应 Sheet;
    // 再点当前 Tab 或 Sheet dismiss 即清除(同时收起对应面板/Sheet)
    var activeBottomTab by rememberSaveable { mutableStateOf<String?>(EditorTools.ID_BASIC) }
    val wideLayout = LocalConfiguration.current.screenWidthDp >= 840
    val sideBarVisible = activeBottomTab == EditorTools.ID_BASIC
    val layersPanelVisible = wideLayout && activeBottomTab == EditorTools.ID_LAYERS
    val filterPanelVisible = activeBottomTab == EditorTools.ID_FILTER
    // 「下一个新形状」的默认样式,由形状面板维护,关闭面板后保留
    var shapeDefaultSpec by remember { mutableStateOf(ShapeSpec.default(ShapeKind.Rectangle)) }
    val immersiveModeState = rememberImmersiveModeState()

    val onToolClick: (EditorTool) -> Unit = { tool ->
        when {
            // 底部 Tab 互斥:已是当前 → 清除;否则设为当前(显隐/开关由 activeBottomTab 派生)
            tool.placement == EditorTool.Placement.BottomTab -> {
                if (tool.id == EditorTools.ID_AI && activeBottomTab != tool.id) {
                    activeBottomTab = null
                    aiCandidateLayerId = component.selectedLayerId
                    aiTargetAction = AiTargetAction.Process
                } else {
                    activeBottomTab = if (activeBottomTab == tool.id) null else tool.id
                }
            }

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
                    EditorTools.ID_AI_GENERATE -> {
                        component.prepareAiGeneration()
                        aiCandidateLayerId = component.selectedLayerId
                        aiTargetAction = AiTargetAction.Generate
                    }
                    EditorTools.ID_MASK -> showMaskSheet = true
                    EditorTools.ID_EFFECTS -> showEffectsSheet = true
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

    // 批处理选图:多选相册图片,uri 列表交给 Component 暂存
    val batchImagePicker = rememberImagePicker(
        picker = Picker.Multiple,
        onSuccess = { uris: List<Uri> -> component.setBatchImages(uris) }
    )

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

    // 首页快捷任务:进入编辑器后自动打开指定工具(如 AI 面板),消费一次即清除
    LaunchedEffect(component.pendingToolId) {
        if (component.pendingToolId != null && component.hasImage) {
            when (component.pendingToolId) {
                EditorTools.ID_AI -> {
                    aiCandidateLayerId = component.selectedLayerId
                    activeBottomTab = EditorTools.ID_AI
                }
            }
            component.consumePendingTool()
        }
    }

    // 返回键由 BaseScreen 处理(沉浸态先退沉浸):框选/绘制模式下返回 = 取消当前模式,否则按原逻辑退出
    val onBack = {
        when {
            aiRectSelect -> aiRectSelect = false
            isDrawMode -> onDrawCancel()
            else -> component.closeProject()
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

    BaseScreen(
        title = {
            Column {
                Text(
                    text = component.projectName.ifBlank { stringResource(R.string.markup_editor_title) },
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = component.taskLabel ?: stringResource(
                        when {
                            component.isProjectSaving -> R.string.studio_project_saving
                            component.projectSaveError != null -> R.string.studio_project_save_failed_title
                            else -> R.string.studio_project_status
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (component.projectSaveError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        },
        onGoBack = onBack,
        backgroundImage = {
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
                onOpenCanvasBackground = { showCanvasBackgroundSheet = true },
                onOpenEffects = { showEffectsSheet = true },
                onOpenMask = { showMaskSheet = true },
                onOpenBatch = { showRecipeSheet = true }
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
                sideBarVisible = sideBarVisible && !aiRectSelect,
                layersPanelVisible = layersPanelVisible && !aiRectSelect,
                filterPanelVisible = filterPanelVisible && !aiRectSelect,
                aiRectSelect = aiRectSelect,
                aiRect = aiRect,
                onAiRectChange = { aiRect = it },
                onEditTextLayer = { layerId ->
                    component.beginTextEditSession(layerId)
                    component.setActiveTool(EditorTools.ID_TEXT)
                },
                onToolClick = onToolClick,
                onOpenBrushSettings = { showBrushSettings = true },
                onAddImageLayer = { imageLayerPicker.pickImage() },
                onExpandLayers = { showLayersSheet = true },
                onOpenFullFilterCatalog = { showFullFilterSheet = true },
                onOpenEffects = { showEffectsSheet = true },
                onRequestRasterize = { layerId, filter ->
                    pendingRasterize = RasterizeRequest(layerId, filter, append = false)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            AnimatedVisibility(visible = immersiveModeState.isUiVisible) {
                when {
                    // 图像修复框选模式:底部 Tab 栏换成取消/确认操作条
                    aiRectSelect -> {
                        AiRectActionBar(
                            onCancel = { aiRectSelect = false },
                            onConfirm = {
                                // 登录/积分预检已在进入框选模式前完成,这里直接执行
                                component.processAiImage(
                                    op = AiImageOp.Inpainting,
                                    rect = aiRect,
                                    pointsCost = aiImageProcessPointsCost()
                                )
                                aiRectSelect = false
                                aiRect = DEFAULT_AI_RECT
                            }
                        )
                    }

                    drawSession != null -> {
                        // 绘制模式:底部 Tab 栏换成取消/完成操作条,画笔操作由画布右侧浮动竖条承载
                        DrawModeActionBar(
                            onCancel = onDrawCancel,
                            onConfirm = onDrawConfirm
                        )
                    }

                    else -> {
                        EditorBottomBar(
                            component = component,
                            activeBottomTab = activeBottomTab,
                            onToolClick = onToolClick,
                            onExportClick = { showExportSheet = true }
                        )
                    }
                }
            }
        }
    )

    // 贴纸共享弹层:AI 生成贴纸 tab 由宿主注入(登录+积分预检通过后组件生成,
    // 成功落 Sticker 图层并关弹层)
    StickerToolSheet(
        visible = showStickerSheet,
        onDismiss = { showStickerSheet = false },
        onStickerClick = { source ->
            component.addLayer(MarkupLayer(type = LayerType.Sticker(source)))
        },
        aiGeneration = AiStickerGeneration(
            isGenerating = component.isGeneratingSticker,
            pointsCost = aiImageProcessPointsCost(),
            onGenerate = { prompt ->
                ActionUtils.ensureLoginAndCheckPoints(
                    source = AI_POINTS_SOURCE,
                    point = aiImageProcessPointsCost()
                ) {
                    component.generateSticker(
                        prompt = prompt,
                        onSuccess = { showStickerSheet = false }
                    )
                }
            }
        )
    )

    LayersSheet(
        visible = showLayersSheet || (!wideLayout && activeBottomTab == EditorTools.ID_LAYERS),
        component = component,
        onDismiss = {
            showLayersSheet = false
            if (activeBottomTab == EditorTools.ID_LAYERS) activeBottomTab = null
        },
        onAddText = {
            showLayersSheet = false
            activeBottomTab = null
            component.beginTextEditSession()
            component.setActiveTool(EditorTools.ID_TEXT)
        },
        onAddSticker = {
            showLayersSheet = false
            activeBottomTab = null
            showStickerSheet = true
        },
        onAddImage = {
            showLayersSheet = false
            activeBottomTab = null
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
        visible = activeBottomTab == EditorTools.ID_ADJUST,
        component = component,
        // Sheet dismiss(下滑/点外侧)时清除 Tab 高亮
        onDismiss = { if (activeBottomTab == EditorTools.ID_ADJUST) activeBottomTab = null }
    )

    EffectsToolSheet(
        visible = showEffectsSheet,
        component = component,
        onDismiss = { showEffectsSheet = false },
        onAddEffect = {
            showEffectsSheet = false
            showFullFilterSheet = true
        }
    )
    MaskToolSheet(
        visible = showMaskSheet,
        component = component,
        onDismiss = {
            showMaskSheet = false
            if (component.activeToolId == EditorTools.ID_MASK) component.setActiveTool(null)
        }
    )

    val addPickedEffect: (UiFilter<*>) -> Unit = { filter ->
        val vector = component.rasterizeRequiredLayer
        if (vector != null) {
            pendingRasterize = RasterizeRequest(vector.id, filter, append = true)
        } else {
            component.addEffect(filter)
            showEffectsSheet = true
        }
        showFullFilterSheet = false
    }
    AddFiltersSheet(
        component = component.addFiltersSheetComponent,
        filterTemplateCreationSheetComponent = component.filterTemplateCreationSheetComponent,
        visible = showFullFilterSheet,
        onDismiss = { showFullFilterSheet = false },
        previewBitmap = component.displayBitmap,
        onFilterPicked = addPickedEffect,
        onFilterPickedWithParams = addPickedEffect,
        canAddTemplates = false
    )

    AiTargetSheet(
        component = component,
        action = aiTargetAction,
        candidateLayerId = aiCandidateLayerId,
        onSelectTarget = { layerId, generationTarget ->
            component.selectLayer(layerId)
            if (aiTargetAction == AiTargetAction.Generate) {
                component.setAiGenerationTarget(generationTarget ?: AiGenerationTarget.NewLayer)
                showAiGenerateSheet = true
            }
            else activeBottomTab = EditorTools.ID_AI
            aiTargetAction = null
        },
        onDismiss = { aiTargetAction = null }
    )

    AiToolSheet(
        visible = activeBottomTab == EditorTools.ID_AI,
        onDismiss = { if (activeBottomTab == EditorTools.ID_AI) activeBottomTab = null },
        pointsCost = aiImageProcessPointsCost(),
        onOpClick = { op ->
            // 面板收起后分流:需框选的能力(图像修复)先做登录+积分预检,通过才进框选模式,
            // Avoid selecting a region before authentication; replacements remain undoable.
            // 确认后才做预检与处理;积分在处理成功后由组件扣除,失败不扣
            activeBottomTab = null
            if (op.needsRect) {
                ActionUtils.ensureLoginAndCheckPoints(
                    source = AI_POINTS_SOURCE,
                    point = aiImageProcessPointsCost()
                ) {
                    aiRect = DEFAULT_AI_RECT
                    if (component.selectedLayer != null) component.prepareMaskTarget()
                    aiRectSelect = true
                }
            } else {
                pendingAiOp = op
            }
        }
    )

    // The confirmation copy describes undoable replacement of the selected target.
    EnhancedAlertDialog(
        visible = pendingAiOp != null,
        onDismissRequest = { pendingAiOp = null },
        title = { Text(stringResource(R.string.markup_ai_replace_title)) },
        text = { Text(stringResource(R.string.markup_ai_replace_message)) },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { pendingAiOp = null }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = {
                    val op = pendingAiOp ?: return@ConfirmButton
                    pendingAiOp = null
                    val cost = aiImageProcessPointsCost()
                    ActionUtils.ensureLoginAndCheckPoints(
                        source = AI_POINTS_SOURCE,
                        point = cost
                    ) {
                        component.processAiImage(op, pointsCost = cost)
                    }
                }
            )
        }
    )

    // 文字/形状图层选滤镜的光栅化确认:确定 = 光栅化为图片图层(可撤销)并应用滤镜;取消 = 零副作用
    EnhancedAlertDialog(
        visible = pendingRasterize != null,
        onDismissRequest = { pendingRasterize = null },
        title = { Text(stringResource(R.string.markup_rasterize_title)) },
        text = { Text(stringResource(R.string.markup_rasterize_message)) },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { pendingRasterize = null }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = {
                    val request = pendingRasterize ?: return@ConfirmButton
                    pendingRasterize = null
                    component.rasterizeLayer(request.layerId) {
                        if (request.append) {
                            component.addEffect(request.filter)
                            showEffectsSheet = true
                        } else component.selectFilter(request.filter)
                    }
                }
            )
        }
    )

    // AI 生成图片(共享 core/ui AiGenerateImageSheet;宿主薄壳负责登录+积分预检):
    // 选中图片图层时图生图编辑(成功后换图,回退走现有 undo 体系,不另建历史)
    AiGenerateImageSheet(
        visible = showAiGenerateSheet,
        title = stringResource(R.string.markup_ai_generate_title),
        editTitle = stringResource(R.string.markup_ai_edit_image_title),
        promptHint = stringResource(R.string.markup_ai_generate_hint),
        editPromptHint = stringResource(R.string.markup_ai_edit_image_hint),
        generateLabel = stringResource(R.string.markup_ai_generate_action),
        pointsHint = stringResource(
            R.string.markup_ai_generate_points_hint,
            aiImageProcessPointsCost()
        ),
        emptyHint = stringResource(R.string.markup_ai_generate_empty),
        currentLabel = stringResource(R.string.markup_ai_edit_current),
        historyLabel = stringResource(R.string.markup_ai_edit_history),
        isGenerating = component.isAiProcessing,
        editImage = component.aiEditImageUri?.let { uri ->
            AiEditImage(
                uri = uri,
                historyUris = component.aiImageHistory,
                onRevert = component::revertAiImage
            )
        },
        onGenerate = { prompt ->
            ActionUtils.ensureLoginAndCheckPoints(
                source = AI_POINTS_SOURCE,
                point = aiImageProcessPointsCost()
            ) {
                component.generateImageLayer(prompt)
                showAiGenerateSheet = false
            }
        },
        onDismiss = { showAiGenerateSheet = false }
    )

    AiResultSheet(component = component)

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
        imageUri = component.uri,
        estimateBitmap = component.compositePreviewBitmap
            ?: component.displayBitmap ?: component.bitmap,
        canOverwriteOriginal = component.canOverwriteOriginal,
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

    // 打开导出面板时先触发一次合成预览,体积估算基于含图层的合成图而非裸底图
    LaunchedEffect(showExportSheet) {
        if (showExportSheet) component.prepareExportPreview()
    }

    RecipeBatchSheet(
        visible = showRecipeSheet,
        recipes = component.recipes,
        onDismiss = { showRecipeSheet = false },
        onSaveCurrentRecipe = component::saveRecipeFromCurrent,
        onApplyRecipe = { recipe ->
            component.applyRecipe(recipe)
            showRecipeSheet = false
        },
        onPickBatchImages = { batchImagePicker.pickImage() },
        onRunBatch = component::runBatch,
        onCancelBatch = component::cancelBatch,
        selectedImageCount = component.batchImageCount,
        progress = component.batchProgress,
        report = component.batchReport,
        error = component.recipeError,
        canSaveCurrentRecipe = component.hasImage,
        onDeleteRecipe = component::deleteRecipe
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

}

private enum class AiTargetAction { Process, Generate }

private data class RasterizeRequest(
    val layerId: String,
    val filter: UiFilter<*>,
    val append: Boolean,
)

@Composable
private fun AiTargetSheet(
    component: MarkupLayersComponent,
    action: AiTargetAction?,
    candidateLayerId: String?,
    onSelectTarget: (String?, AiGenerationTarget?) -> Unit,
    onDismiss: () -> Unit,
) {
    val candidate = component.layers.firstOrNull {
        it.id == candidateLayerId && it.type is LayerType.Image && !it.transform.locked
    }
    EnhancedModalBottomSheet(
        visible = action != null,
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.studio_ai_target_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.studio_ai_target_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                candidate?.let { layer ->
                    ConfirmButton(
                        text = stringResource(
                            R.string.studio_ai_target_layer,
                            layerDisplayName(layer, component.layers)
                        ),
                        enabled = !component.isAiProcessing &&
                            (action != AiTargetAction.Generate || component.canAiEdit),
                        onClick = {
                            onSelectTarget(
                                layer.id,
                                if (action == AiTargetAction.Generate) AiGenerationTarget.SelectedLayer else null
                            )
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    )
                }
                ConfirmButton(
                    text = stringResource(R.string.studio_ai_target_base),
                    enabled = !component.isAiProcessing &&
                        (action != AiTargetAction.Generate || component.canAiEdit),
                    onClick = {
                        onSelectTarget(
                            null,
                            if (action == AiTargetAction.Generate) AiGenerationTarget.Background else null
                        )
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                )
                if (action == AiTargetAction.Generate) {
                    ConfirmButton(
                        text = stringResource(R.string.studio_ai_target_generate),
                        enabled = !component.isAiProcessing,
                        onClick = { onSelectTarget(null, AiGenerationTarget.NewLayer) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    )
                    if (!component.canAiEdit) {
                        Text(
                            text = stringResource(R.string.studio_ai_provider_no_edit),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                CancelButton(
                    text = stringResource(R.string.markup_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                )
            }
        }
    )
}

@Composable
private fun AiResultSheet(component: MarkupLayersComponent) {
    val result = component.pendingAiResult ?: return
    val enabled = !component.isAiProcessing && !component.isImageLoading
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { component.discardAiResult() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .navigationBarsPadding().padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.studio_ai_result_title, result.title),
                    style = MaterialTheme.typography.titleMedium
                )
                Picture(
                    model = result.imagePath,
                    contentDescription = stringResource(R.string.studio_ai_result_preview),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(280.dp).transparencyChecker()
                )
                Text(
                    text = stringResource(R.string.studio_ai_result_keep_editable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result.replaceBackground || result.targetLayerId != null) {
                    ConfirmButton(
                        text = stringResource(R.string.studio_ai_result_replace),
                        enabled = enabled,
                        onClick = { component.acceptAiResult(asNewLayer = false) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    )
                }
                ConfirmButton(
                    text = stringResource(R.string.studio_ai_result_new_layer),
                    enabled = enabled,
                    onClick = { component.acceptAiResult(asNewLayer = true) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                )
                CancelButton(
                    text = stringResource(R.string.markup_cancel),
                    onClick = component::discardAiResult,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                )
            }
        }
    )
}

@Composable
private fun EditorTopBarActions(
    component: MarkupLayersComponent,
    immersiveModeState: ImmersiveModeState,
    onOpenCanvasBackground: () -> Unit,
    onOpenEffects: () -> Unit,
    onOpenMask: () -> Unit,
    onOpenBatch: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameProject by rememberSaveable { mutableStateOf(false) }
    var projectName by rememberSaveable { mutableStateOf("") }
    var showSaveError by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(component.projectSaveError) {
        if (component.projectSaveError != null) showSaveError = true
    }
    // 「清空图层」二次确认:删除全部图层(可撤销),先弹确认对话框
    var showClearLayersDialog by remember { mutableStateOf(false) }
    EnhancedIconButton(
        onClick = { component.saveProject() },
        enabled = !component.isProjectSaving
    ) {
        Icon(
            imageVector = MaterialIcons.Outlined.Save,
            contentDescription = stringResource(R.string.studio_project_save)
        )
    }
    Box {
        EnhancedIconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = MaterialIcons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.markup_more_options)
            )
        }
        EnhancedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.studio_project_rename)) },
                onClick = {
                    menuExpanded = false
                    projectName = component.projectName
                    showRenameProject = true
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.studio_effects_edit, component.targetEffects.size)) },
                enabled = component.activeToolId != EditorTools.ID_DRAW,
                onClick = {
                    menuExpanded = false
                    onOpenEffects()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.studio_mask)) },
                enabled = component.activeToolId != EditorTools.ID_DRAW,
                onClick = {
                    menuExpanded = false
                    onOpenMask()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.studio_batch_recipes)) },
                onClick = {
                    menuExpanded = false
                    onOpenBatch()
                }
            )
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
                text = { Text(stringResource(R.string.studio_workspace_background)) },
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
                    showClearLayersDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.studio_effects_clear)) },
                leadingIcon = {
                    Icon(Icons.Outlined.LineFilters, contentDescription = null)
                },
                // 清除当前目标的滤镜:选中图层 → 清图层滤镜(入 undo);无选中 → 清背景图
                enabled = component.targetEffects.isNotEmpty(),
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

    EnhancedAlertDialog(
        visible = showRenameProject,
        onDismissRequest = { showRenameProject = false },
        title = { Text(stringResource(R.string.studio_project_rename)) },
        text = {
            GlassOutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it.take(120) },
                label = { Text(stringResource(R.string.studio_project_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { showRenameProject = false }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                enabled = projectName.isNotBlank(),
                onClick = {
                    component.renameProject(projectName.trim())
                    showRenameProject = false
                }
            )
        }
    )
    EnhancedAlertDialog(
        visible = showSaveError && component.projectSaveError != null,
        onDismissRequest = { showSaveError = false },
        title = { Text(stringResource(R.string.studio_project_save_failed_title)) },
        text = {
            Text(stringResource(R.string.studio_project_save_error, component.projectSaveError.orEmpty()))
        },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { showSaveError = false }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.studio_retry),
                enabled = !component.isProjectSaving,
                onClick = {
                    showSaveError = false
                    component.saveProject()
                }
            )
        }
    )

    // 清空图层二次确认(确认后执行,仍可通过撤销恢复)
    EnhancedAlertDialog(
        visible = showClearLayersDialog,
        onDismissRequest = { showClearLayersDialog = false },
        title = { Text(stringResource(R.string.markup_clear_layers_title)) },
        text = { Text(stringResource(R.string.markup_clear_layers_message)) },
        dismissButton = {
            CancelButton(
                text = stringResource(R.string.markup_cancel),
                onClick = { showClearLayersDialog = false }
            )
        },
        confirmButton = {
            ConfirmButton(
                text = stringResource(R.string.markup_confirm),
                onClick = {
                    showClearLayersDialog = false
                    component.clearLayers()
                }
            )
        }
    )
}

@Composable
private fun EditorCanvas(
    component: MarkupLayersComponent,
    immersiveModeState: ImmersiveModeState,
    drawSession: DrawSessionState?,
    sideBarVisible: Boolean,
    layersPanelVisible: Boolean,
    filterPanelVisible: Boolean,
    aiRectSelect: Boolean,
    aiRect: NormalizedRect,
    onAiRectChange: (NormalizedRect) -> Unit,
    onEditTextLayer: (String) -> Unit,
    onToolClick: (EditorTool) -> Unit,
    onOpenBrushSettings: () -> Unit,
    onAddImageLayer: () -> Unit,
    onExpandLayers: () -> Unit,
    onOpenFullFilterCatalog: () -> Unit,
    onOpenEffects: () -> Unit,
    onRequestRasterize: (layerId: String, filter: UiFilter<*>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewport = remember { EditorViewportState() }
    val drawMode = drawSession != null
    val localAiTarget = aiRectSelect && component.selectedLayer != null
    val canvasBitmap = if (localAiTarget) component.maskTargetBitmap else component.bitmap
    val canPanSingle = drawSession == null || drawSession.isPanMode

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
    ) {
        val containerHeightPx = constraints.maxHeight.toFloat()
        val containerWidthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current
        val paddingPx = with(density) { 16.dp.roundToPx() } * 2
        val fittedSize = canvasBitmap?.let {
            val fit = min(
                (constraints.maxWidth - paddingPx).coerceAtLeast(1) / it.width.toFloat(),
                (constraints.maxHeight - paddingPx).coerceAtLeast(1) / it.height.toFloat()
            )
            IntSize(
                (it.width * fit).toInt().coerceAtLeast(1),
                (it.height * fit).toInt().coerceAtLeast(1)
            )
        } ?: IntSize.Zero
        val sourceSize = if (localAiTarget) {
            canvasBitmap?.let { IntSize(it.width, it.height) }
        } else component.sourceSize ?: canvasBitmap?.let { IntSize(it.width, it.height) }
        val fittedSourceScale = sourceSize?.let {
            min(fittedSize.width / it.width.toFloat(), fittedSize.height / it.height.toFloat())
        }?.takeIf { it > 0f } ?: 1f
        SideEffect {
            viewport.configure(
                layoutSize = Size(containerWidthPx, containerHeightPx),
                contentSize = fittedSize,
                actualSizeScale = 1f / fittedSourceScale
            )
        }
        LaunchedEffect(sourceSize, localAiTarget) { viewport.fit() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(aiRectSelect, canPanSingle, containerWidthPx, containerHeightPx) {
                    if (aiRectSelect) return@pointerInput
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                        var multiTouch = false
                        var moved = false
                        var accumulatedPan = Offset.Zero
                        while (true) {
                            val initial = awaitPointerEvent(PointerEventPass.Initial)
                            if (initial.changes.count { it.pressed } >= 2) {
                                multiTouch = true
                                viewport.applyGesture(
                                    pan = initial.calculatePan(),
                                    zoom = initial.calculateZoom(),
                                    focalPoint = initial.calculateCentroid()
                                )
                            }
                            if (multiTouch) initial.changes.forEach { it.consume() }
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val consumed = event.changes.any { it.isConsumed }
                            if (!multiTouch && !consumed && canPanSingle) {
                                accumulatedPan += event.calculatePan()
                                if (accumulatedPan.getDistance() > viewConfiguration.touchSlop) {
                                    moved = true
                                }
                                if (moved) {
                                    viewport.applyGesture(
                                        pan = event.calculatePan(),
                                        zoom = 1f,
                                        focalPoint = Offset(containerWidthPx / 2f, containerHeightPx / 2f)
                                    )
                                    event.changes.forEach { if (it.positionChange() != Offset.Zero) it.consume() }
                                }
                            }
                            if (event.changes.none { it.pressed }) {
                                if (!multiTouch && !moved && !consumed && !drawMode && !component.isComparing) {
                                    if (immersiveModeState.isImmersive) immersiveModeState.exitImmersive()
                                    else component.selectLayer(null)
                                }
                                break
                            }
                        }
                    }
                }
                .graphicsLayer {
                    scaleX = viewport.scale
                    scaleY = viewport.scale
                    translationX = viewport.offset.x
                    translationY = viewport.offset.y
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = canvasBitmap
            if (bitmap != null) {
                // 底图(带背景滤镜预览)+ 全量图层 live 渲染;背景调节只作用于底图 Picture,
                // 各图层自身的滤镜(过滤后位图)/调节(colorFilter)在图层渲染处应用
                val display = when {
                    component.isComparing -> component.originalPreviewBitmap ?: bitmap
                    localAiTarget -> bitmap
                    else -> component.displayBitmap ?: bitmap
                }
                val imageBitmap = remember(display) { display.asImageBitmap() }
                val baseAdjustments = component.baseAdjustments
                val baseColorFilter = remember(baseAdjustments, component.isComparing, localAiTarget) {
                    if (baseAdjustments.isNeutral || component.isComparing || localAiTarget) null
                    else ColorFilter.colorMatrix(ColorMatrix(baseAdjustments.toColorMatrixValues()))
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
                    // 画布尺寸统一向下取整到整像素:底图 Picture、透明棋盘格(按 Picture
                    // 布局尺寸绘制)、图层画布与手势/绘制层全部共用同一 IntSize。
                    // 此前直接用 Float 像素转小数 Dp,布局阶段再四舍五入,容器可能比
                    // 图片实际显示尺寸大 1~2px,下缘/右缘露出一条棋盘格
                    val canvasSize = IntSize(
                        width = (bitmap.width * fitScale).toInt().coerceAtLeast(1),
                        height = (bitmap.height * fitScale).toInt().coerceAtLeast(1)
                    )
                    val canvasWidthPx = canvasSize.width.toFloat()
                    val canvasHeightPx = canvasSize.height.toFloat()
                    val density = LocalDensity.current

                    Box(
                        modifier = Modifier
                            .size(
                                width = with(density) { canvasSize.width.toDp() },
                                height = with(density) { canvasSize.height.toDp() }
                            )
                    ) {
                        // 透明棋盘格内缩 1dp:与图片同 bounds 时,缩放/平移带来的亚像素
                        // 边缘抗锯齿会让棋盘格在图片下缘/右缘露出细条;内缩后只有图片
                        // 内部真正的透明区域才会透出棋盘格
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(1.dp)
                                .transparencyChecker()
                        )
                        Picture(
                            model = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .matchParentSize()
                                .clipToBounds()
                                // 背景调节只作用于底图:离屏合成后过 colorFilter,与导出烘焙一致
                                .then(
                                    if (baseColorFilter != null) {
                                        Modifier.graphicsLayer {
                                            compositingStrategy = CompositingStrategy.Offscreen
                                            colorFilter = baseColorFilter
                                        }
                                    } else Modifier
                                )
                        )
                        // 全部图层实时渲染;每个图层应用自身的滤镜(组件预算的过滤后位图)
                        // 与调节(离屏 colorFilter),与导出侧逐图层烘焙一致
                        val liveLayers = component.layers
                        if (liveLayers.isNotEmpty() && !component.isComparing && !localAiTarget) {
                            BoxWithConstraints(
                                modifier = Modifier.matchParentSize()
                            ) {
                                val layerCanvasWidth = constraints.maxWidth.toFloat()
                                val layerCanvasHeight = constraints.maxHeight.toFloat()
                                liveLayers.forEach { layer ->
                                    key(layer.id) {
                                        val textType = layer.type as? LayerType.Text
                                        val onEditRequest = textType
                                            ?.let { { onEditTextLayer(layer.id) } }
                                        EditBox(
                                            transform = layer.transform,
                                            isSelected = layer.id in component.selectedLayerIds &&
                                                !drawMode && !aiRectSelect && !immersiveModeState.isImmersive,
                                            showHandles = component.selectedLayerId == layer.id,
                                            onSelect = { component.selectLayer(layer.id) },
                                            onEditRequest = onEditRequest,
                                            // 选中框上的红色 X 删除(组件内记 undo 快照);
                                            // 绘制/框选模式下不显示(该模式下图层纯静态渲染)
                                            onDelete = if (!drawMode && !aiRectSelect) {
                                                { component.removeLayer(layer.id) }
                                            } else null,
                                            onTransformStart = component::beginLayerTransformChange,
                                            onTransformChange = {
                                                component.updateLayerTransformTransient(layer.id, it)
                                            },
                                            onTransformEnd = {
                                                component.updateLayerTransformTransient(
                                                    layer.id,
                                                    component.snapLayerTransform(it)
                                                )
                                                component.endLayerTransformChange()
                                            },
                                            canvasScale = viewport.scale,
                                            gesturesEnabled = !drawMode && !aiRectSelect &&
                                                !immersiveModeState.isImmersive,
                                            // 绘制/框选模式下图层不响应手势(纯静态渲染)
                                            tapSelectable = !drawMode && !aiRectSelect && layer.type !is LayerType.Draw,
                                            // 文字层:8 向框尺寸手柄(拖边/角改框宽,文字重排,字号不变);
                                            // 手势开始记 undo 快照,中间帧 transient(手势结束即一次 undo)
                                            resizeConfig = textType?.takeIf {
                                                !drawMode && !aiRectSelect && component.selectedLayerId == layer.id
                                            }?.let {
                                                BoxResizeConfig(
                                                    minWidthPx = layerCanvasWidth * 0.1f,
                                                    maxWidthPx = layerCanvasWidth,
                                                    minHeightPx = 1f,
                                                    maxHeightPx = layerCanvasHeight * 2f,
                                                    onResize = { newW, newH, newLeft, newTop ->
                                                        component.updateTextWidthRatioTransient(
                                                            layer.id,
                                                            newW / layerCanvasWidth
                                                        )
                                                        component.updateLayerTransformTransient(
                                                            layer.id,
                                                            layer.transform.copy(
                                                                centerX = ((newLeft + newW / 2) / layerCanvasWidth)
                                                                    .coerceIn(0f, 1f),
                                                                centerY = ((newTop + newH / 2) / layerCanvasHeight)
                                                                    .coerceIn(0f, 1f)
                                                            )
                                                        )
                                                    },
                                                    onGestureStart = {
                                                        component.beginLayerTransformChange()
                                                    }
                                                )
                                            }
                                        ) {
                                            // 图层调节:离屏合成后过 colorFilter,只作用于本图层内容
                                            val layerColorFilter = remember(layer.adjustments) {
                                                if (layer.adjustments.isNeutral) null
                                                else ColorFilter.colorMatrix(
                                                    ColorMatrix(layer.adjustments.toColorMatrixValues())
                                                )
                                            }
                                            Box(
                                                modifier = Modifier.then(
                                                    if (layerColorFilter != null) {
                                                        Modifier.graphicsLayer {
                                                            compositingStrategy =
                                                                CompositingStrategy.Offscreen
                                                            colorFilter = layerColorFilter
                                                        }
                                                    } else Modifier
                                                )
                                            ) {
                                                LayerPreviewRenderers.Content(
                                                    layer = layer,
                                                    canvasWidthPx = layerCanvasWidth,
                                                    canvasHeightPx = layerCanvasHeight,
                                                    filteredBitmap = component
                                                        .layerFilteredBitmap(layer)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // 绘制模式:会话笔画覆盖层(含进行中笔画,橡皮擦离屏隔离)
                        if (drawSession != null && !component.isComparing) {
                            DrawOverlay(
                                session = drawSession,
                                canvasWidthPx = canvasWidthPx,
                                canvasHeightPx = canvasHeightPx
                            )
                        }
                        // 图像修复框选模式:框选层置顶(铺满底图适配盒,归一化坐标直接对应)
                        if (aiRectSelect) {
                            AiRectSelectOverlay(
                                rect = aiRect,
                                onRectChange = onAiRectChange,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
            }
        }

        if (!drawMode && !aiRectSelect && immersiveModeState.isUiVisible) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            ) {
                ZoomCapsule(
                    viewport = viewport,
                    fittedSourceScale = fittedSourceScale,
                    component = component
                )
                Spacer(Modifier.width(4.dp))
                CompareButton(component = component)
            }
        }

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

        // 滤镜横滚面板:半浮动玻璃卡片,浮在画布底部(底部 Tab 栏上方),「滤镜」Tab 控制显隐
        AnimatedVisibility(
            visible = immersiveModeState.isUiVisible && !drawMode && filterPanelVisible && !component.isComparing,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FilterPanel(
                component = component,
                onOpenFullCatalog = onOpenFullFilterCatalog,
                onOpenEffects = onOpenEffects,
                onRequestRasterize = onRequestRasterize,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 左侧工具栏:浮动卡片,可垂直拖动;「基础工具」Tab 控制显隐
        AnimatedVisibility(
            visible = immersiveModeState.isUiVisible && !drawMode && sideBarVisible && !component.isComparing,
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
            visible = immersiveModeState.isUiVisible && !drawMode && layersPanelVisible && !component.isComparing,
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

/** View-only transforms are deliberately separate from document/layer undo history. */
private class EditorViewportState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var minScale by mutableFloatStateOf(1f)
        private set
    val maxScale = 10f
    private var layoutSize = Size.Zero
    private var contentSize = IntSize.Zero

    fun configure(layoutSize: Size, contentSize: IntSize, actualSizeScale: Float) {
        this.layoutSize = layoutSize
        this.contentSize = contentSize
        minScale = min(1f, actualSizeScale).coerceAtLeast(0.01f)
        scale = scale.coerceIn(minScale, maxScale)
        offset = clampOffset(offset, scale)
    }

    fun fit() {
        scale = 1f
        offset = Offset.Zero
    }

    fun zoomTo(target: Float) {
        applyGesture(
            pan = Offset.Zero,
            zoom = target.coerceIn(minScale, maxScale) / scale,
            focalPoint = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
        )
    }

    fun applyGesture(pan: Offset, zoom: Float, focalPoint: Offset) {
        if (!zoom.isFinite() || zoom <= 0f || contentSize == IntSize.Zero) return
        val nextScale = (scale * zoom).coerceIn(minScale, maxScale)
        val focal = focalPoint - Offset(layoutSize.width / 2f, layoutSize.height / 2f)
        offset = clampOffset(focal - (focal - offset) * (nextScale / scale) + pan, nextScale)
        scale = nextScale
    }

    private fun clampOffset(value: Offset, scale: Float): Offset {
        val x = max(0f, (contentSize.width * scale - layoutSize.width) / 2f)
        val y = max(0f, (contentSize.height * scale - layoutSize.height) / 2f)
        return Offset(value.x.coerceIn(-x, x), value.y.coerceIn(-y, y))
    }
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
 * 画布顶部浮动控制条,全图标无文字:缩放(缩小/适应屏幕/实际像素/放大)+ 撤销/重做。
 * Fit and source-pixel 100% are different operations, including for upscaled small images.
 */
@Composable
private fun ZoomCapsule(
    viewport: EditorViewportState,
    fittedSourceScale: Float,
    component: MarkupLayersComponent,
    modifier: Modifier = Modifier,
) {
    val scale = viewport.scale
    val actualScale = 1f / fittedSourceScale
    val actualAvailable = actualScale in viewport.minScale..viewport.maxScale
    val actualDescription = stringResource(
        if (actualAvailable) R.string.studio_zoom_actual_description
        else R.string.studio_zoom_actual_unavailable
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .glassBackground(
                style = GlassStyle.None,
                shape = ShapeDefaults.circle,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            .padding(horizontal = 2.dp)
    ) {
        EnhancedIconButton(
            onClick = { viewport.zoomTo(scale - ZOOM_STEP) },
            enabled = scale > viewport.minScale,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LineZoomOut,
                contentDescription = stringResource(R.string.markup_zoom_out),
                modifier = Modifier.size(18.dp)
            )
        }
        EnhancedIconButton(
            onClick = viewport::fit,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = MaterialIcons.Outlined.FitScreen,
                contentDescription = stringResource(R.string.studio_zoom_fit_description),
                modifier = Modifier.size(18.dp)
            )
        }
        EnhancedIconButton(
            onClick = { viewport.zoomTo(actualScale) },
            enabled = actualAvailable,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = MaterialIcons.Outlined.ZoomOutMap,
                contentDescription = actualDescription,
                modifier = Modifier.size(18.dp)
            )
        }
        EnhancedIconButton(
            onClick = { viewport.zoomTo(scale + ZOOM_STEP) },
            enabled = scale < viewport.maxScale,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LineZoomIn,
                contentDescription = stringResource(R.string.markup_zoom_in),
                modifier = Modifier.size(18.dp)
            )
        }
        // 撤销/重做:从顶栏挪入本控制条
        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .size(width = 1.dp, height = 18.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        EnhancedIconButton(
            onClick = component::undo,
            enabled = component.canUndo,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LineUndo,
                contentDescription = stringResource(R.string.markup_undo),
                modifier = Modifier.size(18.dp)
            )
        }
        EnhancedIconButton(
            onClick = component::redo,
            enabled = component.canRedo,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.LineRedo,
                contentDescription = stringResource(R.string.markup_redo),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** 对比原图:点按切换原图/编辑态预览;按住(超过长按阈值)松手前临时看原图,松手恢复。 */
@Composable
private fun CompareButton(component: MarkupLayersComponent) {
    val enabled = component.originalPreviewBitmap != null
    val description = stringResource(R.string.studio_compare)
    val previewState = stringResource(
        if (component.isComparing) R.string.studio_compare_original else R.string.studio_compare_edited
    )
    DisposableEffect(component) {
        onDispose { component.setComparing(false) }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .glassBackground(
                style = GlassStyle.None,
                shape = ShapeDefaults.circle,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            .semantics {
                contentDescription = description
                stateDescription = previewState
                if (enabled) onClick {
                    component.setComparing(!component.isComparing)
                    true
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        // 按下即看原图:长按则松手恢复;点按切换——原本在对比态则这次点按关回编辑态
                        val wasComparing = component.isComparing
                        component.setComparing(true)
                        val quickTap = withTimeoutOrNull(
                            viewConfiguration.longPressTimeoutMillis
                        ) { tryAwaitRelease() } != null
                        if (quickTap) {
                            if (wasComparing) component.setComparing(false)
                        } else {
                            tryAwaitRelease()
                            component.setComparing(false)
                        }
                    }
                )
            }
    ) {
        Icon(
            imageVector = Icons.Outlined.LineCompare,
            contentDescription = null,
            tint = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                component.isComparing -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
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
            .glassBackground(
                style = GlassStyle.None,
                shape = ShapeDefaults.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            .heightIn(max = 480.dp)
            .verticalScroll(rememberScrollState())
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
    activeBottomTab: String?,
    onToolClick: (EditorTool) -> Unit,
    onExportClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // 容器背景走全局玻璃体系(顶部圆角),玻璃关闭时退化为实色
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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
                        isActive = activeBottomTab == tool.id,
                        onClick = { onToolClick(tool) },
                        modifier = Modifier.width(itemWidth)
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // 「保存」主操作走项目封装的实心确认按钮
        ConfirmButton(
            text = stringResource(R.string.studio_export_image),
            onClick = onExportClick
        )
    }
}

/** 底部 Tab 单项的最小宽度,窄屏低于该宽度时 Tab 区进入横滑 */
private val TAB_ITEM_MIN_WIDTH = 64.dp

/** 绘制模式底部操作条:左「取消」丢弃退出,右「完成」落成 Draw 图层;与底部 Tab 栏同款玻璃容器 */
@Composable
private fun DrawModeActionBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
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

/** 框选模式底部操作条:左「取消」退出框选,中间提示文案,右「确认」执行图像修复;与底部 Tab 栏同款玻璃容器 */
@Composable
private fun AiRectActionBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .glassDense(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CancelButton(
            text = stringResource(R.string.markup_cancel),
            onClick = onCancel
        )
        Text(
            text = stringResource(R.string.markup_ai_rect_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        ConfirmButton(
            text = stringResource(R.string.markup_confirm),
            onClick = onConfirm
        )
    }
}

/** 框选模式初始矩形:居中 50% 区域 */
private val DEFAULT_AI_RECT = NormalizedRect(0.25f, 0.25f, 0.75f, 0.75f)

/** AI 图像处理登录/积分预检的来源标识 */
private const val AI_POINTS_SOURCE = "markup_ai"

@Composable
private fun EditorToolItem(
    tool: EditorTool,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .size(width = 64.dp, height = 56.dp)
            .clip(ShapeDefaults.default)
            .background(
                if (isActive) {
                    MaterialTheme.colorScheme.primaryContainer
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
