package com.hrm.diagram.render.streaming

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity

internal data class RenderedSubPipelineState(
    val ir: DiagramModel,
    val laidOut: LaidOutDiagram,
    val diagnostics: List<Diagnostic>,
    val drawEntities: List<DrawEntity>,
)
