/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
    /** 已暂停（退后台自动触发），进度保留，可继续 */
    PAUSED,
    /** 被撞，游戏失败 */
    GAME_OVER,
    /** 成功存活30秒 */
    WIN,
}
