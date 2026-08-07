package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

/**
 * 工具发现：检索可用 Agent 工具列表。
 *
 * 输入 keywords 关键词数组，每个关键词独立模糊匹配工具的名称、标题、摘要、描述和关键词。
 * 返回 matchedTools（工具列表，用 name 调用）。
 */
class DiscoverToolsTool @Inject constructor(
    private val agentToolRegistry: AgentToolRegistry,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool, ContextAwareAgentTool {

    override val name: String = "discover_tools"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_discover_tools)

    override val title: String =
        textProvider.string(R.string.agent_tool_discover_tools_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_discover_tools_summary)

    override val category: ToolCategory = ToolCategory.SYSTEM

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_discover_tools_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_discover_tools_examples)

    override val bootstrapModes: Set<ChatWorkingMode> = ChatWorkingMode.entries.toSet()

    override val visibleToUser: Boolean = true

    override val requiresConfirmation: Boolean = false

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val sortOrder: Int = -100

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "keywords" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_discover_tools_param_keywords)
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_discover_tools_param_limit)
            )
        ),
        required = listOf("keywords")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return execute(arguments, AgentToolExecutionContext())
    }

    override suspend fun execute(
        arguments: String,
        context: AgentToolExecutionContext
    ): AgentToolResult {
        return runCatching {
            val params = parseArguments(arguments)
            val keywords = params.keywords.orEmpty()
                .mapNotNull { it?.trim() }
                .filter { it.isNotEmpty() }
            val limit = (params.limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)
            val toolCandidates = buildToolCandidates(keywords, limit)

            // 只返回工具名列表（JSON 数组），完整定义通过 tools 参数自动下发，避免重复消耗 token
            val toolNames = toolCandidates.map { it.item.name }

            AgentToolResult(
                content = gson.toJson(toolNames),
                isError = false
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_discover_tools_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    // ==================== Tool Discovery ====================

    private suspend fun buildToolCandidates(
        keywords: List<String>,
        limit: Int
    ): List<ScoredToolCandidate> {
        // 搜索全部可见工具目录。
        // discover_tools 的职责是告知 LLM 有哪些工具可用；
        // 动态扩展由 PromptAssemblyService.buildFollowUpToolsAfterDiscovery 负责将发现的工具加入活跃集。
        val allTools = agentToolRegistry.getTools(includeHidden = false)
            .asSequence()
            .filter { it.name != name && it.name != "discover_apps" }
            .toList()

        if (allTools.isEmpty()) return emptyList()

        val scored = allTools.map { tool -> scoreTool(tool, keywords) }
        val sorted = scored.sortedWith(
            compareByDescending<ScoredToolCandidate> { it.score }
                .thenBy { it.item.sortOrder }
                .thenBy { it.item.title }
        )

        return if (keywords.isEmpty()) {
            sorted.take(limit)
        } else {
            sorted.filter { it.score > 0.0 }.ifEmpty { sorted }.take(limit)
        }
    }

    private fun scoreTool(
        tool: ToolCatalogItem,
        keywords: List<String>
    ): ScoredToolCandidate {
        var score = 0.0

        for (keyword in keywords) {
            if (keyword.length < 2) continue
            if (tool.name.equals(keyword, ignoreCase = true)) {
                score += 4.0
            } else if (tool.name.contains(keyword, ignoreCase = true)) {
                score += 2.5
            }
            if (tool.title.contains(keyword, ignoreCase = true)) score += 2.5
            if (tool.summary.contains(keyword, ignoreCase = true)) score += 2.0
            if (tool.description.contains(keyword, ignoreCase = true)) score += 1.5

            val keywordHits = tool.keywords.count { it.contains(keyword, ignoreCase = true) }
            if (keywordHits > 0) score += keywordHits * 1.2

            val exampleHits = tool.examples.count { it.contains(keyword, ignoreCase = true) }
            if (exampleHits > 0) score += exampleHits
        }

        return ScoredToolCandidate(item = tool, score = score)
    }

    // ==================== Parsing ====================

    private fun parseArguments(arguments: String): DiscoverToolsParams {
        if (arguments.isBlank()) return DiscoverToolsParams()
        return gson.fromJson(arguments, DiscoverToolsParams::class.java)
    }

    // ==================== Data Classes ====================

    private data class DiscoverToolsParams(
        val keywords: List<String?>? = null,
        val limit: Int? = null
    )

    private data class ScoredToolCandidate(
        val item: ToolCatalogItem,
        val score: Double
    )

    private companion object {
        const val DEFAULT_LIMIT = 8
        const val MAX_LIMIT = 20
    }
}
