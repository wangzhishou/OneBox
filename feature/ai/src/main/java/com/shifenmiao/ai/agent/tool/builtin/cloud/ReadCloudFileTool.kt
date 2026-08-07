package com.shifenmiao.ai.agent.tool.builtin.cloud

import android.util.Base64
import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.CloudAgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.ai.R
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.service.CloudFileService
import javax.inject.Inject

/**
 * 读取远端文件并以 base64 返回。
 *
 * 仅适合小文件（<= 256 KB），避免撑爆 LLM 上下文。
 */
class ReadCloudFileTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "read_cloud_file"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_read_cloud_file)
    override val title: String = textProvider.string(R.string.agent_tool_read_cloud_file_title)
    override val summary: String = textProvider.string(R.string.agent_tool_read_cloud_file_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_read_cloud_file_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_read_cloud_file_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_cloud_file_param_connection_id),
            ),
            "root" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_cloud_file_param_root),
            ),
            "path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_cloud_file_param_path),
            ),
        ),
        required = listOf("connection_id", "path"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) ReadParams() else gson.fromJson(arguments, ReadParams::class.java)
        val connection = resolveConnection(params.connection_id)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_read_cloud_file_connection_not_found, params.connection_id.orEmpty()),
                isError = true,
            )
        val root = params.root?.takeIf { it.isNotBlank() }
            ?: cloudFileService.defaultRootName(connection)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_read_cloud_file_missing_root),
                isError = true,
            )
        val path = params.path?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_read_cloud_file_missing_path),
                isError = true,
            )

        return cloudFileService.readBytes(connection, root, path).fold(
            onSuccess = { bytes ->
                if (bytes.size > MAX_PAYLOAD) {
                    return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_read_cloud_file_too_large, bytes.size, MAX_PAYLOAD),
                        isError = true,
                    )
                }
                AgentToolResult(
                    content = gson.toJson(
                        ReadOkResult(
                            connectionId = connection.id,
                            root = root,
                            path = path,
                            size = bytes.size,
                            contentType = detectContentType(path),
                            contentBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                        )
                    )
                )
            },
            onFailure = {
                AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_read_cloud_file_failed, it.message.orEmpty()),
                    isError = true,
                )
            },
        )
    }

    private fun detectContentType(path: String): String {
        val lower = path.lowercase()
        return when {
            lower.endsWith(".txt") -> "text/plain"
            lower.endsWith(".json") -> "application/json"
            lower.endsWith(".xml") -> "application/xml"
            lower.endsWith(".csv") -> "text/csv"
            lower.endsWith(".md") -> "text/markdown"
            lower.endsWith(".html") || lower.endsWith(".htm") -> "text/html"
            else -> "application/octet-stream"
        }
    }

    private fun resolveConnection(connectionId: String?): CloudStorageConnection? {
        if (connectionId.isNullOrBlank()) return null
        return connectionHolder.current().firstOrNull { it.id == connectionId }
    }

    private data class ReadParams(
        val connection_id: String? = null,
        val root: String? = null,
        val path: String? = null,
    )

    private data class ReadOkResult(
        val connectionId: String,
        val root: String,
        val path: String,
        val size: Int,
        val contentType: String,
        val contentBase64: String,
    )

    private companion object {
        private const val MAX_PAYLOAD = 256 * 1024 // 256 KB
    }
}
