package com.hrm.diagram.render.streaming

import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.streaming.IrPatch

internal class StreamingDiffTracker(
    private val edgeKeyOf: (index: Int, edge: Edge) -> String = { index, edge ->
        "${edge.from.value}->${edge.to.value}:$index"
    },
) {
    private var lastNodeIds: Set<NodeId> = emptySet()
    private var lastEdgeKeys: Set<String> = emptySet()
    private var lastDiagnosticCount: Int = 0

    fun diffGraph(ir: GraphIR, diagnostics: List<Diagnostic>): StreamingDiff {
        val nodeIds = ir.nodes.map { it.id }.toSet()
        val edgeKeys = ir.edges.mapIndexed(edgeKeyOf).toSet()
        val addedNodes = ir.nodes.filter { it.id !in lastNodeIds }.map { it.id }
        val addedEdges = ir.edges.filterIndexed { index, edge -> edgeKeyOf(index, edge) !in lastEdgeKeys }
        val newDiagnostics = diagnostics.drop(lastDiagnosticCount)

        lastNodeIds = nodeIds
        lastEdgeKeys = edgeKeys
        lastDiagnosticCount = diagnostics.size

        return StreamingDiff(
            addedNodes = addedNodes,
            addedEdges = addedEdges,
            newDiagnostics = newDiagnostics,
            irPatches = buildList {
                addedNodes.forEach { id ->
                    ir.nodes.firstOrNull { it.id == id }?.let { add(IrPatch.AddNode(it)) }
                }
                addedEdges.forEach { add(IrPatch.AddEdge(it)) }
                newDiagnostics.forEach { add(IrPatch.AddDiagnostic(it)) }
            },
        )
    }

    fun reset() {
        lastNodeIds = emptySet()
        lastEdgeKeys = emptySet()
        lastDiagnosticCount = 0
    }
}

internal data class StreamingDiff(
    val addedNodes: List<NodeId>,
    val addedEdges: List<Edge>,
    val newDiagnostics: List<Diagnostic>,
    val irPatches: List<IrPatch>,
)
