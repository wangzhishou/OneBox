package com.wanbaohe.xiangqi.presentation

import com.wanbaohe.boardgame.export.BoardReplayMove
import com.wanbaohe.boardgame.export.BoardReplaySnapshot
import com.wanbaohe.xiangqi.component.XiangqiAnalysisUiState
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.UcciNotation
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Side

data class XiangqiGifPosition(
    val boardState: BoardState,
    val lastMove: Pair<BoardPoint, BoardPoint>? = null,
)

fun XiangqiAnalysisUiState.toGifReplaySnapshot(
    title: String,
    resultText: String,
    redLabel: String,
    blackLabel: String,
): BoardReplaySnapshot<XiangqiGifPosition> {
    require(isLoaded) { "The replay has not loaded" }
    return BoardReplaySnapshot(
        title = title,
        initialPosition = XiangqiGifPosition(FenCodec.parse(initialFen)),
        currentPly = currentPly,
        resultText = resultText,
        moves = plies.sortedBy { it.ply }.map { record ->
            val lastMove = requireNotNull(UcciNotation.parseMove(record.moveUcci)) {
                "Invalid move at ply ${record.ply}"
            }
            BoardReplayMove(
                ply = record.ply,
                beforePosition = XiangqiGifPosition(FenCodec.parse(record.beforeFen)),
                afterPosition = XiangqiGifPosition(FenCodec.parse(record.afterFen), lastMove),
                notation = record.moveCn.ifBlank { record.moveUcci },
                moveCode = record.moveUcci,
                moverLabel = if (record.moverSide == Side.RED) redLabel else blackLabel,
            )
        },
    )
}
