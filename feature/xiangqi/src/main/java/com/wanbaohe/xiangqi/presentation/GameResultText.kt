package com.wanbaohe.xiangqi.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.domain.GameResultCode
import com.wanbaohe.xiangqi.domain.GameResultResolver
import com.wanbaohe.xiangqi.domain.model.GameStatus

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
