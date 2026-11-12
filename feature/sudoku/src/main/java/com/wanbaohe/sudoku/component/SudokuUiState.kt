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

package com.wanbaohe.sudoku.component

import com.wanbaohe.sudoku.data.SudokuSettings
import com.wanbaohe.sudoku.logic.SudokuDifficulty
import com.wanbaohe.sudoku.logic.SudokuRecord

/** 模块内底部 tab: 游戏 / 统计 / 设置。横切切换, 不进返回栈。 */
enum class SudokuTab { GAME, STATS, SETTINGS }

/**
 * 游戏 tab 内部的阶段: 难度选择(含继续游戏) → 对局中 → 完成结算。
 * tab 切换不影响阶段, 对局中切去统计再切回来棋盘原样还在。
 */
enum class SudokuGamePhase { HOME, PLAYING, COMPLETED }

data class SudokuUiState(
    val tab: SudokuTab = SudokuTab.GAME,
    val phase: SudokuGamePhase = SudokuGamePhase.HOME,
    val settings: SudokuSettings = SudokuSettings(),

    /** 题目(81 格, 0 = 空格); 给定数字不可改。 */
    val puzzle: List<Int> = emptyList(),
    /** 终盘解(81 格)。 */
    val solution: List<Int> = emptyList(),
    /** 当前盘面 = 给定 + 用户填写(81 格, 0 = 未填)。 */
    val entries: List<Int> = emptyList(),
    val difficulty: SudokuDifficulty = SudokuDifficulty.MEDIUM,
    /** 当前选中格 index, 未选中为 null。 */
    val selectedIndex: Int? = null,
    /** 撤销栈深度, 用来置灰撤销按钮(栈本体在 Component 里)。 */
    val undoDepth: Int = 0,

    /** 本局已用时(秒)。 */
    val elapsedSeconds: Int = 0,
    /** 计时当前是否在走(手动模式下由用户点用时卡控制)。 */
    val timerRunning: Boolean = false,

    /** 存在未完成对局快照 = 显示"继续游戏"卡片。 */
    val hasSavedGame: Boolean = false,
    /** 快照里用户已填格数, 继续游戏卡片的进度分子(/81)。 */
    val savedFilledCount: Int = 0,

    /** 战绩(完成 + 放弃都会记), 统计页的全部输入。 */
    val records: List<SudokuRecord> = emptyList(),
    /** 本局完成时该难度的历史最佳(含本局), 结算页"最佳纪录"卡。 */
    val completedBestSeconds: Int? = null
)
