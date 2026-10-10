package com.wanbaohe.xiangqi.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GameOrigin(
    val gameId: String,
    val ply: Int,
    val fen: String,
    val title: String,
) {
    init {
        require(gameId.isNotBlank())
        require(ply >= 0)
        require(fen.isNotBlank())
    }
}
