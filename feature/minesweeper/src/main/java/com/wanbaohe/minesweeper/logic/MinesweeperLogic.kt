package com.wanbaohe.minesweeper.logic

data class Cell(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val neighborMines: Int = 0
)

enum class GameState {
    INITIAL, PLAYING, WON, LOST
}

enum class Difficulty(val rows: Int, val cols: Int, val mines: Int) {
    EASY(9, 9, 10),
    MEDIUM(16, 16, 40),
    HARD(16, 30, 99)
}
