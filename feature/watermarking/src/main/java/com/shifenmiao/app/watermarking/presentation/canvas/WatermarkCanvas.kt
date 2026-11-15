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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WatermarkCanvas(
    bitmap: Bitmap?,
    resolvedElements: List<ResolvedWatermarkElement>,
    selectedElementId: String?,
    isLoading: Boolean,
    isComparing: Boolean,
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
    createVisuals: suspend (Int, Int) -> Map<String, WatermarkVisual<Bitmap>>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val viewport = remember { EditorViewportState() }
        val density = LocalDensity.current
        val paddingPx = with(density) { 16.dp.roundToPx() } * 2
        val fittedSize = bitmap?.let {
            val fit = min(
                (constraints.maxWidth - paddingPx).coerceAtLeast(1) / it.width.toFloat(),
                (constraints.maxHeight - paddingPx).coerceAtLeast(1) / it.height.toFloat(),
            )
            IntSize(
                (it.width * fit).toInt().coerceAtLeast(1),
                (it.height * fit).toInt().coerceAtLeast(1),
            )
        } ?: IntSize.Zero
        val fittedSourceScale = bitmap?.let {
            min(fittedSize.width / it.width.toFloat(), fittedSize.height / it.height.toFloat())
        }?.takeIf { it > 0f } ?: 1f
        SideEffect {
            viewport.configure(
                layoutSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()),
                contentSize = fittedSize,
                actualSizeScale = 1f / fittedSourceScale,
            )
        }
        LaunchedEffect(bitmap) { viewport.fit() }
        val visualKey = resolvedElements.map {
            listOf(it.element.id, it.text, it.imageData, it.element.content, it.element.style)
        }
        val visuals by produceState<Map<String, WatermarkVisual<Bitmap>>>(
            initialValue = emptyMap(),
            visualKey,
            fittedSize,
        ) {
            value = withContext(Dispatchers.Default) {
                createVisuals(fittedSize.width, fittedSize.height)
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
                                    else onSelectElement(null)
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
                val sourceImage = remember(source) { source.asImageBitmap() }
                val originalDescription = stringResource(R.string.original)
                Box(
                    modifier = Modifier
                        .size(with(density) { fittedSize.width.toDp() }, with(density) { fittedSize.height.toDp() })
                        .clipToBounds()
                        .transparencyChecker(),
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { contentDescription = originalDescription },
                    ) {
                        // 底图外扩 2~16px 再裁回画布:居中/缩放会把图片边缘落在亚像素上,
                        // 双线性采样吸到透明像素后,棋盘格会从边缘漏出一条细线。
                        // 与图片创作 EditorCanvas 同款处理,任意 density/缩放都不漏。
                        val minScale = min(viewport.scale, 1f).coerceAtLeast(0.05f)
                        val pad = (2f / minScale).roundToInt().coerceIn(2, 16)
                        drawImage(
                            image = sourceImage,
                            dstOffset = IntOffset(-pad, -pad),
                            dstSize = IntSize(
                                size.width.roundToInt() + pad * 2,
                                size.height.roundToInt() + pad * 2,
                            ),
                        )
                    }
                    if (!isComparing) {
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            resolvedElements.forEach { resolved ->
                                val element = resolved.element
                                if (!element.transform.visible) return@forEach
                                val visual = visuals[element.id] ?: return@forEach
                                // 平铺层只画在全屏 Canvas 上,不接收点击:否则它会盖住
                                // 其它图层,连"点空白取消选中"都会失效。
                                if (element.layout.isRepeated) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawIntoCanvas { canvas ->
                                            WatermarkDrawEngine.drawRepeated(
                                                canvas.nativeCanvas,
                                                visual,
                                                element,
                                            )
                                        }
                                    }
                                }
                                // 平铺层的选框锚在平铺相位中心。单图层的内容直接放在选框内,
                                // 位移、缩放、旋转和翻转都只由同一个容器处理。
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
                                    canvasScale = viewport.scale,
                                ) {
                                    val contentModifier = Modifier.size(
                                        with(density) { visual.width.toDp() },
                                        with(density) { visual.height.toDp() },
                                    )
                                    if (element.layout.isRepeated) {
                                        Box(modifier = contentModifier)
                                    } else {
                                        Canvas(modifier = contentModifier) {
                                            drawIntoCanvas { canvas ->
                                                WatermarkDrawEngine.drawContent(
                                                    canvas.nativeCanvas,
                                                    visual,
                                                    element,
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
            VerticalDraggable(
                containerHeightPx = constraints.maxHeight.toFloat(),
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                EditorToolRail(
                    tools = WatermarkKind.entries.map { kind ->
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
