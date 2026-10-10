package com.wanbaohe.gomoku.domain.model

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.gomoku.domain.FenCodec
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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
        FenCodec.parse(fen)
    }

    fun encode(): String = AppJson.encodeToString(this)

    companion object {
        fun decode(json: String): GameOrigin? =
            runCatching { AppJson.decodeFromString<GameOrigin>(json) }.getOrNull()
    }
}
