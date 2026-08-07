package com.shifenmiao.ai.agent.tool.expression

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

sealed class AgentToolExpressionValidationResult {
    data object Allowed : AgentToolExpressionValidationResult()
    data class Denied(val reason: String) : AgentToolExpressionValidationResult()
}

/**
 * 执行前表达式校验器。
 *
 * 这是一个受限 DSL 解析器，不执行 Kotlin Script / 反射 / 动态代码。
 * 当前支持：
 * - 参数路径：args.foo、arg.foo、args.items[0].name
 * - 字面量：字符串、数字、true、false、null
 * - 运算符：== != > >= < <= && || !
 * - 括号：(...)
 *
 * 示例：
 * - args.path != null && args.path != ""
 * - args.quality >= 1 && args.quality <= 100
 * - args.enabled == true
 */
@Singleton
class AgentToolExpressionValidator @Inject constructor() {

    fun validate(
        expression: String,
        arguments: String
    ): AgentToolExpressionValidationResult {
        val normalizedExpression = expression.trim()
        if (normalizedExpression.isBlank()) {
            return AgentToolExpressionValidationResult.Allowed
        }

        val args = runCatching {
            if (arguments.isBlank()) JsonObject() else JsonParser.parseString(arguments)
        }.getOrElse { error ->
            return AgentToolExpressionValidationResult.Denied(
                "执行前表达式校验失败：工具参数不是合法 JSON（${error.message ?: "unknown"}）"
            )
        }

        return runCatching {
            val tokens = Tokenizer(normalizedExpression).tokenize()
            val parser = Parser(tokens, args)
            val value = parser.parseExpression()
            parser.ensureFullyConsumed()
            if (value.asBoolean()) {
                AgentToolExpressionValidationResult.Allowed
            } else {
                AgentToolExpressionValidationResult.Denied("执行前表达式校验未通过")
            }
        }.getOrElse { error ->
            AgentToolExpressionValidationResult.Denied(
                "执行前表达式校验失败：${error.message ?: "unknown"}"
            )
        }
    }

    private enum class TokenType {
        IDENTIFIER,
        STRING,
        NUMBER,
        TRUE,
        FALSE,
        NULL,
        AND,
        OR,
        NOT,
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE,
        DOT,
        L_PAREN,
        R_PAREN,
        L_BRACKET,
        R_BRACKET,
        END
    }

    private data class Token(
        val type: TokenType,
        val text: String,
        val position: Int
    )

    private class Tokenizer(private val input: String) {
        private var index = 0

        fun tokenize(): List<Token> {
            val tokens = mutableListOf<Token>()
            while (index < input.length) {
                val char = input[index]
                when {
                    char.isWhitespace() -> index++
                    char == '&' && peekNext() == '&' -> tokens.add(twoChar(TokenType.AND))
                    char == '|' && peekNext() == '|' -> tokens.add(twoChar(TokenType.OR))
                    char == '=' && peekNext() == '=' -> tokens.add(twoChar(TokenType.EQ))
                    char == '!' && peekNext() == '=' -> tokens.add(twoChar(TokenType.NE))
                    char == '>' && peekNext() == '=' -> tokens.add(twoChar(TokenType.GTE))
                    char == '<' && peekNext() == '=' -> tokens.add(twoChar(TokenType.LTE))
                    char == '!' -> tokens.add(single(TokenType.NOT))
                    char == '>' -> tokens.add(single(TokenType.GT))
                    char == '<' -> tokens.add(single(TokenType.LT))
                    char == '.' -> tokens.add(single(TokenType.DOT))
                    char == '(' -> tokens.add(single(TokenType.L_PAREN))
                    char == ')' -> tokens.add(single(TokenType.R_PAREN))
                    char == '[' -> tokens.add(single(TokenType.L_BRACKET))
                    char == ']' -> tokens.add(single(TokenType.R_BRACKET))
                    char == '"' || char == '\'' -> tokens.add(readString(char))
                    char.isDigit() || (char == '-' && peekNext()?.isDigit() == true) -> tokens.add(readNumber())
                    char.isIdentifierStart() -> tokens.add(readIdentifier())
                    else -> throw IllegalArgumentException("不支持的字符 '$char'，位置 $index")
                }
            }
            tokens.add(Token(TokenType.END, "", index))
            return tokens
        }

