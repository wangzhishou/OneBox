package com.wanbaohe.markuplayers.presentation.editor

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.t8rin.imagetoolbox.core.resources.icons.Save
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFilters
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
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
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import com.wanbaohe.markuplayers.presentation.components.EditBox
import com.wanbaohe.markuplayers.presentation.components.LayersFloatingPanel
import com.wanbaohe.markuplayers.presentation.components.LayersSheet
import com.wanbaohe.markuplayers.presentation.draw.DrawToolScreen
import com.wanbaohe.markuplayers.presentation.export.ExportSettingsSheet
import com.wanbaohe.markuplayers.presentation.render.LayerPreviewRenderers
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.wanbaohe.markuplayers.presentation.tools.EditorTool
import com.wanbaohe.markuplayers.presentation.tools.EditorTools
import com.wanbaohe.markuplayers.presentation.tools.adjust.AdjustToolSheet
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import com.wanbaohe.markuplayers.presentation.tools.ai.AiToolSheet
import com.wanbaohe.markuplayers.presentation.tools.basic.BasicToolsSheet
import com.wanbaohe.markuplayers.presentation.tools.crop.CropToolScreen
import com.wanbaohe.markuplayers.presentation.tools.shape.ShapeToolSheet
import com.wanbaohe.markuplayers.presentation.tools.sticker.StickerToolSheet
import com.wanbaohe.markuplayers.presentation.tools.text.TextToolScreen
import kotlin.math.min
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * 主编辑界面(设计稿「图片创作」):基于 BaseScreen,全屏棋盘格背景,
 * 顶栏(返回/撤销/重做/更多菜单) + 画布区(左侧工具栏) + 底部主 Tab 栏与保存按钮,
 * 右侧浮动图层面板;更多菜单可进沉浸预览,沉浸下单击画布或返回键退出。
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
    var showBasicSheet by rememberSaveable { mutableStateOf(false) }
    var showAiSheet by rememberSaveable { mutableStateOf(false) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showAdjustSheet by rememberSaveable { mutableStateOf(false) }
    var showCanvasBackgroundSheet by rememberSaveable { mutableStateOf(false) }
    // 「下一个新形状」的默认样式,由形状面板维护,关闭面板后保留
    var shapeDefaultSpec by remember { mutableStateOf(ShapeSpec.default(ShapeKind.Rectangle)) }
    val immersiveModeState = rememberImmersiveModeState()

    // 返回键由 BaseScreen 处理:沉浸态先退沉浸,否则走这里
    val onBack = {
        if (component.haveChanges) {
            showExitDialog = true
        } else {
            component.resetState()
        }
    }

    val onToolClick: (EditorTool) -> Unit = { tool ->
        component.setActiveTool(tool.id)
        when (tool.id) {
            EditorTools.ID_SELECT -> component.selectLayer(null)
            // 文字为全屏工具页:新建默认文字图层并进入编辑会话
            EditorTools.ID_TEXT -> component.beginTextEditSession()
            EditorTools.ID_STICKER -> showStickerSheet = true
            EditorTools.ID_SHAPE -> showShapeSheet = true
            EditorTools.ID_LAYERS -> showLayersSheet = true
            EditorTools.ID_BASIC -> showBasicSheet = true
            EditorTools.ID_AI -> showAiSheet = true
            EditorTools.ID_FILTER -> showFilterSheet = true
            EditorTools.ID_ADJUST -> showAdjustSheet = true
            // FullScreen 工具由 activeToolId 驱动,下方直接切换全屏页
        }
    }

    val imageLayerPicker = rememberImagePicker { uri: Uri ->
        component.addLayer(
            MarkupLayer(type = LayerType.Image(imageData = uri))
        )
    }

    // 全屏工具页:覆盖主编辑界面,关闭时由工具页自行 setActiveTool(null)
    val fullScreenTool = component.activeToolId
        ?.let(EditorTools::byId)
        ?.takeIf { it.mode == EditorTool.Mode.FullScreen }
    when (fullScreenTool?.id) {
        EditorTools.ID_DRAW -> {
            DrawToolScreen(component = component)
            return
        }
        EditorTools.ID_TEXT -> {
            TextToolScreen(component = component)
            return
        }
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
            // 操作台背景:默认棋盘格透出底图透明区域,可切换纯色(画布背景设置)
            when (val bg = component.canvasBackground) {
                CanvasBackground.Checkerboard -> Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .transparencyChecker()
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
        foreground = {
            LayersFloatingPanel(
                component = component,
                onAddText = { EditorTools.byId(EditorTools.ID_TEXT)?.let(onToolClick) },
                onAddSticker = { EditorTools.byId(EditorTools.ID_STICKER)?.let(onToolClick) },
                onAddImage = { imageLayerPicker.pickImage() },
                onExpand = { EditorTools.byId(EditorTools.ID_LAYERS)?.let(onToolClick) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        },
        // 底部 Tab 栏自带 navigationBarsPadding,避免叠加
        showNavigationBarsPadding = false,
        // 顶栏不跟随全局玻璃效果,保持不透明 surface 底色,避免透出棋盘格
        supportGlassEffect = false,
        immersiveModeState = immersiveModeState,
        content = {
            EditorCanvas(
                component = component,
                immersiveModeState = immersiveModeState,
                onEditTextLayer = { layerId ->
                    component.beginTextEditSession(layerId)
                    component.setActiveTool(EditorTools.ID_TEXT)
                },
                onToolClick = onToolClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            AnimatedVisibility(visible = immersiveModeState.isUiVisible) {
                EditorBottomBar(
                    component = component,
                    onToolClick = onToolClick,
                    onSaveClick = { showExportSheet = true }
                )
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

    BasicToolsSheet(
        visible = showBasicSheet,
        component = component,
        onOpenCrop = { component.setActiveTool(EditorTools.ID_CROP) },
        onOpenFilter = { showFilterSheet = true },
        onDismiss = { showBasicSheet = false }
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
    onEditTextLayer: (String) -> Unit,
    onToolClick: (EditorTool) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberZoomState(maxScale = 10f)

    Box(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState),
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
                                .clipToBounds()
                        )
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
                                            tapSelectable = layer.type !is LayerType.Draw
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
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = immersiveModeState.isUiVisible,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            EditorSideBar(
                component = component,
                onToolClick = onToolClick,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

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

@Composable
private fun EditorBottomBar(
    component: MarkupLayersComponent,
    onToolClick: (EditorTool) -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // 不透明容器背景(顶部圆角),避免棋盘格透出,与顶栏风格协调
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        EditorTools.bottomTab.forEach { tool ->
            EditorToolItem(
                tool = tool,
                isActive = component.activeToolId == tool.id,
                onClick = { onToolClick(tool) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.width(8.dp))
        // 实心主色确认按钮(M3 Button 不受全局玻璃样式影响)
        Button(onClick = onSaveClick) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.markup_save))
        }
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

