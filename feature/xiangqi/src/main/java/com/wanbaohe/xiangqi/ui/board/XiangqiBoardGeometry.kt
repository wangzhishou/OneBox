package com.wanbaohe.xiangqi.ui.board

internal fun xiangqiBoardAspectRatio(width: Float, height: Float): Float =
    (width / height.coerceAtLeast(1f)).coerceIn(9f / 10f, 1.25f)

internal fun xiangqiPieceDiameter(horizontalGap: Float, verticalGap: Float): Float =
    minOf(horizontalGap, verticalGap) * 0.85f
