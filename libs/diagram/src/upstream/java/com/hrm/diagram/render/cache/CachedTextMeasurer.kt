package com.hrm.diagram.render.cache

import com.hrm.diagram.core.draw.FontSpec
import com.hrm.diagram.core.text.TextMeasurer
import com.hrm.diagram.core.text.TextMetrics

/**
 * Session-scoped text measurement facade.
 *
 * Pipelines should receive this wrapper instead of calling a platform [TextMeasurer] directly, so
 * repeated layout/render advances reuse stable metrics by `(text, font, maxWidth)`.
 */
internal class CachedTextMeasurer(
    private val delegate: TextMeasurer,
    maxEntries: Int = MeasureCache.DEFAULT_MAX_ENTRIES,
) : TextMeasurer {
    private val cache = MeasureCache<TextMetrics>(maxEntries)

    val hits: Long get() = cache.hits
    val misses: Long get() = cache.misses
    val size: Int get() = cache.size

    override fun measure(text: String, font: FontSpec, maxWidth: Float?): TextMetrics =
        cache.getOrPut(MeasureKey(text, font, maxWidth)) {
            delegate.measure(it.text, it.font, it.maxWidth)
        }

    fun clear() {
        cache.clear()
    }
}

internal fun TextMeasurer.cached(): TextMeasurer =
    if (this is CachedTextMeasurer) this else CachedTextMeasurer(this)
