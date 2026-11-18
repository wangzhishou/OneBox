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

@file:OptIn(ExperimentalPdfApi::class)

package com.t8rin.imagetoolbox.feature.pdf_tools.data.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.pdf.ExperimentalPdfApi
import androidx.pdf.ocr.OcrProvider
import androidx.pdf.ocr.OcrResult
import androidx.pdf.ocr.OcrText
import com.shifenmiao.interfaces.ocr.OcrLayoutService
import com.shifenmiao.interfaces.ocr.OcrTextLine
import com.t8rin.imagetoolbox.core.utils.makeLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 把 App 自带的 PP-OCRv6 接到 androidx.pdf 的 OCR 接口上, 给扫描件铺可搜索文字层.
 *
 * 坐标约定: 返回的 [Rect] 必须是传入 [Bitmap] 的像素坐标(左上原点, y 向下),
 * pdf-viewer 会用 OcrContext.imageRect / bitmapSize 自己换算到页面坐标.
 *
 * 注意: androidx.pdf 自带的 MlKitOcrProvider 只认拉丁字母, 中文扫描件识别不出来,
 * 所以本模块在 build.gradle.kts 里排除了 pdf-ocr-play-services.
 */
internal class PdfPageOcrProvider(
    private val service: OcrLayoutService,
) : OcrProvider {

    override suspend fun recognizeText(bitmap: Bitmap): OcrResult =
        withContext(Dispatchers.Default) {
            val lines = runCatching { service.recognizeLines(bitmap) }
                .onFailure { it.makeLog("PdfPageOcrProvider") }
                .getOrDefault(emptyList())
            PdfOcrResult(lines)
        }

    override fun close() = Unit
}

/**
 * PP-OCR 只给整行文本 + 行外框, 没有字级坐标, 这里按字符类别宽度估算字级位置,
 * 用于 getWordAt / getSearchBounds 的高亮范围.
 */
internal class PdfOcrResult(
    lines: List<OcrTextLine>,
) : OcrResult {

    private val ocrLines: List<Line> = lines.mapNotNull { Line.of(it) }

    override fun getAllText(): OcrText = OcrText(
        text = ocrLines.joinToString("\n") { it.text },
        bounds = ocrLines.map { it.rect },
    )

    override fun getText(left: Int, top: Int, right: Int, bottom: Int): OcrText {
        val query = Rect(
            min(left, right),
            min(top, bottom),
            max(left, right),
            max(top, bottom),
        )
        val hits = ocrLines.filter { Rect.intersects(it.rect, query) }
        return OcrText(
            text = hits.joinToString("\n") { it.text },
            bounds = hits.map { it.rect },
        )
    }

    override fun getWordAt(x: Int, y: Int): OcrText {
        ocrLines.forEach { line ->
            line.words.firstOrNull { it.rect.contains(x, y) }?.let { word ->
                return OcrText(word.text, listOf(word.rect))
            }
        }
        return EMPTY_TEXT
    }

    override fun getSearchBounds(query: String, ignoreCase: Boolean): List<List<Rect>> {
        if (query.isEmpty()) return emptyList()
        val result = mutableListOf<List<Rect>>()
        ocrLines.forEach { line ->
            var index = line.text.indexOf(query, 0, ignoreCase)
            while (index >= 0) {
                result += listOf(line.rectOf(index, index + query.length))
                index = line.text.indexOf(query, index + 1, ignoreCase)
            }
        }
        return result
    }

    private class Line(
        val text: String,
        val rect: Rect,
        private val charLeft: IntArray,
        private val charRight: IntArray,
        val words: List<Word>,
    ) {

        fun rectOf(start: Int, end: Int): Rect {
            val from = start.coerceIn(0, text.length)
            val to = end.coerceIn(from, text.length)
            if (from == to) return rect
            val left = charLeft[from]
            val right = charRight[to - 1]
            return Rect(left, rect.top, max(left + 1, right), rect.bottom)
        }

        companion object {

            fun of(line: OcrTextLine): Line? {
                val text = line.text
                if (text.isBlank()) return null

                val rect = Rect(
                    min(line.left, line.right),
                    min(line.top, line.bottom),
                    max(line.left, line.right),
                    max(line.top, line.bottom),
                )
                if (rect.width() <= 0 || rect.height() <= 0) return null

                val weights = FloatArray(text.length) { index -> charWeight(text[index]) }
                val total = weights.sum()
                if (total <= 0f) return null

                val charLeft = IntArray(text.length + 1)
                val charRight = IntArray(text.length)
                charLeft[0] = rect.left
                var accumulated = 0f
                weights.forEachIndexed { index, weight ->
                    accumulated += weight
                    val scaled = (rect.width() * (accumulated / total)).roundToInt()
                    // 文字比行框还宽(窄框噪声)时会一路夹到 rect.right, 字宽退化成 0, rectOf() 里有兜底
                    val edge = (rect.left + scaled).coerceIn(charLeft[index], rect.right)
                    charLeft[index + 1] = edge
                    charRight[index] = edge
                }

                val words = mutableListOf<Word>()
                var wordStart = -1

                fun flushWord(endExclusive: Int) {
                    if (wordStart >= 0 && endExclusive > wordStart) {
                        words += Word(
                            text = text.substring(wordStart, endExclusive),
                            rect = Rect(
                                charLeft[wordStart],
                                rect.top,
                                charLeft[endExclusive],
                                rect.bottom,
                            ),
                        )
                    }
                    wordStart = -1
                }

                text.forEachIndexed { index, char ->
                    when {
                        char.isWhitespace() -> flushWord(index)
                        isCjk(char) -> {
                            flushWord(index)
                            words += Word(
                                text = char.toString(),
                                rect = Rect(
                                    charLeft[index],
                                    rect.top,
                                    charLeft[index + 1],
                                    rect.bottom,
                                ),
                            )
                        }

                        else -> if (wordStart < 0) wordStart = index
                    }
                }
                flushWord(text.length)

                return Line(text, rect, charLeft, charRight, words)
            }

            private fun charWeight(char: Char): Float = when {
                char.isWhitespace() -> 0.35f
                isCjk(char) -> 1f
                else -> 0.55f
            }

            private fun isCjk(char: Char): Boolean = when (char.code) {
                in 0x2E80..0x9FFF,
                in 0xAC00..0xD7AF,
                in 0xF900..0xFAFF,
                in 0xFF00..0xFFEF,
                -> true

                else -> false
            }
        }
    }

    private class Word(
        val text: String,
        val rect: Rect,
    )

    private companion object {
        val EMPTY_TEXT = OcrText("", emptyList())
    }
}
