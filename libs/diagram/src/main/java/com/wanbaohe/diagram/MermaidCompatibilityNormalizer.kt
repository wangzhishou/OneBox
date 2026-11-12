package com.wanbaohe.diagram

internal fun normalizeMermaidChainedEdges(source: String): String =
    source.split('\n').joinToString("\n") { line -> normalizeChainedEdgeLine(line) }

private fun normalizeChainedEdgeLine(line: String): String {
    val arrows = topLevelArrowOffsets(line)
    if (arrows.size < 2) return line

    val indent = line.takeWhile(Char::isWhitespace)
    val output = mutableListOf<String>()
    var segmentStart = 0
    for (index in 1 until arrows.size) {
        val arrowStart = arrows[index]
        val segment = line.substring(segmentStart, arrowStart).trimEnd()
        output += if (output.isEmpty()) segment else indent + segment

        val targetStart = chainTargetStart(line, arrows[index - 1] + 3)
        if (targetStart < 0 || targetStart > arrowStart) return line
        if (line.substring(targetStart, arrowStart).isBlank()) return line
        segmentStart = targetStart
    }
    output += indent + line.substring(segmentStart).trimEnd()
    return output.joinToString("\n")
}

private fun chainTargetStart(line: String, arrowEnd: Int): Int {
    var cursor = arrowEnd
    while (cursor < line.length && line[cursor].isWhitespace()) cursor++
    if (cursor < line.length && line[cursor] == '|') {
        val labelEnd = line.indexOf('|', cursor + 1)
        if (labelEnd < 0) return -1
        cursor = labelEnd + 1
    }
    while (cursor < line.length && line[cursor].isWhitespace()) cursor++
    return cursor
}

private fun topLevelArrowOffsets(line: String): List<Int> {
    val offsets = mutableListOf<Int>()
    var squareDepth = 0
    var roundDepth = 0
    var curlyDepth = 0
    var inQuote = false
    var inEdgeLabel = false
    var index = 0

    while (index < line.length) {
        val char = line[index]
        if (inQuote) {
            when (char) {
                '\\' -> index++
                '"' -> inQuote = false
            }
            index++
            continue
        }
        if (char == '"') {
            inQuote = true
            index++
            continue
        }
        if (char == '%' && index + 1 < line.length && line[index + 1] == '%') break
        if (char == '|' && squareDepth == 0 && roundDepth == 0 && curlyDepth == 0) {
            inEdgeLabel = !inEdgeLabel
            index++
            continue
        }
        if (inEdgeLabel) {
            index++
            continue
        }
        when (char) {
            '[' -> squareDepth++
            ']' -> squareDepth = (squareDepth - 1).coerceAtLeast(0)
            '(' -> roundDepth++
            ')' -> roundDepth = (roundDepth - 1).coerceAtLeast(0)
            '{' -> curlyDepth++
            '}' -> curlyDepth = (curlyDepth - 1).coerceAtLeast(0)
        }
        val insideLabel = squareDepth > 0 || roundDepth > 0 || curlyDepth > 0
        if (!insideLabel && char == '-' && line.startsWith("-->", index)) {
            offsets += index
            index += 3
        } else {
            index++
        }
    }
    return offsets
}
