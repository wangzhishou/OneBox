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

package com.shifenmiao.interfaces.ocr

import android.graphics.Bitmap

/**
 * 一行文字及其外框, 坐标为位图像素坐标(左上原点, y 向下)。
 */
data class OcrTextLine(
    val text: String,
    val confidence: Float,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

/**
 * 带版面的端侧文字识别.
 *
 * 与 [TextRecognitionService] 的分工:
 * - [TextRecognitionService] 只给纯文本(拍照取字的结果页);
 * - [OcrLayoutService] 还要每行坐标, 给 PDF 扫描件的可搜索文字层 / 选区用.
 */
interface OcrLayoutService {

    fun isModelReady(): Boolean

    suspend fun recognizeLines(bitmap: Bitmap): List<OcrTextLine>
}
