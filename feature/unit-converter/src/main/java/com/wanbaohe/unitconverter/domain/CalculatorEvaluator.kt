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

package com.wanbaohe.unitconverter.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

object CalculatorEvaluator {

    fun evaluate(expression: String): Double? {
        return runCatching {
            Parser(
                input = expression.replace("×", "*").replace("÷", "/")
            ).parse()
        }.getOrNull()
    }

    fun format(value: Double): String {
        if (!value.isFinite()) return "∞"
        if (value == 0.0) return "0"
        val absValue = abs(value)
        return when {
            absValue >= 1e12 || (absValue > 0.0 && absValue < 1e-8) -> {
                val exponent = floor(log10(absValue)).toInt()
                val mantissa = value / 10.0.pow(exponent.toDouble())
                "${"%.6f".format(mantissa).trimEnd('0').trimEnd('.')}E$exponent"
            }

            absValue >= 1e6 -> "%.6f".format(value).trimEnd('0').trimEnd('.')
            else -> "%.10f".format(value).trimEnd('0').trimEnd('.')
        }
    }

    private class Parser(
        private val input: String,
    ) {
        private var index = 0

        fun parse(): Double {
            val result = parseExpression()
            skipWhitespace()
            check(index == input.length) { "Unexpected token" }
            check(result.isFinite()) { "Invalid result" }
            return result
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                value = when {
                    match('+') -> value + parseTerm()
                    match('-') -> value - parseTerm()
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                skipWhitespace()
                value = when {
                    match('*') -> value * parseFactor()
                    match('/') -> {
                        val divisor = parseFactor()
                        check(divisor != 0.0) { "Division by zero" }
                        value / divisor
                    }

                    else -> return value
                }
            }
        }

        private fun parseFactor(): Double {
            var value = parseUnary()
            while (true) {
                skipWhitespace()
                if (!match('%')) return value
                value /= 100.0
            }
        }

        private fun parseUnary(): Double {
            skipWhitespace()
            return when {
                match('+') -> parseUnary()
                match('-') -> -parseUnary()
                else -> parsePrimary()
            }
        }

        private fun parsePrimary(): Double {
            skipWhitespace()
            if (match('(')) {
                val value = parseExpression()
                check(match(')')) { "Missing )" }
                return value
            }
            return parseNumber()
        }

        private fun parseNumber(): Double {
            skipWhitespace()
            val start = index
            var hasDot = false
            while (index < input.length) {
                val char = input[index]
                when {
                    char.isDigit() -> index++
                    char == '.' && !hasDot -> {
                        hasDot = true
                        index++
                    }

                    else -> break
                }
            }
            check(index > start) { "Missing number" }
            return input.substring(start, index).toDouble()
        }

        private fun match(expected: Char): Boolean {
            if (index >= input.length || input[index] != expected) return false
            index++
            return true
        }

        private fun skipWhitespace() {
            while (index < input.length && input[index].isWhitespace()) {
                index++
            }
        }
    }
}
