package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.streaming.Token

internal fun expandMermaidFlowchartLines(lines: List<List<Token>>): List<List<Token>> =
    lines.flatMap(::expandMermaidFlowchartLine)

private fun expandMermaidFlowchartLine(line: List<Token>): List<List<Token>> {
    val arrows = line.indices.filter { line[it].text.toString() == SOLID_ARROW }
    if (arrows.size < 2) return listOf(line)

    val statements = ArrayList<List<Token>>(arrows.size)
    var statementStart = 0
    var previousArrow = arrows.first()
    for (arrow in arrows.drop(1)) {
        statements += line.subList(statementStart, arrow)
        statementStart = previousArrow + 1
        previousArrow = arrow
    }
    statements += line.subList(statementStart, line.size)
    return statements
}

private const val SOLID_ARROW = "-->"
