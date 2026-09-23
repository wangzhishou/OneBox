package com.wanbaohe.chess.domain.model

/** 对局状态;国际象棋有将军(CHECK)概念 */
enum class GameStatus {
    NOT_STARTED,
    PAUSED,
    PLAYING,
    CHECK,
    WHITE_WINS,
    BLACK_WINS,
    DRAW,
    RESIGNED,
}