        private fun peekNext(): Char? = input.getOrNull(index + 1)

        private fun single(type: TokenType): Token {
            val token = Token(type, input[index].toString(), index)
            index++
            return token
        }

        private fun twoChar(type: TokenType): Token {
            val token = Token(type, input.substring(index, index + 2), index)
            index += 2
            return token
        }

        private fun readString(quote: Char): Token {
            val start = index
            index++
            val builder = StringBuilder()
            while (index < input.length) {
                val char = input[index++]
                when {
                    char == quote -> return Token(TokenType.STRING, builder.toString(), start)
                    char == '\\' -> {
                        val escaped = input.getOrNull(index++)
                            ?: throw IllegalArgumentException("字符串转义未完成，位置 $index")
                        builder.append(
                            when (escaped) {
                                '\\' -> '\\'
                                '"' -> '"'
                                '\'' -> '\''
                                'n' -> '\n'
                                'r' -> '\r'
                                't' -> '\t'
                                else -> escaped
                            }
                        )
                    }
                    else -> builder.append(char)
                }
            }
            throw IllegalArgumentException("字符串未闭合，位置 $start")
        }

        private fun readNumber(): Token {
            val start = index
            if (input[index] == '-') index++
            while (input.getOrNull(index)?.isDigit() == true) index++
            if (input.getOrNull(index) == '.') {
                index++
                while (input.getOrNull(index)?.isDigit() == true) index++
            }
            return Token(TokenType.NUMBER, input.substring(start, index), start)
        }

        private fun readIdentifier(): Token {
            val start = index
            while (input.getOrNull(index)?.isIdentifierPart() == true) index++
            val text = input.substring(start, index)
            val type = when (text) {
                "true" -> TokenType.TRUE
                "false" -> TokenType.FALSE
                "null" -> TokenType.NULL
                else -> TokenType.IDENTIFIER
            }
            return Token(type, text, start)
        }

        private fun Char.isIdentifierStart(): Boolean = this == '_' || isLetter()
        private fun Char.isIdentifierPart(): Boolean = this == '_' || isLetterOrDigit()
    }

