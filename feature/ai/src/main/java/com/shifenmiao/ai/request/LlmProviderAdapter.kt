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

package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import kotlinx.coroutines.flow.Flow

/**
 * LLM Provider Adapter.
 *
 * 将统一请求模型 [LlmTurnRequest] 转为具体协议/后端所需格式，
 * 并将底层响应统一转换为 [LlmStreamEvent]。
 *
 * 注册方式：在 Hilt Module 中通过 `@IntoSet` 注入 [LlmRequestGateway]，
 * Gateway 按 [protocol] 做 dispatch。
 *
 * 设计约束：
 * - 同一时间不应有两个 Adapter 声明相同 [protocol]；重复时 Gateway 记录告警并取首条。
 * - Adapter 自身不负责 Agent orchestration、消息持久化、UI 状态，
 *   这些继续由上层 consumer 负责。
 */
interface LlmProviderAdapter {
    val protocol: AiRequestProtocol

    fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest,
    ): Flow<LlmStreamEvent>
}
