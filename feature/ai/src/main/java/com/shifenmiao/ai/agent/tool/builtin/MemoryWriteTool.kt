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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.ai.memory.MemoryRepository
import com.shifenmiao.database.ai.MemoryLimits
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 内置工具：写入一条长期记忆（log 条目）。
 *
 * 纯隐式系统能力（visibleToUser = false）：不进工具中心、不参与 bootstrap，
 * 只受全局 + 会话两个记忆开关控制（由 PromptAssemblyService 强制并集进 tools 列表）。
 * 来源会话经 [AgentToolExecutionContext.conversationId] 透传（ContextAwareAgentTool）。
 */
class MemoryWriteTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val memoryRepository: MemoryRepository,
) : AgentTool, ContextAwareAgentTool {

    companion object {
        const val TOOL_NAME = "memory_write"
    }

    override val name: String = TOOL_NAME

    override val description: String = textProvider.string(R.string.agent_tool_memory_write_description)

    override val category: ToolCategory = ToolCategory.SYSTEM

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    /** DB 写操作，不参与并行批次 */
    override val parallelizable: Boolean = false

    /** 隐式系统工具，不在工具中心展示 */
    override val visibleToUser: Boolean = false

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "content" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_memory_write_param_content)
            )
        ),
        required = listOf("content")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return execute(arguments, AgentToolExecutionContext())
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext
    ): AgentToolResult {
        val params = runCatching {
            AppJson.decodeFromString<MemoryWriteParams>(arguments)
        }.getOrNull()
        val content = params?.content?.trim().orEmpty()
        if (content.isEmpty()) {
            return AgentToolResult(
                content = "Parameter 'content' is required and must not be blank",
                isError = true
            )
        }
        // 写入侧限长：与管理页手动新增统一上限，超限拒绝
        if (content.length > MemoryLimits.MAX_ENTRY_CHARS) {
            return AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_memory_write_too_long,
                    MemoryLimits.MAX_ENTRY_CHARS
                ),
                isError = true
            )
        }
        val id = memoryRepository.writeMemory(
            content = content,
            sourceConversationId = context.conversationId
        )
        return AgentToolResult(
            content = textProvider.string(R.string.agent_tool_memory_write_result_saved, id)
        )
    }

    @Serializable
    private data class MemoryWriteParams(val content: String? = null)
}
