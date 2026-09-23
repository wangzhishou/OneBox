package com.wanbaohe.chess.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.chess.R
import com.wanbaohe.chess.domain.GameResultCode
import com.wanbaohe.chess.domain.GameResultResolver
import com.wanbaohe.chess.domain.model.GameStatus

/**
 * 终局结果的本地化文案。
 *
 * 唯一消费入口：历史卡片与导出文本都走这里，避免各自 when 一遍结果词表。
 *
 * 返回空串表示**尚未终局**，调用方应自行决定是否占位。
 */
@Composable
fun localizedGameResultText(status: GameStatus): String =
    localizedGameResultText(GameResultResolver.resultText(status))

/** 见 [localizedGameResultText] 的 [GameStatus] 重载；此处直接消费持久化的结果码。 */
@Composable
fun localizedGameResultText(resultCode: String): String = when (resultCode) {
    GameResultCode.BLACK -> stringResource(R.string.chess_game_over_black)
    GameResultCode.WHITE -> stringResource(R.string.chess_game_over_white)
    GameResultCode.DRAW -> stringResource(R.string.chess_game_over_draw)
    GameResultCode.RESIGNED -> stringResource(R.string.chess_resign_result)
    else -> ""
}
