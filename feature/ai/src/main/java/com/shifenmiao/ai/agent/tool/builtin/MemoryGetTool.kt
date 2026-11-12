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
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.memory.MemoryRepository
import com.shifenmiao.database.ai.MemoryLimits
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

/**
 * 内置工具：检索长期记忆条目。
 *
 * 关键词搜索（全部关键词命中即返回整条，时间倒序）/ 无关键词 dump；
 * 上限由 [MemoryRepository] 控制（搜索 60 条 / dump 500 条 / 30KB UTF-8），截断附提示。
 * 隐式系统工具（visibleToUser = false）。
 */
class MemoryGetTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val memoryRepository: MemoryRepository,
) : AgentTool {

    companion object {
        const val TOOL_NAME = "memory_get"
    }

    override val name: String = TOOL_NAME

    override val description: String = textProvider.string(R.string.agent_tool_memory_get_description)

    override val category: ToolCategory = ToolCategory.SYSTEM

    /** 隐式系统工具，不在工具中心展示 */
    override val visibleToUser: Boolean = false

    /** 记忆 dump 可能较长，默认 4096 会截断 */
    override val maxResultLength: Int = 16384

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "keywords" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_memory_get_param_keywords)
            ),
            "scope" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_memory_get_param_scope),
                enum = listOf("log", "all")
            )
        ),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = runCatching {
            AppJson.decodeFromString<MemoryGetParams>(arguments)
        }.getOrNull() ?: MemoryGetParams()
        val keywords = params.keywords
            ?.split(Regex("[,，\\s]+"))
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        val scope = if (params.scope.equals("all", ignoreCase = true)) {
            MemoryRepository.SearchScope.ALL
        } else {
            MemoryRepository.SearchScope.LOG
        }

        val result = memoryRepository.search(keywords = keywords, scope = scope)
        if (result.entries.isEmpty()) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_memory_get_result_empty)
            )
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val content = buildString {
            result.entries.forEach { entry ->
                appendLine(
                    textProvider.string(
                        R.string.agent_tool_memory_get_result_entry,
                        entry.id,
                        entry.kind,
                        dateFormat.format(Date(entry.createdAt))
                    )
                )
                appendLine(entry.content.trim())
                appendLine()
            }
            if (result.omittedByCount > 0) {
                appendLine(
                    textProvider.string(
                        R.string.agent_tool_memory_get_result_truncated_count,
                        MemoryLimits.SEARCH_RESULT_LIMIT
                    )
                )
            }
            if (result.truncatedEntries > 0) {
                appendLine(
                    textProvider.string(
                        R.string.agent_tool_memory_get_result_truncated_entries,
                        result.truncatedEntries
                    )
                )
            }
            if (result.omittedByBytes > 0) {
                appendLine(
                    textProvider.string(
                        R.string.agent_tool_memory_get_result_truncated_bytes,
                        result.omittedByBytes
                    )
                )
            }
        }
        return AgentToolResult(content = content.trim())
    }

    @Serializable
    private data class MemoryGetParams(
        val keywords: String? = null,
        val scope: String? = null
    )
}
