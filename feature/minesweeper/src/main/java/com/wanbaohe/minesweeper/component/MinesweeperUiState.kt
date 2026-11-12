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

package com.wanbaohe.minesweeper.component

import com.wanbaohe.minesweeper.logic.Cell
import com.wanbaohe.minesweeper.logic.GameState

data class MinesweeperUiState(
    val board: List<List<Cell>> = emptyList(),
    val gameState: GameState = GameState.INITIAL,
    val timer: Int = 0,
    val minesLeft: Int = 0,
    /** true = 插旗模式(单击即插旗), false = 挖雷模式(单击即翻开) */
    val flagMode: Boolean = false,
    /** 音效与背景音乐总开关 */
    val soundEnabled: Boolean = true
)
