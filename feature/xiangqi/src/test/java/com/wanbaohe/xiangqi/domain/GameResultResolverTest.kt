package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.GameStatus
import org.junit.Test
import kotlin.test.assertEquals

class GameResultResolverTest {
    @Test
    fun undoAndRedoKeepAPausedGamePaused() {
        for (evaluated in listOf(GameStatus.PLAYING, GameStatus.CHECK)) {
            assertEquals(
                GameStatus.PAUSED,
                GameResultResolver.statusAfterHistoryChange(GameStatus.PAUSED, evaluated),
            )
        }
    }

    @Test
    fun anActiveGameStillReflectsTheRecomputedPosition() {
        assertEquals(
            GameStatus.CHECK,
            GameResultResolver.statusAfterHistoryChange(GameStatus.PLAYING, GameStatus.CHECK),
        )
        assertEquals(
            GameStatus.PLAYING,
            GameResultResolver.statusAfterHistoryChange(GameStatus.CHECK, GameStatus.PLAYING),
        )
    }

    @Test
    fun redoingATerminalPositionStillRestoresItsResult() {
        assertEquals(
            GameStatus.RED_WINS,
            GameResultResolver.statusAfterHistoryChange(GameStatus.PAUSED, GameStatus.RED_WINS),
        )
    }

    @Test
    fun historyChangesNeverUndoAnExplicitResignation() {
        assertEquals(
            GameStatus.RESIGNED,
            GameResultResolver.statusAfterHistoryChange(GameStatus.RESIGNED, GameStatus.PLAYING),
        )
    }
}
