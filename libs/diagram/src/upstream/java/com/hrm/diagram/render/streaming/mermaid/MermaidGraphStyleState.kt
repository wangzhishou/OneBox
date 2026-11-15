package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.ir.ArgbColor
import com.hrm.diagram.core.ir.Edge
import com.hrm.diagram.core.ir.EdgeStyle
import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.Node
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.parser.mermaid.MermaidStyleDecl

/**
 * Minimal, streaming-friendly style state for Mermaid GraphIR-based diagrams.
 *
 * Scope (Phase 1): `classDef` + `class` + `style` + `linkStyle` (node/edge colors & strokes).
 *
 * Important: This intentionally does NOT change geometry-related properties (font/padding).
 * Any geometry-affecting styling must be delayed to `finish()` per the pinned-layout contract.
 */
internal class MermaidGraphStyleState {
    var classDefs: Map<String, MermaidStyleDecl> = emptyMap()
    var nodeClassBindings: Map<NodeId, List<String>> = emptyMap()
    var nodeInline: Map<NodeId, MermaidStyleDecl> = emptyMap()
    var linkDefault: MermaidStyleDecl? = null
    /** Mermaid linkStyle index is 1-based and follows edge definition order. */
    var linkByIndex: Map<Int, MermaidStyleDecl> = emptyMap()

    fun clear() {
        classDefs = emptyMap()
        nodeClassBindings = emptyMap()
        nodeInline = emptyMap()
        linkDefault = null
        linkByIndex = emptyMap()
    }

    fun applyTo(ir: GraphIR): GraphIR {
        if (classDefs.isEmpty() && nodeClassBindings.isEmpty() && nodeInline.isEmpty() && linkDefault == null && linkByIndex.isEmpty()) {
            return ir
        }

        val defaultClassDecl = classDefs["default"]

        val newNodes = ir.nodes.map { n ->
            val mergedDecl = mergeNodeDecl(n.id, defaultClassDecl)
            if (mergedDecl == null) n else n.copy(style = mergeNodeStyle(n.style, mergedDecl))
        }
        val newEdges = ir.edges.mapIndexed { i, e ->
            val idx1 = i + 1
            val mergedDecl = mergeEdgeDecl(idx1)
            if (mergedDecl == null) e else e.copy(style = mergeEdgeStyle(e.style, mergedDecl))
        }
        return ir.copy(nodes = newNodes, edges = newEdges)
    }

    private fun mergeNodeDecl(nodeId: NodeId, defaultClassDecl: MermaidStyleDecl?): MermaidStyleDecl? {
        var out: MermaidStyleDecl? = null
        // theme default is represented by the existing NodeStyle in IR (base style).
        defaultClassDecl?.let { out = mergeMermaidStyleDecl(out, it) }
        nodeClassBindings[nodeId]?.forEach { cls ->
            val decl = classDefs[cls] ?: return@forEach
            out = mergeMermaidStyleDecl(out, decl)
        }
        nodeInline[nodeId]?.let { out = mergeMermaidStyleDecl(out, it) }
        return out
    }

    private fun mergeEdgeDecl(idx1: Int): MermaidStyleDecl? {
        var out: MermaidStyleDecl? = null
        linkDefault?.let { out = mergeMermaidStyleDecl(out, it) }
        linkByIndex[idx1]?.let { out = mergeMermaidStyleDecl(out, it) }
        return out
    }

    private fun mergeNodeStyle(base: NodeStyle, decl: MermaidStyleDecl): NodeStyle {
        return NodeStyle(
            fill = decl.fill ?: base.fill,
            stroke = decl.stroke ?: base.stroke,
            strokeWidth = decl.strokeWidthPx ?: base.strokeWidth,
            textColor = decl.textColor ?: base.textColor,
        )
    }

    private fun mergeEdgeStyle(base: EdgeStyle, decl: MermaidStyleDecl): EdgeStyle {
        // Mermaid uses 'stroke'/'stroke-width' for edge line, and 'color' for label text.
        // We map 'fill' to labelBg as a pragmatic, Mermaid-like "chip" customization.
        return EdgeStyle(
            color = decl.stroke ?: base.color,
            width = decl.strokeWidthPx ?: base.width,
            dash = decl.strokeDashArrayPx ?: base.dash,
            labelBg = decl.fill ?: base.labelBg,
        )
    }
}
