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
