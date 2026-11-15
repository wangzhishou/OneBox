package com.hrm.diagram.render.streaming

import com.hrm.diagram.render.cache.DrawEntity

/** Small cache seam for sub-pipelines that render entities through a specialized kernel. */
internal class DrawEntitySnapshotCache {
    private var entities: List<DrawEntity> = emptyList()

    fun store(next: List<DrawEntity>): List<DrawEntity> {
        entities = next
        return next
    }

    fun snapshot(): List<DrawEntity> = entities

    fun clear() {
        entities = emptyList()
    }
}
