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
import com.shifenmiao.model.file.AgentApplyTextPatchParams
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import com.shifenmiao.model.file.AgentTextPatchHunk
import javax.inject.Inject

class ApplyTextPatchTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "apply_text_patch"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_apply_text_patch)
    override val title: String = textProvider.string(R.string.agent_tool_apply_text_patch_title)
    override val summary: String = textProvider.string(R.string.agent_tool_apply_text_patch_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_apply_text_patch_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_apply_text_patch_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS
    override val requiresConfirmation: Boolean = true

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "file_uri" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_apply_text_patch_param_file_uri),
            ),
            "hunks" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_apply_text_patch_param_hunks),
            ),
        ),
        required = listOf("file_uri", "hunks"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ApplyTextPatchToolParams::class.java)
            val fileUri = params.file_uri?.takeIf { it.isNotBlank() }
                ?: return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_apply_text_patch_missing_file_uri),
                    isError = true,
                )
            if (params.hunks.isNullOrEmpty()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_apply_text_patch_missing_hunks),
                    isError = true,
                )
            }
            val hunks = params.hunks.mapIndexed { index, hunk ->
                val oldText = hunk.old_text ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_apply_text_patch_invalid_hunk, index + 1)
                )
                val newText = hunk.new_text ?: throw IllegalArgumentException(
                    textProvider.string(R.string.agent_tool_apply_text_patch_invalid_hunk, index + 1)
                )
                AgentTextPatchHunk(
                    oldText = oldText,
                    newText = newText,
                    replaceAll = hunk.replace_all == true,
                )
            }
            when (
                val result = agentFileService.applyTextPatch(
                    AgentApplyTextPatchParams(
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
                    R.string.agent_tool_apply_text_patch_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_apply_text_patch_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

private data class ApplyTextPatchToolParams(
    val file_uri: String? = null,
    val hunks: List<ApplyTextPatchHunkPayload>? = null,
)

private data class ApplyTextPatchHunkPayload(
    val old_text: String? = null,
    val new_text: String? = null,
    val replace_all: Boolean? = null,
)

