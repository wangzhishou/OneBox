package com.wanbaohe.boardgame.ui

/**
 * 棋盘外圈留白,保证最外圈的棋子不被裁掉。
 *
 * 交点式棋盘的棋子是**以交点为中心**画的,最外圈棋子有一半探进留白里;棋子的直径按格距算,
 * 而格距 = (棋盘宽 - 留白×2) / 格数 —— 留白一旦被上限钉死(五子棋 20dp、象棋 28dp),
 * 大棋盘(平板、横屏)上就先到顶,边路的棋子被 GlassSurface 的圆角切掉一牙。
 *
 * 两者互相牵制,所以迭代两轮收敛:先按基础留白估一次需要的半径,取较大值后再校一次。
 * 小屏上基础留白本来就够,结果与原来一致。
 *
 * @param basePaddingPx 基础留白(屏宽/屏高比例与 dp 上限三者取小)
 * @param fileGaps 横向格数(路数 - 1)
 * @param rankGaps 纵向格数(线数 - 1)
 * @param pieceRatio 棋子直径与格距的比例
 * @param marginPx 棋子边缘到外框的最小余量
 */
fun boardPaddingForPieces(
    basePaddingPx: Float,
    boardWidthPx: Float,
    boardHeightPx: Float,
    fileGaps: Float,
    rankGaps: Float,
    pieceRatio: Float,
    marginPx: Float = 1f,
): Float {
    var padding = basePaddingPx
    repeat(2) {
        val fileGap = (boardWidthPx - padding * 2f) / fileGaps
        val rankGap = (boardHeightPx - padding * 2f) / rankGaps
        val needed = minOf(fileGap, rankGap) * pieceRatio / 2f + marginPx
        padding = maxOf(padding, needed)
    }
    // 棋盘小到离谱时兜个底,别让留白把网格吃掉。
    return padding.coerceAtMost(minOf(boardWidthPx, boardHeightPx) / 6f)
}
