/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.canvas

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import com.shifenmiao.common.ui.ImmersiveModeState
import com.shifenmiao.model.editor.LayerTransform
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomIn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineZoomOut
import com.shifenmiao.app.watermarking.data.render.WatermarkDrawEngine
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkFrame
import com.shifenmiao.app.watermarking.domain.model.WatermarkHorizontalAlignment
import com.shifenmiao.app.watermarking.domain.model.WatermarkKind
import com.shifenmiao.app.watermarking.domain.model.kind
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisual
import com.shifenmiao.app.watermarking.presentation.model.icon
import com.shifenmiao.app.watermarking.presentation.model.title
import com.wanbaohe.core.ui.widget.editor.EditorLayerBox
import com.wanbaohe.core.ui.widget.editor.EditorLayerBoxLabels
import com.wanbaohe.core.ui.widget.editor.EditorRailTool
import com.wanbaohe.core.ui.widget.editor.EditorToolRail
import com.wanbaohe.core.ui.widget.editor.EditorViewportState
import com.wanbaohe.core.ui.widget.editor.VerticalDraggable
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRect
import com.wanbaohe.privacy.redaction.presentation.PrivacyRedactionOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WatermarkCanvas(
    bitmap: Bitmap?,
    frame: WatermarkFrame = WatermarkFrame.InPlace,
    resolvedElements: List<ResolvedWatermarkElement>,
    selectedElementId: String?,
    isLoading: Boolean,
    isComparing: Boolean,
    isToolRailVisible: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    immersiveModeState: ImmersiveModeState,
    onCompareChange: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSelectElement: (String?) -> Unit,
    onTransformStart: () -> Unit,
    onTransformChange: (String, LayerTransform) -> Unit,
    onTransformEnd: (String, LayerTransform) -> Unit,
    onDeleteElement: (String) -> Unit,
    onEditElement: (String) -> Unit,
    onAddElement: (WatermarkKind) -> Unit,
    imageIndex: Int,
    imageCount: Int,
    onPreviousImage: () -> Unit,
    onNextImage: () -> Unit,
    hasElements: Boolean,
    hiddenOnly: Boolean,
    privacyDocument: PrivacyDocument? = null,
    selectedPrivacyRegionId: String? = null,
    isPrivacyDrawing: Boolean = false,
    onSelectPrivacyRegion: (String?) -> Unit = {},
    onPrivacyBoundsChange: (String, PrivacyRect) -> Unit = { _, _ -> },
    onDeletePrivacyRegion: (String) -> Unit = {},
    onAddPrivacyRegion: (PrivacyRect) -> Unit = {},
    createPrivacyPreview: suspend (Bitmap) -> Bitmap = { it },
    createVisuals: suspend (Int, Int, Int) -> Map<String, WatermarkVisual<Bitmap>>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val viewport = remember { EditorViewportState() }
        val density = LocalDensity.current
        val paddingPx = with(density) { 16.dp.roundToPx() } * 2
        // BottomBand 的输出画布 = 原图 + 下方白带。预览必须按「整张输出画布」做 fit，
        // 否则白带会被挤出视口，PHOTO / BAND 两套归一化坐标也无法与导出对齐。
        val bandColor = (frame as? WatermarkFrame.BottomBand)?.color ?: -1
        val canvasSize = bitmap?.let {
            val outputWidth = it.width
            // 护栏按源图宽度算，再随 fit 一起缩放；预览与导出因此保持同一比例。
            val sourceBandHeight = (frame as? WatermarkFrame.BottomBand)
                ?.bandHeightFor(outputWidth) ?: 0
            val outputHeight = (it.height + sourceBandHeight).coerceAtLeast(1)
            val fit = min(
                (constraints.maxWidth - paddingPx).coerceAtLeast(1) / outputWidth.toFloat(),
                (constraints.maxHeight - paddingPx).coerceAtLeast(1) / outputHeight.toFloat(),
            )
            IntSize(
                (outputWidth * fit).toInt().coerceAtLeast(1),
                (outputHeight * fit).toInt().coerceAtLeast(1),
            )
        } ?: IntSize.Zero
        // 白带高度取差值，保证 photoHeight + bandHeight 精确等于 canvasSize.height，
        // 不因两次取整在照片与白带之间漏出一条缝。
        val photoHeight = bitmap?.let {
            (it.height * (canvasSize.width / it.width.toFloat())).roundToInt()
                .coerceIn(1, canvasSize.height)
        } ?: 0
        val bandHeight = (canvasSize.height - photoHeight).coerceAtLeast(0)
        val fittedSourceScale = bitmap?.let { canvasSize.width / it.width.toFloat() }
            ?.takeIf { it > 0f } ?: 1f
        SideEffect {
            viewport.configure(
                layoutSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()),
                contentSize = canvasSize,
                actualSizeScale = 1f / fittedSourceScale,
            )
        }
        // 切模板会改变输出画布比例（多出白带），复位用户缩放才能完整看到新画布。
        LaunchedEffect(bitmap, frame) { viewport.fit() }
        val visualKey = resolvedElements.map {
            listOf(it.element.id, it.text, it.imageData, it.element.content, it.element.style)
        }
        val visuals by produceState<Map<String, WatermarkVisual<Bitmap>>>(
            initialValue = emptyMap(),
            visualKey,
            canvasSize,
            bandHeight,
            frame,
        ) {
            value = withContext(Dispatchers.Default) {
                createVisuals(canvasSize.width, canvasSize.height, bandHeight)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bitmap, isComparing, constraints.maxWidth, constraints.maxHeight) {
                    if (bitmap == null) return@pointerInput
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
                                    focalPoint = initial.calculateCentroid(),
                                )
                            }
                            if (multiTouch) initial.changes.forEach { it.consume() }
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val consumed = event.changes.any { it.isConsumed }
                            if (!multiTouch && !consumed && viewport.scale > 1f) {
                                accumulatedPan += event.calculatePan()
                                if (accumulatedPan.getDistance() > viewConfiguration.touchSlop) moved = true
                                if (moved) {
                                    viewport.applyGesture(
                                        pan = event.calculatePan(),
                                        zoom = 1f,
                                        focalPoint = Offset(
                                            constraints.maxWidth / 2f,
                                            constraints.maxHeight / 2f,
                                        ),
                                    )
                                    event.changes.forEach { it.consume() }
                                }
                            }
                            if (event.changes.none { it.pressed }) {
                                if (!multiTouch && !moved && !consumed && !isComparing) {
                                    if (immersiveModeState.isImmersive) immersiveModeState.exitImmersive()
                                    else {
                                        onSelectElement(null)
                                        onSelectPrivacyRegion(null)
                                    }
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
            contentAlignment = Alignment.Center,
        ) {
            bitmap?.let { source ->
                val privacyPreview by produceState<Bitmap?>(null, source, privacyDocument) {
                    val rendered = if (privacyDocument == null) source else createPrivacyPreview(source)
                    value = rendered
                    awaitDispose {
                        if (rendered !== source && !rendered.isRecycled) {
                            value = source
                            rendered.recycle()
                        }
                    }
                }
                val displayedBitmap = if (isComparing) source else privacyPreview ?: source
                val displayedImage = remember(displayedBitmap) { displayedBitmap.asImageBitmap() }
                val originalDescription = stringResource(R.string.original)
                val canvasWidthDp = with(density) { canvasSize.width.toDp() }
                val canvasHeightDp = with(density) { canvasSize.height.toDp() }
                val photoHeightDp = with(density) { photoHeight.toDp() }
                val bandHeightDp = with(density) { bandHeight.toDp() }
                // 没有白带时 BAND 元素一律降级回 PHOTO，避免出现「元素存在但落在画布外」。
                val (photoElements, bandElements) = resolvedElements.partition { resolved ->
                    resolved.element.anchor != WatermarkAnchor.BAND || bandHeight == 0
                }
                Column(
                    modifier = Modifier.size(canvasWidthDp, canvasHeightDp),
                ) {
                    // PHOTO 区：容器 constraints 恰好等于导出的 photoRect，
                    // centerX/centerY 只在该 rect 内归一化，与 drawSingle 的换算一致。
                    Box(
                        modifier = Modifier
                            .size(canvasWidthDp, photoHeightDp)
                            .zIndex(
                                if (photoElements.any { it.element.id == selectedElementId }) 1f else 0f
                            )
                            .transparencyChecker(),
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .semantics { contentDescription = originalDescription },
                        ) {
                            // 底图外扩 2~16px 再裁回画布:居中/缩放会把图片边缘落在亚像素上,
                            // 双线性采样吸到透明像素后,棋盘格会从边缘漏出一条细线。
                            // 与图片创作 EditorCanvas 同款处理,任意 density/缩放都不漏。
                            val minScale = min(viewport.scale, 1f).coerceAtLeast(0.05f)
                            val pad = (2f / minScale).roundToInt().coerceIn(2, 16)
                            drawImage(
                                image = displayedImage,
                                dstOffset = IntOffset(-pad, -pad),
                                dstSize = IntSize(
                                    size.width.roundToInt() + pad * 2,
                                    size.height.roundToInt() + pad * 2,
                                ),
                            )
                        }
                        if (!isComparing) {
                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                WatermarkElementLayers(
                                    elements = photoElements,
                                    visuals = visuals,
                                    selectedElementId = selectedElementId,
                                    immersiveModeState = immersiveModeState,
                                    viewportScale = viewport.scale,
                                    onSelectElement = onSelectElement,
                                    onEditElement = onEditElement,
                                    onTransformStart = onTransformStart,
                                    onTransformChange = onTransformChange,
                                    onTransformEnd = onTransformEnd,
                                    onDeleteElement = onDeleteElement,
                                )
                                if (privacyDocument != null && !immersiveModeState.isImmersive) {
                                    PrivacyRedactionOverlay(
                                        document = privacyDocument,
                                        selectedRegionId = selectedPrivacyRegionId,
                                        isDrawing = isPrivacyDrawing,
                                        gesturesEnabled = !isLoading,
                                        viewportScale = viewport.scale,
                                        onSelectRegion = onSelectPrivacyRegion,
                                        onBoundsChange = onPrivacyBoundsChange,
                                        onDeleteRegion = onDeletePrivacyRegion,
                                        onAddRegion = onAddPrivacyRegion,
                                    )
                                }
                            }
                        }
                    }
                    // BAND 区：白底 + 独立的归一化空间，元素不随原图宽高比漂移。
                    if (bandHeight > 0) {
                        Box(
                            modifier = Modifier
                                .size(canvasWidthDp, bandHeightDp)
                                .zIndex(
                                    if (bandElements.any { it.element.id == selectedElementId }) 1f else 0f
                                )
                                .background(Color(bandColor)),
                        ) {
                            if (!isComparing) {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    WatermarkElementLayers(
                                        elements = bandElements,
                                        visuals = visuals,
                                        selectedElementId = selectedElementId,
                                        immersiveModeState = immersiveModeState,
                                        viewportScale = viewport.scale,
                                        onSelectElement = onSelectElement,
                                        onEditElement = onEditElement,
                                        onTransformStart = onTransformStart,
                                        onTransformChange = onTransformChange,
                                        onTransformEnd = onTransformEnd,
                                        onDeleteElement = onDeleteElement,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (immersiveModeState.isUiVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .glassBackground(GlassStyle.None, ShapeDefaults.circle, MaterialTheme.colorScheme.surfaceContainer)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CanvasControl(Icons.Outlined.LineZoomOut, R.string.watermark_zoom_out, viewport.scale > viewport.minScale) {
                    viewport.zoomTo(viewport.scale - 0.5f)
                }
                CanvasControl(MaterialIcons.Outlined.FitScreen, R.string.watermark_zoom_fit) { viewport.fit() }
                CanvasControl(MaterialIcons.Outlined.ZoomOutMap, R.string.watermark_actual_size) {
                    viewport.zoomTo(1f / fittedSourceScale)
                }
                CanvasControl(Icons.Outlined.LineZoomIn, R.string.watermark_zoom_in, viewport.scale < viewport.maxScale) {
                    viewport.zoomTo(viewport.scale + 0.5f)
                }
                CanvasControl(Icons.Outlined.LineUndo, R.string.undo, canUndo) { onUndo() }
                CanvasControl(Icons.Outlined.LineRedo, R.string.redo, canRedo) { onRedo() }
                HoldCanvasControl(
                    icon = Icons.Outlined.LineCompare,
                    descriptionRes = R.string.watermark_compare_hold,
                    active = isComparing,
                    onHold = onCompareChange,
                )
            }
            if (privacyDocument == null && isToolRailVisible) {
                VerticalDraggable(
                    containerHeightPx = constraints.maxHeight.toFloat(),
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    EditorToolRail(
                        // 模板专属元素由预设带入，不从工具栏新建（属性面板尚未提供对应编辑器）。
                        tools = WatermarkKind.entries
                            .filter {
                                it != WatermarkKind.CAMERA_PARAMS && it != WatermarkKind.DIVIDER
                            }.map { kind ->
                                EditorRailTool(kind.name, kind.icon(), kind.title())
                            },
                        activeId = resolvedElements.firstOrNull {
                            it.element.id == selectedElementId
                        }?.element?.content?.kind?.name,
                        onToolClick = { onAddElement(WatermarkKind.valueOf(it.id)) },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        if (!immersiveModeState.isUiVisible) {
            EnhancedIconButton(
                onClick = { immersiveModeState.exitImmersive() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .glassBackground(GlassStyle.None, ShapeDefaults.circle, MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Icon(
                    MaterialIcons.Outlined.Close,
                    contentDescription = stringResource(R.string.watermark_exit_immersive),
                )
            }
        } else if (bitmap != null && !isLoading && !isComparing) {
            val hint = when {
                privacyDocument != null -> null
                !hasElements -> R.string.watermark_empty_canvas
                hiddenOnly -> R.string.watermark_hidden_only
                else -> null
            }
            if (hint != null) {
                Text(
                    text = stringResource(hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 72.dp)
                        .glassBackground(GlassStyle.None, ShapeDefaults.default, MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        if (imageCount > 1 && immersiveModeState.isUiVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .glassBackground(GlassStyle.None, ShapeDefaults.circle, MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CanvasControl(MaterialIcons.Outlined.ChevronLeft, R.string.watermark_previous_image) { onPreviousImage() }
                Text(
                    text = stringResource(R.string.watermark_image_index, imageIndex.coerceAtLeast(1), imageCount),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                CanvasControl(MaterialIcons.Outlined.ChevronRight, R.string.watermark_next_image) { onNextImage() }
            }
        }
    }
}

/**
 * 单个锚定空间内的可交互图层。调用方必须把它放进一个尺寸等于导出 rect 的
 * BoxWithConstraints：EditorLayerBox 用容器 constraints 归一化 centerX/centerY，
 * 预览拖拽与 drawSingle 的 rect 换算才会是同一套坐标。
 */
@Composable
private fun BoxWithConstraintsScope.WatermarkElementLayers(
    elements: List<ResolvedWatermarkElement>,
    visuals: Map<String, WatermarkVisual<Bitmap>>,
    selectedElementId: String?,
    immersiveModeState: ImmersiveModeState,
    viewportScale: Float,
    onSelectElement: (String?) -> Unit,
    onEditElement: (String) -> Unit,
    onTransformStart: () -> Unit,
    onTransformChange: (String, LayerTransform) -> Unit,
    onTransformEnd: (String, LayerTransform) -> Unit,
    onDeleteElement: (String) -> Unit,
) {
    val density = LocalDensity.current
    elements.forEach { resolved ->
        val element = resolved.element
        if (!element.transform.visible) return@forEach
        val visual = visuals[element.id] ?: return@forEach
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                if (element.layout.isRepeated) {
                    WatermarkDrawEngine.drawRepeated(canvas.nativeCanvas, visual, element)
                } else {
                    WatermarkDrawEngine.drawSingle(
                        canvas = canvas.nativeCanvas,
                        visual = visual,
                        element = element,
                        rect = RectF(0f, 0f, size.width, size.height),
                    )
                }
            }
        }
    }
    // 内容与操作层分离：编辑时允许越界，手柄始终盖在内容上；导出由输出位图裁切。
    elements.forEach { resolved ->
        val element = resolved.element
        if (!element.transform.visible) return@forEach
        val visual = visuals[element.id] ?: return@forEach
        EditorLayerBox(
            transform = element.transform,
            isSelected = selectedElementId == element.id && !immersiveModeState.isImmersive,
            onSelect = { onSelectElement(element.id) },
            onEditRequest = { onEditElement(element.id) },
            onTransformStart = onTransformStart,
            onTransformChange = { onTransformChange(element.id, it) },
            onTransformEnd = { onTransformEnd(element.id, it) },
            onDelete = { onDeleteElement(element.id) },
            labels = EditorLayerBoxLabels(
                scale = stringResource(R.string.watermark_scale),
                rotate = stringResource(R.string.watermark_rotate),
                resizeWidth = stringResource(R.string.watermark_resize),
                delete = stringResource(R.string.delete),
            ),
            gesturesEnabled = !immersiveModeState.isImmersive,
            showHandles = selectedElementId == element.id,
            canvasScale = viewportScale,
            startAligned = element.horizontalAlignment == WatermarkHorizontalAlignment.START,
            allowOutside = true,
        ) {
            Box(
                modifier = Modifier.size(
                    with(density) { visual.width.toDp() },
                    with(density) { visual.height.toDp() },
                )
            )
        }
    }
}

@Composable
private fun HoldCanvasControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descriptionRes: Int,
    active: Boolean,
    onHold: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .pointerInput(onHold) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    down.consume()
                    onHold(true)
                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                            if (event.changes.none { it.pressed }) break
                        }
                    } finally {
                        onHold(false)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        EnhancedIconButton(
            onClick = {},
            containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            modifier = Modifier.size(38.dp),
        ) {
            Icon(icon, contentDescription = stringResource(descriptionRes), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CanvasControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descriptionRes: Int,
    enabled: Boolean = true,
    containerColor: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    EnhancedIconButton(
        onClick = onClick,
        enabled = enabled,
        containerColor = containerColor,
        modifier = Modifier.size(38.dp),
    ) {
        Icon(icon, contentDescription = stringResource(descriptionRes), modifier = Modifier.size(18.dp))
    }
}
