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

package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.ChessMove

/** UCI 坐标记谱(如 "e2e4",升变如 "e7e8q");模块内唯一记谱,中西不分套 */
object UciNotation {

    fun format(move: ChessMove): String = format(move.from, move.to, move.promotion?.let { promotionChar(it) })

    fun format(from: BoardPoint, to: BoardPoint, promotion: String? = null): String =
        from.toCoordinate() + to.toCoordinate() + (promotion ?: "")

    private fun promotionChar(type: com.wanbaohe.chess.domain.model.PieceType): String = when (type) {
        com.wanbaohe.chess.domain.model.PieceType.QUEEN -> "q"
        com.wanbaohe.chess.domain.model.PieceType.ROOK -> "r"
        com.wanbaohe.chess.domain.model.PieceType.BISHOP -> "b"
        com.wanbaohe.chess.domain.model.PieceType.KNIGHT -> "n"
        else -> ""
    }
}

/** 解析/格式化入口(与五子棋 Notation 同形态) */
object Notation {

    fun parse(text: String): BoardPoint? = BoardPoint.fromCoordinate(text.trim())

    /** 解析 UCI 着法(如 "e2e4"/"e7e8q")为起止坐标 */
    fun parseMove(uci: String): Pair<BoardPoint, BoardPoint>? {
        val t = uci.trim()
        if (t.length < 4) return null
        val from = BoardPoint.fromCoordinate(t.substring(0, 2)) ?: return null
        val to = BoardPoint.fromCoordinate(t.substring(2, 4)) ?: return null
        return from to to
    }
}
