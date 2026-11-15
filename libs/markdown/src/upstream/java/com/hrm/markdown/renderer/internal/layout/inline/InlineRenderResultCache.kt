package com.hrm.markdown.renderer.internal.layout.inline

import androidx.compose.ui.text.TextStyle
import com.hrm.markdown.renderer.inline.InlineRenderResult

internal class InlineRenderResultCache(
    private val maxEntries: Int = DefaultMaxEntries,
    private val maxEstimatedBytes: Long = DefaultMaxEstimatedBytes,
) {
    private val cache = WeightedLruCache<InlineRenderResultCacheKey, InlineRenderResult>(
        maxEntries = maxEntries,
        maxEstimatedBytes = maxEstimatedBytes,
        estimateValueBytes = ::estimateInlineRenderResultBytes,
    )

    fun getOrPut(
        epoch: InlineLayoutEpoch,
        stableId: Long,
        contentRevision: Long,
        style: TextStyle,
        compute: () -> InlineRenderResult,
    ): InlineRenderResult {
        val key = InlineRenderResultCacheKey(
            epoch = epoch,
            stableId = stableId,
            contentRevision = contentRevision,
            styleIdentity = StructuralIdentity.of(style),
        )
        return cache.getOrPut(key, compute)
    }

    fun clear() {
        cache.clear()
    }

    fun metricsSnapshot(): LruCacheMetricsSnapshot = cache.snapshot()

    fun resetStatistics() = cache.resetStatistics()

    private data class InlineRenderResultCacheKey(
        val epoch: InlineLayoutEpoch,
        val stableId: Long,
        val contentRevision: Long,
        val styleIdentity: StructuralIdentity,
    )

    private companion object {
        const val DefaultMaxEntries = 2048
        const val DefaultMaxEstimatedBytes = 24L * 1024L * 1024L
    }
}

private fun estimateInlineRenderResultBytes(result: InlineRenderResult): Long {
    var bytes = 96L + result.annotated.length * 2L
    for ((_, payload) in result.paintPayloads) {
        bytes += 96L + payload.alternateText.length * 2L
    }
    for (segment in result.flowInput.segments) {
        bytes += when (segment) {
            is InlineFlowSegment.TextRun -> 64L + segment.annotated.length * 2L
            is InlineFlowSegment.InlineRun -> 64L + segment.placeholder.alternateText.length * 2L
            InlineFlowSegment.Newline -> 16L
        }
    }
    return bytes
}
