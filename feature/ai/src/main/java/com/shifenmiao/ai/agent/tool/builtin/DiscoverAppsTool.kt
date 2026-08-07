package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolExecutionContext
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.AppNavigationCatalogRepository
import com.shifenmiao.ai.agent.tool.ContextAwareAgentTool
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

/**
 * 页面发现：检索可跳转页面入口。
 *
 * 输入 keywords 关键词数组，每个关键词独立模糊匹配页面标题。
 * 返回精简的页面列表，供展示或继续传给跳转工具。
 */
class DiscoverAppsTool @Inject constructor(
    private val appNavigationCatalogRepository: AppNavigationCatalogRepository,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool, ContextAwareAgentTool {

    override val name: String = "discover_apps"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_discover_apps)

    override val title: String =
        textProvider.string(R.string.agent_tool_discover_apps_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_discover_apps_summary)

    override val category: ToolCategory = ToolCategory.SYSTEM

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_discover_apps_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_discover_apps_examples)

    override val bootstrapModes: Set<ChatWorkingMode> = ChatWorkingMode.entries.toSet()

    override val visibleToUser: Boolean = true

    override val requiresConfirmation: Boolean = false

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val sortOrder: Int = -99

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "keywords" to ToolParameterProperty(
                type = "array",
                description = textProvider.string(R.string.agent_tool_discover_apps_param_keywords)
            ),
            "limit" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_discover_apps_param_limit)
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
                .distinct()
            val limit = (params.limit ?: DEFAULT_LIMIT).coerceIn(1, MAX_LIMIT)

            val screens = fetchScreens(keywords, limit)

            val response = DiscoverAppsResponse(
                apps = screens
            )

            AgentToolResult(
                content = gson.toJson(response),
                isError = false
            )
        }.getOrElse { error ->
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_discover_apps_failed,
                    error.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private suspend fun fetchScreens(
        keywords: List<String>,
        limit: Int
    ): List<MatchedApp> {
        val targets = appNavigationCatalogRepository.searchTargets(
            query = "",
            keywords = keywords,
            limit = limit
        )
        return targets.map { target ->
            val deeplink = target.deeplink.trim()
            if (deeplink.isBlank()) return@map null
            MatchedApp(
                t = target.title,
                u = deeplink,
                d = target.description.takeIf { it.isNotBlank() },
                c = target.listType.takeIf { it.isNotBlank() }
                    ?: target.targetType.name.lowercase(),
                r = target.routeKey.takeIf { it.isNotBlank() },
            )
        }.filterNotNull()
    }

    private fun parseArguments(arguments: String): DiscoverAppsParams {
        if (arguments.isBlank()) return DiscoverAppsParams()
        return gson.fromJson(arguments, DiscoverAppsParams::class.java)
    }

    private data class DiscoverAppsParams(
        val keywords: List<String?>? = null,
        val limit: Int? = null
    )

    private data class DiscoverAppsResponse(
        val apps: List<MatchedApp>
    )

    private data class MatchedApp(
        val t: String,
        val u: String,
        val d: String? = null,
        val c: String? = null,
        val r: String? = null,
    )

    private companion object {
        const val DEFAULT_LIMIT = 8
        const val MAX_LIMIT = 20
    }
}
