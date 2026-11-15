package com.hrm.diagram.render.streaming.kernel

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawCommandStore
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.cache.withMeasuredEntityTextBounds
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.PipelineAdvanceAssembler
import com.hrm.diagram.render.streaming.RenderedSubPipelineState
import com.hrm.diagram.render.streaming.StreamingDiff

/**
 * Shared finalization kernel for non-GraphIR streaming families.
 *
 * Sub-pipelines own parsing, layout, and entity rendering; this kernel owns the
 * session-local DrawCommandStore, text bounds finalization, and patch assembly.
 */
internal class StreamingFamilyPipelineKernel(
    private val textMeasurer: TextMeasurer,
) {
    private val drawStore = DrawCommandStore()

    fun finalizeAdvance(
        advance: PipelineAdvance,
        drawEntities: List<DrawEntity>,
        diagnostics: List<Diagnostic> = advance.snapshot.diagnostics,
        newDiagnostics: List<Diagnostic> = emptyList(),
    ): PipelineAdvance {
        val drawDelta = drawStore.updateEntities(drawEntities.withMeasuredEntityTextBounds(textMeasurer))
        val mergedPatchDiagnostics = if (newDiagnostics.isEmpty()) {
            advance.patch.newDiagnostics
        } else {
            advance.patch.newDiagnostics + newDiagnostics
        }
        val model = advance.snapshot.ir ?: return advance.copy(
            snapshot = advance.snapshot.copy(drawCommands = drawDelta.fullFrame, diagnostics = diagnostics),
            patch = advance.patch.copy(addedDrawCommands = drawDelta.addedCommands, newDiagnostics = mergedPatchDiagnostics),
        )
        return PipelineAdvanceAssembler.assemble(
            seq = advance.snapshot.seq,
            isFinal = advance.snapshot.isFinal,
            sourceLanguage = advance.snapshot.sourceLanguage,
            model = model,
            laidOut = advance.snapshot.laidOut,
            drawDelta = drawDelta,
            diagnostics = diagnostics,
            diff = StreamingDiff(
                addedNodes = advance.patch.addedNodes,
                addedEdges = advance.patch.addedEdges,
                newDiagnostics = mergedPatchDiagnostics,
                irPatches = advance.irBatch.patches,
            ),
        )
    }

    fun assembleRendered(
        seq: Long,
        isFinal: Boolean,
        sourceLanguage: SourceLanguage,
        rendered: RenderedSubPipelineState,
        diagnostics: List<Diagnostic>,
        diff: StreamingDiff,
    ): PipelineAdvance = assembleRendered(
        seq = seq,
        isFinal = isFinal,
        sourceLanguage = sourceLanguage,
        model = rendered.ir,
        laidOut = rendered.laidOut,
        drawEntities = rendered.drawEntities,
        diagnostics = diagnostics,
        diff = diff,
    )

    fun assembleRendered(
        seq: Long,
        isFinal: Boolean,
        sourceLanguage: SourceLanguage,
        model: DiagramModel,
        laidOut: LaidOutDiagram?,
        drawEntities: List<DrawEntity>,
        diagnostics: List<Diagnostic>,
        diff: StreamingDiff,
    ): PipelineAdvance {
        val drawDelta = drawStore.updateEntities(drawEntities.withMeasuredEntityTextBounds(textMeasurer))
        return PipelineAdvanceAssembler.assemble(
            seq = seq,
            isFinal = isFinal,
            sourceLanguage = sourceLanguage,
            model = model,
            laidOut = laidOut,
            drawDelta = drawDelta,
            diagnostics = diagnostics,
            diff = diff,
        )
    }

    fun clear() {
        drawStore.clear()
    }
}
