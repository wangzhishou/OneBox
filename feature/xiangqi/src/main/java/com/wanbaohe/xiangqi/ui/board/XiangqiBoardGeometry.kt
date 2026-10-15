package com.wanbaohe.xiangqi.ui.board

import com.wanbaohe.boardgame.ui.boardPaddingForPieces

/** 棋子直径与格距的比例,直径取两个方向格距里小的那个。 */
private const val PIECE_TO_GAP_RATIO = 0.85f

/** 棋盘网格:9 路 × 10 线。 */
private const val FILE_GAP_COUNT = 8f
private const val RANK_GAP_COUNT = 9f

internal fun xiangqiBoardAspectRatio(width: Float, height: Float): Float =
    (width / height.coerceAtLeast(1f)).coerceIn(9f / 10f, 1.25f)

internal fun xiangqiPieceDiameter(horizontalGap: Float, verticalGap: Float): Float =
    minOf(horizontalGap, verticalGap) * PIECE_TO_GAP_RATIO

/**
 * 棋盘外圈留白(见 [boardPaddingForPieces])。
 * 平板、横屏上留白原本被 28dp 上限钉死,車/俥 这些边路棋子会被切掉一牙
 * (小米平板 5 Pro 上约 5dp)。
 */
internal fun xiangqiBoardPadding(
    basePaddingPx: Float,
    boardWidthPx: Float,
    boardHeightPx: Float,
    marginPx: Float = 1f,
): Float = boardPaddingForPieces(
    basePaddingPx = basePaddingPx,
    boardWidthPx = boardWidthPx,
    boardHeightPx = boardHeightPx,
    fileGaps = FILE_GAP_COUNT,
    rankGaps = RANK_GAP_COUNT,
    pieceRatio = PIECE_TO_GAP_RATIO,
    marginPx = marginPx,
)
