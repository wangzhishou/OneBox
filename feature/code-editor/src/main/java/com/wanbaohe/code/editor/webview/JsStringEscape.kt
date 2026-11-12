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

package com.wanbaohe.code.editor.webview

/**
 * JS 字符串字面量转义工具
 *
 * Kotlin/Java 字符串嵌入到 JS 字符串字面量时,必须转义所有 JS 字符串里
 * 有特殊含义的字符,否则会导致 "Unterminated string literal" 错误或内容损坏。
 *
 * 完整转义:
 * - `\`     → `\\`     (反斜杠本身)
 * - `"`     → `\"`     (双引号结束字符串)
 * - `\n`    → `\n`     (换行符)
 * - `\r`    → `\r`     (回车符)
 * - `\t`    → `\t`     (制表符;虽然 JS 字符串允许直接出现,但保持显式更安全)
 * - `\b`    → `\b`     (退格符,会破坏 JS 字符串)
 * - `\u000C`→ `\f`     (换页符,同上)
 * - `\u2028`→ `\u2028` (Line Separator,JS 字符串里允许但某些解析器会视为换行)
 * - `\u2029`→ `\u2029` (Paragraph Separator,同上)
 *
 * @param text 原始字符串
 * @return 安全的 JS 字符串字面量内容(不含外侧引号)
 */
internal fun escapeJsString(text: String): String {
    val sb = StringBuilder(text.length + 16)
    for (c in text) {
        when (c) {
            '\\' -> sb.append("\\\\")
            '"' -> sb.append("\\\"")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            '\b' -> sb.append("\\b")
            '\u000C' -> sb.append("\\f")
            '\u2028' -> sb.append("\\u2028")
            '\u2029' -> sb.append("\\u2029")
            else -> sb.append(c)
        }
    }
    return sb.toString()
}
