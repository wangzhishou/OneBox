package com.wanbaohe.chess.domain.model

enum class ConnectionState {
    IDLE,
    CONNECTING,
    WAITING_FOR_OPPONENT,
    READY,
    PLAYING,
    OPPONENT_DISCONNECTED,
    ERROR,
}
