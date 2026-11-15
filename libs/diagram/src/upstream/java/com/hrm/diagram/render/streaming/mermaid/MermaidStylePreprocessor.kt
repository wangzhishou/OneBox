package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.render.streaming.style.LanguageStyleIngress

internal class MermaidStylePreprocessor(
    override val state: MermaidLanguageStyleState,
) : LanguageStyleIngress<MermaidLanguageStyleState> {
    fun supportsStyleDirectives(kind: MermaidDiagramKind?): Boolean =
        kind == MermaidDiagramKind.Flowchart ||
            kind == MermaidDiagramKind.Er ||
            kind == MermaidDiagramKind.State ||
            kind == MermaidDiagramKind.Class ||
            kind == MermaidDiagramKind.Requirement ||
            kind == MermaidDiagramKind.Architecture ||
            kind == MermaidDiagramKind.C4 ||
            kind == MermaidDiagramKind.Block

    fun supportsClassAssignDirective(kind: MermaidDiagramKind?): Boolean =
        kind == MermaidDiagramKind.Flowchart ||
            kind == MermaidDiagramKind.Er ||
            kind == MermaidDiagramKind.State ||
            kind == MermaidDiagramKind.Requirement ||
            kind == MermaidDiagramKind.Architecture ||
            kind == MermaidDiagramKind.C4 ||
            kind == MermaidDiagramKind.Block

    fun supportsTripleColonRewrite(kind: MermaidDiagramKind?): Boolean =
        supportsClassAssignDirective(kind)

    fun invalidateExtras() {
        state.invalidateExtras()
    }
}
