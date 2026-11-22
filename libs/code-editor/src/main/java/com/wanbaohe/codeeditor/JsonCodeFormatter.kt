package com.wanbaohe.codeeditor

sealed interface JsonFormatResult {
    data class Formatted(val text: String) : JsonFormatResult
    data class Invalid(val offset: Int) : JsonFormatResult
    data object TooLarge : JsonFormatResult
}

/** 校验并只重排空白，不重写数字、字符串转义、键顺序或重复键。 */
object JsonCodeFormatter {
    fun format(source: String, indentWidth: Int): JsonFormatResult {
        if (source.length > MAX_OUTPUT) return JsonFormatResult.TooLarge
        return try {
            val formatted = Parser(source, indentWidth.coerceIn(2, 8)).format()
            if (formatted.length > MAX_OUTPUT) JsonFormatResult.TooLarge
            else JsonFormatResult.Formatted(formatted)
        } catch (error: InvalidJson) {
            JsonFormatResult.Invalid(error.offset)
        } catch (_: FormatLimit) {
            JsonFormatResult.TooLarge
        }
    }

    private class Parser(private val source: String, width: Int) {
        private val indent = " ".repeat(width * MAX_DEPTH)
        private val output = StringBuilder(source.length)
        private var offset = 0

        fun format(): String {
            value(0)
            skipWhitespace()
            if (offset != source.length) invalid()
            return output.toString()
        }

        private fun value(depth: Int) {
            skipWhitespace()
            when (source.getOrNull(offset)) {
                '{' -> container(depth, '{', '}', hasKeys = true)
                '[' -> container(depth, '[', ']', hasKeys = false)
                '"' -> string()
                't' -> literal("true")
                'f' -> literal("false")
                'n' -> literal("null")
                '-', in '0'..'9' -> number()
                else -> invalid()
            }
        }

        private fun container(depth: Int, open: Char, close: Char, hasKeys: Boolean) {
            if (depth >= MAX_DEPTH) throw FormatLimit()
            take(open)
            skipWhitespace()
            if (take(close)) return
            do {
                newline(depth + 1)
                skipWhitespace()
                if (hasKeys) {
                    string()
                    skipWhitespace()
                    expect(':')
                    output.append(' ')
                }
                value(depth + 1)
                skipWhitespace()
            } while (take(','))
            newline(depth)
            expect(close)
        }

        private fun string() {
            val start = offset
            if (source.getOrNull(offset) != '"') invalid()
            offset++
            while (offset < source.length) {
                val char = source[offset++]
                when {
                    char == '"' -> {
                        output.append(source, start, offset)
                        return
                    }
                    char == '\\' -> escape()
                    char < ' ' -> throw InvalidJson(offset - 1)
                }
            }
            invalid()
        }

        private fun escape() {
            when (source.getOrNull(offset)) {
                '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> offset++
                'u' -> {
                    offset++
                    repeat(4) {
                        val digit = source.getOrNull(offset) ?: invalid()
                        if (digit !in "0123456789abcdefABCDEF") invalid()
                        offset++
                    }
                }
                else -> invalid()
            }
        }

        private fun number() {
            val match = NUMBER.matchAt(source, offset) ?: invalid()
            val end = match.range.last + 1
            output.append(source, offset, end)
            offset = end
        }

        private fun literal(text: String) {
            if (!source.startsWith(text, offset)) invalid()
            offset += text.length
            output.append(text)
        }

        private fun skipWhitespace() {
            while (offset < source.length && source[offset] in " \t\n\r") offset++
        }

        private fun take(char: Char): Boolean {
            if (source.getOrNull(offset) != char) return false
            offset++
            output.append(char)
            return true
        }

        private fun expect(char: Char) {
            if (!take(char)) invalid()
        }

        private fun newline(depth: Int) {
            val end = depth * (indent.length / MAX_DEPTH)
            if (output.length + end + 1 > MAX_OUTPUT) throw FormatLimit()
            output.append('\n').append(indent, 0, end)
        }

        private fun invalid(): Nothing = throw InvalidJson(offset)
    }

    private class InvalidJson(val offset: Int) : RuntimeException()
    private class FormatLimit : RuntimeException()
    private val NUMBER = Regex("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")
    private const val MAX_DEPTH = 128
    private const val MAX_OUTPUT = 1_000_000
}
