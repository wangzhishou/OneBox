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

package com.wanbaohe.xiangqi.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.domain.GameResultCode
import com.wanbaohe.xiangqi.domain.GameResultResolver
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * 终局结果的本地化文案。
 *
 * 唯一消费入口：历史卡片与导出文本都走这里，避免各自 when 一遍结果词表
 * （历史上 `resultText` 曾有三套方言，任何消费方漏分支就会显示原始英文枚举）。
 *
 * 返回空串表示**尚未终局**，调用方应自行决定是否占位。
 */
@Composable
fun localizedGameResultText(status: GameStatus): String =
    localizedGameResultText(GameResultResolver.resultText(status))

/** 见 [localizedGameResultText] 的 [GameStatus] 重载；此处直接消费持久化的结果码。 */
@Composable
fun localizedGameResultText(resultCode: String): String = when (resultCode) {
    GameResultCode.RED -> stringResource(R.string.xiangqi_game_over_red)
    GameResultCode.BLACK -> stringResource(R.string.xiangqi_game_over_black)
    GameResultCode.DRAW -> stringResource(R.string.xiangqi_game_over_draw)
    GameResultCode.RESIGNED -> stringResource(R.string.xiangqi_resign_result)
    else -> ""
}

/**
 * 认输终局的完整文案：「红方认输」/「黑方认输」。
 *
 * @param winnerSide 落库胜方（`Side.name`）。认输方是胜方的对面；
 * 传空或非法值时退回通用「认输」。
 */
@Composable
fun localizedResignResultText(winnerSide: String): String {
    val resigningSideName = when (winnerSide.uppercase()) {
        Side.RED.name -> stringResource(R.string.xiangqi_side_black)
        Side.BLACK.name -> stringResource(R.string.xiangqi_side_red)
        else -> ""
    }
    return if (resigningSideName.isEmpty()) {
        stringResource(R.string.xiangqi_resign_result)
    } else {
        stringResource(R.string.xiangqi_resign_by_side, resigningSideName)
    }
}

/** 认输方；无法判定时返回 null。 */
fun resigningSideOf(winnerSide: String): Side? = when (winnerSide.uppercase()) {
    Side.RED.name -> Side.BLACK
    Side.BLACK.name -> Side.RED
    else -> null
}
