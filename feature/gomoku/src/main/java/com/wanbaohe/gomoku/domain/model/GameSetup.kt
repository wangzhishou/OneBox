package com.wanbaohe.gomoku.domain.model

data class GameSetup(
    val mode: GameMode,
    val seats: List<PlayerSeat>,
) {
    init {
        val sides = seats.map { it.side }.toSet()
        require(sides == Side.entries.toSet()) { "Each side must have exactly one seat" }
        require(seats.size == Side.entries.size) { "Must not duplicate seats" }
        require(mode.isCompatibleWith(seats.map { it.playerType })) {
            "Game mode ${mode.name} is incompatible with seats"
        }
    }

    fun playerTypeFor(side: Side): PlayerType =
        seats.first { it.side == side }.playerType

    companion object {
        fun local(): GameSetup = GameSetup(
            mode = GameMode.LOCAL_PVP,
            seats = listOf(
                PlayerSeat(Side.BLACK, PlayerType.HUMAN),
                PlayerSeat(Side.WHITE, PlayerType.HUMAN),
            ),
        )

        /** 人机对战;五子棋黑先,默认人类执黑 */
        fun humanVsAi(aiSide: Side = Side.WHITE): GameSetup = GameSetup(
            mode = GameMode.HUMAN_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.BLACK, if (aiSide == Side.BLACK) PlayerType.LLM else PlayerType.HUMAN),
                PlayerSeat(Side.WHITE, if (aiSide == Side.WHITE) PlayerType.LLM else PlayerType.HUMAN),
            ),
        )

        fun aiVsAi(): GameSetup = GameSetup(
            mode = GameMode.LLM_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.BLACK, PlayerType.LLM),
                PlayerSeat(Side.WHITE, PlayerType.LLM),
            ),
        )

        /** 在线双人:我方 HUMAN 坐 [mySide],对方 REMOTE 坐对面 */
        fun online(mySide: Side): GameSetup = GameSetup(
            mode = GameMode.ONLINE_PVP,
            seats = listOf(
                PlayerSeat(Side.BLACK, if (mySide == Side.BLACK) PlayerType.HUMAN else PlayerType.REMOTE),
                PlayerSeat(Side.WHITE, if (mySide == Side.WHITE) PlayerType.HUMAN else PlayerType.REMOTE),
            ),
        )
    }
}

private fun GameMode.isCompatibleWith(playerTypes: List<PlayerType>): Boolean = when (this) {
    GameMode.LOCAL_PVP -> playerTypes.all { it == PlayerType.HUMAN }
    GameMode.HUMAN_VS_LLM -> playerTypes.count { it == PlayerType.HUMAN } == 1 &&
        playerTypes.count { it == PlayerType.LLM } == 1
    GameMode.LLM_VS_LLM -> playerTypes.all { it == PlayerType.LLM }
    GameMode.ONLINE_PVP -> playerTypes.count { it == PlayerType.HUMAN } == 1 &&
        playerTypes.count { it == PlayerType.REMOTE } == 1
}
