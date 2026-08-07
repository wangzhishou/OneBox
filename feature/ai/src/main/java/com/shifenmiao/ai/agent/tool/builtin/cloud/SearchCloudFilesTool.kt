package com.shifenmiao.ai.agent.tool.builtin.cloud

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.CloudAgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.ai.R
import com.wanbaohe.cloud.storage.data.protocol.ObjectStoragePathResolver
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.service.CloudFileService
import javax.inject.Inject

/**
 * 远端文件搜索 —— 当前实现是 `list + name contains` 的轻量策略。
 *
 * 后续可换成真实协议搜索（OSS `?prefix=` 列表 + 客户端过滤）。
 */
class SearchCloudFilesTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "search_cloud_files"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_search_cloud_files)
    override val title: String = textProvider.string(R.string.agent_tool_search_cloud_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_search_cloud_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_search_cloud_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_search_cloud_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_cloud_files_param_connection_id),
            ),
            "root" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_cloud_files_param_root),
            ),
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_cloud_files_param_query),
            ),
            "max_results" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_search_cloud_files_param_max_results),
            ),
        ),
        required = listOf("connection_id", "query"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) SearchParams() else gson.fromJson(arguments, SearchParams::class.java)
        val connection = resolveConnection(params.connection_id)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_search_cloud_files_connection_not_found, params.connection_id.orEmpty()),
                isError = true,
            )
        val root = params.root?.takeIf { it.isNotBlank() }
            ?: cloudFileService.defaultRootName(connection)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_search_cloud_files_missing_root),
                isError = true,
            )
        val query = params.query?.trim()?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_search_cloud_files_missing_query),
                isError = true,
            )
        val max = params.max_results ?: 50

        val searchPath = if (query.contains('/')) {
            ObjectStoragePathResolver.normalizePrefix(query)
        } else {
            ""
        }

        return cloudFileService.listDirectory(connection, root, searchPath).fold(
            onSuccess = { items ->
                val matched = items.asSequence()
                    .filter { it.displayName.contains(query, ignoreCase = true) }
                    .take(max)
                    .toList()
                AgentToolResult(
                    content = gson.toJson(
                        SearchResult(
                            connectionId = connection.id,
                            root = root,
                            query = query,
                            scannedCount = items.size,
                            matchCount = matched.size,
                            items = matched.map {
                                SearchItemPayload(
                                    key = it.key,
                                    displayName = it.displayName,
                                    isDirectory = it.isDirectory,
                                    size = it.size,
                                )
                            },
                        )
                    )
                )
            },
            onFailure = {
                AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_search_cloud_files_failed, it.message.orEmpty()),
                    isError = true,
                )
            },
        )
    }

    private fun resolveConnection(connectionId: String?): CloudStorageConnection? {
        if (connectionId.isNullOrBlank()) return null
        return connectionHolder.current().firstOrNull { it.id == connectionId }
    }

    private data class SearchParams(
        val connection_id: String? = null,
        val root: String? = null,
        val query: String? = null,
        val max_results: Int? = null,
    )

    private data class SearchItemPayload(
        val key: String,
        val displayName: String,
        val isDirectory: Boolean,
        val size: Long,
    )

    private data class SearchResult(
        val connectionId: String,
        val root: String,
        val query: String,
        val scannedCount: Int,
        val matchCount: Int,
        val items: List<SearchItemPayload>,
    )
}
