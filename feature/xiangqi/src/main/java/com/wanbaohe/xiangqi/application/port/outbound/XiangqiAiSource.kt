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

    /**
     * 是否需要登录才能使用。
     * 服务端开源引擎(Pikafish 等)与本地引擎免登录；聊天 LLM / Jev 需要账号。
     */
    val requiresLogin: Boolean
        get() = this !is RemoteEngine && this !is LocalEngine

    /** 是否消耗积分（走我方代理的 LLM/Jev；开源引擎免费） */
    val requiresPoints: Boolean
        get() = requiresLogin

    /**
     * 开局积分门槛（仅校验余额，不在此扣减）。
     * 免费引擎为 0；付费模型预留一局的基本消耗，避免开完局每步都失败。
     */
    val startPoints: Int
        get() = if (requiresPoints) START_POINTS else 0

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

    /**
     * 端侧 Fairy-Stockfish（需先下载 NNUE 权重）。
     * 作为一等公民出现在 AI 选择器里，与云端引擎平级切换；浅层搜索仍只作最后兜底。
     */
    data object LocalEngine : XiangqiAiSource

    companion object {
        /** 付费模型开局最低积分 */
        const val START_POINTS = 20

        /** 未配置时的默认走棋 AI：Pikafish */
        val default: XiangqiAiSource = RemoteEngine(RemoteEngine.PIKAFISH)

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
                    add(LocalEngine)
                }
            }

        fun fromKey(key: String?): XiangqiAiSource {
            return when (key?.trim()?.lowercase()) {
                null, "" -> default
                "working_model", "workingmodel", "fast" -> WorkingModel
                "jev", "typesafe", "systemone" -> {
                    if (com.shifenmiao.model.channel.FlavorType.fromName().isOverseas) Jev
                    else default
                }
                "local_engine", "localengine", "offline", "local" -> LocalEngine
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
    XiangqiAiSource.LocalEngine -> "local_engine"
    is XiangqiAiSource.RemoteEngine -> engineId
}
