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
