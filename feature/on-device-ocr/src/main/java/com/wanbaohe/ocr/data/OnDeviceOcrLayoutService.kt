/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.ocr.data

import android.graphics.Bitmap
import com.shifenmiao.interfaces.ocr.OcrLayoutService
import com.shifenmiao.interfaces.ocr.OcrTextLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/**
 * 把 PP-OCRv6 的版面结果暴露给需要坐标的调用方(PDF 扫描件可搜索文字层).
 */
@Singleton
internal class OnDeviceOcrLayoutService @Inject constructor(
    private val modelStore: OcrModelStore,
    private val engine: PpOcrV6Engine,
) : OcrLayoutService {

    override fun isModelReady(): Boolean = modelStore.isReady()

    override suspend fun recognizeLines(bitmap: Bitmap): List<OcrTextLine> =
        withContext(Dispatchers.Default) {
            val longestSide = max(bitmap.width, bitmap.height)
            if (longestSide <= MAX_LONGEST_SIDE) {
                return@withContext recognizeLines(bitmap, coordinateScale = 1f)
            }

            // 300dpi 以上整页扫描件直接喂 OpenCV 要吃掉上百 MB, 先按长边缩一次, 坐标再折算回原图
            val scale = MAX_LONGEST_SIDE.toFloat() / longestSide
            val scaled = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true,
            )
            try {
                recognizeLines(scaled, coordinateScale = 1f / scale)
            } finally {
                if (scaled !== bitmap) scaled.recycle()
            }
        }

    private fun recognizeLines(
        bitmap: Bitmap,
        coordinateScale: Float,
    ): List<OcrTextLine> = engine.recognize(bitmap).mapNotNull { line ->
        if (line.text.isBlank() || line.corners.isEmpty()) {
            null
        } else {
            val right = line.corners.maxOf { it.x }
            val bottom = line.corners.maxOf { it.y }
            if (right <= line.left || bottom <= line.top) {
                null
            } else {
                OcrTextLine(
                    text = line.text,
                    confidence = line.confidence,
                    left = floor(line.left * coordinateScale).toInt(),
                    top = floor(line.top * coordinateScale).toInt(),
                    right = ceil(right * coordinateScale).toInt(),
                    bottom = ceil(bottom * coordinateScale).toInt(),
                )
            }
        }
    }

    private companion object {
        const val MAX_LONGEST_SIDE = 2600
    }
}
