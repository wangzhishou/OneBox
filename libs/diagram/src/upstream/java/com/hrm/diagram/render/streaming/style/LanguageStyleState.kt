package com.hrm.diagram.render.streaming.style

/** Session-local language style state that can be reset independently from parser state. */
internal interface LanguageStyleState {
    fun reset()
}
