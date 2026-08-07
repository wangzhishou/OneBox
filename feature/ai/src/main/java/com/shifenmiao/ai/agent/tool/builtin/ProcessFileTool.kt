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
import com.shifenmiao.model.file.AgentReadFileParams
import com.shifenmiao.model.file.AgentSearchContextParams
import com.shifenmiao.model.file.AgentSearchFileParams
import javax.inject.Inject

class ProcessFileTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "process_file"

    override val description: String = textProvider.raw(R.raw.agent_tool_description_process_file)

    override val title: String = textProvider.string(R.string.agent_tool_process_file_title)

    override val summary: String = textProvider.string(R.string.agent_tool_process_file_summary)

    override val category: ToolCategory = ToolCategory.FILE

    override val keywords: List<String> = textProvider.array(R.array.agent_tool_process_file_keywords)

    override val examples: List<String> = textProvider.array(R.array.agent_tool_process_file_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "file_path" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_process_file_param_file_path),
            ),
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_process_file_param_action),
                enum = listOf("read", "search", "context"),
            ),
            "start_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_process_file_param_start_line),
            ),
            "end_line" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_process_file_param_end_line),
            ),
            "max_length" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_process_file_param_max_length),
            ),
            "keyword" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_process_file_param_keyword),
            ),
            "context_lines" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_process_file_param_context_lines),
            ),
            "max_matches" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_process_file_param_max_matches),
            ),
            "case_sensitive" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_process_file_param_case_sensitive),
            ),
        ),
        required = listOf("file_path", "action"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ProcessFileParams::class.java)
            val filePath = params.file_path?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_process_file_missing_file_path),
                    isError = true,
                )

            when (params.action?.trim()) {
                "read" -> handleRead(filePath, params)
                "search" -> handleSearch(filePath, params)
                "context" -> handleContext(filePath, params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_process_file_invalid_action,
                        params.action.orEmpty(),
                    ),
                    isError = true,
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_process_file_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private suspend fun handleRead(filePath: String, params: ProcessFileParams): AgentToolResult {
        return when (
            val result = agentFileService.readFile(
                AgentReadFileParams(
                    fileUri = filePath,
                    startLine = params.start_line,
                    endLine = params.end_line,
                    maxLength = params.max_length ?: 4096,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> AgentToolResult(content = gson.toJson(result.data))
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private suspend fun handleSearch(filePath: String, params: ProcessFileParams): AgentToolResult {
        val keyword = params.keyword?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_process_file_missing_keyword),
                isError = true,
            )
        return when (
            val result = agentFileService.searchInFile(
                AgentSearchFileParams(
                    fileUri = filePath,
                    keyword = keyword,
                    maxMatches = params.max_matches ?: 20,
                    caseSensitive = params.case_sensitive == true,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> AgentToolResult(content = gson.toJson(result.data))
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private suspend fun handleContext(filePath: String, params: ProcessFileParams): AgentToolResult {
        val keyword = params.keyword?.takeIf { it.isNotBlank() }
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_process_file_missing_keyword),
                isError = true,
            )
        return when (
            val result = agentFileService.readSearchContext(
                AgentSearchContextParams(
                    fileUri = filePath,
                    keyword = keyword,
                    contextLines = params.context_lines ?: 50,
                    maxMatches = params.max_matches ?: 10,
                    caseSensitive = params.case_sensitive == true,
                )
            )
        ) {
            is AgentFileOperationResult.Success -> AgentToolResult(content = gson.toJson(result.data))
            is AgentFileOperationResult.Error -> failure(result.message)
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_process_file_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

private data class ProcessFileParams(
    val file_path: String? = null,
    val action: String? = null,
    val start_line: Int? = null,
    val end_line: Int? = null,
    val max_length: Int? = null,
    val keyword: String? = null,
    val context_lines: Int? = null,
    val max_matches: Int? = null,
    val case_sensitive: Boolean? = null
)

