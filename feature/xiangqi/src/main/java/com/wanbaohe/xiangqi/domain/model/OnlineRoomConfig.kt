package com.wanbaohe.xiangqi.domain.model

import com.wanbaohe.xiangqi.domain.FenCodec

/**
 * Pure online-room rules shared by app use cases and IO adapters.
 * The server stores this object but the app remains the source of truth for FEN validation.
 */
data class OnlineRoomConfig(
    val initialFen: String = FenCodec.INITIAL_FEN,
    val hostSide: Side = Side.RED,
    val guestSide: Side = Side.BLACK,
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

