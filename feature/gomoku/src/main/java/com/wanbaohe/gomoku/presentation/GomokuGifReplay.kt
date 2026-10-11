package com.wanbaohe.gomoku.presentation

import com.wanbaohe.boardgame.export.BoardReplayMove
import com.wanbaohe.boardgame.export.BoardReplaySnapshot
import com.wanbaohe.gomoku.component.GomokuAnalysisUiState
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.Notation
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side

data class GomokuGifPosition(
    val boardState: BoardState,
    val lastMove: BoardPoint? = null,
)

fun GomokuAnalysisUiState.toGifReplaySnapshot(
    title: String,
    resultText: String,
    blackLabel: String,
    whiteLabel: String,
): BoardReplaySnapshot<GomokuGifPosition> {
    require(isLoaded) { "The replay has not loaded" }
    return BoardReplaySnapshot(
        title = title,
        initialPosition = GomokuGifPosition(FenCodec.parse(initialFen)),
        currentPly = currentPly,
        resultText = resultText,
        moves = plies.sortedBy { it.ply }.map { record ->
            val lastMove = requireNotNull(Notation.parse(record.moveUcci)) {
                "Invalid move at ply ${record.ply}"
            }
            BoardReplayMove(
                ply = record.ply,
                beforePosition = GomokuGifPosition(FenCodec.parse(record.beforeFen)),
                afterPosition = GomokuGifPosition(FenCodec.parse(record.afterFen), lastMove),
                notation = record.moveCn.ifBlank { record.moveUcci },
                moveCode = record.moveUcci,
                moverLabel = if (record.moverSide == Side.BLACK) blackLabel else whiteLabel,
            )
        },
    )
}
