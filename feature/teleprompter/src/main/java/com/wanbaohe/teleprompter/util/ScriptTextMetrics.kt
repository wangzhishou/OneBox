package com.wanbaohe.teleprompter.util

import kotlin.math.ceil

/**
 * 文稿的字数与朗读时长口径。
 *
 * 保存时算的字数、编辑页实时显示的字数和时长都走这里，
 * 避免列表卡片和编辑页各算一套，字数对不上。
 */

/** 与保存口径一致：去掉空白后按字符计（中文按字，英文按字母）。 */
private val whitespaceRegex = Regex("\\s+")

internal fun countScriptWords(content: String): Int =
    content.replace(whitespaceRegex, "").length

/**
 * 按"人说话的速度"估算朗读时长。
 *
 * 中文约 4.2 字/秒、拉丁字符约 14.5 字符/秒（≈3 词/秒）；
 * 中日韩标点也算一拍，所以归到 CJK 一侧。纯符号 / 空白不计。
 */
private const val CJK_CHARS_PER_SECOND = 4.2f
private const val LATIN_CHARS_PER_SECOND = 14.5f

internal fun estimateReadingSeconds(content: String): Int {
    var cjk = 0
    var latin = 0
    for (char in content) {
        when {
            char.isWhitespace() -> Unit
            char.isCjkOrFullWidth() -> cjk++
            char.isLetterOrDigit() -> latin++
        }
    }
    if (cjk + latin == 0) return 0
    return ceil(cjk / CJK_CHARS_PER_SECOND + latin / LATIN_CHARS_PER_SECOND).toInt()
}

private fun Char.isCjkOrFullWidth(): Boolean = code in 0x2E80..0x9FFF ||
    code in 0xAC00..0xD7AF ||
    code in 0xF900..0xFAFF ||
    code in 0xFE30..0xFE4F ||
    code in 0xFF00..0xFFEF
