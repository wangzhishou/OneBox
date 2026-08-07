package com.t8rin.imagetoolbox.core.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * FlowRow 专用排列策略：SpaceBetween 风格的网格列对齐。
 *
 * 将 totalSize 按 SpaceBetween 公式均分为 [fullCount] 个列位，
 * 每行首个 item 贴左（x=0），满行末尾 item 贴右（x=totalSize-size）。
 * 不满行时，item 仍落在与满行相同的列位置上，实现跨行网格对齐。
 *
 * 原理：
 * - 用当前行 item 的平均宽度估算"标准 item 宽度"
 * - 按 SpaceBetween 公式计算 stride（item 宽度 + 间距）
 * - 第 i 个 item 的 x 坐标 = i × stride
 *
 * @param fullCount 每行满额 item 数量（通常对应 FlowRow 的 maxItemsInEachRow）
 */
class GridFlowRowArrangement(
    private val fullCount: Int
) : Arrangement.Horizontal {

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray
    ) {
        if (sizes.isEmpty()) return

        // 用当前行 item 平均宽度作为"标准 item 宽度"来计算 SpaceBetween 间距
        val avgSize = sizes.sum().toFloat() / sizes.size
        val gap = (totalSize - fullCount * avgSize) / (fullCount - 1)
        val stride = avgSize + gap  // 相邻 item 起始位置间距

        for (i in sizes.indices) {
            val col = if (layoutDirection == LayoutDirection.Ltr) i else fullCount - 1 - i
            outPositions[i] = (col * stride).toInt()
        }
    }
}
