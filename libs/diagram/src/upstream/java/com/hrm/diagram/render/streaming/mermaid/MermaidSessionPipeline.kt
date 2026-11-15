package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.Severity
import com.hrm.diagram.core.ir.StyleHints
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.core.text.HeuristicTextMeasurer
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.parser.mermaid.MermaidFrontend
import com.hrm.diagram.parser.mermaid.MermaidStyleConfig
import com.hrm.diagram.parser.mermaid.MermaidStyleDecl
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.SessionPatch
import com.hrm.diagram.render.streaming.SessionPipeline
import com.hrm.diagram.render.streaming.drawEntitiesOrEmpty
import com.hrm.diagram.render.streaming.dispatcher.DiagramKindDispatcher
import com.hrm.diagram.render.streaming.ingress.TokenLineDrain
import com.hrm.diagram.render.streaming.kernel.StreamingFamilyPipelineKernel

/**
 * Top-level Mermaid pipeline. Lexes the incoming source once and routes complete logical lines
 * to one of two sub-pipelines:
 *  - [MermaidFlowchartSubPipeline] when the first non-blank header is `flowchart` / `graph`.
 *  - [MermaidSequenceSubPipeline] when the first non-blank header is `sequenceDiagram`.
 *
 * Until the dispatcher has seen the header line it buffers tokens; on EOS without any header
 * it falls back to the flowchart sub-pipeline (which will surface a clear diagnostic).
 */
