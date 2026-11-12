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

package com.wanbaohe.gomoku.domain.model

/** 15×15 棋盘坐标;file 0-14 对应列 A-O,rank 0-14 对应行 1-15 */
data class BoardPoint(
    val file: Int,
    val rank: Int,
) {
    val index: Int get() = rank * FILE_COUNT + file

    fun isInside(): Boolean = file in 0 until FILE_COUNT && rank in 0 until RANK_COUNT

    fun offset(fileDelta: Int, rankDelta: Int): BoardPoint =
        BoardPoint(file + fileDelta, rank + rankDelta)

    /** 坐标记谱,如 "H8"(列 A-O + 行 1-15) */
    fun toCoordinate(): String = "${('A' + file)}${rank + 1}"

    companion object {
        const val FILE_COUNT = 15
        const val RANK_COUNT = 15

        fun fromCoordinate(text: String): BoardPoint? {
            if (text.length < 2) return null
            val file = text[0].uppercaseChar() - 'A'
            val rank = text.substring(1).toIntOrNull()?.minus(1) ?: return null
            return BoardPoint(file, rank).takeIf { it.isInside() }
        }
    }
}
