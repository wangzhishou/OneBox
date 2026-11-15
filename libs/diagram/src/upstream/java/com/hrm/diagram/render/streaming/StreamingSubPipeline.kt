package com.hrm.diagram.render.streaming

import com.hrm.diagram.render.cache.DrawEntity

/** Shared lifecycle contract for language-specific streaming sub-pipelines. */
internal interface StreamingSubPipeline {
    fun dispose() {}
}

/** Capability for sub-pipelines that expose a DrawEntity snapshot after rendering. */
internal interface DrawEntitySnapshotProvider : StreamingSubPipeline {
    fun drawEntitiesFor(snapshot: DiagramSnapshot): List<DrawEntity>
}

/** Capability for line-oriented sub-pipelines that consume already framed language input. */
internal interface LineStreamingSubPipeline<in L, out R> : StreamingSubPipeline {
    fun acceptLine(line: L): R
}

/** Capability for sub-pipelines whose parser advances on batches of complete logical lines. */
internal interface BatchLineStreamingSubPipeline<in L, out R> : StreamingSubPipeline {
    fun acceptLines(
        previousSnapshot: DiagramSnapshot,
        lines: List<L>,
        seq: Long,
        isFinal: Boolean,
    ): R
}

/** Capability for sub-pipelines that render their current parser state on demand. */
internal interface RenderStateProvider<out T> : StreamingSubPipeline {
    fun render(previousSnapshot: DiagramSnapshot, seq: Long, isFinal: Boolean): T
}

internal fun DrawEntitySnapshotProvider?.drawEntitiesOrEmpty(snapshot: DiagramSnapshot): List<DrawEntity> =
    this?.drawEntitiesFor(snapshot).orEmpty()
