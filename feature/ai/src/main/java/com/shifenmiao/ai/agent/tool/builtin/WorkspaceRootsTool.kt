package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.file.AgentFileOperationResult
import com.shifenmiao.model.file.AgentFileService
import javax.inject.Inject

class WorkspaceRootsTool @Inject constructor(
    private val agentFileService: AgentFileService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "workspace_roots"
    override val description: String = textProvider.raw(R.raw.agent_tool_description_workspace_roots)
    override val title: String = textProvider.string(R.string.agent_tool_workspace_roots_title)
    override val summary: String = textProvider.string(R.string.agent_tool_workspace_roots_summary)
    override val category: ToolCategory = ToolCategory.FILE
    override val keywords: List<String> = textProvider.array(R.array.agent_tool_workspace_roots_keywords)
    override val examples: List<String> = textProvider.array(R.array.agent_tool_workspace_roots_examples)
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE
    override val parametersSchema: ToolParameters = ToolParameters(type = "object")

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            when (val result = agentFileService.workspaceRoots()) {
                is AgentFileOperationResult.Success -> AgentToolResult(gson.toJson(result.data))
                is AgentFileOperationResult.Error -> failure(result.message)
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_workspace_roots_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                ),
                isError = true,
            )
        }
    }

    private fun failure(message: String): AgentToolResult {
        return AgentToolResult(
            content = textProvider.string(
                R.string.agent_tool_workspace_roots_failed,
                message.ifBlank { textProvider.string(R.string.agent_tool_unknown_error) },
            ),
            isError = true,
        )
    }
}

