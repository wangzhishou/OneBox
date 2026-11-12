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

package com.wanbaohe.chess.presentation

import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.port.outbound.ChessAiSource

fun GameAiPlayerConfig.displayNames(): Pair<String, String> {
    if (!isSupported) return AppContext.getString(R.string.chess_ai_opponent_unavailable) to ""
    return when (val source = source) {
        ChessAiSource.WorkingModel -> engineTitle.ifBlank { engineName } to
            model?.let { it.title.ifBlank { it.name } }.orEmpty()
        is ChessAiSource.RemoteEngine -> source.engineId.let {
            if (it == ChessAiSource.RemoteEngine.STOCKFISH) "Stockfish" else it
        } to ""
    }
}
