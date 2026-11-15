package com.hrm.diagram.render.streaming.style

/**
 * Shared seam for language-specific style ingress.
 *
 * Mermaid strips/re-writes style directives before token parsing, while PlantUML routes
 * skinparam/style blocks before sub-pipeline parsing. Both adapters own a resettable
 * [LanguageStyleState] so session pipelines do not manage style lifecycle ad hoc.
 */
internal interface LanguageStyleIngress<S : LanguageStyleState> {
    val state: S

    fun resetStyleIngress() {
        state.reset()
    }
}