    private class Parser(
        private val tokens: List<Token>,
        private val args: JsonElement
    ) {
        private var index = 0

        fun parseExpression(): Value = parseOr()

        fun ensureFullyConsumed() {
            val token = current()
            if (token.type != TokenType.END) {
                throw IllegalArgumentException("无法解析 '${token.text}'，位置 ${token.position}")
            }
        }

        private fun parseOr(): Value {
            var left = parseAnd()
            while (match(TokenType.OR)) {
                val right = parseAnd()
                left = Value.BooleanValue(left.asBoolean() || right.asBoolean())
            }
            return left
        }

        private fun parseAnd(): Value {
            var left = parseEquality()
            while (match(TokenType.AND)) {
                val right = parseEquality()
                left = Value.BooleanValue(left.asBoolean() && right.asBoolean())
            }
            return left
        }

        private fun parseEquality(): Value {
            var left = parseComparison()
            while (true) {
                left = when {
                    match(TokenType.EQ) -> Value.BooleanValue(left.isEqualTo(parseComparison()))
                    match(TokenType.NE) -> Value.BooleanValue(!left.isEqualTo(parseComparison()))
                    else -> return left
                }
            }
        }

        private fun parseComparison(): Value {
            var left = parseUnary()
            while (true) {
                left = when {
                    match(TokenType.GT) -> Value.BooleanValue(left.compareTo(parseUnary()) > 0)
                    match(TokenType.GTE) -> Value.BooleanValue(left.compareTo(parseUnary()) >= 0)
                    match(TokenType.LT) -> Value.BooleanValue(left.compareTo(parseUnary()) < 0)
                    match(TokenType.LTE) -> Value.BooleanValue(left.compareTo(parseUnary()) <= 0)
                    else -> return left
                }
            }
        }

        private fun parseUnary(): Value {
            return if (match(TokenType.NOT)) {
                Value.BooleanValue(!parseUnary().asBoolean())
            } else {
                parsePrimary()
            }
        }

        private fun parsePrimary(): Value {
            val token = current()
            return when (token.type) {
                TokenType.STRING -> {
                    advance()
                    Value.StringValue(token.text)
                }
                TokenType.NUMBER -> {
                    advance()
                    Value.NumberValue(token.text.toBigDecimal())
                }
                TokenType.TRUE -> {
                    advance()
                    Value.BooleanValue(true)
                }
                TokenType.FALSE -> {
                    advance()
                    Value.BooleanValue(false)
                }
                TokenType.NULL -> {
                    advance()
                    Value.NullValue
                }
                TokenType.IDENTIFIER -> parseIdentifierValue()
                TokenType.L_PAREN -> {
                    advance()
                    val value = parseExpression()
                    expect(TokenType.R_PAREN, "缺少右括号")
                    value
                }
                else -> throw IllegalArgumentException("预期表达式，实际为 '${token.text}'，位置 ${token.position}")
            }
        }

        private fun parseIdentifierValue(): Value {
            val root = expect(TokenType.IDENTIFIER, "预期参数根节点").text
            if (root != "args" && root != "arg") {
                throw IllegalArgumentException("只允许访问 args 或 arg 参数根节点，实际为 '$root'")
            }

            var currentElement: JsonElement? = args
            while (true) {
                when {
                    match(TokenType.DOT) -> {
                        val property = expect(TokenType.IDENTIFIER, "点号后需要属性名").text
                        currentElement = currentElement?.takeIf { it.isJsonObject }
                            ?.asJsonObject
                            ?.get(property)
                    }
                    match(TokenType.L_BRACKET) -> {
                        val indexToken = expect(TokenType.NUMBER, "数组下标必须是非负整数")
                        val arrayIndex = indexToken.text.toIntOrNull()
                            ?: throw IllegalArgumentException("数组下标必须是整数，位置 ${indexToken.position}")
                        if (arrayIndex < 0) {
                            throw IllegalArgumentException("数组下标不能为负数，位置 ${indexToken.position}")
                        }
                        expect(TokenType.R_BRACKET, "缺少右中括号")
                        currentElement = currentElement?.takeIf { it.isJsonArray }
                            ?.asJsonArray
                            ?.getOrNull(arrayIndex)
                    }
                    else -> return Value.fromJson(currentElement)
                }
            }
        }

        private fun current(): Token = tokens[index]

        private fun advance(): Token = tokens[index++]

        private fun match(type: TokenType): Boolean {
            if (current().type != type) return false
            advance()
            return true
        }

        private fun expect(type: TokenType, message: String): Token {
            val token = current()
            if (token.type != type) {
                throw IllegalArgumentException("$message，位置 ${token.position}")
            }
            return advance()
        }

        private fun com.google.gson.JsonArray.getOrNull(index: Int): JsonElement? {
            return if (index in 0 until size()) get(index) else null
        }
    }

    private sealed class Value {
        data class StringValue(val value: String) : Value()
        data class NumberValue(val value: BigDecimal) : Value()
        data class BooleanValue(val value: Boolean) : Value()
        data object NullValue : Value()

        fun asBoolean(): Boolean {
            return when (this) {
                is BooleanValue -> value
                is NumberValue -> value.compareTo(BigDecimal.ZERO) != 0
                is StringValue -> value.isNotBlank()
                NullValue -> false
            }
        }

        fun isEqualTo(other: Value): Boolean {
            if (this is NullValue || other is NullValue) return this is NullValue && other is NullValue
            if (this is NumberValue && other is NumberValue) return value.compareTo(other.value) == 0
            return this == other
        }

        fun compareTo(other: Value): Int {
            if (this is NumberValue && other is NumberValue) {
                return value.compareTo(other.value)
            }
            if (this is StringValue && other is StringValue) {
                return value.compareTo(other.value)
            }
            throw IllegalArgumentException("比较运算只支持数字或字符串")
        }

        companion object {
            fun fromJson(element: JsonElement?): Value {
                if (element == null || element is JsonNull || element.isJsonNull) return NullValue
                if (!element.isJsonPrimitive) return StringValue(element.toString())
                val primitive = element.asJsonPrimitive
                return when {
                    primitive.isBoolean -> BooleanValue(primitive.asBoolean)
                    primitive.isNumber -> NumberValue(primitive.asBigDecimal)
                    primitive.isString -> StringValue(primitive.asString)
                    else -> StringValue(element.toString())
                }
            }
        }
    }
}

