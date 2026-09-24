package com.wanbaohe.chess.application.port.outbound

/**
 * 国际象棋走棋 AI 来源。与聊天模型分离:决策/引擎类不进通用模型选择面板。
 *
 * 扩展新的服务端引擎:
 * 1. 在 [RemoteEngine.presets] 增加 `RemoteEngine("engine-id")`;
 * 2. 服务端 `/chess/engine/bestmove` 按 `engine` 字段路由到对应二进制;
 * 3. 客户端 UCI 协议可继续走 [com.wanbaohe.chess.data.EngineMoveChooser]。
 */
sealed interface ChessAiSource {

    /**
     * 是否需要登录才能使用。
     * 服务端开源引擎(Stockfish 等)免登录;聊天 LLM 需要账号。
     */
    val requiresLogin: Boolean
        get() = this !is RemoteEngine

    /** 是否消耗积分(走我方代理的 LLM;开源引擎免费) */
    val requiresPoints: Boolean
        get() = requiresLogin

    /**
     * 开局积分门槛(仅校验余额,不在此扣减)。
     * 免费引擎为 0;付费模型预留一局的基本消耗,避免开完局每步都失败。
     */
    val startPoints: Int
        get() = if (requiresPoints) START_POINTS else 0

    /** 全局「快速工作模型」聊天 LLM(跟聊天共用 FAST/工作槽,不在此另存模型) */
    data object WorkingModel : ChessAiSource

    /**
     * 服务端国际象棋引擎。
     * @param engineId 服务端引擎标识(如 "stockfish"),透传给 bestmove 接口
     */
    data class RemoteEngine(val engineId: String) : ChessAiSource {
        companion object {
            const val STOCKFISH = "stockfish"

            /** 当前内置可选的远程国际象棋引擎;新驱动加在这里 */
            val presets: List<RemoteEngine> = listOf(RemoteEngine(STOCKFISH))
        }
    }

    companion object {
        /** 付费模型开局最低积分 */
        const val START_POINTS = 20

        /** 未配置时的默认走棋 AI:Stockfish(服务端免费引擎,免登录免积分) */
        val default: ChessAiSource = RemoteEngine(RemoteEngine.STOCKFISH)

        /** 设置页 / 对局内选择器展示的完整列表 */
        val presets: List<ChessAiSource>
            get() = buildList {
                add(WorkingModel)
                addAll(RemoteEngine.presets)
            }

        fun fromKey(key: String?): ChessAiSource {
            return when (key?.trim()?.lowercase()) {
                null, "" -> default
                "working_model", "workingmodel", "fast" -> WorkingModel
                else -> {
                    val id = key.trim().lowercase()
                    RemoteEngine.presets.firstOrNull { it.engineId == id }
                        ?: RemoteEngine(id)
                }
            }
        }
    }
}

fun ChessAiSource.storageKey(): String = when (this) {
    ChessAiSource.WorkingModel -> "working_model"
    is ChessAiSource.RemoteEngine -> engineId
}
