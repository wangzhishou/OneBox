package com.hrm.diagram.render.streaming.plantuml

import com.hrm.diagram.render.streaming.style.LanguageStyleState

internal class PlantUmlLanguageStyleState : LanguageStyleState {
    val bufferedSkinparamLines: MutableList<String> = ArrayList()

    var bufferingSkinparamBlock: Boolean = false
    var bufferingStyleBlock: Boolean = false
    var ignoredSkinparamBlock: Boolean = false

    fun appendBufferedSkinparam(trimmed: String) {
        bufferedSkinparamLines += trimmed
        if (trimmed.endsWith("{")) bufferingSkinparamBlock = true
    }

    fun continueBufferedSkinparam(trimmed: String): Boolean {
        if (!bufferingSkinparamBlock) return false
        bufferedSkinparamLines += trimmed
        if (trimmed == "}") bufferingSkinparamBlock = false
        return true
    }

    fun continueIgnoredSkinparam(trimmed: String): Boolean {
        if (!ignoredSkinparamBlock) return false
        if (trimmed == "}") ignoredSkinparamBlock = false
        return true
    }

    override fun reset() {
        bufferedSkinparamLines.clear()
        bufferingSkinparamBlock = false
        bufferingStyleBlock = false
        ignoredSkinparamBlock = false
    }
}
