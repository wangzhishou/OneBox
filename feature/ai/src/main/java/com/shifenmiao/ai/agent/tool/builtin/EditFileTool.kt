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
import com.shifenmiao.model.file.AgentEditFileData
import com.shifenmiao.model.file.AgentEditFileParams
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import javax.inject.Inject

class EditFileTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "edit_file"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_edit_file)

    override val title: String = textProvider.string(R.string.agent_tool_edit_file_title)

    override val summary: String = textProvider.string(R.string.agent_tool_edit_file_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_edit_file_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_edit_file_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "file_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_file_uri),
            ),
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_action),
                enum = listOf(
                    "replace_text",
                    "replace_lines",
                    "insert_before_line",
                    "insert_after_line",
                    "append",
                    "prepend",
                ),
            ),
            "old_text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_old_text),
            ),
            "new_text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_edit_file_param_new_text),
            ),
            "start_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_start_line),
            ),
            "end_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_end_line),
            ),
            "line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_edit_file_param_line),
            ),
            "replace_all" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_edit_file_param_replace_all),
            ),
        ),
        required = listOf("file_uri", "action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, EditFileToolParams::class.java)
            val fileUri = params.file_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_edit_file_missing_file_uri),
                    isError = true,
                )
            val action = params.action?.trim().orEmpty()
            if (action.isBlank()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_edit_file_missing_action),
                    isError = true,
                )
            }
            val validationError = validate(params)
            if (validationError != null) {
                return AgentToolResult(content = validationError, isError = true)
            }

            when (
                val result = agentFileService.editFile(
                    AgentEditFileParams(
                        fileUri = fileUri,
                        action = action,
                        newText = params.new_text,
                        oldText = params.old_text,
                        startLine = params.start_line,
                        endLine = params.end_line,
                        line = params.line,
                        replaceAll = params.replace_all == true,
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> success(result.data)
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_edit_file_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun validate(params: EditFileToolParams): String? {
        return when (params.action?.trim()) {
            "replace_text" -> {
                when {
                    params.old_text.isNullOrEmpty() -> textProvider.string(R.string.agent_tool_edit_file_missing_old_text)
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    else -> null
                }
            }

            "replace_lines" -> {
                when {
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    params.start_line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_start_line)
                    params.end_line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_end_line)
                    else -> null
                }
            }

            "insert_before_line", "insert_after_line" -> {
                when {
                    params.new_text == null -> textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                    params.line == null -> textProvider.string(R.string.agent_tool_edit_file_missing_line)
                    else -> null
                }
            }

            "append", "prepend" -> {
                if (params.new_text == null) {
                    textProvider.string(R.string.agent_tool_edit_file_missing_new_text)
                } else {
                    null
                }
            }

            else -> textProvider.string(
                R.string.agent_tool_edit_file_invalid_action,
                params.action.orEmpty(),
            )
        }
    }

    private fun success(data: AgentEditFileData): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                EditFileResult(
                    action = data.action,
                    success = true,
                    requiresConfirmation = requiresConfirmation,
                    fileUri = data.fileUri,
                    preview = data.preview,
                    replacedCount = data.replacedCount,
                    startLine = data.startLine,
                    endLine = data.endLine,
                    line = data.line,
                    deeplink = data.parentDirectoryUri?.let { "dir://$it" },
                )
            )
        )
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_edit_file_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

private data class EditFileToolParams(
    val file_uri: String? = null,
    val action: String? = null,
    val old_text: String? = null,
    val new_text: String? = null,
    val start_line: Int? = null,
    val end_line: Int? = null,
    val line: Int? = null,
    val replace_all: Boolean? = null,
)

private data class EditFileResult(
    val action: String,
    val success: Boolean,
    val requiresConfirmation: Boolean,
    val fileUri: String,
    val preview: String,
    val replacedCount: Int,
    val startLine: Int?,
    val endLine: Int?,
    val line: Int?,
    val deeplink: String?,
)

