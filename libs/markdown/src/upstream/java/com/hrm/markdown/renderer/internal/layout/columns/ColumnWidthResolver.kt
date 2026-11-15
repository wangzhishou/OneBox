package com.hrm.markdown.renderer.internal.layout.columns

private val PercentWidth = Regex("""^(\d+(?:\.\d+)?)%$""")
private val PixelWidth = Regex("""^(\d+(?:\.\d+)?)px$""", RegexOption.IGNORE_CASE)

internal sealed interface ColumnWidthSpec {
    data class Percent(val fraction: Float) : ColumnWidthSpec
    data class Pixels(val value: Float) : ColumnWidthSpec
    data object Flexible : ColumnWidthSpec
}

internal fun parseColumnWidth(value: String): ColumnWidthSpec {
    val normalized = value.trim()
    PercentWidth.matchEntire(normalized)?.groupValues?.get(1)?.toFloatOrNull()?.let { percent ->
        if (percent > 0f) return ColumnWidthSpec.Percent(percent / 100f)
    }
    PixelWidth.matchEntire(normalized)?.groupValues?.get(1)?.toFloatOrNull()?.let { pixels ->
        if (pixels > 0f) return ColumnWidthSpec.Pixels(pixels)
    }
    return ColumnWidthSpec.Flexible
}

/** Resolves the final physical width of every column from one shared policy. */
internal fun resolveColumnWidths(
    values: List<String>,
    totalWidthPx: Float,
    spacingPx: Float,
): List<Float> {
    if (values.isEmpty()) return emptyList()
    val usableWidth = (totalWidthPx - spacingPx * (values.size - 1)).coerceAtLeast(0f)
    val specs = values.map(::parseColumnWidth)
    val widths = FloatArray(values.size)
    var explicitTotal = 0f
    var flexibleCount = 0

    specs.forEachIndexed { index, spec ->
        val width = when (spec) {
            is ColumnWidthSpec.Percent -> usableWidth * spec.fraction
            is ColumnWidthSpec.Pixels -> spec.value
            ColumnWidthSpec.Flexible -> {
                flexibleCount++
                0f
            }
        }
        widths[index] = width
        explicitTotal += width
    }

    if (explicitTotal > usableWidth && explicitTotal > 0f) {
        val scale = usableWidth / explicitTotal
        for (index in widths.indices) widths[index] *= scale
        return widths.toList()
    }

    val remaining = (usableWidth - explicitTotal).coerceAtLeast(0f)
    if (flexibleCount > 0) {
        val flexibleWidth = remaining / flexibleCount
        specs.forEachIndexed { index, spec ->
            if (spec === ColumnWidthSpec.Flexible) widths[index] = flexibleWidth
        }
    }
    return widths.toList()
}
