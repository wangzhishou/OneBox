package com.hrm.markdown.renderer.internal.layout.inline

import androidx.compose.ui.text.TextStyle

internal class InlineFlowLayoutCache(
    private val maxEntries: Int = DefaultMaxEntries,
    private val maxEstimatedBytes: Long = DefaultMaxEstimatedBytes,
) {
    private val cache = WeightedLruCache<InlineFlowLayoutCacheKey, InlineFlowLayout>(
        maxEntries = maxEntries,
        maxEstimatedBytes = maxEstimatedBytes,
        estimateValueBytes = ::estimateInlineFlowLayoutBytes,
    )

    fun getOrPut(
        epoch: InlineLayoutEpoch,
        stableId: Long,
        layoutRevision: Long,
        widthPx: Float,
        maxLines: Int,
        style: TextStyle,
        compute: () -> InlineFlowLayout,
    ): InlineFlowLayout {
        val key = InlineFlowLayoutCacheKey(
            epoch = epoch,
            stableId = stableId,
            layoutRevision = layoutRevision,
            widthBits = widthPx.toBits(),
            maxLines = maxLines,
            styleIdentity = StructuralIdentity.of(style),
        )
        return cache.getOrPut(key, compute)
    }

    fun clear() {
        cache.clear()
    }

    fun metricsSnapshot(): LruCacheMetricsSnapshot = cache.snapshot()

    fun resetStatistics() = cache.resetStatistics()

    private data class InlineFlowLayoutCacheKey(
        val epoch: InlineLayoutEpoch,
        val stableId: Long,
        val layoutRevision: Long,
        val widthBits: Int,
        val maxLines: Int,
        val styleIdentity: StructuralIdentity,
    )

    private companion object {
        const val DefaultMaxEntries = 2048
        const val DefaultMaxEstimatedBytes = 32L * 1024L * 1024L
    }
}

private fun estimateInlineFlowLayoutBytes(layout: InlineFlowLayout): Long {
    var bytes = 64L
    for (line in layout.lines) {
        bytes += 96L
        for (item in line.items) {
            bytes += when (item) {
                is LineItem.TextItem -> 64L + item.text.length * 2L
                is LineItem.InlineItem -> 64L + item.alternateText.length * 2L
            }
        }
    }
    return bytes
}
