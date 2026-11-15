package com.hrm.diagram.render.streaming

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawCommandDelta

internal object PipelineAdvanceAssembler {
    fun assemble(
        seq: Long,
        isFinal: Boolean,
        sourceLanguage: SourceLanguage,
        model: DiagramModel,
        laidOut: LaidOutDiagram?,
        drawDelta: DrawCommandDelta,
        diagnostics: List<Diagnostic>,
        diff: StreamingDiff,
    ): PipelineAdvance {
        val snapshot = DiagramSnapshot(
            ir = model,
            laidOut = laidOut,
            drawCommands = drawDelta.fullFrame,
            diagnostics = diagnostics,
            seq = seq,
            isFinal = isFinal,
            sourceLanguage = sourceLanguage,
        )
        return PipelineAdvance(
            snapshot = snapshot,
            patch = SessionPatch(
                seq = seq,
                addedNodes = diff.addedNodes,
                addedEdges = diff.addedEdges,
                addedDrawCommands = drawDelta.addedCommands,
                newDiagnostics = diff.newDiagnostics,
                isFinal = isFinal,
            ),
            irBatch = IrPatchBatch(seq, diff.irPatches),
        )
    }
}
