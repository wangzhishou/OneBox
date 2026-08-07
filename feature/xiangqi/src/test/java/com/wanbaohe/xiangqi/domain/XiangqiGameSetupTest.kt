package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameSetup
import com.wanbaohe.xiangqi.domain.model.PlayerSeat
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class XiangqiGameSetupTest {

    @Test
    fun localGameAssignsOneHumanToEachSide() {
        val setup = GameSetup.local()

        assertEquals(GameMode.LOCAL_PVP, setup.mode)
        assertEquals(PlayerType.HUMAN, setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.HUMAN, setup.playerTypeFor(Side.BLACK))
        assertEquals(listOf(Side.RED, Side.BLACK), setup.seats.map { it.side })
    }

    @Test
    fun humanVsAiWithAiRedAssignsHumanBlack() {
        val setup = GameSetup.humanVsAi(aiSide = Side.RED)

        assertEquals(GameMode.HUMAN_VS_LLM, setup.mode)
        assertEquals(PlayerType.LLM, setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.HUMAN, setup.playerTypeFor(Side.BLACK))
    }

    @Test
    fun humanVsAiWithAiBlackAssignsHumanRed() {
        val setup = GameSetup.humanVsAi(aiSide = Side.BLACK)

        assertEquals(GameMode.HUMAN_VS_LLM, setup.mode)
        assertEquals(PlayerType.HUMAN, setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.LLM, setup.playerTypeFor(Side.BLACK))
    }

    @Test
    fun aiVsAiAssignsAiToBothUniqueSides() {
        val setup = GameSetup.aiVsAi()

        assertEquals(GameMode.LLM_VS_LLM, setup.mode)
        assertEquals(PlayerType.LLM, setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.LLM, setup.playerTypeFor(Side.BLACK))
        assertEquals(setOf(Side.RED, Side.BLACK), setup.seats.map { it.side }.toSet())
    }

    @Test
    fun onlineGameAssignsHumanAndRemoteToOppositeSides() {
        val setup = GameSetup.online(mySide = Side.BLACK)

        assertEquals(GameMode.ONLINE_PVP, setup.mode)
        assertEquals(PlayerType.REMOTE, setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.HUMAN, setup.playerTypeFor(Side.BLACK))
    }

    @Test
    fun duplicatedSidesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GameSetup(
                mode = GameMode.HUMAN_VS_LLM,
                seats = listOf(
                    PlayerSeat(Side.RED, PlayerType.HUMAN),
                    PlayerSeat(Side.RED, PlayerType.LLM),
                ),
            )
        }
    }
}
