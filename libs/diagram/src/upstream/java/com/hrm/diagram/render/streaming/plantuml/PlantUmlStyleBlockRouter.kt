package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.core.streaming.IrPatch
import com.hrm.diagram.render.streaming.style.LanguageStyleIngress

internal class PlantUmlStyleBlockRouter(
    override val state: PlantUmlLanguageStyleState,
    private val registry: PlantUmlSubPipelineRegistry,
) : LanguageStyleIngress<PlantUmlLanguageStyleState> {
    fun route(
        trimmed: String,
        activeKind: PlantUmlDiagramKind?,
        activePipeline: PlantUmlSubPipeline?,
        bufferedBodyLines: MutableList<String>,
        out: MutableList<IrPatch>,
        ignoredSkinparamWarning: () -> IrPatch,
    ): Boolean {
        if (state.continueBufferedSkinparam(trimmed)) return true
        if (state.continueIgnoredSkinparam(trimmed)) return true
        if (state.bufferingStyleBlock) {
            forwardStyleLine(trimmed, activePipeline, bufferedBodyLines, out)
            if (trimmed.equals("</style>", ignoreCase = true)) state.bufferingStyleBlock = false
            return true
        }
        if (trimmed.equals("<style>", ignoreCase = true) || trimmed.startsWith("<style ", ignoreCase = true)) {
            state.bufferingStyleBlock = true
            forwardStyleLine(trimmed, activePipeline, bufferedBodyLines, out)
            return true
        }
        if (trimmed.startsWith("skinparam", ignoreCase = true)) {
            if (activePipeline != null && activeKind != null && registry.acceptsActiveSkinparam(activeKind, trimmed)) {
                out += activePipeline.acceptLine(trimmed).patches
            } else if (activePipeline != null) {
                out += ignoredSkinparamWarning()
                if (trimmed.endsWith("{")) state.ignoredSkinparamBlock = true
            } else {
                state.appendBufferedSkinparam(trimmed)
            }
            return true
        }
        return false
    }

    private fun forwardStyleLine(
        trimmed: String,
        activePipeline: PlantUmlSubPipeline?,
        bufferedBodyLines: MutableList<String>,
        out: MutableList<IrPatch>,
    ) {
        if (activePipeline != null) {
            out += activePipeline.acceptLine(trimmed).patches
        } else {
            bufferedBodyLines += trimmed
        }
    }
}
