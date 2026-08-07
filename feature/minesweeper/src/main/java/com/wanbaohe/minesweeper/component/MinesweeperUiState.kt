package com.wanbaohe.minesweeper.component

import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.Difficulty
import com.wanbaohe.minesweeper.logic.GameState

data class MinesweeperUiState(
    val board: List<List<Cell>> = emptyList(),
    val gameState: GameState = GameState.INITIAL,
    val difficulty: Difficulty = Difficulty.EASY,
    val timer: Int = 0,
    val minesLeft: Int = 0
)
