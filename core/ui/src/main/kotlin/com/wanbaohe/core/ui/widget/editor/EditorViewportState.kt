/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.core.ui.widget.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

class EditorViewportState {
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
