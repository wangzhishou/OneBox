package com.wanbaohe.survive30s.component

import androidx.compose.runtime.Immutable
import com.wanbaohe.survive30s.engine.Obstacle
import com.wanbaohe.survive30s.engine.Player

/**
 * 躲避30秒游戏 UI 状态
 *
 * @param gameState     当前游戏阶段
 * @param player        玩家位置和大小
 * @param obstacles     当前帧的障碍物列表
 * @param elapsedSec    已经过时间（秒）
 * @param bestTime      历史最佳存活时间（秒）
 * @param canvasWidth   画布宽度（px），首次布局后设定
 * @param canvasHeight  画布高度（px），首次布局后设定
 */
@Immutable
data class Survive30sUiState(
    val gameState: GameState = GameState.IDLE,
    val player: Player = Player(0f, 0f, 0f),
    val obstacles: List<Obstacle> = emptyList(),
    val elapsedSec: Float = 0f,
    val bestTime: Float = 0f,
    val shieldCount: Int = 0,
    val nearMissCount: Int = 0,
    val nearMissCharge: Int = 0,
    val dangerLevel: Float = 0f,
    val invincibleSec: Float = 0f,
    val phase: SurvivalPhase = SurvivalPhase.Warmup,
    val canvasWidth: Float = 0f,
    val canvasHeight: Float = 0f,
)

enum class SurvivalPhase {
    Warmup,
    Rush,
    Storm,
}

/** 游戏阶段 */
enum class GameState {
    /** 等待开始 */
    IDLE,
    /** 游戏进行中 */
    PLAYING,
    /** 被撞，游戏失败 */
    GAME_OVER,
    /** 成功存活30秒 */
    WIN,
}
