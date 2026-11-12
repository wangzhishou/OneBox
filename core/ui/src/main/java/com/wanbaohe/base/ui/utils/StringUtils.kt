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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.base.ui.utils

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