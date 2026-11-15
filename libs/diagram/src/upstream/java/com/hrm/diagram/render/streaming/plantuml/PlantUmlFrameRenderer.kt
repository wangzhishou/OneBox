package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.ir.DiagramModel
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.family.FrameEntityRenderer

internal object PlantUmlFrameRenderer {
    fun sink(
        model: DiagramModel,
        laidOut: LaidOutDiagram,
    ): FrameEntityRenderer.EntitySink =
        FrameEntityRenderer.sink(prefix = "plantuml", model = model, laidOut = laidOut)
}
