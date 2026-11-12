package com.halilibo.richtext.markwon

import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.ext.latex.JLatexMathBlock
import org.commonmark.node.Block
import org.commonmark.parser.Parser
import org.commonmark.parser.SourceLine
import org.commonmark.parser.block.AbstractBlockParser
import org.commonmark.parser.block.AbstractBlockParserFactory
import org.commonmark.parser.block.BlockContinue
import org.commonmark.parser.block.BlockStart
import org.commonmark.parser.block.MatchedBlockParser
import org.commonmark.parser.block.ParserState

internal class MarkdownMathPlugin : AbstractMarkwonPlugin() {
    override fun configureParser(builder: Parser.Builder) {
        builder.customBlockParserFactory(MarkdownMathBlockParserFactory())
    }
}

internal class MarkdownMathBlockParserFactory : AbstractBlockParserFactory() {
    override fun tryStart(state: ParserState, matchedBlockParser: MatchedBlockParser): BlockStart? {
        if (state.indent >= 4) return null

        val line = state.line.content
        val start = state.nextNonSpaceIndex
        val delimiterLength = line.delimiterLength(start)
        if (delimiterLength < 2 || !line.isTrailingWhitespace(start + delimiterLength)) return null

        return BlockStart.of(MarkdownMathBlockParser(delimiterLength))
            .atIndex(line.length)
    }
}

private class MarkdownMathBlockParser(
    private val delimiterLength: Int
) : AbstractBlockParser() {
    private val block = JLatexMathBlock()
    private val content = StringBuilder()

    override fun getBlock(): Block = block

    override fun tryContinue(state: ParserState): BlockContinue {
        val line = state.line.content
        val start = state.nextNonSpaceIndex
        if (state.indent < 4 &&
            line.delimiterLength(start) == delimiterLength &&
            line.isTrailingWhitespace(start + delimiterLength)
        ) {
            return BlockContinue.finished()
        }
        return BlockContinue.atIndex(state.index)
    }

    override fun addLine(line: SourceLine) {
        content.append(line.content).append('\n')
    }

    override fun closeBlock() {
        block.latex(content.toString())
    }
}

private fun CharSequence.delimiterLength(start: Int): Int {
    var end = start
    while (end < length && this[end] == '$') end++
    return end - start
}

private fun CharSequence.isTrailingWhitespace(start: Int): Boolean {
    for (index in start until length) {
        if (!this[index].isWhitespace()) return false
    }
    return true
}
