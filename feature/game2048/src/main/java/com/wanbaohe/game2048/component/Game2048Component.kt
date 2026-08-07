package com.wanbaohe.game2048.component

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.game2048.data.Game2048Storage
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 2048 游戏业务逻辑组件
 *
 * 职责：
 * 1. 维护 [Game2048UiState] 并通过 [uiState] 暴露给 Compose
 * 2. 处理滑动方向 → 调用 [GameEngine] → 更新状态
 * 3. 持久化最高分与棋盘快照到 MMKV
 * 4. 提供新游戏、继续游戏等交互方法
 */
class Game2048Component @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(loadOrNewGame())
    val uiState = _uiState.asStateFlow()

    // ─── 公开交互接口 ─────────────────────────────────────────────────────────

    /**
     * 向指定方向移动
     * - 无效移动（棋盘不变）不会触发状态更新
     * - 每次有效移动后自动持久化快照
     */
    fun move(direction: Direction) {
        val current = _uiState.value
        if (current.isGameOver) return

        val (newGrid, addedScore, moved) = GameEngine.move(current.grid, direction)
        if (!moved) return

        val newScore = current.score + addedScore
        val newBest = maxOf(newScore, current.bestScore)
        val gameOver = GameEngine.isGameOver(newGrid)
        val won = GameEngine.hasWon(newGrid)

        // 更新最高分
        if (newBest > current.bestScore) {
            Game2048Storage.saveBestScore(newBest)
        }
        // 持久化当前棋盘
        Game2048Storage.saveBoard(newGrid, newScore)

        _uiState.update {
            it.copy(
                grid = newGrid,
                score = newScore,
                bestScore = newBest,
                isGameOver = gameOver,
                isWon = won,
            )
        }
    }

    /** 开始新游戏：重置棋盘和分数 */
    fun newGame() {
        Game2048Storage.clearBoard()
        val board = GameEngine.newBoard()
        val bestScore = Game2048Storage.loadBestScore()
        Game2048Storage.saveBoard(board, 0)

        _uiState.value = Game2048UiState(
            grid = board,
            score = 0,
            bestScore = bestScore,
            isGameOver = false,
            isWon = false,
            hasShownWinDialog = false,
        )
    }

    /** 标记胜利弹窗已展示，继续游戏 */
    fun dismissWinDialog() {
        _uiState.update { it.copy(hasShownWinDialog = true) }
    }

    // ─── 私有方法 ──────────────────────────────────────────────────────────────

    /** 加载存档或创建新游戏 */
    private fun loadOrNewGame(): Game2048UiState {
        val bestScore = Game2048Storage.loadBestScore()
        val savedBoard = Game2048Storage.loadBoard()
        return if (savedBoard != null) {
            val savedScore = Game2048Storage.loadCurrentScore()
            Game2048UiState(
                grid = savedBoard,
                score = savedScore,
                bestScore = bestScore,
                isGameOver = GameEngine.isGameOver(savedBoard),
                isWon = GameEngine.hasWon(savedBoard),
                hasShownWinDialog = GameEngine.hasWon(savedBoard), // 恢复时不再弹窗
            )
        } else {
            val board = GameEngine.newBoard()
            Game2048Storage.saveBoard(board, 0)
            Game2048UiState(
                grid = board,
                score = 0,
                bestScore = bestScore,
            )
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit
        ): Game2048Component
    }
}

