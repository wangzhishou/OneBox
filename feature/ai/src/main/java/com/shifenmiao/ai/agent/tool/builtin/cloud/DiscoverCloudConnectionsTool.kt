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

package com.shifenmiao.ai.agent.tool.builtin.cloud

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.CloudAgentToolTextProvider
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.ai.R
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.service.CloudFileService
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * 浏览已保存的远程存储连接 —— LLM 用来发现可用的连接。
 *
 * read-only / SAFE。
 */
class DiscoverCloudConnectionsTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
) : AgentTool {

    override val name: String = "discover_cloud_connections"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_discover_cloud_connections)
    override val title: String = textProvider.string(R.string.agent_tool_discover_cloud_connections_title)
    override val summary: String = textProvider.string(R.string.agent_tool_discover_cloud_connections_summary)
    override val category: ToolCategory = ToolCategory.KNOWLEDGE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_discover_cloud_connections_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_discover_cloud_connections_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "name_filter" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_discover_cloud_connections_param_name_filter),
            ),
            "protocol" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_discover_cloud_connections_param_protocol),
                enum = listOf("S3_COMPAT", "WEB_DAV", "SMB"),
            ),
        ),
        required = emptyList(),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) DiscoverParams() else AppJson.decodeFromString<DiscoverParams>(arguments)
        val all = connectionHolder.current()
        val connections = run {
            val filtered = params.protocol?.let { proto -> all.filter { it.protocol.name == proto } } ?: all
            params.name_filter?.takeIf { it.isNotBlank() }
                ?.let { f -> filtered.filter { it.displayName.contains(f, ignoreCase = true) } }
                ?: filtered
        }
        val payload = DiscoverResult(
            totalCount = connections.size,
            connections = connections.map { c ->
                DiscoverConnectionPayload(
                    id = c.id,
                    displayName = c.displayName,
                    protocol = c.protocol.name,
                    defaultRoot = cloudFileService.defaultRootName(c),
                    isDefault = c.isDefault,
                )
            },
        )
        return AgentToolResult(content = AppJson.encodeToString(payload))
    }

    @Serializable
    private data class DiscoverParams(
        val name_filter: String? = null,
        val protocol: String? = null,
    )

    @Serializable
    private data class DiscoverConnectionPayload(
        val id: String,
        val displayName: String,
        val protocol: String,
        val defaultRoot: String?,
        val isDefault: Boolean,
    )

    @Serializable
    private data class DiscoverResult(
        val totalCount: Int,
        val connections: List<DiscoverConnectionPayload>,
    )
}
