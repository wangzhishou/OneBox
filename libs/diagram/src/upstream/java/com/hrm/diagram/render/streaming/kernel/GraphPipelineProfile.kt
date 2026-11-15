package com.hrm.diagram.render.streaming.kernel

import com.hrm.diagram.core.draw.Size
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.SourceLanguage
import com.hrm.diagram.core.layout.LayoutOptions
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.layout.IncrementalLayout
import com.hrm.diagram.layout.LaidOutDiagram
import com.hrm.diagram.render.cache.DrawEntity
import com.hrm.diagram.render.graph.GraphMeasurePolicy

internal data class GraphPipelineProfile(
    val sourceLanguage: SourceLanguage,
    val defaultNodeSize: Size,
    val measurePolicy: GraphMeasurePolicy,
    val layout: IncrementalLayout<GraphIR> =
        StreamingGraphPipelineKernel.sugiyamaLayout(defaultNodeSize, measurePolicy),
    val layoutModel: (GraphIR) -> GraphIR = { it },
    val layoutOptions: (GraphIR, Boolean) -> LayoutOptions = { ir, isFinal ->
        LayoutOptions(
            direction = ir.styleHints.direction,
            incremental = !isFinal,
            allowGlobalReflow = isFinal,
            extras = ir.styleHints.extras,
        )
    },
    val postLayout: (GraphIR, LaidOutDiagram) -> LaidOutDiagram = { _, laid -> laid },
    val edgeKeyOf: (index: Int, edge: Edge) -> String = { index, edge -> "${edge.from.value}->${edge.to.value}:$index" },
) {
    fun kernel(
        textMeasurer: TextMeasurer,
        renderEntities: (GraphIR, LaidOutDiagram) -> List<DrawEntity>,
    ): StreamingGraphPipelineKernel =
        StreamingGraphPipelineKernel(
            textMeasurer = textMeasurer,
            sourceLanguage = sourceLanguage,
            measurePolicy = measurePolicy,
            layout = layout,
            renderEntities = renderEntities,
            layoutModel = layoutModel,
            layoutOptions = layoutOptions,
            postLayout = postLayout,
            edgeKeyOf = edgeKeyOf,
        )
}
