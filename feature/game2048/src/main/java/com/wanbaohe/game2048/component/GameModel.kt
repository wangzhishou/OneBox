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

package com.wanbaohe.game2048.component

import androidx.compose.runtime.Immutable

// ─── 滑动方向 ──────────────────────────────────────────────────────────────────

/** 四个滑动方向 */
enum class Direction { Up, Down, Left, Right }

// ─── UI 状态快照 ───────────────────────────────────────────────────────────────

/**
 * 2048 游戏页面的完整 UI 状态
 *
 * @param grid      4×4 棋盘，0 表示空格
 * @param score     当前得分
 * @param bestScore 历史最高分
 * @param isGameOver 游戏是否结束（无法移动）
 * @param isWon     是否已达成 2048
 * @param hasShownWinDialog 是否已弹过胜利弹窗（避免重复弹窗）
 */
@Immutable
data class Game2048UiState(
    val grid: List<List<Int>> = List(GRID_SIZE) { List(GRID_SIZE) { 0 } },
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val isWon: Boolean = false,
    val hasShownWinDialog: Boolean = false,
) {
    companion object {
        const val GRID_SIZE = 4
    }
}

