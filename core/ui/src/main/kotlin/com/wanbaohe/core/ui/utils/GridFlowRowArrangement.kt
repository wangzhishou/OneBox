/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.core.ui.utils

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
