package com.wanbaohe.gomoku.presentation

import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.gomoku.R
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource

fun GameAiPlayerConfig.displayNames(): Pair<String, String> {
    if (!isSupported) return AppContext.getString(R.string.gomoku_ai_opponent_unavailable) to ""
    return when (val source = source) {
        GomokuAiSource.WorkingModel ->
            engineTitle.ifBlank { engineName } to model?.let { it.title.ifBlank { it.name } }.orEmpty()
        is GomokuAiSource.RemoteEngine ->
            (if (source.engineId == GomokuAiSource.RemoteEngine.RAPFI) "Rapfi" else source.engineId) to ""
    }
}
