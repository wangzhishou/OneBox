package com.wanbaohe.markdown

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class OneBoxMarkdownPerformancePolicy(
    val embeddedViewportThresholdChars: Int = 6_000,
    val embeddedViewportMaxHeight: Dp = 560.dp,
) {
    fun useEmbeddedViewport(markdown: String): Boolean =
        markdown.length >= embeddedViewportThresholdChars

    companion object {
        val Default = OneBoxMarkdownPerformancePolicy()
    }
}
