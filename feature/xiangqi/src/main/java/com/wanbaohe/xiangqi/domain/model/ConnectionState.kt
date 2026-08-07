package com.wanbaohe.xiangqi.domain.model

enum class ConnectionState {
    IDLE,
    CONNECTING,
    WAITING_FOR_OPPONENT,
    READY,
    PLAYING,
    OPPONENT_DISCONNECTED,
    ERROR,
}
