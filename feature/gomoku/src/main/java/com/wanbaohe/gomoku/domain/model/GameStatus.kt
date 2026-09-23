package com.wanbaohe.gomoku.domain.model

/** 对局状态;五子棋无将军概念 */
enum class GameStatus {
    NOT_STARTED,
    PAUSED,
    PLAYING,
    BLACK_WINS,
    WHITE_WINS,
    DRAW,
    RESIGNED,
}
