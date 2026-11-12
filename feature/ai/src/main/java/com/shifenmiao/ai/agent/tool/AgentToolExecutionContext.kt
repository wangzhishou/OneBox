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

package com.shifenmiao.ai.agent.tool

import com.shifenmiao.ai.agent.callback.ToolCallback

/**
 * 工具执行上下文。
 *
 * 通过执行链路向需要“感知当前会话边界”的工具传递只读信息，
 * 例如 toolCallId、interactionOwnerId 和 conversationId。
 */
data class AgentToolExecutionContext(
    val toolCallId: String? = null,
    val interactionOwnerId: String? = null,
    /** 当前会话 id（conversation.id），memory_write 等工具记录来源会话用；草稿态/恢复链路外为 null */
    val conversationId: String? = null
)

/**
 * 上下文感知型工具接口。
 *
 * 普通工具无需实现，只有需要读取执行上下文的工具才实现。
 * 典型场景：工具目录查询、策略解释、会话级安全边界回显。
 */
interface ContextAwareAgentTool {
    suspend fun execute(arguments: String, context: AgentToolExecutionContext): AgentToolResult
}

/**
 * 同时需要“执行上下文”与“callback 能力”的工具接口。
 *
 * 这样 callback 子工具链路也能和主链路共用同一套上下文信息：
 * - toolCallId
 * - interactionOwnerId
 */
interface ContextAwareCallbackAgentTool {
    suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext,
        callback: ToolCallback
    ): AgentToolResult
}
