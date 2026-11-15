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
import android.graphics.Paint
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkAnchor
import com.shifenmiao.app.watermarking.domain.model.WatermarkFrame
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkEmbedder
import com.shifenmiao.app.watermarking.domain.render.WatermarkRenderer
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import kotlinx.coroutines.CancellationException
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
        val frame = resolution.frame
        // 带高按「图片宽度」成比例，与旧相机水印渲染器、预览共用同一套护栏。
        val bandHeight = when (frame) {
            is WatermarkFrame.BottomBand -> frame.bandHeightFor(image.width)
            WatermarkFrame.InPlace -> 0
        }
        val output = Bitmap.createBitmap(
            image.width,
            image.height + bandHeight,
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(output)
        canvas.drawBitmap(image, 0f, 0f, null)
        if (frame is WatermarkFrame.BottomBand && bandHeight > 0) {
            canvas.drawRect(
                0f,
                image.height.toFloat(),
                image.width.toFloat(),
                output.height.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = frame.color
                    style = Paint.Style.FILL
                },
            )
        }
        val photoRect = RectF(0f, 0f, image.width.toFloat(), image.height.toFloat())
        val bandRect = RectF(
            0f,
            image.height.toFloat(),
            image.width.toFloat(),
            output.height.toFloat(),
        )
        resolution.elements.forEach { resolved ->
            if (!resolved.element.transform.visible) return@forEach
            val inBand = resolved.element.anchor == WatermarkAnchor.BAND && bandHeight > 0
            val rect = if (inBand) bandRect else photoRect
            // BAND 元素的字号 / 图片尺寸以带内容为基准，不随原图宽高比漂移。
            val sizeBasis = if (inBand) bandHeight else image.width
            val visual = try {
                visualFactory.create(
                    resolved = resolved,
                    canvasWidth = image.width,
                    canvasHeight = rect.height().toInt().coerceAtLeast(1),
                    sizeBasisWidth = sizeBasis,
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                null
            } ?: return@forEach
            if (resolved.element.layout.isRepeated) {
                WatermarkDrawEngine.drawRepeated(canvas, visual, resolved.element)
            } else {
                WatermarkDrawEngine.drawSingle(canvas, visual, resolved.element, rect)
            }
            visual.image.recycle()
        }
        // 隐形水印在库内部生成一张新位图。嵌入失败(文本过长、图片读取失败等)时
        // 仍要交付带可见水印的原图,不能让整张图的导出直接失败。
        val embedded = try {
            hiddenWatermarkEmbedder.embed(output, resolution, hiddenWatermark) ?: output
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            output
        }
        if (embedded !== output) output.recycle()
        embedded
    }
}
