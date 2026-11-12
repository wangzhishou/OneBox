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
 * 管理（写）远端文件 / 目录 —— 删 / 改名 / 移动 / 建目录。
 *
 * DANGEROUS + requiresConfirmation=true。
 */
class ManageCloudFilesTool @Inject constructor(
    private val textProvider: CloudAgentToolTextProvider,
    private val cloudFileService: CloudFileService,
    private val connectionHolder: CloudAgentToolConnectionHolder,
) : AgentTool {

    override val name: String = "manage_cloud_files"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_manage_cloud_files)
    override val title: String = textProvider.string(R.string.agent_tool_manage_cloud_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_manage_cloud_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_manage_cloud_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_manage_cloud_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS
    override val requiresConfirmation: Boolean = true
    override val confirmationTitle: String = textProvider.string(R.string.agent_tool_manage_cloud_files_confirm_title)
    override val confirmationToolPresentation: String = textProvider.string(R.string.agent_tool_manage_cloud_files_confirm_presentation)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_action),
                enum = listOf("create_directory", "delete", "rename"),
            ),
            "connection_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_connection_id),
            ),
            "root" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_root),
            ),
            "path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_path),
            ),
            "target_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_target_path),
            ),
            "is_directory" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_cloud_files_param_is_directory),
            ),
        ),
        required = listOf("action", "connection_id"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = if (arguments.isBlank()) ManageParams() else AppJson.decodeFromString<ManageParams>(arguments)
        val connection = resolveConnection(params.connection_id)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_cloud_files_connection_not_found, params.connection_id.orEmpty()),
                isError = true,
            )
        val root = params.root?.takeIf { it.isNotBlank() }
            ?: cloudFileService.defaultRootName(connection)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_cloud_files_missing_root),
                isError = true,
            )

        return when (params.action?.trim()) {
            "create_directory" -> {
                val path = params.path?.takeIf { it.isNotBlank() }
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_manage_cloud_files_missing_path),
                        isError = true,
                    )
                cloudFileService.createDirectory(connection, root, path).fold(
                    onSuccess = { AgentToolResult(content = AppJson.encodeToString(ManageOkResult("create_directory", root, path))) },
                    onFailure = { failure(it) },
                )
            }
            "delete" -> {
                val path = params.path?.takeIf { it.isNotBlank() }
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_manage_cloud_files_missing_path),
                        isError = true,
                    )
                cloudFileService.delete(connection, root, path, params.is_directory == true).fold(
                    onSuccess = { AgentToolResult(content = AppJson.encodeToString(ManageOkResult("delete", root, path))) },
                    onFailure = { failure(it) },
                )
            }
            "rename" -> {
                val from = params.path?.takeIf { it.isNotBlank() }
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_manage_cloud_files_missing_path),
                        isError = true,
                    )
                val to = params.target_path?.takeIf { it.isNotBlank() }
                    ?: return AgentToolResult(
                        content = textProvider.string(R.string.agent_tool_manage_cloud_files_missing_target_path),
                        isError = true,
                    )
                cloudFileService.rename(connection, root, from, to).fold(
                    onSuccess = { AgentToolResult(content = AppJson.encodeToString(ManageRenameResult(root, from, to))) },
                    onFailure = { failure(it) },
                )
            }
            else -> AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_cloud_files_invalid_action, params.action.orEmpty()),
                isError = true,
            )
        }
    }

    private fun failure(t: Throwable): AgentToolResult = AgentToolResult(
        content = textProvider.string(R.string.agent_tool_manage_cloud_files_failed, t.message.orEmpty()),
        isError = true,
    )

    private fun resolveConnection(connectionId: String?): CloudStorageConnection? {
        if (connectionId.isNullOrBlank()) return null
        return connectionHolder.current().firstOrNull { it.id == connectionId }
    }

    @Serializable
    private data class ManageParams(
        val action: String? = null,
        val connection_id: String? = null,
        val root: String? = null,
        val path: String? = null,
        val target_path: String? = null,
        val is_directory: Boolean? = null,
    )

    @Serializable
    private data class ManageOkResult(val action: String, val root: String, val path: String)
    @Serializable
    private data class ManageRenameResult(val root: String, val from: String, val to: String)
}
