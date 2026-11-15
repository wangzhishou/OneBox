package com.hrm.diagram.render.streaming.kernel

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.RenderedSubPipelineState

internal class RenderedSubPipelineKernel<M : DiagramModel, L : DiagramModel>(
    private val snapshot: () -> M,
    private val diagnostics: () -> List<Diagnostic>,
    private val layoutModel: (M) -> L,
    private val layout: (
        previous: LaidOutDiagram?,
        model: L,
        options: LayoutOptions,
        seq: Long,
        isFinal: Boolean,
    ) -> LaidOutDiagram,
    private val renderEntities: (model: M, layoutModel: L, laidOut: LaidOutDiagram, isFinal: Boolean) -> List<DrawEntity>,
    private val transformModel: (M) -> M = { it },
    private val layoutOptions: (model: M, layoutModel: L, isFinal: Boolean) -> LayoutOptions = { _, _, isFinal ->
        LayoutOptions(incremental = !isFinal, allowGlobalReflow = isFinal)
    },
    private val beforeLayout: (model: M, layoutModel: L, isFinal: Boolean) -> Unit = { _, _, _ -> },
    private val postLayout: (model: M, layoutModel: L, laidOut: LaidOutDiagram, seq: Long, isFinal: Boolean) -> LaidOutDiagram =
        { _, _, laidOut, seq, _ -> laidOut.copy(seq = seq) },
) {
    fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): RenderedSubPipelineState {
        val model = transformModel(snapshot())
        val layoutModel = layoutModel(model)
        beforeLayout(model, layoutModel, isFinal)
        val laidOut = layout(previousSnapshot.laidOut, layoutModel, layoutOptions(model, layoutModel, isFinal), seq, isFinal)
            .let { postLayout(model, layoutModel, it, seq, isFinal) }
        return RenderedSubPipelineState(
            ir = model,
            laidOut = laidOut,
            drawEntities = renderEntities(model, layoutModel, laidOut, isFinal),
            diagnostics = diagnostics(),
        )
    }
}

internal class FamilyRenderedSubPipelineKernel<M : DiagramModel>(
    snapshot: () -> M,
    diagnostics: () -> List<Diagnostic>,
    layout: (previous: LaidOutDiagram?, model: M, options: LayoutOptions) -> LaidOutDiagram,
    renderEntities: (model: M, laidOut: LaidOutDiagram) -> List<DrawEntity>,
    transformModel: (M) -> M = { it },
    layoutOptions: (model: M, isFinal: Boolean) -> LayoutOptions = { _, isFinal ->
        LayoutOptions(incremental = !isFinal, allowGlobalReflow = isFinal)
    },
    beforeLayout: (model: M) -> Unit = {},
    postLayout: (model: M, laidOut: LaidOutDiagram, seq: Long) -> LaidOutDiagram = { _, laidOut, seq ->
        laidOut.copy(seq = seq)
    },
) {
    private val delegate = RenderedSubPipelineKernel(
        snapshot = snapshot,
        diagnostics = diagnostics,
        layoutModel = { it },
        layout = { previous, model, options, _, _ -> layout(previous, model, options) },
        renderEntities = { model, _, laidOut, _ -> renderEntities(model, laidOut) },
        transformModel = transformModel,
        layoutOptions = { model, _, isFinal -> layoutOptions(model, isFinal) },
        beforeLayout = { model, _, _ -> beforeLayout(model) },
        postLayout = { model, _, laidOut, seq, _ -> postLayout(model, laidOut, seq) },
    )

    fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): RenderedSubPipelineState =
        delegate.render(previousSnapshot, seq, isFinal)
}
