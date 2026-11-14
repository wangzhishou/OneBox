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

import android.content.Context
import android.graphics.Bitmap
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.shifenmiao.app.watermarking.domain.HiddenWatermark
import com.shifenmiao.app.watermarking.domain.model.HiddenWatermarkParams
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkDetector
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkEmbedder
import com.watermark.androidwm.WatermarkBuilder
import com.watermark.androidwm.WatermarkDetector
import com.watermark.androidwm.bean.WatermarkImage
import com.watermark.androidwm.bean.WatermarkText
import com.watermark.androidwm.listener.BuildFinishListener
import com.watermark.androidwm.listener.DetectFinishListener
import com.watermark.androidwm.task.DetectionReturnValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AndroidHiddenWatermarkHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
) : HiddenWatermarkDetector<Bitmap>, HiddenWatermarkEmbedder<Bitmap> {
    @OptIn(InternalCoroutinesApi::class)
    override suspend fun embed(
        image: Bitmap,
        resolution: WatermarkResolution,
        params: HiddenWatermarkParams,
    ): Bitmap? {
        if (!params.enabled) return image
        val resolved = resolution.elements.firstOrNull {
            it.element.content is WatermarkContent.Text || it.element.content is WatermarkContent.Image
        } ?: return image
        val builder = WatermarkBuilder.create(context, image, false)
        when (val content = resolved.element.content) {
            is WatermarkContent.Text -> builder.loadWatermarkText(WatermarkText(resolved.text.orEmpty()))
            is WatermarkContent.Image -> {
                val watermark = imageGetter.getImage(content.imageData as Any, originalSize = false)
                    ?: return null
                builder.loadWatermarkImage(WatermarkImage(watermark))
            }
            else -> return image
        }
        return runSuspendCatching {
            suspendCancellableCoroutine { continuation ->
                builder.setInvisibleWMListener(
                    params.isLSB,
                    object : BuildFinishListener<Bitmap> {
                        override fun onSuccess(image: Bitmap) = continuation.resume(image)
                        override fun onFailure(reason: String) = continuation.resume(null)
                    },
                )
            }
        }.getOrNull()
    }

    @OptIn(InternalCoroutinesApi::class)
    override suspend fun detect(image: Bitmap): HiddenWatermark? = runSuspendCatching {
        suspendCancellableCoroutine { continuation ->
            WatermarkDetector.create(image, true).detect(
                object : DetectFinishListener {
                    override fun onSuccess(returnValue: DetectionReturnValue) = continuation.resume(
                        returnValue.watermarkBitmap?.let(HiddenWatermark::Image)
                            ?: returnValue.watermarkString?.takeIf(String::isNotEmpty)
                                ?.let(HiddenWatermark::Text)
                    )

                    override fun onFailure(message: String?) = continuation.resume(null)
                }
            )
        }
    }.getOrNull()
}
