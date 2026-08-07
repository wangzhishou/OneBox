package com.wanbaohe.minesweeper.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.Difficulty
import com.wanbaohe.minesweeper.logic.GameState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class MinesweeperComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(MinesweeperUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        resetGame(Difficulty.EASY)
        componentContext.lifecycle.doOnDestroy {
            timerJob?.cancel()
        }
    }

    fun resetGame(difficulty: Difficulty = _uiState.value.difficulty) {
        timerJob?.cancel()
        timerJob = null
        val board = List(difficulty.rows) { r ->
            List(difficulty.cols) { c ->
                Cell(row = r, col = c)
            }
        }
        _uiState.update {
            it.copy(
                board = board,
                gameState = GameState.INITIAL,
                difficulty = difficulty,
                timer = 0,
                minesLeft = difficulty.mines
            )
        }
    }

    private fun placeMinesAndCalculateNeighbors(firstClickR: Int, firstClickC: Int) {
        val diff = _uiState.value.difficulty
        val rows = diff.rows
        val cols = diff.cols
        val mines = diff.mines

        var board = _uiState.value.board.map { it.toMutableList() }.toMutableList()
        var minesPlaced = 0

        while (minesPlaced < mines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            // Ensure first click and its immediate neighbors are safe
            if (Math.abs(r - firstClickR) <= 1 && Math.abs(c - firstClickC) <= 1) continue
            if (!board[r][c].isMine) {
                board[r][c] = board[r][c].copy(isMine = true)
                minesPlaced++
            }
        }

        // Calculate neighbors
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r][c].isMine) continue
                var count = 0
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols && board[nr][nc].isMine) {
                            count++
                        }
                    }
                }
                board[r][c] = board[r][c].copy(neighborMines = count)
            }
        }

        _uiState.update { it.copy(board = board) }
    }

    fun onCellClicked(r: Int, c: Int) {
        val state = _uiState.value
        if (state.gameState == GameState.WON || state.gameState == GameState.LOST) return
        
        var board = state.board.map { it.toMutableList() }.toMutableList()
        val cell = board[r][c]
        
        if (cell.isFlagged || cell.isRevealed) return

        if (state.gameState == GameState.INITIAL) {
            placeMinesAndCalculateNeighbors(r, c)
            _uiState.update { it.copy(gameState = GameState.PLAYING) }
            startTimer()
            // reload board since it changed
            board = _uiState.value.board.map { it.toMutableList() }.toMutableList()
        }

        if (board[r][c].isMine) {
            revealAllMines(board)
            _uiState.update { it.copy(board = board, gameState = GameState.LOST) }
            timerJob?.cancel()
            return
        }

        revealEmptyCells(board, r, c)
        
        val isWon = checkWin(board)
        val nextState = if (isWon) GameState.WON else GameState.PLAYING
        if (isWon) timerJob?.cancel()
        
        _uiState.update { it.copy(board = board, gameState = nextState) }
    }

    fun onCellLongClicked(r: Int, c: Int) {
        val state = _uiState.value
        if (state.gameState == GameState.WON || state.gameState == GameState.LOST || state.gameState == GameState.INITIAL) return

        val board = state.board.map { it.toMutableList() }.toMutableList()
        val cell = board[r][c]

        if (cell.isRevealed) return

        board[r][c] = cell.copy(isFlagged = !cell.isFlagged)
        val minesLeft = state.minesLeft + if (cell.isFlagged) 1 else -1

        _uiState.update { it.copy(board = board, minesLeft = minesLeft) }
    }

    private fun revealEmptyCells(board: MutableList<MutableList<Cell>>, r: Int, c: Int) {
        if (r !in 0 until board.size || c !in 0 until board[0].size) return
        if (board[r][c].isRevealed || board[r][c].isFlagged || board[r][c].isMine) return

        board[r][c] = board[r][c].copy(isRevealed = true)

        if (board[r][c].neighborMines == 0) {
            for (dr in -1..1) {
                for (dc in -1..1) {
                    if (dr != 0 || dc != 0) revealEmptyCells(board, r + dr, c + dc)
                }
            }
        }
    }

    private fun revealAllMines(board: MutableList<MutableList<Cell>>) {
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c].isMine) {
                    board[r][c] = board[r][c].copy(isRevealed = true)
                }
            }
        }
    }

    private fun checkWin(board: List<List<Cell>>): Boolean {
        for (r in board.indices) {
            for (c in board[r].indices) {
                val cell = board[r][c]
                if (!cell.isMine && !cell.isRevealed) return false
            }
        }
        return true
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = componentScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(timer = it.timer + 1) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): MinesweeperComponent
    }
}