internal class MermaidSessionPipeline(
    private val textMeasurer: TextMeasurer = HeuristicTextMeasurer(),
    private val theme: DiagramTheme = DiagramTheme.Default,
) : SessionPipeline {

    private val lexer = MermaidFrontend.createLexingSession()
    private val familyKernel = StreamingFamilyPipelineKernel(textMeasurer)
    private val tokenLines = TokenLineDrain<Token>(MermaidFrontend::isNewlineToken)
    private val pendingLines: MutableList<List<Token>> = ArrayList()
    private val subPipelineRegistry = MermaidSubPipelineRegistry(textMeasurer, theme)
    private val dispatcher = DiagramKindDispatcher(subPipelineRegistry)
    private val sub: MermaidSubPipeline?
        get() = dispatcher.current
    private var headerHint: MermaidDiagramKind? = null

    private val styleState = MermaidLanguageStyleState()
    private val stylePreprocessor = MermaidStylePreprocessor(styleState)

    override fun advance(
        previousSnapshot: DiagramSnapshot,
        chunk: CharSequence,
        absoluteOffset: Int,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance {
        val pre = preprocessForStyle(chunk, absoluteOffset, isFinal)
        styleState.diagnosticsAll += pre.newStyleDiagnostics

        if (pre.lexerFeeds.isEmpty()) {
            // Still need to advance lexer state on EOS so pending is flushed deterministically.
            tokenLines.add(lexer.feed("", absoluteOffset, eos = isFinal))
        } else {
            for ((i, feed) in pre.lexerFeeds.withIndex()) {
                tokenLines.add(
                    lexer.feed(
                        chunk = feed.text,
                        absoluteOffset = feed.absoluteOffset,
                        eos = isFinal && i == pre.lexerFeeds.lastIndex,
                    ),
                )
            }
        }

        val lines = tokenLines.drain(isFinal)

        // Decide the sub-pipeline using either: (a) lexer mode (after header was lexed) or
        // (b) the first non-blank line we have buffered so far.
        if (sub == null) {
            headerHint?.let { dispatcher.attach(it) }
        }
        if (sub == null) {
            // Look for header in pendingLines + new lines.
            val all = pendingLines + lines
            for (line in all) {
                val firstSig = line.firstOrNull { !MermaidFrontend.isCommentToken(it) }
                if (firstSig == null) continue
                val kind = subPipelineRegistry.kindForHeaderText(firstSig.text.toString())
                if (kind != null) {
                    dispatcher.attach(kind)
                    break
                }
            }
            if (sub == null) {
                // Buffer until we know.
                pendingLines += lines
                if (isFinal) {
                    // Fallback: route as flowchart so caller still gets a diagnostic.
                    val fallback = dispatcher.attachDefault() ?: error("Mermaid default sub-pipeline is not registered")
                    val drained = pendingLines.toList()
                    pendingLines.clear()
                    fallback.updateStyleExtras(ensureStyleExtrasCached())
                    return wrapWithStyleDiagnosticsAndHints(
                        fallback.acceptLines(previousSnapshot, drained, seq, isFinal),
                        pre.newStyleDiagnostics,
                    )
                }
                // Empty advance: nothing to draw yet.
                return wrapWithStyleDiagnosticsAndHints(
                    emptyAdvance(previousSnapshot, seq, isFinal),
                    pre.newStyleDiagnostics,
                )
            }
        }

        val toFeed = if (pendingLines.isNotEmpty()) {
            val combined = pendingLines.toMutableList().also { it.addAll(lines) }
            pendingLines.clear()
            combined
        } else lines

        // Make the latest style state available to graph-based sub-pipelines.
        styleState.graphStyleState.classDefs = styleState.styleClasses.toMap()
        styleState.graphStyleState.nodeClassBindings = styleState.nodeClassBindings.mapValues { it.value.toList() }
        styleState.graphStyleState.nodeInline = styleState.nodeInlineStyles.toMap()
        styleState.graphStyleState.linkDefault = styleState.linkStyleDefault
        styleState.graphStyleState.linkByIndex = styleState.linkStyleByIndex.toMap()
        sub!!.updateGraphStyles(styleState.graphStyleState)
        sub!!.updateStyleExtras(ensureStyleExtrasCached())

        return wrapWithStyleDiagnosticsAndHints(
            sub!!.acceptLines(previousSnapshot, toFeed, seq, isFinal),
            pre.newStyleDiagnostics,
        )
    }

    private fun emptyAdvance(prev: DiagramSnapshot, seq: Long, isFinal: Boolean): PipelineAdvance {
        val snap = prev.copy(seq = seq, isFinal = isFinal)
        return PipelineAdvance(
            snapshot = snap,
            patch = SessionPatch.empty(seq, isFinal),
        )
    }

    private data class LexerFeed(
        val text: String,
        val absoluteOffset: Int,
        val endAbsoluteOffset: Int,
    )

    private data class StylePreprocessResult(
        val lexerFeeds: List<LexerFeed>,
        val newStyleDiagnostics: List<Diagnostic>,
    )

    /**
     * Preprocess incoming source to:
     * - Strip Mermaid frontmatter (`--- ... ---`) at the beginning of the document.
     * - Parse and remove `classDef ...` lines from the lexer stream (Phase 1).
     *
     * Rationale:
     * - Mermaid style decl strings contain `:` / `#` / `-` which are not uniformly tokenized in all lexer modes.
     * - Feeding these lines to the Mermaid lexer produces spurious ERROR tokens and breaks downstream parsers.
     */
    private fun preprocessForStyle(chunk: CharSequence, absoluteOffset: Int, isFinal: Boolean): StylePreprocessResult {
        // Merge with previous raw pending (line buffering) similar to the lexer.
        val buf = buildString {
            append(styleState.rawPending)
            append(chunk)
        }
        val baseOffset = absoluteOffset - styleState.rawPending.length
        styleState.rawPendingAbsoluteOffset = baseOffset
        styleState.rawPending = ""

        val newDiags = ArrayList<Diagnostic>()
        var startIdx = 0

        // Frontmatter stripping: only once, only if it appears at the very beginning.
        if (!styleState.frontmatterStripped && baseOffset == 0) {
            val fm = tryStripFrontmatter(buf)
            if (fm != null) {
                styleState.frontmatterStripped = true
                startIdx = fm.endIdxExclusive
                // Always strip frontmatter from lexer input; if it contains theme config, parse it.
                val r = MermaidFrontend.parseFrontmatterThemeConfig(fm.text)
                if (r != null) {
                    styleState.styleConfig = r.config
                    newDiags += r.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                }
            } else if (buf.startsWith("---") && !isFinal) {
                // Potential frontmatter split across chunks; keep buffering until closed.
                styleState.rawPending = buf
                return StylePreprocessResult(emptyList(), emptyList())
            } else {
                // No frontmatter.
                styleState.frontmatterStripped = true
            }
        }

        val feeds = ArrayList<LexerFeed>()

        // Scan line-by-line. We only decide to skip `classDef` when the whole line is present.
        var i = startIdx
        var runStart: Int? = null
        while (i < buf.length) {
            val nl = buf.indexOf('\n', startIndex = i)
            if (nl < 0) break
            val lineStart = i
            val lineEndExclusive = nl + 1 // include '\n'
            val rawLine = buf.substring(lineStart, lineEndExclusive)
            val trimmedLeading = rawLine.trimStart()

            // Capture header hint early so preprocessing can be diagram-type aware even before lexer routing.
            if (headerHint == null) {
                headerHint = subPipelineRegistry.kindForHeaderText(trimmedLeading.trim())
            }

            val allowStyleDirectives = stylePreprocessor.supportsStyleDirectives(headerHint)
            val allowClassAssignDirective = stylePreprocessor.supportsClassAssignDirective(headerHint)
            val allowTripleColonRewrite = stylePreprocessor.supportsTripleColonRewrite(headerHint)

            if (allowStyleDirectives && trimmedLeading.startsWith("classDef ")) {
                // Flush pending kept run before the skipped line.
                if (runStart != null && runStart < lineStart) {
                    val seg = buf.substring(runStart, lineStart)
                    feeds += LexerFeed(
                        text = seg,
                        absoluteOffset = baseOffset + runStart,
                        endAbsoluteOffset = baseOffset + lineStart,
                    )
                }
                val parsed = MermaidFrontend.parseClassDefLine(trimmedLeading.trimEnd())
                if (parsed != null) {
                    for (cls in parsed.classes) styleState.styleClasses[cls.name] = cls.decl
                    newDiags += parsed.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                }
                // This line is skipped from lexer input.
                runStart = null
                // Skip the whole line (including newline).
            } else if (allowClassAssignDirective && trimmedLeading.startsWith("class ")) {
                if (runStart != null && runStart < lineStart) {
                    val seg = buf.substring(runStart, lineStart)
                    feeds += LexerFeed(
                        text = seg,
                        absoluteOffset = baseOffset + runStart,
                        endAbsoluteOffset = baseOffset + lineStart,
                    )
                }
                val parsed = MermaidFrontend.parseClassAssignLine(trimmedLeading.trimEnd())
                if (parsed != null) {
                    for (rawId in parsed.nodeIds) {
                        val id = NodeId(rawId)
                        val list = styleState.nodeClassBindings.getOrPut(id) { ArrayList() }
                        list.addAll(parsed.classNames)
                    }
                    newDiags += parsed.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                }
                // This line is skipped from lexer input.
                runStart = null
            } else if (allowStyleDirectives && trimmedLeading.startsWith("style ")) {
                if (runStart != null && runStart < lineStart) {
                    val seg = buf.substring(runStart, lineStart)
                    feeds += LexerFeed(
                        text = seg,
                        absoluteOffset = baseOffset + runStart,
                        endAbsoluteOffset = baseOffset + lineStart,
                    )
                }
                val parsed = MermaidFrontend.parseNodeStyleLine(trimmedLeading.trimEnd())
                if (parsed != null) {
                    for (rawId in parsed.nodeIds) {
                        styleState.nodeInlineStyles[NodeId(rawId)] = parsed.decl
                    }
                    newDiags += parsed.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                }
                // This line is skipped from lexer input.
                runStart = null
            } else if (allowStyleDirectives && trimmedLeading.startsWith("linkStyle ")) {
                if (runStart != null && runStart < lineStart) {
                    val seg = buf.substring(runStart, lineStart)
                    feeds += LexerFeed(
                        text = seg,
                        absoluteOffset = baseOffset + runStart,
                        endAbsoluteOffset = baseOffset + lineStart,
                    )
                }
                val parsed = MermaidFrontend.parseLinkStyleLine(trimmedLeading.trimEnd())
                if (parsed != null) {
                    if (parsed.isDefault) {
                        styleState.linkStyleDefault = parsed.decl
                    } else {
                        for (idx in parsed.indexes) styleState.linkStyleByIndex[idx] = parsed.decl
                    }
                    newDiags += parsed.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                }
                // This line is skipped from lexer input.
                runStart = null
            } else if (allowTripleColonRewrite && trimmedLeading.contains(":::")) {
                // Support Mermaid shorthand class application (`nodeId:::class1,class2`) for GraphIR-based diagrams.
                val r = extractTripleColonClasses(rawLine)
                if (r != null) {
                    if (runStart != null && runStart < lineStart) {
                        val seg = buf.substring(runStart, lineStart)
                        feeds += LexerFeed(
                            text = seg,
                            absoluteOffset = baseOffset + runStart,
                            endAbsoluteOffset = baseOffset + lineStart,
                        )
                        runStart = null
                    }
                    for ((id, classes) in r.bindings) {
                        val list = styleState.nodeClassBindings.getOrPut(id) { ArrayList() }
                        list.addAll(classes)
                    }
                    feeds += LexerFeed(
                        text = r.rewrittenLine,
                        absoluteOffset = baseOffset + lineStart,
                        endAbsoluteOffset = baseOffset + lineEndExclusive,
                    )
                    newDiags += r.diagnostics
                    styleState.cachedStyleExtras = emptyMap()
                    runStart = lineEndExclusive
                } else {
                    if (runStart == null) runStart = lineStart
                }
            } else {
                if (runStart == null) runStart = lineStart
            }
            i = lineEndExclusive
        }

        // Tail without newline: keep for next chunk unless EOS.
        val tailStart = i
        val tail = buf.substring(tailStart)
        if (isFinal) {
            if (tail.isNotEmpty()) {
                val trimmedLeading = tail.trimStart()
                val allowStyleDirectives = stylePreprocessor.supportsStyleDirectives(headerHint)
                val allowClassAssignDirective = stylePreprocessor.supportsClassAssignDirective(headerHint)
                val allowTripleColonRewrite = stylePreprocessor.supportsTripleColonRewrite(headerHint)

                if (allowStyleDirectives && trimmedLeading.startsWith("classDef ")) {
                    val parsed = MermaidFrontend.parseClassDefLine(trimmedLeading.trimEnd())
                    if (parsed != null) {
                        for (cls in parsed.classes) styleState.styleClasses[cls.name] = cls.decl
                        newDiags += parsed.diagnostics
                        styleState.cachedStyleExtras = emptyMap()
                    }
                    // Do not feed to lexer.
                } else if (allowClassAssignDirective && trimmedLeading.startsWith("class ")) {
                    val parsed = MermaidFrontend.parseClassAssignLine(trimmedLeading.trimEnd())
                    if (parsed != null) {
                        for (rawId in parsed.nodeIds) {
                            val id = NodeId(rawId)
                            val list = styleState.nodeClassBindings.getOrPut(id) { ArrayList() }
                            list.addAll(parsed.classNames)
                        }
                        newDiags += parsed.diagnostics
                        styleState.cachedStyleExtras = emptyMap()
                    }
                } else if (allowStyleDirectives && trimmedLeading.startsWith("style ")) {
                    val parsed = MermaidFrontend.parseNodeStyleLine(trimmedLeading.trimEnd())
                    if (parsed != null) {
                        for (rawId in parsed.nodeIds) styleState.nodeInlineStyles[NodeId(rawId)] = parsed.decl
                        newDiags += parsed.diagnostics
                        styleState.cachedStyleExtras = emptyMap()
                    }
                } else if (allowStyleDirectives && trimmedLeading.startsWith("linkStyle ")) {
                    val parsed = MermaidFrontend.parseLinkStyleLine(trimmedLeading.trimEnd())
                    if (parsed != null) {
                        if (parsed.isDefault) {
                            styleState.linkStyleDefault = parsed.decl
                        } else {
                            for (idx in parsed.indexes) styleState.linkStyleByIndex[idx] = parsed.decl
                        }
                        newDiags += parsed.diagnostics
                        styleState.cachedStyleExtras = emptyMap()
                    }
                } else if (allowTripleColonRewrite && trimmedLeading.contains(":::")) {
                    val r = extractTripleColonClasses(tail)
                    if (r != null) {
                        for ((id, classes) in r.bindings) {
                            val list = styleState.nodeClassBindings.getOrPut(id) { ArrayList() }
                            list.addAll(classes)
                        }
                        // Feed the rewritten tail directly, keeping same absolute offsets for this tail range.
                        feeds += LexerFeed(
                            text = r.rewrittenLine,
                            absoluteOffset = baseOffset + tailStart,
                            endAbsoluteOffset = baseOffset + tailStart + r.rewrittenLine.length,
                        )
                        newDiags += r.diagnostics
                        styleState.cachedStyleExtras = emptyMap()
                    } else {
                        if (runStart == null) runStart = tailStart
                    }
                } else {
                    if (runStart == null) runStart = tailStart
                }
            }
        } else {
            // Buffer tail to ensure we never split a style line across chunks.
            styleState.rawPending = tail
        }

        // Flush last kept run.
        if (runStart != null) {
            val end = if (isFinal) buf.length else tailStart
            if (end > runStart) {
                val seg = buf.substring(runStart, end)
                feeds += LexerFeed(
                    text = seg,
                    absoluteOffset = baseOffset + runStart,
                    endAbsoluteOffset = baseOffset + end,
                )
            }
        }

        return StylePreprocessResult(feeds, newDiags)
    }

    private data class FrontmatterStrip(val text: String, val endIdxExclusive: Int)

    private fun tryStripFrontmatter(buf: String): FrontmatterStrip? {
        // Only treat the very beginning as frontmatter.
        if (!buf.startsWith("---")) return null
        // Find a closing line that is exactly '---' (possibly with trailing spaces).
        var idx = 0
        while (true) {
            val nl = buf.indexOf('\n', startIndex = idx)
            if (nl < 0) return null
            val line = buf.substring(idx, nl).trimEnd()
            if (idx != 0 && line == "---") {
                val end = nl + 1
                return FrontmatterStrip(text = buf.substring(0, end), endIdxExclusive = end)
            }
            idx = nl + 1
        }
    }

    private fun wrapWithStyleDiagnosticsAndHints(advance: PipelineAdvance, newStyleDiags: List<Diagnostic>): PipelineAdvance {
        val styledSnapshot = injectStyleHints(advance.snapshot)
        val drawEntities = sub.drawEntitiesOrEmpty(styledSnapshot)
        val diagnostics = if (styleState.diagnosticsAll.isEmpty()) {
            styledSnapshot.diagnostics
        } else {
            styledSnapshot.diagnostics + styleState.diagnosticsAll
        }
        return familyKernel.finalizeAdvance(
            advance = advance.copy(snapshot = styledSnapshot),
            drawEntities = drawEntities,
            diagnostics = diagnostics,
            newDiagnostics = newStyleDiags,
        )
    }

    private fun injectStyleHints(snapshot: DiagramSnapshot): DiagramSnapshot {
        val ir = snapshot.ir ?: return snapshot
        // Only inject once per advance; extras are cached and updated when style inputs change.
        val extras = ensureStyleExtrasCached()
        if (extras.isEmpty()) return snapshot

        val updated: DiagramModel = when (ir) {
            is GraphIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.TimeSeriesIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.KanbanIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.XYChartIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.QuadrantChartIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.TreeIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.JourneyIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.SankeyIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            is com.hrm.diagram.core.ir.GitGraphIR -> ir.copy(styleHints = mergeHints(ir.styleHints, extras))
            else -> ir
        }
        return if (updated === ir) snapshot else snapshot.copy(ir = updated)
    }

    private fun mergeHints(old: StyleHints, styleExtras: Map<String, String>): StyleHints {
        val themeName = styleState.styleConfig?.theme?.name?.lowercase()
        val mergedExtras = if (old.extras.isEmpty()) styleExtras else old.extras + styleExtras
        return old.copy(
            theme = themeName ?: old.theme,
            extras = mergedExtras,
        )
    }

    private fun ensureStyleExtrasCached(): Map<String, String> {
        // Rebuild lazily only when we have new style inputs and cache is empty.
        if (styleState.cachedStyleExtras.isNotEmpty()) return styleState.cachedStyleExtras
        val out = LinkedHashMap<String, String>()
        val cfg = styleState.styleConfig
        if (cfg != null) {
            out["mermaid.styleModelVersion"] = "1"
            cfg.theme?.let { out["mermaid.theme"] = it.name.lowercase() }
            cfg.themeTokens?.let { out["mermaid.themeTokens"] = MermaidFrontend.encodeThemeTokens(it) }
            for ((k, v) in cfg.chartConfig) out["mermaid.config.$k"] = v
        }
        if (styleState.styleClasses.isNotEmpty()) {
            out["mermaid.classDefs"] = MermaidFrontend.encodeClassDefs(styleState.styleClasses)
        }
        styleState.cachedStyleExtras = out
        return styleState.cachedStyleExtras
    }

    override fun dispose() {
        familyKernel.clear()
        tokenLines.clear()
        pendingLines.clear()
        dispatcher.clear()
        stylePreprocessor.resetStyleIngress()
        headerHint = null
    }

    private data class TripleColonResult(
        val rewrittenLine: String,
        val bindings: List<Pair<NodeId, List<String>>>,
        val diagnostics: List<Diagnostic>,
    )

    /**
     * Extract `nodeId:::class1,class2` occurrences from a single raw line.
     * Returns a rewritten line with the `:::...` portion replaced by spaces (length preserved),
     * plus the class bindings for nodes.
     */
    private fun extractTripleColonClasses(rawLine: String): TripleColonResult? {
        // Ignore anything after Mermaid comment start.
        val commentIdx = rawLine.indexOf("%%")
        val scanLimit = if (commentIdx >= 0) commentIdx else rawLine.length
        val idx = rawLine.indexOf(":::", startIndex = 0)
        if (idx < 0 || idx >= scanLimit) return null

        val diags = ArrayList<Diagnostic>()
        val out = StringBuilder(rawLine)
        val bindings = ArrayList<Pair<NodeId, List<String>>>()

        var pos = 0
        while (true) {
            val tri = rawLine.indexOf(":::", startIndex = pos)
            if (tri < 0 || tri >= scanLimit) break

            // Find nodeId immediately before ':::' as a token-ish run (letters/digits/_/-).
            var left = tri - 1
            while (left >= 0 && rawLine[left].isWhitespace()) left--
            var idEnd = left + 1
            while (left >= 0 && isIdChar(rawLine[left])) left--
            val idStart = left + 1
            if (idStart >= idEnd) {
                pos = tri + 3
                continue
            }
            val nodeId = rawLine.substring(idStart, idEnd)

            // Parse class list right after ':::' until delimiter.
            var right = tri + 3
            val clsStart = right
            while (right < scanLimit && isClassChar(rawLine[right])) right++
            if (right <= clsStart) {
                pos = tri + 3
                continue
            }
            val clsRaw = rawLine.substring(clsStart, right)
            val classes = clsRaw.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            if (classes.isNotEmpty()) {
                bindings += NodeId(nodeId) to classes
            } else {
                diags += Diagnostic(Severity.WARNING, "Invalid ::: class list for '$nodeId' ignored", "MERMAID-W012")
            }

            // Replace the entire ':::...' segment with spaces to preserve length/offsets.
            for (i in tri until right) out.set(i, ' ')
            pos = right
        }

        if (bindings.isEmpty()) return null
        return TripleColonResult(out.toString(), bindings, diags)
    }

    private fun isIdChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || c == '-'

    private fun isClassChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || c == '-' || c == ','
}
