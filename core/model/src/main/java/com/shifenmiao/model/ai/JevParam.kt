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
import kotlinx.serialization.json.JsonObject

/**
 * TypeSafe System One (Jev) 判断接口请求。
 *
 * state 与 questions 由调用方按业务自由拼装, 因此直接以 JsonObject 承载。
 * model 必须以 "jev" 开头(go-proxy 侧校验)。
 */
@Serializable
data class JevRequest(
    @SerialName("state")
    val state: JsonObject,
    @SerialName("model")
    val model: String,
    @SerialName("questions")
    val questions: JsonObject,
)

@Serializable
data class JevResponse(
    @SerialName("model")
    val model: String = "",
    @SerialName("answers")
    val answers: Map<String, JevChoiceAnswer> = emptyMap(),
    @SerialName("usage")
    val usage: JsonObject? = null,
)

/**
 * choice 判断题答案。
 *
 * 注意: 服务端实测 choice 是按 probabilities 采样的结果而非 argmax,
 * 需要稳定选择时应自行对 probabilities 取概率最高的键。
 */
@Serializable
data class JevChoiceAnswer(
    @SerialName("type")
    val type: String = "",
    @SerialName("choice")
    val choice: String = "",
    @SerialName("confidence")
    val confidence: Double = 0.0,
    @SerialName("probabilities")
    val probabilities: Map<String, Double> = emptyMap(),
)
