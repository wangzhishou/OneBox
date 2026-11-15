package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.streaming.IrPatchBatch
import com.hrm.diagram.render.streaming.DiagramSnapshot
import com.hrm.diagram.render.streaming.LineStreamingSubPipeline
import com.hrm.diagram.render.streaming.RenderedSubPipelineState
import com.hrm.diagram.render.streaming.RenderStateProvider

internal typealias PlantUmlRenderState = RenderedSubPipelineState

internal interface PlantUmlSubPipeline : LineStreamingSubPipeline<String, IrPatchBatch>, RenderStateProvider<PlantUmlRenderState> {
    override fun acceptLine(line: String): IrPatchBatch
    fun finish(blockClosed: Boolean): IrPatchBatch
    override fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): PlantUmlRenderState
}
