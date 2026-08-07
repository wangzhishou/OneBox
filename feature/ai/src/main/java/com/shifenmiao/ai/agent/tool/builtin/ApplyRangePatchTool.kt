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
import com.shifenmiao.model.file.AgentApplyRangePatchParams
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentRangePatchHunk
import javax.inject.Inject

class ApplyRangePatchTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "apply_range_patch"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_apply_range_patch)
    override val title: String = textProvider.string(R.string.agent_tool_apply_range_patch_title)
    override val summary: String = textProvider.string(R.string.agent_tool_apply_range_patch_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_apply_range_patch_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_apply_range_patch_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS
    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "file_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_apply_range_patch_param_file_uri),
            ),
            "hunks" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_apply_range_patch_param_hunks),
            ),
        ),
        required = listOf("file_uri", "hunks"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ApplyRangePatchToolParams::class.java)
            val fileUri = params.file_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_apply_range_patch_missing_file_uri),
                    isError = true,
                )
            if (params.hunks.isNullOrEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_apply_range_patch_missing_hunks),
                    isError = true,
                )
            }
            val hunks = params.hunks.mapIndexed { index, hunk ->
                val startLine = hunk.start_line ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_apply_range_patch_invalid_hunk, index + 1)
                )
                val endLine = hunk.end_line ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_apply_range_patch_invalid_hunk, index + 1)
                )
                val newText = hunk.new_text ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_apply_range_patch_invalid_hunk, index + 1)
                )
                AgentRangePatchHunk(
                    startLine = startLine,
                    endLine = endLine,
                    newText = newText,
                    oldText = hunk.old_text,
                )
            }
            when (
                val result = agentFileService.applyRangePatch(
                    AgentApplyRangePatchParams(
                        fileUri = fileUri,
                        hunks = hunks,
                    )
                )
            ) {
                is AgentFileOperationResult.Success -> AgentToolResult(gson.toJson(result.data))
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_apply_range_patch_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_apply_range_patch_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

private data class ApplyRangePatchToolParams(
    val file_uri: String? = null,
    val hunks: List<ApplyRangePatchHunkPayload>? = null,
)

private data class ApplyRangePatchHunkPayload(
    val start_line: Int? = null,
    val end_line: Int? = null,
    val new_text: String? = null,
    val old_text: String? = null,
)

