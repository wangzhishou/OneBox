package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.core.streaming.Token
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.PipelineAdvance
import com.hrm.diagram.render.streaming.kernel.StreamingGraphPipelineKernel

internal class MermaidGraphSubPipelineKernel(
    private val acceptLine: (List<Token>) -> IrPatchBatch,
    private val snapshot: () -> GraphIR,
    private val diagnostics: () -> List<Diagnostic>,
    private val graphKernel: StreamingGraphPipelineKernel,
    private val beforeAdvance: (GraphIR, Boolean) -> Unit = { _, _ -> },
) {
    private val styleTransform = MermaidStyleTransformState.graph()

    fun updateGraphStyles(styles: MermaidGraphStyleState) {
        styleTransform.update(styles)
    }

    fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<List<Token>>,
        seq: Long,
        isFinal: Boolean,
    ): PipelineAdvance {
        for (line in lines) {
            acceptLine(line)
        }
        val ir = styleTransform.apply(snapshot())
        beforeAdvance(ir, isFinal)
        return graphKernel.advance(
            previousSnapshot = previousSnapshot,
            seq = seq,
            isFinal = isFinal,
            ir = ir,
            diagnostics = diagnostics(),
        )
    }

    fun drawEntities(): List<DrawEntity> = graphKernel.drawEntities()

    fun clear() {
        graphKernel.clear()
    }
}
