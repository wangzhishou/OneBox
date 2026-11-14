/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkEmbedder
import com.shifenmiao.app.watermarking.domain.render.WatermarkRenderer
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BitmapWatermarkRenderer @Inject constructor(
    private val visualFactory: WatermarkVisualFactory<Bitmap>,
    private val hiddenWatermarkEmbedder: HiddenWatermarkEmbedder<Bitmap>,
    dispatchersHolder: DispatchersHolder,
) : WatermarkRenderer<Bitmap>, DispatchersHolder by dispatchersHolder {
    override suspend fun render(
        image: Bitmap,
        resolution: WatermarkResolution,
        hiddenWatermark: HiddenWatermarkParams,
    ): Bitmap? = withContext(defaultDispatcher) {
        val output = image.copy(Bitmap.Config.ARGB_8888, true) ?: return@withContext null
        val canvas = Canvas(output)
        resolution.elements.forEach { resolved ->
            if (!resolved.element.transform.visible) return@forEach
            val visual = visualFactory.create(resolved, output.width, output.height)
                ?: return@forEach
            if (resolved.element.layout.isRepeated) {
                WatermarkDrawEngine.drawRepeated(canvas, visual, resolved.element)
            } else {
                WatermarkDrawEngine.drawSingle(canvas, visual, resolved.element)
            }
            visual.image.recycle()
        }
        hiddenWatermarkEmbedder.embed(output, resolution, hiddenWatermark)
    }
}
