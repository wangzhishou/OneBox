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

package com.wanbaohe.sudoku.logic

/**
 * 一条战绩。完成/放弃都会记一条, 用 [completed] 区分。
 *
 * @param timestampMillis 对局结束(完成或放弃)时刻
 * @param timeSec 本局用时(秒), 放弃局是放弃时的已用时
 */
data class SudokuRecord(
    val timestampMillis: Long,
    val difficulty: SudokuDifficulty,
    val timeSec: Int,
    val completed: Boolean
)
