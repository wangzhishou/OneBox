package com.wanbaohe.xiangqi.application.port.outbound

/**
 * 象棋走棋 AI 来源。与聊天模型分离：决策/引擎类（Jev、UCI 引擎）不进通用模型选择面板。
 *
 * 扩展新的开源象棋驱动：
 * 1. 在 [RemoteEngine.presets] 增加 `RemoteEngine("engine-id")`；
 * 2. 服务端 `/xiangqi/engine/bestmove` 按 `engine` 字段路由到对应二进制；
 * 3. 客户端 UCI 协议可继续走 [com.wanbaohe.xiangqi.data.PikafishMoveChooser]。
 */
sealed interface XiangqiAiSource {

    /** 全局「快速工作模型」聊天 LLM（跟聊天共用 FAST/工作槽，不在此另存模型） */
    data object WorkingModel : XiangqiAiSource

    /** TypeSafe System One 判断模型 */
    data object Jev : XiangqiAiSource

    /**
     * 服务端 UCI/UCCI 象棋引擎。
     * @param engineId 服务端引擎标识（如 "pikafish"），透传给 bestmove 接口
     */
    data class RemoteEngine(val engineId: String) : XiangqiAiSource {
        companion object {
            const val PIKAFISH = "pikafish"

            /** 当前内置可选的远程象棋引擎；新驱动加在这里 */
            val presets: List<RemoteEngine> = listOf(RemoteEngine(PIKAFISH))
        }
    }

    companion object {
        /**
         * 设置页 / 对局内选择器展示的完整列表。
         * Jev(TypeSafe) 国内无备案，仅海外渠道可选。
         */
        val presets: List<XiangqiAiSource>
            get() {
                val overseas = com.shifenmiao.model.channel.FlavorType.fromName().isOverseas
                return buildList {
                    add(WorkingModel)
                    if (overseas) add(Jev)
                    addAll(RemoteEngine.presets)
                }
            }

        fun fromKey(key: String?): XiangqiAiSource {
            return when (key?.trim()?.lowercase()) {
                null, "", "working_model", "workingmodel", "fast" -> WorkingModel
                "jev", "typesafe", "systemone" -> {
                    if (com.shifenmiao.model.channel.FlavorType.fromName().isOverseas) Jev
                    else WorkingModel
                }
                else -> {
                    val id = key.trim().lowercase()
                    RemoteEngine.presets.firstOrNull { it.engineId == id }
                        ?: RemoteEngine(id)
                }
            }
        }
    }
}

fun XiangqiAiSource.storageKey(): String = when (this) {
    XiangqiAiSource.WorkingModel -> "working_model"
    XiangqiAiSource.Jev -> "jev"
    is XiangqiAiSource.RemoteEngine -> engineId
}
