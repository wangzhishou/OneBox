package com.hrm.diagram.render.streaming.dispatcher

import com.hrm.diagram.render.streaming.StreamingSubPipeline

/** Registry that owns the mapping from language-specific diagram kind to sub-pipeline factory. */
internal interface SubPipelineRegistry<K : Any, P : StreamingSubPipeline> {
    val defaultKind: K

    fun create(kind: K): P?
}

/**
 * Session-local selector for language-specific sub-pipelines.
 *
 * The dispatcher intentionally does not parse source text. Mermaid and PlantUML adapters keep
 * their own header/cue detection while sharing the selected kind and sub-pipeline lifecycle here.
 */
internal class DiagramKindDispatcher<K : Any, P : StreamingSubPipeline>(
    private val registry: SubPipelineRegistry<K, P>,
) {
    var currentKind: K? = null
        private set

    var current: P? = null
        private set

    fun attach(kind: K): P? {
        current?.let { return it }
        val next = registry.create(kind) ?: return null
        currentKind = kind
        current = next
        return next
    }

    fun attachDefault(): P? = attach(registry.defaultKind)

    fun clear() {
        current?.dispose()
        current = null
        currentKind = null
    }
}
