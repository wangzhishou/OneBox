package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentManageFileData
import com.shifenmiao.model.file.AgentManageFileParams
import javax.inject.Inject

class ManageFilesTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "manage_files"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_manage_files)

    override val title: String = textProvider.string(R.string.agent_tool_manage_files_title)

    override val summary: String = textProvider.string(R.string.agent_tool_manage_files_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_manage_files_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_manage_files_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_action),
                enum = listOf("delete", "rename", "copy", "move", "create_file", "create_folder", "write_file")
            ),
            "source_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_source_uri)
            ),
            "destination_dir_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_destination_dir_uri)
            ),
            "new_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_new_name)
            ),
            "file_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_file_name)
            ),
            "folder_name" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_folder_name)
            ),
            "mime_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_mime_type)
            ),
            "content" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_content)
            ),
            "write_mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_files_param_write_mode),
                enum = listOf("overwrite", "append")
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = if (arguments.isBlank()) ManageFilesParams() else {
                gson.fromJson(arguments, ManageFilesParams::class.java)
            }

            when (params.action?.trim()) {
                "delete" -> executeDelete(params)
                "rename" -> executeRename(params)
                "copy" -> executeCopy(params)
                "move" -> executeMove(params)
                "create_file" -> executeCreateFile(params)
                "create_folder" -> executeCreateFolder(params)
                "write_file" -> executeWriteFile(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_files_invalid_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_files_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun executeDelete(params: ManageFilesParams): AgentToolResult {
        requireSourceUri(params)
        return manage(params)
    }

    private suspend fun executeRename(params: ManageFilesParams): AgentToolResult {
        requireSourceUri(params)
        val newName = params.new_name?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_files_missing_new_name),
                isError = true
            )
        return manage(params.copy(new_name = newName))
    }

    private suspend fun executeCopy(params: ManageFilesParams): AgentToolResult {
        requireSourceUri(params)
        requireDestinationDirUri(params)
        return manage(params)
    }

    private suspend fun executeMove(params: ManageFilesParams): AgentToolResult {
        requireSourceUri(params)
        requireDestinationDirUri(params)
        return manage(params)
    }

    private suspend fun executeCreateFile(params: ManageFilesParams): AgentToolResult {
        val fileName = params.file_name?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_files_missing_file_name),
                isError = true
            )
        return manage(params.copy(file_name = fileName))
    }

    private suspend fun executeCreateFolder(params: ManageFilesParams): AgentToolResult {
        val folderName = params.folder_name?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_files_missing_folder_name),
                isError = true
            )
        return manage(params.copy(folder_name = folderName))
    }

    private suspend fun executeWriteFile(params: ManageFilesParams): AgentToolResult {
        requireSourceUri(params)
        val content = params.content?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_manage_files_missing_content),
                isError = true
            )
        return manage(params.copy(content = content))
    }

    private fun requireSourceUri(params: ManageFilesParams): String {
        return params.source_uri?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException(textProvider.string(R.string.agent_tool_manage_files_missing_source_uri))
    }

    private fun requireDestinationDirUri(params: ManageFilesParams): String {
        return params.destination_dir_uri
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException(
                textProvider.string(R.string.agent_tool_manage_files_missing_destination_dir_uri)
            )
    }

    private suspend fun manage(params: ManageFilesParams): AgentToolResult {
        return when (
            val result = agentFileService.manageFile(
                AgentManageFileParams(
                    action = params.action.orEmpty(),
                    sourceUri = params.source_uri,
                    destinationDirectoryUri = params.destination_dir_uri,
                    newName = params.new_name,
                    fileName = params.file_name,
                    folderName = params.folder_name,
                    mimeType = params.mime_type,
                    content = params.content,
                    append = params.write_mode == "append",
                )
            )
        ) {
            is AgentFileOperationResult.Success -> successResult(result.data)
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private fun successResult(data: AgentManageFileData): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                ManageFilesResult(
                    action = data.action,
                    success = true,
                    requiresConfirmation = requiresConfirmation,
                    affectedUri = data.affectedUri,
                    targetUri = data.targetUri,
                    created = data.created,
                    deeplink = data.parentDirectoryUri?.let { "dir://$it" }
                )
            )
        )
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_manage_files_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) }
            ),
            isError = true
        )
    }
}

private data class ManageFilesParams(
    val action: String? = null,
    val source_uri: String? = null,
    val destination_dir_uri: String? = null,
    val new_name: String? = null,
    val file_name: String? = null,
    val folder_name: String? = null,
    val mime_type: String? = null,
    val content: String? = null,
    val write_mode: String? = null
)

private data class ManageFilesResult(
    val action: String,
    val success: Boolean,
    val requiresConfirmation: Boolean,
    val affectedUri: String?,
    val targetUri: String?,
    val created: Boolean,
    val deeplink: String?
)
