package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.Severity
import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.text.HeuristicTextMeasurer
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.theme.DiagramTheme
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.SessionPatch
import com.hrm.diagram.render.streaming.SessionPipeline
import com.hrm.diagram.render.streaming.StreamingDiff
import com.hrm.diagram.render.streaming.dispatcher.DiagramKindDispatcher
import com.hrm.diagram.render.streaming.ingress.StreamingLineIngress
import com.hrm.diagram.render.streaming.kernel.StreamingFamilyPipelineKernel

/**
 * Streaming PlantUML dispatcher for the Phase-4 MVP.
 *
 * Current scope:
 * - `sequence` sub-pipeline
 * - `class` sub-pipeline
 * - `@startuml ... @enduml`
 * - `skinparam` warning passthrough
 *
 * Unsupported PlantUML diagram families keep yielding diagnostics instead of throwing.
 */
internal class PlantUmlSessionPipeline(
    private val textMeasurer: TextMeasurer = HeuristicTextMeasurer(),
    private val theme: DiagramTheme = DiagramTheme.Default,
) : SessionPipeline {
    private val diagnosticsAll: MutableList<Diagnostic> = ArrayList()
    private val familyKernel = StreamingFamilyPipelineKernel(textMeasurer)

    private val lineIngress = StreamingLineIngress()
    private var blockStarted: Boolean = false
    private var blockClosed: Boolean = false
    private val bufferedBodyLines: MutableList<String> = ArrayList()
    private val styleState = PlantUmlLanguageStyleState()
    private val subPipelineRegistry = PlantUmlSubPipelineRegistry(textMeasurer, theme)
    private val dispatcher = DiagramKindDispatcher(subPipelineRegistry)
    private val styleRouter = PlantUmlStyleBlockRouter(styleState, subPipelineRegistry)
    private val subPipeline: PlantUmlSubPipeline?
        get() = dispatcher.current
    private var closingDirective: String = "@enduml"

    override fun advance(
        previousSnapshot: DiagramSnapshot,
        chunk: CharSequence,
        absoluteOffset: Int,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance {
        val lines = lineIngress.feed(chunk, isFinal)

        val newPatches = ArrayList<IrPatch>()
        for (line in lines) {
            processLine(line, newPatches)
        }
        if (isFinal) {
            materializeDeferredBodyIfNeeded(newPatches)
            subPipeline?.let { newPatches += it.finish(blockClosed).patches }
        }
        val newDiagnostics = newPatches.filterIsInstance<IrPatch.AddDiagnostic>().map { it.diagnostic }
        if (newDiagnostics.isNotEmpty()) diagnosticsAll += newDiagnostics

        val rendered = subPipeline?.render(previousSnapshot, seq, isFinal)
        return if (rendered != null) {
            familyKernel.assembleRendered(
                seq = seq,
                isFinal = isFinal,
                sourceLanguage = previousSnapshot.sourceLanguage,
                rendered = rendered,
                diagnostics = diagnosticsAll + rendered.diagnostics,
                diff = StreamingDiff(
                    addedNodes = emptyList(),
                    addedEdges = emptyList(),
                    newDiagnostics = newDiagnostics,
                    irPatches = newPatches,
                ),
            )
        } else {
            PipelineAdvance(
                snapshot = previousSnapshot.copy(seq = seq, isFinal = isFinal),
                patch = SessionPatch.empty(seq, isFinal),
                irBatch = IrPatchBatch(seq, newPatches),
            )
        }
    }

    private fun processLine(line: String, out: MutableList<IrPatch>) {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("'") || trimmed.startsWith("//")) return

        if (!blockStarted) {
            if (trimmed.equals("@startuml", ignoreCase = true)) {
                blockStarted = true
                closingDirective = "@enduml"
                return
            }
            val explicitStart = subPipelineRegistry.matchStartDirective(trimmed)
            if (explicitStart != null) {
                blockStarted = true
                closingDirective = explicitStart.closingDirective
                attachSubPipeline(explicitStart.kind, out)
                return
            }
            return
        }
        if (blockClosed) return

        if (trimmed.equals(closingDirective, ignoreCase = true)) {
            blockClosed = true
            return
        }
        if (styleRouter.route(trimmed, dispatcher.currentKind, subPipeline, bufferedBodyLines, out, ::ignoredSkinparamWarning)) return

        val chosen = subPipeline
        if (chosen != null) {
            val chosenKind = dispatcher.currentKind
            val payloadLine = if (chosenKind != null && subPipelineRegistry.requiresRawPayload(chosenKind)) line else trimmed
            out += chosen.acceptLine(payloadLine).patches
            return
        }

        bufferedBodyLines += trimmed
        val kind = subPipelineRegistry.classifyImmediate(trimmed)
        if (kind != null && !subPipelineRegistry.shouldDeferImmediate(kind, trimmed, bufferedBodyLines)) {
            attachSubPipeline(kind, out)
        }
    }

    private fun materializeDeferredBodyIfNeeded(out: MutableList<IrPatch>) {
        if (subPipeline != null || bufferedBodyLines.isEmpty()) return
        val kind = subPipelineRegistry.detectBufferedKind(bufferedBodyLines) ?: PlantUmlDiagramKind.Sequence
        attachSubPipeline(kind, out)
    }

    private fun attachSubPipeline(kind: PlantUmlDiagramKind, out: MutableList<IrPatch>) {
        if (subPipeline != null) return
        val selected = dispatcher.attach(kind) ?: return
        if (styleState.bufferedSkinparamLines.isNotEmpty()) {
            for (line in styleState.bufferedSkinparamLines) {
                if (subPipelineRegistry.acceptsBufferedSkinparamLine(kind, line)) {
                    out += selected.acceptLine(line).patches
                } else {
                    out += ignoredSkinparamWarning()
                }
            }
            styleState.bufferedSkinparamLines.clear()
        }
        val pending = bufferedBodyLines.toList()
        bufferedBodyLines.clear()
        for (line in pending) {
            out += selected.acceptLine(line).patches
        }
    }

    private fun ignoredSkinparamWarning(): IrPatch =
        IrPatch.AddDiagnostic(
            Diagnostic(
                severity = Severity.WARNING,
                message = "Unsupported 'skinparam' ignored",
                code = "PLANTUML-W001",
            ),
        )

    override fun dispose() {
        familyKernel.clear()
        diagnosticsAll.clear()
        lineIngress.clear()
        bufferedBodyLines.clear()
        styleRouter.resetStyleIngress()
        dispatcher.clear()
    }

    @Suppress("unused")
    private fun unusedOffset(offset: Int) = offset
}
