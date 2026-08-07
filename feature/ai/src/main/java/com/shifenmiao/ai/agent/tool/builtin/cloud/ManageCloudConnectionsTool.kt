package com.shifenmiao.ai.agent.tool.builtin.cloud

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.CloudAgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.wanbaohe.cloud.storage.agent.CloudAgentToolConnectionHolder
import com.wanbaohe.cloud.storage.data.CloudStorageRepository
import com.wanbaohe.cloud.storage.model.CloudStorageConnection
import com.wanbaohe.cloud.storage.model.S3Vendor
import com.wanbaohe.cloud.storage.service.CloudFileService
import java.util.UUID
import javax.inject.Inject

/**
 * 管理（写）远程存储连接 —— 创建 / 测试 / 删除。
 *
 * 三类凭据都直接由用户提供，**不**支持 LLM 端到端生成。LLM 应从用户输入中提取或
 * 询问。成功创建后凭据写入 EncryptedSharedPreferences（Android Keystore 加密），
 * 后续 browse / upload 等工具通过 connection_id 引用，**不应**再次传递明文。
 *
 * SENSITIVE + requiresConfirmation=true —— 创建/删除都会触发用户确认弹窗。
 */
class ManageCloudConnectionsTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val repository: CloudStorageRepository,
    private val connectionHolder: CloudAgentToolConnectionHolder,
    private val gson: Gson,
) : AgentTool {

    override val name: String = "manage_cloud_connections"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_manage_cloud_connections)
    override val title: String = textProvider.string(R.string.agent_tool_manage_cloud_connections_title)
    override val summary: String = textProvider.string(R.string.agent_tool_manage_cloud_connections_summary)
    override val category: ToolCategory = ToolCategory.BUSINESS
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_manage_cloud_connections_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_manage_cloud_connections_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE
    override val requiresConfirmation: Boolean = true
    override val confirmationTitle: String = textProvider.string(R.string.agent_tool_manage_cloud_connections_confirm_title)
    override val confirmationToolPresentation: String = textProvider.string(R.string.agent_tool_manage_cloud_connections_confirm_presentation)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_action),
                enum = listOf("create", "test", "delete"),
            ),
            "protocol" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_protocol),
                enum = listOf("S3_COMPAT", "WEB_DAV", "SMB"),
            ),
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_connection_id),
            ),
            "display_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_display_name),
            ),
            "is_default" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_is_default),
            ),
            "s3_vendor" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_vendor),
                enum = S3Vendor.entries.map { it.name },
            ),
            "s3_endpoint" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_endpoint),
            ),
            "s3_region" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_region),
            ),
            "s3_bucket" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_bucket),
            ),
            "s3_access_key_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_access_key_id),
            ),
            "s3_secret_access_key" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_s3_secret_access_key),
            ),
            "webdav_base_url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_webdav_base_url),
            ),
            "webdav_root_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_webdav_root_path),
            ),
            "webdav_username" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_webdav_username),
            ),
            "webdav_password" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_webdav_password),
            ),
            "smb_host" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_host),
            ),
            "smb_port" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_port),
            ),
            "smb_share" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_share),
            ),
            "smb_domain" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_domain),
            ),
            "smb_username" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_username),
            ),
            "smb_password" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_connections_param_smb_password),
            ),
        ),
        required = listOf("action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) ManageParams() else gson.fromJson(arguments, ManageParams::class.java)
        return when (params.action?.trim()) {
            "test" -> executeTest(params)
            "create" -> executeCreate(params)
            "delete" -> executeDelete(params)
            else -> AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_cloud_connections_invalid_action,
                    params.action.orEmpty(),
                ),
                isError = true,
            )
        }
    }

    private suspend fun executeTest(params: ManageParams): AgentToolResult {
        val conn = buildConnection(params) ?: return buildProtocolError(params)
        return cloudFileService.testConnection(conn).fold(
            onSuccess = {
                AgentToolResult(
                    content = gson.toJson(
                        TestResult(
                            ok = true,
                            protocol = conn.protocol.name,
                            displayName = conn.displayName,
                            message = textProvider.string(
                                R.string.agent_tool_manage_cloud_connections_test_ok,
                                conn.displayName,
                            ),
                        )
                    )
                )
            },
            onFailure = { error ->
                AgentToolResult(
                    content = gson.toJson(
                        TestResult(
                            ok = false,
                            protocol = conn.protocol.name,
                            displayName = conn.displayName,
                            message = error.message.orEmpty(),
                        )
                    )
                )
            },
        )
    }

    private suspend fun executeCreate(params: ManageParams): AgentToolResult {
        val conn = buildConnection(params) ?: return buildProtocolError(params)
        return cloudFileService.testConnection(conn).fold(
            onSuccess = {
                repository.saveConnection(conn)
                pushHolderUpdate()
                AgentToolResult(
                    content = gson.toJson(
                        CreateResult(
                            connectionId = conn.id,
                            displayName = conn.displayName,
                            protocol = conn.protocol.name,
                            isDefault = conn.isDefault,
                            message = textProvider.string(
                                R.string.agent_tool_manage_cloud_connections_create_ok,
                                conn.displayName,
                            ),
                        )
                    )
                )
            },
            onFailure = { error ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_cloud_connections_create_test_failed,
                        conn.displayName,
                        error.message.orEmpty(),
                    ),
                    isError = true,
                )
            },
        )
    }

    private fun executeDelete(params: ManageParams): AgentToolResult {
        val id = params.connection_id?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_cloud_connections_missing_connection_id),
                isError = true,
            )
        val existing = connectionHolder.current().firstOrNull { it.id == id }
            ?: return AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_cloud_connections_connection_not_found,
                    id,
                ),
                isError = true,
            )
        repository.deleteConnection(id)
        pushHolderUpdate()
        return AgentToolResult(
            content = gson.toJson(
                DeleteResult(
                    connectionId = id,
                    displayName = existing.displayName,
                    protocol = existing.protocol.name,
                    message = textProvider.string(
                        R.string.agent_tool_manage_cloud_connections_delete_ok,
                        existing.displayName,
                    ),
                )
            )
        )
    }

    private fun buildConnection(params: ManageParams): CloudStorageConnection? {
        val displayName = params.display_name?.takeIf { it.isNotBlank() } ?: return null
        val isDefault = params.is_default == true
        return when (params.protocol?.trim()) {
            "S3_COMPAT" -> {
                val vendor = params.s3_vendor?.let { name ->
                    runCatching { S3Vendor.valueOf(name) }.getOrNull()
                } ?: S3Vendor.S3_COMPATIBLE
                val endpoint = params.s3_endpoint?.takeIf { it.isNotBlank() } ?: vendor.defaultEndpointHint
                val region = params.s3_region.orEmpty()
                val bucket = params.s3_bucket.orEmpty()
                val accessKey = params.s3_access_key_id.orEmpty()
                val secretKey = params.s3_secret_access_key.orEmpty()
                CloudStorageConnection.S3Compat(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    vendor = vendor,
                    endpoint = endpoint,
                    region = region,
                    bucket = bucket,
                    accessKeyId = accessKey,
                    secretAccessKey = secretKey,
                    isDefault = isDefault,
                )
            }
            "WEB_DAV" -> {
                val baseUrl = params.webdav_base_url?.takeIf { it.isNotBlank() } ?: return null
                val username = params.webdav_username.orEmpty()
                val password = params.webdav_password.orEmpty()
                CloudStorageConnection.WebDav(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    baseUrl = baseUrl,
                    username = username,
                    password = password,
                    rootPath = params.webdav_root_path?.takeIf { it.isNotBlank() } ?: "/",
                    isDefault = isDefault,
                )
            }
            "SMB" -> {
                // SMB 只 host 必填。share / domain / username / password 全部可选，
                // 支持匿名 / Guest 访问，以及 "先建连接后列举 share" 流程。
                val host = params.smb_host?.takeIf { it.isNotBlank() } ?: return null
                CloudStorageConnection.Smb(
                    id = UUID.randomUUID().toString(),
                    displayName = displayName,
                    host = host,
                    port = params.smb_port ?: 445,
                    share = params.smb_share.orEmpty(),
                    domain = params.smb_domain.orEmpty(),
                    username = params.smb_username.orEmpty(),
                    password = params.smb_password.orEmpty(),
                    isDefault = isDefault,
                )
            }
            else -> null
        }
    }

    private fun buildProtocolError(params: ManageParams): AgentToolResult = AgentToolResult(
        content = textProvider.string(
            R.string.agent_tool_manage_cloud_connections_missing_protocol,
            params.protocol.orEmpty(),
        ),
        isError = true,
    )

    private fun pushHolderUpdate() {
        connectionHolder.update(repository.getConnections())
    }

    private data class ManageParams(
        val action: String? = null,
        val protocol: String? = null,
        val connection_id: String? = null,
        val display_name: String? = null,
        val is_default: Boolean? = null,
        val s3_vendor: String? = null,
        val s3_endpoint: String? = null,
        val s3_region: String? = null,
        val s3_bucket: String? = null,
        val s3_access_key_id: String? = null,
        val s3_secret_access_key: String? = null,
        val webdav_base_url: String? = null,
        val webdav_root_path: String? = null,
        val webdav_username: String? = null,
        val webdav_password: String? = null,
        val smb_host: String? = null,
        val smb_port: Int? = null,
        val smb_share: String? = null,
        val smb_domain: String? = null,
        val smb_username: String? = null,
        val smb_password: String? = null,
    )

    private data class TestResult(
        val ok: Boolean,
        val protocol: String,
        val displayName: String,
        val message: String,
    )

    private data class CreateResult(
        val connectionId: String,
        val displayName: String,
        val protocol: String,
        val isDefault: Boolean,
        val message: String,
    )

    private data class DeleteResult(
        val connectionId: String,
        val displayName: String,
        val protocol: String,
        val message: String,
    )
}
