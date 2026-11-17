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

package com.wanbaohe.codeeditor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.hrm.codehigh.ast.TokenType
import com.hrm.codehigh.lexer.LanguageRegistry
import com.hrm.codehigh.renderer.CodeColors

/** 查找命中的表达方式；颜色为空表示不上底色。 */
@Immutable
data class CodeEditorSearchStyle(
    val matchHighlight: Color? = null,
    val activeMatchHighlight: Color? = null,
    val activeMatchContent: Color? = null,
)

/**
 * 语法着色：codehighlight-parser 出 token，这里按 [CodeColors] 映射成 [AnnotatedString]。
 *
 * 不复用 renderer 的 `buildHighlightedString`：搜索命中需要一个可以叠加在
 * 高亮结果之上的样式层，自己拼装更直接，也不受上游按行切片的约束。
 */
class CodeHighlighter(private val colors: CodeColors) {

    private val tokenColors: Map<TokenType, Color> = mapOf(
        TokenType.KEYWORD to colors.keyword,
        TokenType.STRING to colors.string,
        TokenType.NUMBER to colors.number,
        TokenType.COMMENT to colors.comment,
        TokenType.OPERATOR to colors.operator,
        TokenType.PUNCTUATION to colors.punctuation,
        TokenType.IDENTIFIER to colors.identifier,
        TokenType.TYPE to colors.type,
        TokenType.FUNCTION to colors.function,
        TokenType.VARIABLE to colors.variable,
        TokenType.CONSTANT to colors.constant,
        TokenType.ANNOTATION to colors.annotation,
        TokenType.DECORATOR to colors.decorator,
        TokenType.BUILTIN to colors.builtin,
        TokenType.PLAIN to colors.text,
    )

    fun highlight(
        text: String,
        language: CodeLanguage,
        matches: List<IntRange> = emptyList(),
        activeMatch: IntRange? = null,
        searchStyle: CodeEditorSearchStyle = CodeEditorSearchStyle(),
    ): AnnotatedString {
        if (text.isEmpty()) return AnnotatedString("")
        val tokens = when (language) {
            CodeLanguage.PlainText -> emptyList()
            else -> runCatching { LanguageRegistry.getOrPlain(language.lexerKey).tokenize(text) }
                .getOrDefault(emptyList())
        }
        val annotated = buildAnnotatedString {
            addStyle(SpanStyle(color = colors.text), 0, text.length)
            var cursor = 0
            tokens.forEach { token ->
                val start = token.range.first.coerceIn(0, text.length)
                val end = (token.range.last + 1).coerceIn(0, text.length)
                if (start >= end) return@forEach
                val from = maxOf(start, cursor)
                if (from >= end) return@forEach
                if (from > cursor) append(text.substring(cursor, from))
                withStyle(SpanStyle(color = tokenColors[token.type] ?: colors.text)) {
                    append(text.substring(from, end))
                }
                cursor = end
            }
            if (cursor < text.length) append(text.substring(cursor))
        }
        return overlayMatches(
            source = annotated,
            fullText = text,
            matches = matches,
            activeMatch = activeMatch,
            searchStyle = searchStyle,
        )
    }
}

/**
 * 把查找命中的底色叠到高亮结果上。活动命中用强调色填充 + 反色文字，
 * 既和普通命中区分开，也不需要靠半透明叠加来压暗正文。
 */
internal fun overlayMatches(
    source: AnnotatedString,
    fullText: String,
    matches: List<IntRange>,
    activeMatch: IntRange?,
    searchStyle: CodeEditorSearchStyle,
): AnnotatedString {
    if (matches.isEmpty()) return source
    val builder = AnnotatedString.Builder(source)
    val textLength = fullText.length
    matches.forEach { match ->
        val start = match.first.coerceIn(0, textLength)
        val end = (match.last + 1).coerceIn(0, textLength)
        if (start >= end) return@forEach
        val isActive = activeMatch == match
        val background = if (isActive) {
            searchStyle.activeMatchHighlight
        } else {
            searchStyle.matchHighlight
        } ?: return@forEach
        val style = if (isActive && searchStyle.activeMatchContent != null) {
            SpanStyle(background = background, color = searchStyle.activeMatchContent)
        } else {
            SpanStyle(background = background)
        }
        builder.addStyle(style, start, end)
    }
    return builder.toAnnotatedString()
}
