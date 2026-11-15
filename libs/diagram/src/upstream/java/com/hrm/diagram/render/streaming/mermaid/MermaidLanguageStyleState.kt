package com.hrm.diagram.render.streaming.mermaid

import com.hrm.diagram.core.ir.Diagnostic
import com.hrm.diagram.core.ir.NodeId
import com.hrm.diagram.parser.mermaid.MermaidStyleConfig
import com.hrm.diagram.parser.mermaid.MermaidStyleDecl
import com.hrm.diagram.render.streaming.style.LanguageStyleState

internal class MermaidLanguageStyleState : LanguageStyleState {
    var rawPending: String = ""
    var rawPendingAbsoluteOffset: Int = 0
    var frontmatterStripped: Boolean = false
    var styleConfig: MermaidStyleConfig? = null
    val styleClasses: LinkedHashMap<String, MermaidStyleDecl> = LinkedHashMap()
    val nodeClassBindings: LinkedHashMap<NodeId, MutableList<String>> = LinkedHashMap()
    val nodeInlineStyles: LinkedHashMap<NodeId, MermaidStyleDecl> = LinkedHashMap()
    var linkStyleDefault: MermaidStyleDecl? = null
    val linkStyleByIndex: LinkedHashMap<Int, MermaidStyleDecl> = LinkedHashMap()
    val diagnosticsAll: MutableList<Diagnostic> = ArrayList()
    var cachedStyleExtras: Map<String, String> = emptyMap()
    val graphStyleState: MermaidGraphStyleState = MermaidGraphStyleState()

    fun invalidateExtras() {
        cachedStyleExtras = emptyMap()
    }

    override fun reset() {
        rawPending = ""
        rawPendingAbsoluteOffset = 0
        frontmatterStripped = false
        styleConfig = null
        styleClasses.clear()
        nodeClassBindings.clear()
        nodeInlineStyles.clear()
        linkStyleDefault = null
        linkStyleByIndex.clear()
        graphStyleState.clear()
        diagnosticsAll.clear()
        cachedStyleExtras = emptyMap()
    }
}
