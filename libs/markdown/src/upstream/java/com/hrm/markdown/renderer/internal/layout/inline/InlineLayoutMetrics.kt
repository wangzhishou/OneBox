package com.hrm.markdown.renderer.internal.layout.inline

internal data class InlineLayoutMetricsSnapshot(
    val renderResultRequests: Long,
    val renderResultComputations: Long,
    val inlineMathBuildRequests: Long,
    val flowLayoutRequests: Long,
    val flowLayoutComputations: Long,
    val renderResultCache: LruCacheMetricsSnapshot,
    val flowLayoutCache: LruCacheMetricsSnapshot,
) {
    val renderResultCacheHits: Long get() = renderResultCache.hits
    val flowLayoutCacheHits: Long get() = flowLayoutCache.hits
}

internal class InlineLayoutMetrics {
    var renderResultRequests: Long = 0
    var renderResultComputations: Long = 0
    var inlineMathBuildRequests: Long = 0
    var flowLayoutRequests: Long = 0
    var flowLayoutComputations: Long = 0

    fun snapshot(
        renderResultCache: LruCacheMetricsSnapshot,
        flowLayoutCache: LruCacheMetricsSnapshot,
    ): InlineLayoutMetricsSnapshot = InlineLayoutMetricsSnapshot(
        renderResultRequests = renderResultRequests,
        renderResultComputations = renderResultComputations,
        inlineMathBuildRequests = inlineMathBuildRequests,
        flowLayoutRequests = flowLayoutRequests,
        flowLayoutComputations = flowLayoutComputations,
        renderResultCache = renderResultCache,
        flowLayoutCache = flowLayoutCache,
    )

    fun reset() {
        renderResultRequests = 0
        renderResultComputations = 0
        inlineMathBuildRequests = 0
        flowLayoutRequests = 0
        flowLayoutComputations = 0
    }
}
