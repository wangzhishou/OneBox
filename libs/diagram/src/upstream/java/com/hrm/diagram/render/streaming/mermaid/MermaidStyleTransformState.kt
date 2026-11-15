package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.ir.GraphIR
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.core.ir.NodeStyle
import com.hrm.diagram.parser.mermaid.MermaidStyleDecl

internal class MermaidStyleTransformState<M>(
    private val transform: (model: M, styles: MermaidGraphStyleState) -> M,
) {
    private var styles: MermaidGraphStyleState? = null

    fun update(styles: MermaidGraphStyleState) {
        this.styles = styles
    }

    fun apply(model: M): M =
        styles?.let { transform(model, it) } ?: model

    fun nodeStyleFor(
        nodeId: NodeId,
        classNames: List<String>? = null,
    ): NodeStyle? {
        val styles = styles ?: return null
        val classes = classNames ?: styles.nodeClassBindings[nodeId].orEmpty()
        if (styles.classDefs.isEmpty() && classes.isEmpty() && styles.nodeInline[nodeId] == null) return null

        var decl: MermaidStyleDecl? = null
        styles.classDefs["default"]?.let { decl = mergeMermaidStyleDecl(decl, it) }
        for (className in classes) {
            styles.classDefs[className]?.let { decl = mergeMermaidStyleDecl(decl, it) }
        }
        styles.nodeInline[nodeId]?.let { decl = mergeMermaidStyleDecl(decl, it) }
        val merged = decl ?: return null
        return NodeStyle(
            fill = merged.fill,
            stroke = merged.stroke,
            strokeWidth = merged.strokeWidthPx,
            textColor = merged.textColor,
        )
    }

    companion object {
        fun graph(): MermaidStyleTransformState<GraphIR> =
            MermaidStyleTransformState { model, styles -> styles.applyTo(model) }
    }
}

internal fun mergeMermaidStyleDecl(base: MermaidStyleDecl?, override: MermaidStyleDecl): MermaidStyleDecl {
    val b = base ?: MermaidStyleDecl()
    return MermaidStyleDecl(
        fill = override.fill ?: b.fill,
        stroke = override.stroke ?: b.stroke,
        strokeWidthPx = override.strokeWidthPx ?: b.strokeWidthPx,
        strokeDashArrayPx = override.strokeDashArrayPx ?: b.strokeDashArrayPx,
        textColor = override.textColor ?: b.textColor,
        fontFamily = override.fontFamily ?: b.fontFamily,
        fontSizePx = override.fontSizePx ?: b.fontSizePx,
        fontWeight = override.fontWeight ?: b.fontWeight,
        italic = override.italic ?: b.italic,
        extras = if (b.extras.isEmpty()) override.extras else b.extras + override.extras,
    )
}
