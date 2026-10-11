package com.wanbaohe.chess.presentation

import com.wanbaohe.boardgame.export.BoardReplayMove
import com.wanbaohe.boardgame.export.BoardReplaySnapshot
import com.wanbaohe.chess.component.ChessAnalysisUiState
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.Notation
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Side

data class ChessGifPosition(
    val boardState: BoardState,
    val lastMove: Pair<BoardPoint, BoardPoint>? = null,
)

fun ChessAnalysisUiState.toGifReplaySnapshot(
    title: String,
    resultText: String,
    whiteLabel: String,
    blackLabel: String,
): BoardReplaySnapshot<ChessGifPosition> {
    require(isLoaded) { "The replay has not loaded" }
    return BoardReplaySnapshot(
        title = title,
        initialPosition = ChessGifPosition(FenCodec.parse(initialFen)),
        currentPly = currentPly,
        resultText = resultText,
        moves = plies.sortedBy { it.ply }.map { record ->
            val lastMove = requireNotNull(Notation.parseMove(record.moveUcci)) {
                "Invalid move at ply ${record.ply}"
            }
            BoardReplayMove(
                ply = record.ply,
                beforePosition = ChessGifPosition(FenCodec.parse(record.beforeFen)),
                afterPosition = ChessGifPosition(FenCodec.parse(record.afterFen), lastMove),
                notation = record.moveCn.ifBlank { record.moveUcci },
                moveCode = record.moveUcci,
                moverLabel = if (record.moverSide == Side.WHITE) whiteLabel else blackLabel,
            )
        },
    )
}
