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

package com.wanbaohe.latex

internal fun normalizeNativeLatexSource(source: String): String {
    if ("cases" !in source) return source
    var normalized = source
    CASE_ENVIRONMENT_REPLACEMENTS.forEach { (original, replacement) ->
        normalized = normalized.replace(original, replacement)
    }
    return normalized
}

private val CASE_ENVIRONMENT_REPLACEMENTS = buildList {
    val environments = listOf(
        "cases" to ("\\left\\{\\begin{array}{ll}" to "\\end{array}\\right."),
        "dcases" to ("{\\displaystyle\\left\\{\\begin{array}{ll}" to "\\end{array}\\right.}"),
        "rcases" to ("\\left.\\begin{array}{ll}" to "\\end{array}\\right\\}"),
    )
    environments.forEach { (name, delimiters) ->
        val (start, end) = delimiters
        listOf(name, "$name*").forEach { environment ->
            add("\\begin{$environment}" to start)
            add("\\end{$environment}" to end)
        }
    }
}
