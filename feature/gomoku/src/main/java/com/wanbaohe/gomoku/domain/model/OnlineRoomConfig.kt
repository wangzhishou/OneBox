package com.wanbaohe.gomoku.domain.model

import com.wanbaohe.gomoku.domain.FenCodec

/**
 * Pure online-room rules shared by app use cases and IO adapters.
 * The server stores this object but the app remains the source of truth for FEN validation.
 * 五子棋黑先:房主默认执黑。
 */
data class OnlineRoomConfig(
    val initialFen: String = FenCodec.INITIAL_FEN,
    val hostSide: Side = Side.BLACK,
    val guestSide: Side = Side.WHITE,
    val allowUndo: Boolean = false,
) {
    init {
        FenCodec.parse(initialFen)
        require(hostSide != guestSide) { "Host and guest must sit on different sides" }
    }


    companion object {
        fun fromFen(fen: String): OnlineRoomConfig {
            val normalized = FenCodec.encode(FenCodec.parse(fen.ifBlank { FenCodec.INITIAL_FEN }))
            return OnlineRoomConfig(initialFen = normalized)
        }
    }
}
