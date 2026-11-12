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

import kotlinx.coroutines.flow.Flow

/**
 * 象棋专用走棋 AI 配置（与聊天工作槽分离）。默认 Pikafish。
 */
data class XiangqiAiConfig(
    val fastSource: XiangqiAiSource = XiangqiAiSource.default,
    val duelASource: XiangqiAiSource = XiangqiAiSource.default,
    val duelBSource: XiangqiAiSource = XiangqiAiSource.default,
) {
    fun sourceFor(slot: EngineSlot): XiangqiAiSource = when (slot) {
        EngineSlot.FAST -> fastSource
        EngineSlot.DUEL_A -> duelASource
        EngineSlot.DUEL_B -> duelBSource
    }

    fun withSource(slot: EngineSlot, source: XiangqiAiSource): XiangqiAiConfig = when (slot) {
        EngineSlot.FAST -> copy(fastSource = source)
        EngineSlot.DUEL_A -> copy(duelASource = source)
        EngineSlot.DUEL_B -> copy(duelBSource = source)
    }

    /** 人机对弈只用 FAST 槽 */
    fun requiresLoginForHumanVsAi(): Boolean = fastSource.requiresLogin

    /** AI 对战两侧任一需要登录则要登录 */
    fun requiresLoginForAiVsAi(): Boolean =
        duelASource.requiresLogin || duelBSource.requiresLogin

    /** 开局积分门槛（取两侧较高者；免费引擎为 0） */
    fun startPointsForHumanVsAi(): Int = fastSource.startPoints

    fun startPointsForAiVsAi(): Int =
        maxOf(duelASource.startPoints, duelBSource.startPoints)
}

interface XiangqiAiStore {
    fun observe(): Flow<XiangqiAiConfig>
    suspend fun get(): XiangqiAiConfig
    suspend fun update(config: XiangqiAiConfig)
}
