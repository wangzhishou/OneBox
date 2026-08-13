package com.wanbaohe.markuplayers.presentation.editor

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.Dots
import com.t8rin.imagetoolbox.core.resources.icons.Save
import com.t8rin.imagetoolbox.core.resources.icons.Share
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBar
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.tappable
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.t8rin.imagetoolbox.core.ui.widget.text.marquee
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
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import com.wanbaohe.markuplayers.presentation.tools.ai.AiToolSheet
import com.wanbaohe.markuplayers.presentation.tools.basic.BasicToolsSheet
import com.wanbaohe.markuplayers.presentation.tools.crop.CropToolScreen
import com.wanbaohe.markuplayers.presentation.tools.shape.ShapeToolSheet
import com.wanbaohe.markuplayers.presentation.tools.sticker.StickerToolSheet
import com.wanbaohe.markuplayers.presentation.tools.text.TextToolScreen
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

/**
 * 主编辑界面(设计稿「图片创作」):
 * 顶栏(返回/撤销/重做/更多) + 画布区(左侧工具栏、底部缩放胶囊) + 底部主 Tab 栏与保存按钮。
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
    // 「下一个新形状」的默认样式,由形状面板维护,关闭面板后保留
    var shapeDefaultSpec by remember { mutableStateOf(ShapeSpec.default(ShapeKind.Rectangle)) }
    var placeholderToolId by rememberSaveable { mutableStateOf<String?>(null) }

    val onBack = {
        if (component.haveChanges) {
            showExitDialog = true
        } else {
            component.resetState()
        }
    }
    BackHandler(onBack = onBack)

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
            // FullScreen 工具由 activeToolId 驱动,下方直接切换全屏页
            else -> if (tool.mode == EditorTool.Mode.Sheet) placeholderToolId = tool.id
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

    Column(modifier = Modifier.fillMaxSize()) {
        EditorTopBar(
            component = component,
            onBack = onBack
        )
        EditorCanvas(
            component = component,
            onEditTextLayer = { layerId ->
                component.beginTextEditSession(layerId)
                component.setActiveTool(EditorTools.ID_TEXT)
            },
            onToolClick = onToolClick,
            onAddImageLayer = { imageLayerPicker.pickImage() },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        EditorBottomBar(
            component = component,
            onToolClick = onToolClick,
            onSaveClick = { showExportSheet = true }
        )
    }

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
        onDismiss = { showBasicSheet = false }
    )

    AiToolSheet(
        visible = showAiSheet,
        onDismiss = { showAiSheet = false }
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

    val placeholderTool = placeholderToolId?.let(EditorTools::byId)
    EnhancedModalBottomSheet(
        visible = placeholderTool != null,
        onDismiss = { placeholderToolId = null },
        sheetContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                placeholderTool?.let { tool ->
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(tool.titleRes),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.markup_coming_soon),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    )

    ExitWithoutSavingDialog(
        onExit = { component.resetState() },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}

@Composable
private fun EditorTopBar(
    component: MarkupLayersComponent,
    onBack: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    EnhancedTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.markup_editor_title),
                modifier = Modifier.marquee()
            )
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
    )
}

@Composable
private fun EditorCanvas(
    component: MarkupLayersComponent,
    onEditTextLayer: (String) -> Unit,
    onToolClick: (EditorTool) -> Unit,
    onAddImageLayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zoomState = rememberZoomState(maxScale = 10f)
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    val scope = rememberCoroutineScope()
    val zoomBy: (Float) -> Unit = { factor ->
        val target = (zoomState.scale * factor).coerceIn(1f, 10f)
        val pivot = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
        scope.launch { zoomState.changeScale(target, pivot) }
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { viewportSize = it }
            .tappable { component.selectLayer(null) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = component.bitmap
            if (bitmap != null) {
                val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
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
                        modifier = Modifier.size(
                            width = with(density) { canvasWidthPx.toDp() },
                            height = with(density) { canvasHeightPx.toDp() }
                        )
                    ) {
                        Picture(
                            model = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            colorFilter = baseColorFilter,
                            modifier = Modifier
                                .matchParentSize()
                                .clipToBounds()
                                .transparencyChecker()
                        )
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

        EditorSideBar(
            component = component,
            onToolClick = onToolClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )

        LayersFloatingPanel(
            component = component,
            onAddText = { EditorTools.byId(EditorTools.ID_TEXT)?.let(onToolClick) },
            onAddSticker = { EditorTools.byId(EditorTools.ID_STICKER)?.let(onToolClick) },
            onAddImage = onAddImageLayer,
            onExpand = { EditorTools.byId(EditorTools.ID_LAYERS)?.let(onToolClick) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )

        ZoomPill(
            scale = zoomState.scale,
            onZoomOut = { zoomBy(1 / 1.25f) },
            onZoomIn = { zoomBy(1.25f) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
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
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f))
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
        EnhancedButton(
            onClick = onSaveClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
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

@Composable
private fun ZoomPill(
    scale: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        ZoomButton(label = "−", onClick = onZoomOut)
        Text(
            text = "${(scale * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        ZoomButton(label = "+", onClick = onZoomIn)
    }
}

@Composable
private fun ZoomButton(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}
