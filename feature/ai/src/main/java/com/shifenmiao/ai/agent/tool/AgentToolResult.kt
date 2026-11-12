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

import com.shifenmiao.model.ai.AttachedMedia

/**
 * Agent 工具执行结果。
 *
 * @param content 工具返回的文本内容，将作为 tool role 消息回传给 LLM
 * @param isError 是否为错误结果；true 时 LLM 会知道工具执行失败
 * @param multiModalAttachments 多模态附件（如截图），会作为 image_url 与 [content] 一同回传给 LLM。
 *            仅在 LLM 支持多模态 tool_result 的协议下生效；纯文本协议会忽略附件仅保留 content。
 *            工具内应保证附件数量可控（建议 ≤ 3），避免单次 tool result 体积过大。
 */
data class AgentToolResult(
    val content: String,
    val isError: Boolean = false,
    val multiModalAttachments: List<AttachedMedia> = emptyList()
)

