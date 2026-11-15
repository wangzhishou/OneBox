package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.kernel.RenderedSubPipelineKernel

internal class PlantUmlRenderSubPipelineKernel<M : DiagramModel, L : DiagramModel>(
    snapshot: () -> M,
    diagnostics: () -> List<Diagnostic>,
    layoutModel: (M) -> L,
    layout: (
        previous: LaidOutDiagram?,
        model: L,
        options: LayoutOptions,
        seq: Long,
        isFinal: Boolean,
    ) -> LaidOutDiagram,
    renderEntities: (model: M, layoutModel: L, laidOut: LaidOutDiagram, isFinal: Boolean) -> List<DrawEntity>,
    layoutOptions: (model: M, layoutModel: L, isFinal: Boolean) -> LayoutOptions = { _, _, isFinal ->
        LayoutOptions(incremental = !isFinal, allowGlobalReflow = isFinal)
    },
    beforeLayout: (model: M, layoutModel: L, isFinal: Boolean) -> Unit = { _, _, _ -> },
    postLayout: (model: M, layoutModel: L, laidOut: LaidOutDiagram, seq: Long, isFinal: Boolean) -> LaidOutDiagram =
        { _, _, laidOut, seq, _ -> laidOut.copy(seq = seq) },
) {
    private val delegate = RenderedSubPipelineKernel(
        snapshot = snapshot,
        diagnostics = diagnostics,
        layoutModel = layoutModel,
        layout = layout,
        renderEntities = renderEntities,
        layoutOptions = layoutOptions,
        beforeLayout = beforeLayout,
        postLayout = postLayout,
    )

    fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        delegate.render(previousSnapshot, seq, isFinal)
}

internal class PlantUmlFamilyRenderSubPipelineKernel<M : DiagramModel>(
    snapshot: () -> M,
    diagnostics: () -> List<Diagnostic>,
    layout: (previous: LaidOutDiagram?, model: M, options: LayoutOptions) -> LaidOutDiagram,
    renderEntities: (model: M, laidOut: LaidOutDiagram) -> List<DrawEntity>,
    layoutOptions: (model: M, isFinal: Boolean) -> LayoutOptions = { _, isFinal ->
        LayoutOptions(incremental = !isFinal, allowGlobalReflow = isFinal)
    },
    beforeLayout: (model: M) -> Unit = {},
    postLayout: (model: M, laidOut: LaidOutDiagram, seq: Long) -> LaidOutDiagram = { _, laidOut, seq ->
        laidOut.copy(seq = seq)
    },
) {
    private val delegate = PlantUmlRenderSubPipelineKernel(
        snapshot = snapshot,
        diagnostics = diagnostics,
        layoutModel = { it },
        layout = { previous, model, options, _, _ -> layout(previous, model, options) },
        renderEntities = { model, _, laidOut, _ -> renderEntities(model, laidOut) },
        layoutOptions = { model, _, isFinal -> layoutOptions(model, isFinal) },
        beforeLayout = { model, _, _ -> beforeLayout(model) },
        postLayout = { model, _, laidOut, seq, _ -> postLayout(model, laidOut, seq) },
    )

    fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState =
        delegate.render(previousSnapshot, seq, isFinal)
}
