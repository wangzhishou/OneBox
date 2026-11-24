/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.theme.AppFontMode
import com.shifenmiao.theme.AppTheme

data class ItemGridSpec(
    val cells: StaggeredGridCells,
    val columnWidth: Dp,
)

@Composable
fun itemGridSpec(isGrid: Boolean, availableWidth: Dp): ItemGridSpec {
    val fontMode = AppTheme.uiProfile.fontMode
    val contentGap = AppTheme.dimens.contentGap
    val contentWidth = (availableWidth - AppTheme.dimens.pageHorizontal * 2)
        .coerceAtLeast(0.dp)

    return when {
        !isGrid -> adaptiveItemGridSpec(280.dp, contentWidth, contentGap)
        availableWidth < 360.dp -> fixedItemGridSpec(1, contentWidth, contentGap)
        fontMode == AppFontMode.ExtraLarge && availableWidth < 480.dp ->
            fixedItemGridSpec(1, contentWidth, contentGap)

        availableWidth < 600.dp -> fixedItemGridSpec(2, contentWidth, contentGap)
        fontMode == AppFontMode.ExtraLarge && availableWidth < 720.dp ->
            fixedItemGridSpec(2, contentWidth, contentGap)

        else -> adaptiveItemGridSpec(
            minSize = if (fontMode == AppFontMode.ExtraLarge) 280.dp else 240.dp,
            contentWidth = contentWidth,
            contentGap = contentGap,
        )
    }
}

@Composable
fun itemGridContentPadding() = PaddingValues(
    start = AppTheme.dimens.pageHorizontal,
    end = AppTheme.dimens.pageHorizontal,
    top = 4.dp,
    bottom = AppTheme.dimens.pageHorizontal,
)

private fun fixedItemGridSpec(
    columnCount: Int,
    contentWidth: Dp,
    contentGap: Dp,
) = ItemGridSpec(
    cells = StaggeredGridCells.Fixed(columnCount),
    columnWidth = itemGridColumnWidth(columnCount, contentWidth, contentGap),
)

private fun adaptiveItemGridSpec(
    minSize: Dp,
    contentWidth: Dp,
    contentGap: Dp,
): ItemGridSpec {
    val columnCount = ((contentWidth + contentGap) / (minSize + contentGap))
        .toInt()
        .coerceAtLeast(1)
    return ItemGridSpec(
        cells = StaggeredGridCells.Adaptive(minSize),
        columnWidth = itemGridColumnWidth(columnCount, contentWidth, contentGap),
    )
}

private fun itemGridColumnWidth(columnCount: Int, contentWidth: Dp, contentGap: Dp): Dp =
    ((contentWidth - contentGap * (columnCount - 1)) / columnCount).coerceAtLeast(0.dp)
