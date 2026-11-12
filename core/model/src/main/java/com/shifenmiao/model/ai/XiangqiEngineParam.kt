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

package com.shifenmiao.model.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pikafish 走棋接口请求（go-proxy `POST /xiangqi/engine/bestmove`）。
 *
 * movetime_ms 与 depth 二选一；都为空时服务端用默认 movetime。
 * skill: 0-20 越高越强, -1 表示不限制。
 */
@Serializable
data class XiangqiEngineRequest(
    @SerialName("fen")
    val fen: String,
    /** 服务端引擎标识（如 "pikafish"），预留多引擎路由 */
    @SerialName("engine")
    val engine: String? = null,
    @SerialName("moves")
    val moves: String? = null,
    @SerialName("movetime_ms")
    val moveTimeMs: Int? = null,
    @SerialName("depth")
    val depth: Int? = null,
    @SerialName("skill")
    val skill: Int? = null,
)

@Serializable
data class XiangqiEngineResponse(
    @SerialName("bestmove")
    val bestmove: String = "",
    @SerialName("ponder")
    val ponder: String? = null,
    @SerialName("score_cp")
    val scoreCp: Int? = null,
    @SerialName("score_mate")
    val scoreMate: Int? = null,
    @SerialName("depth")
    val depth: Int? = null,
    @SerialName("pv")
    val pv: String? = null,
    @SerialName("time_ms")
    val timeMs: Long? = null,
)
