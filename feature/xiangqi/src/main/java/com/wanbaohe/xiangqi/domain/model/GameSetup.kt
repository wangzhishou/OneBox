package com.wanbaohe.xiangqi.domain.model

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
                PlayerSeat(Side.RED, PlayerType.HUMAN),
                PlayerSeat(Side.BLACK, PlayerType.HUMAN),
            ),
        )

        fun humanVsAi(aiSide: Side): GameSetup = GameSetup(
            mode = GameMode.HUMAN_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.RED, if (aiSide == Side.RED) PlayerType.LLM else PlayerType.HUMAN),
                PlayerSeat(Side.BLACK, if (aiSide == Side.BLACK) PlayerType.LLM else PlayerType.HUMAN),
            ),
        )

        fun aiVsAi(): GameSetup = GameSetup(
            mode = GameMode.LLM_VS_LLM,
            seats = listOf(
                PlayerSeat(Side.RED, PlayerType.LLM),
                PlayerSeat(Side.BLACK, PlayerType.LLM),
            ),
        )

        fun online(mySide: Side): GameSetup = GameSetup(
            mode = GameMode.ONLINE_PVP,
            seats = listOf(
                PlayerSeat(Side.RED, if (mySide == Side.RED) PlayerType.HUMAN else PlayerType.REMOTE),
                PlayerSeat(Side.BLACK, if (mySide == Side.BLACK) PlayerType.HUMAN else PlayerType.REMOTE),
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
