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
 * 把 base64 内容上传到远端。
 *
 * DANGEROUS + requiresConfirmation=true。
 */
class UploadToCloudTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "upload_to_cloud"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_upload_to_cloud)
    override val title: String = textProvider.string(R.string.agent_tool_upload_to_cloud_title)
    override val summary: String = textProvider.string(R.string.agent_tool_upload_to_cloud_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_upload_to_cloud_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_upload_to_cloud_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS
    override val requiresConfirmation: Boolean = true
    override val confirmationTitle: String = textProvider.string(R.string.agent_tool_upload_to_cloud_confirm_title)
    override val confirmationToolPresentation: String = textProvider.string(R.string.agent_tool_upload_to_cloud_confirm_presentation)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_upload_to_cloud_param_connection_id),
            ),
            "root" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_upload_to_cloud_param_root),
            ),
            "path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_upload_to_cloud_param_path),
            ),
            "content_base64" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_upload_to_cloud_param_content_base64),
            ),
            "content_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_upload_to_cloud_param_content_type),
            ),
        ),
        required = listOf("connection_id", "path", "content_base64"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) UploadParams() else gson.fromJson(arguments, UploadParams::class.java)
        val connection = resolveConnection(params.connection_id)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_upload_to_cloud_connection_not_found, params.connection_id.orEmpty()),
                isError = true,
            )
        val root = params.root?.takeIf { it.isNotBlank() }
            ?: cloudFileService.defaultRootName(connection)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_upload_to_cloud_missing_root),
                isError = true,
            )
        val path = params.path?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_upload_to_cloud_missing_path),
                isError = true,
            )
        val payload = params.content_base64
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_upload_to_cloud_missing_payload),
                isError = true,
            )
        val bytes = runCatching { Base64.decode(payload, Base64.DEFAULT) }
            .getOrElse {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_upload_to_cloud_invalid_base64, it.message.orEmpty()),
                    isError = true,
                )
            }
        if (bytes.size > MAX_PAYLOAD) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_upload_to_cloud_payload_too_large, bytes.size, MAX_PAYLOAD),
                isError = true,
            )
        }
        val contentType = params.content_type?.takeIf { it.isNotBlank() } ?: "application/octet-stream"
        return cloudFileService.upload(connection, root, path, bytes, contentType).fold(
            onSuccess = {
                AgentToolResult(
                    content = gson.toJson(
                        UploadOkResult(
                            connectionId = connection.id,
                            root = root,
                            path = path,
                            size = bytes.size,
                            contentType = contentType,
                        )
                    )
                )
            },
            onFailure = {
                AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_upload_to_cloud_failed, it.message.orEmpty()),
                    isError = true,
                )
            },
        )
    }

    private fun resolveConnection(connectionId: String?): CloudStorageConnection? {
        if (connectionId.isNullOrBlank()) return null
        return connectionHolder.current().firstOrNull { it.id == connectionId }
    }

    private data class UploadParams(
        val connection_id: String? = null,
        val root: String? = null,
        val path: String? = null,
        val content_base64: String? = null,
        val content_type: String? = null,
    )

    private data class UploadOkResult(
        val connectionId: String,
        val root: String,
        val path: String,
        val size: Int,
        val contentType: String,
    )

    private companion object {
        private const val MAX_PAYLOAD = 1 * 1024 * 1024 // 1 MB
    }
}
