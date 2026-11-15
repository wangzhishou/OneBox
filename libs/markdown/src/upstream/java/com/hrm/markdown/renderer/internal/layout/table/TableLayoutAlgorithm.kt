package com.hrm.markdown.renderer.internal.layout.table

import kotlin.math.floor
import kotlin.math.roundToInt

internal fun computeAutoTableColumnWidths(
    minContentWidths: List<Float>,
    maxContentWidths: List<Float>,
    availableWidth: Float?,
): List<Float> {
    val columnCount = maxOf(minContentWidths.size, maxContentWidths.size)
    if (columnCount == 0) return emptyList()

    val minWidths = List(columnCount) { index ->
        minContentWidths.getOrElse(index) { 0f }.coerceAtLeast(0f)
    }
    val maxWidths = List(columnCount) { index ->
        maxContentWidths.getOrElse(index) { minWidths[index] }
            .coerceAtLeast(minWidths[index])
    }
    val minTotal = minWidths.sum()
    val maxTotal = maxWidths.sum()
    val finiteAvailable = availableWidth?.takeIf { it.isFinite() && it > 0f }

    val targetWidth = when {
        finiteAvailable == null -> maxTotal
        finiteAvailable >= maxTotal -> finiteAvailable
        finiteAvailable >= minTotal -> finiteAvailable
        else -> minTotal
    }

    return when {
        targetWidth >= maxTotal -> {
            val extra = targetWidth - maxTotal
            if (extra <= 0f) {
                maxWidths
            } else {
                val extraPerColumn = extra / columnCount.toFloat()
                maxWidths.map { it + extraPerColumn }
            }
        }

        targetWidth <= minTotal -> minWidths

        else -> {
            val shrinkBudget = maxTotal - targetWidth
            val flexWidths = maxWidths.mapIndexed { index, width ->
                (width - minWidths[index]).coerceAtLeast(0f)
            }
            val totalFlex = flexWidths.sum()
            if (totalFlex <= 0f) {
                maxWidths
            } else {
                maxWidths.mapIndexed { index, width ->
                    width - shrinkBudget * (flexWidths[index] / totalFlex)
                }
            }
        }
    }
}

internal fun computeTableRowHeights(
    cellHeights: IntArray,
    columnCount: Int,
): IntArray {
    if (columnCount <= 0 || cellHeights.isEmpty()) return IntArray(0)

    val rowCount = (cellHeights.size + columnCount - 1) / columnCount
    return IntArray(rowCount) { rowIndex ->
        val fromIndex = rowIndex * columnCount
        val toIndex = minOf(fromIndex + columnCount, cellHeights.size)
        var rowHeight = 0
        for (index in fromIndex until toIndex) {
            rowHeight = maxOf(rowHeight, cellHeights[index])
        }
        rowHeight
    }
}

/** Rounds precomputed layout dimensions while preserving their rounded total. */
internal fun roundTableDimensionsPx(dimensions: List<Float>): IntArray {
    if (dimensions.isEmpty()) return IntArray(0)
    val normalized = dimensions.map { dimension ->
        dimension.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
    }
    val result = IntArray(normalized.size) { index -> floor(normalized[index]).toInt() }
    val target = normalized.sum().roundToInt().coerceAtLeast(0)
    val remainder = target - result.sum()
    if (remainder > 0) {
        val largestFractions = normalized.indices.sortedWith(
            compareByDescending<Int> { index -> normalized[index] - result[index] }
                .thenBy { it }
        )
        repeat(remainder) { offset ->
            result[largestFractions[offset % largestFractions.size]] += 1
        }
    }
    return result
}
