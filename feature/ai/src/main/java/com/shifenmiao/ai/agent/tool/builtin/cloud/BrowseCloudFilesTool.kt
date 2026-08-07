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
import com.wanbaohe.cloud.storage.model.CloudObjectItem
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.service.CloudFileService
import javax.inject.Inject

/**
 * 浏览远程存储目录 / 读取对象元信息。
 *
 * action: list | stat
 * read-only / SAFE。
 */
class BrowseCloudFilesTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "browse_cloud_files"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_browse_cloud_files)
    override val title: String = textProvider.string(R.string.agent_tool_browse_cloud_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_browse_cloud_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_browse_cloud_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_browse_cloud_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_cloud_files_param_action),
                enum = listOf("list", "stat", "list_roots"),
            ),
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_cloud_files_param_connection_id),
            ),
            "root" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_cloud_files_param_root),
            ),
            "path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browse_cloud_files_param_path),
            ),
        ),
        required = listOf("action", "connection_id"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) BrowseParams() else gson.fromJson(arguments, BrowseParams::class.java)
        val connection = resolveConnection(params.connection_id)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browse_cloud_files_connection_not_found, params.connection_id.orEmpty()),
                isError = true,
            )

        return when (params.action?.trim()) {
            "list_roots" -> {
                cloudFileService.listRoots(connection).fold(
                    onSuccess = { roots ->
                        AgentToolResult(content = gson.toJson(BrowseListRootsResult(roots.map { it.name })))
                    },
                    onFailure = { failure(it) },
                )
            }
            "list" -> {
                val root = params.root?.takeIf { it.isNotBlank() }
                    ?: cloudFileService.defaultRootName(connection)
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_browse_cloud_files_missing_root),
                        isError = true,
                    )
                val path = params.path.orEmpty()
                cloudFileService.listDirectory(connection, root, path).fold(
                    onSuccess = { items ->
                        AgentToolResult(
                            content = gson.toJson(
                                BrowseListResult(
                                    connectionId = connection.id,
                                    connectionName = connection.displayName,
                                    protocol = connection.protocol.name,
                                    root = root,
                                    path = path,
                                    count = items.size,
                                    items = items.map(::toPayload),
                                )
                            )
                        )
                    },
                    onFailure = { failure(it) },
                )
            }
            "stat" -> {
                val root = params.root?.takeIf { it.isNotBlank() }
                    ?: cloudFileService.defaultRootName(connection)
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_browse_cloud_files_missing_root),
                        isError = true,
                    )
                val path = params.path?.takeIf { it.isNotBlank() }
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_browse_cloud_files_missing_path),
                        isError = true,
                    )
                cloudFileService.stat(connection, root, path).fold(
                    onSuccess = { item ->
                        AgentToolResult(content = gson.toJson(BrowseStatResult(connection.id, connection.displayName, root, toPayload(item))))
                    },
                    onFailure = { failure(it) },
                )
            }
            else -> AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browse_cloud_files_invalid_action, params.action.orEmpty()),
                isError = true,
            )
        }
    }

    private fun failure(t: Throwable): AgentToolResult = AgentToolResult(
        content = textProvider.string(R.string.agent_tool_browse_cloud_files_failed, t.message.orEmpty()),
        isError = true,
    )

    private fun resolveConnection(connectionId: String?): CloudStorageConnection? {
        if (connectionId.isNullOrBlank()) return null
        return connectionHolder.current().firstOrNull { it.id == connectionId }
    }

    private fun toPayload(item: CloudObjectItem): ItemPayload = ItemPayload(
        key = item.key,
        displayName = item.displayName,
        isDirectory = item.isDirectory,
        size = item.size,
        lastModified = item.lastModified,
        contentType = item.contentType,
        eTag = item.eTag,
    )

    private data class BrowseParams(
        val action: String? = null,
        val connection_id: String? = null,
        val root: String? = null,
        val path: String? = null,
    )

    private data class ItemPayload(
        val key: String,
        val displayName: String,
        val isDirectory: Boolean,
        val size: Long,
        val lastModified: String?,
        val contentType: String?,
        val eTag: String?,
    )

    private data class BrowseListResult(
        val connectionId: String,
        val connectionName: String,
        val protocol: String,
        val root: String,
        val path: String,
        val count: Int,
        val items: List<ItemPayload>,
    )

    private data class BrowseListRootsResult(
        val roots: List<String>,
    )

    private data class BrowseStatResult(
        val connectionId: String,
        val connectionName: String,
        val root: String,
        val item: ItemPayload,
    )
}
