package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Geometry only. Scroll ownership and selection are separate Markdown parameters. */
@Immutable
data class MarkdownLayout(
    val blockSpacing: Dp = 12.dp,
    val listIndent: Dp = 24.dp,
    val tableCellPadding: Dp = 8.dp,
    val blockQuoteBorderWidth: Dp = 4.dp,
    val blockQuotePadding: Dp = 12.dp,
    val dividerThickness: Dp = 1.dp,
    val blockCornerRadius: Dp = 8.dp,
    val blockPadding: Dp = 12.dp,
) {
    init {
        require(listOf(blockSpacing, listIndent, tableCellPadding, blockQuoteBorderWidth, blockQuotePadding,
            dividerThickness, blockCornerRadius, blockPadding).all { it.value.isFinite() && it >= 0.dp }) {
            "Markdown dimensions must be finite and non-negative"
        }
    }
}
