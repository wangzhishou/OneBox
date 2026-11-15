package com.hrm.diagram.render

import androidx.compose.runtime.Immutable
import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.ir.SourceLanguage

/**
 * Detection result for a Markdown code fence, prose block, or streaming text prefix.
 *
 * Minimal usage:
 * ```kotlin
 * val detection = Diagram.detectSource(fenceBody, hint = fenceInfo)
 * if (detection.shouldRouteToDiagram) {
 *     DiagramView(source = fenceBody)
 * }
 * ```
 */
@DiagramApi
@Immutable
data class DiagramSourceDetection(
    val status: DiagramSourceStatus,
    val language: SourceLanguage?,
    val reason: String,
) {
    /** True when the block is ready to hand to `DiagramView(source = ...)`. */
    val isDiagram: Boolean get() = status == DiagramSourceStatus.DIAGRAM

    /** True when a stream prefix strongly suggests a diagram but needs more text. */
    val isPending: Boolean get() = status == DiagramSourceStatus.PENDING

    /** True when a Markdown container should reserve this block for diagram rendering. */
    val shouldRouteToDiagram: Boolean get() = isDiagram || isPending
}

/**
 * Three-state source detection for containers that see text before `DiagramView` is created.
 */
@DiagramApi
@Immutable
enum class DiagramSourceStatus {
    DIAGRAM,
    PENDING,
    NOT_DIAGRAM,
}

internal object DiagramSourceDetector {
    fun detect(source: CharSequence, hint: String? = null): DiagramSourceDetection {
        val hinted = languageFromHint(hint)
        if (hinted != null) {
            return DiagramSourceDetection(
                status = DiagramSourceStatus.DIAGRAM,
                language = hinted,
                reason = "matched code fence hint '${hint.orEmpty().trim()}'",
            )
        }

        val first = firstSignificantLine(source)
            ?: return DiagramSourceDetection(
                status = DiagramSourceStatus.NOT_DIAGRAM,
                language = null,
                reason = "no significant source line",
            )
        val lower = first.lowercase()
        if (lower.startsWith("@start")) {
            return DiagramSourceDetection(DiagramSourceStatus.DIAGRAM, SourceLanguage.PLANTUML, "matched PlantUML start directive")
        }

        val mermaid = detectMermaid(lower)
        if (mermaid != null) return mermaid

        val dot = detectDot(lower)
        if (dot != null) return dot

        return DiagramSourceDetection(DiagramSourceStatus.NOT_DIAGRAM, null, "no diagram header or hint")
    }

    private fun languageFromHint(hint: String?): SourceLanguage? {
        val token = hint
            ?.trim()
            ?.removePrefix("```")
            ?.split(Regex("\\s+"))
            ?.firstOrNull()
            ?.trim('{', '}', '.', '`')
            ?.lowercase()
            ?: return null
        return when (token) {
            "mermaid", "mmd" -> SourceLanguage.MERMAID
            "plantuml", "puml", "uml" -> SourceLanguage.PLANTUML
            "dot", "gv", "graphviz" -> SourceLanguage.DOT
            else -> null
        }
    }

    private fun firstSignificantLine(source: CharSequence): String? =
        source.toString()
            .lineSequence()
            .map { it.trimStart() }
            .firstOrNull { it.isNotBlank() && !it.isLeadingComment() }

    private fun String.isLeadingComment(): Boolean =
        startsWith("%%") || startsWith("'") || startsWith("//") || startsWith("#")

    private fun detectMermaid(line: String): DiagramSourceDetection? {
        val firstWord = line.takeWhile { !it.isWhitespace() }
        if (firstWord == "graph") {
            val rest = line.removePrefix("graph").trimStart()
            val direction = rest.takeWhile { !it.isWhitespace() }
            return if (direction in MERMAID_GRAPH_DIRECTIONS) {
                DiagramSourceDetection(DiagramSourceStatus.DIAGRAM, SourceLanguage.MERMAID, "matched Mermaid graph header")
            } else if (rest.isEmpty()) {
                DiagramSourceDetection(DiagramSourceStatus.PENDING, SourceLanguage.MERMAID, "partial Mermaid graph header")
            } else {
                null
            }
        }
        return if (firstWord in MERMAID_HEADERS) {
            DiagramSourceDetection(DiagramSourceStatus.DIAGRAM, SourceLanguage.MERMAID, "matched Mermaid '$firstWord' header")
        } else {
            null
        }
    }

    private fun detectDot(line: String): DiagramSourceDetection? {
        val withoutStrict = keywordRest(line, "strict")?.takeIf { it.isNotEmpty() } ?: line
        val digraphRest = keywordRest(withoutStrict, "digraph")
        val graphRest = keywordRest(withoutStrict, "graph")
        return when {
            digraphRest != null && dotHeaderIsPlausible(digraphRest) ->
                DiagramSourceDetection(DiagramSourceStatus.DIAGRAM, SourceLanguage.DOT, "matched DOT digraph header")
            graphRest != null && dotHeaderOpensOnFirstLine(graphRest) ->
                DiagramSourceDetection(DiagramSourceStatus.DIAGRAM, SourceLanguage.DOT, "matched DOT graph header")
            graphRest != null && dotHeaderPrefixIsPlausible(graphRest) ->
                DiagramSourceDetection(DiagramSourceStatus.PENDING, SourceLanguage.DOT, "partial DOT graph header")
            else -> null
        }
    }

    private fun keywordRest(line: String, keyword: String): String? {
        if (!line.startsWith(keyword)) return null
        if (line.length == keyword.length) return ""
        val next = line[keyword.length]
        if (!next.isWhitespace() && next != '{') return null
        return line.substring(keyword.length).trimStart()
    }

    private fun dotHeaderIsPlausible(rest: String): Boolean =
        dotHeaderOpensOnFirstLine(rest) || dotHeaderPrefixIsPlausible(rest)

    private fun dotHeaderOpensOnFirstLine(rest: String): Boolean {
        if (rest.startsWith("{")) return true
        val brace = rest.indexOf('{')
        return brace >= 0 && rest.take(brace).all { it.isDotIdChar() || it.isWhitespace() || it == '"' }
    }

    private fun dotHeaderPrefixIsPlausible(rest: String): Boolean {
        return rest.isEmpty() || rest.all { it.isDotIdChar() || it.isWhitespace() || it == '"' }
    }

    private fun Char.isDotIdChar(): Boolean = isLetterOrDigit() || this == '_' || this == '-'

    private val MERMAID_GRAPH_DIRECTIONS = setOf("tb", "td", "bt", "rl", "lr")

    private val MERMAID_HEADERS = setOf(
        "flowchart",
        "sequencediagram",
        "classdiagram",
        "statediagram",
        "statediagram-v2",
        "erdiagram",
        "journey",
        "gantt",
        "pie",
        "gauge",
        "gitgraph",
        "mindmap",
        "timeline",
        "requirementdiagram",
        "architecture-beta",
        "c4context",
        "c4container",
        "c4component",
        "c4dynamic",
        "c4deployment",
        "block-beta",
        "block",
        "packet-beta",
        "kanban",
        "xychart-beta",
        "quadrantchart",
        "sankey-beta",
    )
}
