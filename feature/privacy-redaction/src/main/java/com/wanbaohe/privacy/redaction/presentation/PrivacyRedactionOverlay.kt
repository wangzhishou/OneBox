/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.model.editor.LayerTransform
import com.wanbaohe.core.ui.widget.editor.EditorLayerBox
import com.wanbaohe.core.ui.widget.editor.EditorLayerBoxLabels
import com.wanbaohe.privacy.redaction.R
import com.wanbaohe.privacy.redaction.domain.model.PrivacyDocument
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRect
import com.wanbaohe.privacy.redaction.domain.model.PrivacyRegion
import com.t8rin.imagetoolbox.core.resources.R as CoreR

@Composable
fun BoxWithConstraintsScope.PrivacyRedactionOverlay(
    document: PrivacyDocument,
    selectedRegionId: String?,
    isDrawing: Boolean,
    gesturesEnabled: Boolean,
    viewportScale: Float,
    onSelectRegion: (String?) -> Unit,
    onBoundsChange: (String, PrivacyRect) -> Unit,
    onDeleteRegion: (String) -> Unit,
    onAddRegion: (PrivacyRect) -> Unit,
) {
    var draftStart by remember { mutableStateOf(Offset.Zero) }
    var draftEnd by remember { mutableStateOf(Offset.Zero) }
    var hasDraft by remember { mutableStateOf(false) }
    val accent = MaterialTheme.colorScheme.onPrimaryContainer
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    if (isDrawing && gesturesEnabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isDrawing) {
                    detectDragGestures(
                        onDragStart = { start ->
                            draftStart = start
                            draftEnd = start
                            hasDraft = true
                        },
                        onDrag = { change, _ ->
                            draftEnd = change.position
                            change.consume()
                        },
                        onDragEnd = {
                            PrivacyRect.fromCorners(
                                startX = draftStart.x / size.width,
                                startY = draftStart.y / size.height,
                                endX = draftEnd.x / size.width,
                                endY = draftEnd.y / size.height,
                            )?.let(onAddRegion)
                            hasDraft = false
                        },
                        onDragCancel = { hasDraft = false },
                    )
                },
        )
    }
    document.regions.forEach { region ->
        key(region.id) {
            PrivacyRegionBox(
                region = region,
                isSelected = selectedRegionId == region.id,
                gesturesEnabled = gesturesEnabled && !isDrawing,
                viewportScale = viewportScale,
                accent = accent,
                muted = muted,
                onSelect = { onSelectRegion(region.id) },
                onBoundsChange = { bounds -> onBoundsChange(region.id, bounds) },
                onDelete = { onDeleteRegion(region.id) },
            )
        }
    }
    if (hasDraft) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val left = minOf(draftStart.x, draftEnd.x)
            val top = minOf(draftStart.y, draftEnd.y)
            drawRect(
                color = accent,
                topLeft = Offset(left, top),
                size = Size(
                    kotlin.math.abs(draftEnd.x - draftStart.x),
                    kotlin.math.abs(draftEnd.y - draftStart.y),
                ),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.PrivacyRegionBox(
    region: PrivacyRegion,
    isSelected: Boolean,
    gesturesEnabled: Boolean,
    viewportScale: Float,
    accent: androidx.compose.ui.graphics.Color,
    muted: androidx.compose.ui.graphics.Color,
    onSelect: () -> Unit,
    onBoundsChange: (PrivacyRect) -> Unit,
    onDelete: () -> Unit,
) {
    val bounds = region.bounds
    val regionWidth = maxWidth * bounds.width
    val regionHeight = maxHeight * bounds.height
    EditorLayerBox(
        transform = LayerTransform(
            centerX = bounds.centerX,
            centerY = bounds.centerY,
            alpha = if (region.isEnabled) 1f else 0.45f,
        ),
        isSelected = isSelected,
        onSelect = onSelect,
        onTransformEnd = { transform ->
            onBoundsChange(bounds.resized(transform.centerX, transform.centerY, transform.scale))
        },
        onDelete = onDelete,
        labels = EditorLayerBoxLabels(
            scale = stringResource(R.string.privacy_scale),
            rotate = stringResource(R.string.privacy_rotate),
            resizeWidth = stringResource(R.string.privacy_resize),
            delete = stringResource(CoreR.string.delete),
        ),
        gesturesEnabled = gesturesEnabled,
        showHandles = isSelected,
        canvasScale = viewportScale,
        rotationEnabled = false,
    ) {
        Box(
            modifier = Modifier
                .size(regionWidth, regionHeight)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (region.isEnabled) accent else muted,
                ),
        )
    }
}
