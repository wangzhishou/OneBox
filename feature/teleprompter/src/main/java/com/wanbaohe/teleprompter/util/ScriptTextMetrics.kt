package com.wanbaohe.teleprompter.util

import kotlin.math.ceil

/**
 * 文稿的字数与朗读时长口径。
 *
 * 保存时算的字数、编辑页实时显示的字数和时长都走这里，
 * 避免列表卡片和编辑页各算一套，字数对不上。
 */

/** 这一份文稿该按"字"还是按"词"计数：中文按字，使用空格分词的文字按词。 */
internal enum class ScriptCountUnit { CHARACTERS, WORDS }

private val whitespaceRegex = Regex("\\s+")

/**
 * 计数单位跟着文稿本身的文字走，而不是跟着界面语言：
 * 中文界面里录一段英文稿，显示"54 字"同样是错的。
 */
internal fun scriptCountUnit(content: String): ScriptCountUnit =
    if (content.any { it.isCjkOrFullWidth() }) ScriptCountUnit.CHARACTERS
    else if (content.any { it.isLetterOrDigit() }) ScriptCountUnit.WORDS
    else ScriptCountUnit.CHARACTERS

/**
 * 字数：CJK 按字、其余按词（和 Word/微信的"字数"口径一致）。
 *
 * 纯拉丁文稿按词计，不再把字母数当"字数"—— 67 个字符报成"54 字"在英文场景下没有意义。
 */
internal fun countScriptWords(content: String): Int = when (scriptCountUnit(content)) {
    ScriptCountUnit.CHARACTERS -> cjkCharCount(content) + latinWordCount(content)
    ScriptCountUnit.WORDS -> latinWordCount(content)
}

/** 只数 CJK 的文字：句读、全角标点（isLetterOrDigit 为 false）不该抬高字数 */
private fun cjkCharCount(content: String): Int =
    content.count { it.isCjkOrFullWidth() && it.isLetterOrDigit() }

/**
 * 按空白切词，只数"还剩下字母/数字"的块：中文标点、破折号不算词，
 * 俄语/德语这类同样用空格分词的文字靠 [Char.isLetterOrDigit] 自然覆盖。
 */
private fun latinWordCount(content: String): Int =
    content.split(whitespaceRegex).count { token ->
        token.any { it.isLetterOrDigit() && !it.isCjkOrFullWidth() }
    }

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
