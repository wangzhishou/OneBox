package com.shifenmiao.base.ui.utils

import java.io.PrintWriter
import java.io.StringWriter

object StringUtils {

    /**
     *
     *         Log.d("调用栈跟踪", StringUtils.getStackTraceString())
     */
    fun getStackTraceString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        Exception("Stack trace").printStackTrace(pw)
        return sw.toString()
    }

    data class EmojiCategory(
        val title: String,
        val emojis: List<String>
    )

    fun generateEmojiCategories(): List<EmojiCategory> {
        val categories = listOf(
            "表情" to 0x1F600..0x1F64F,
            "自然" to 0x1F300..0x1F5FF,
            "交通" to 0x1F680..0x1F6FF,
            "符号" to 0x2600..0x26FF,
            "装饰" to 0x2700..0x27BF,
            "补充" to 0x1F900..0x1F9FF
        )

        return categories.map { (title, range) ->
            EmojiCategory(
                title = title,
                emojis = range.map { String(Character.toChars(it)) }
            )
        }
    }

    fun generateEmojiList(): List<String> {
        // 生成常用 Emoji 的 Unicode 范围（可根据需要扩展）
        val emojiRanges = listOf(
            0x1F600..0x1F64F,  // 表情符号
            0x1F300..0x1F5FF,  // 符号和图形
            0x1F680..0x1F6FF,  // 交通和地图符号
            0x2600..0x26FF,    // 杂项符号
            0x2700..0x27BF,    // 装饰符号
            0xFE00..0xFE0F,    // 变体选择器
            0x1F900..0x1F9FF   // 补充符号和图形
        )

        return emojiRanges.flatMap { range ->
            range.map { codePoint ->
                String(Character.toChars(codePoint))
            }
        }
    }

    fun generateRandomEmoji(): String {
        // 生成常用 Emoji 的 Unicode 范围（可根据需要扩展）
        val emojiRanges = listOf(
            0x1F600..0x1F64F,  // 表情符号
            0x1F300..0x1F5FF,  // 符号和图形
        )

        return emojiRanges.flatMap { range ->
            range.map { codePoint ->
                String(Character.toChars(codePoint))
            }
        }.random()
    }
}