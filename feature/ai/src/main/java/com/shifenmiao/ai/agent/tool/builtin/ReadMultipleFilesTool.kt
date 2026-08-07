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
import com.shifenmiao.model.file.AgentReadMultipleFilesItemParams
import com.shifenmiao.model.file.AgentReadMultipleFilesParams
import javax.inject.Inject

class ReadMultipleFilesTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "read_multiple_files"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_read_multiple_files)
    override val title: String = textProvider.string(R.string.agent_tool_read_multiple_files_title)
    override val summary: String = textProvider.string(R.string.agent_tool_read_multiple_files_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_read_multiple_files_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_read_multiple_files_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "files" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_read_multiple_files_param_files),
            ),
            "max_length_per_file" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_read_multiple_files_param_max_length_per_file),
            ),
        ),
        required = listOf("files"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ReadMultipleFilesToolParams::class.java)
            if (params.files.isNullOrEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_read_multiple_files_missing_files),
                    isError = true,
                )
            }
            val files = params.files.mapIndexed { index, item ->
                val fileUri = item.file_uri?.takeIf { it.isNotBlank() } ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_read_multiple_files_invalid_file_item, index + 1)
                )
                AgentReadMultipleFilesItemParams(
                    fileUri = fileUri,
                    startLine = item.start_line,
                    endLine = item.end_line,
                )
            }
            when (
                val result = agentFileService.readMultipleFiles(
                    AgentReadMultipleFilesParams(
                        files = files,
                        maxLengthPerFile = params.max_length_per_file ?: 4096,
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> AgentToolResult(gson.toJson(result.data))
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_read_multiple_files_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_read_multiple_files_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

private data class ReadMultipleFilesToolParams(
    val files: List<ReadMultipleFilesItemPayload>? = null,
    val max_length_per_file: Int? = null,
)

private data class ReadMultipleFilesItemPayload(
    val file_uri: String? = null,
    val start_line: Int? = null,
    val end_line: Int? = null,
)

