package com.wanbaohe.xiangqi.presentation

import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource

fun GameAiPlayerConfig.displayNames(): Pair<String, String> = when (val source = source) {
    XiangqiAiSource.WorkingModel ->
        engineTitle.ifBlank { engineName } to model?.let { it.title.ifBlank { it.name } }.orEmpty()
    XiangqiAiSource.Jev -> "Jev" to ""
    XiangqiAiSource.LocalEngine -> AppContext.getString(R.string.xiangqi_ai_source_local_engine) to ""
    is XiangqiAiSource.RemoteEngine ->
        (if (source.engineId == XiangqiAiSource.RemoteEngine.PIKAFISH) "Pikafish" else source.engineId) to ""
}
