package com.shifenmiao.ai.agent.tool.builtin.network

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.network.service.WebSearchService
import javax.inject.Inject

/**
 * Agent 工具：网络搜索
 *
 * 支持多个搜索引擎：百度、Google、Bing、DuckDuckGo
 */
class SearchWebTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val webSearchService: WebSearchService,
    private val gson: Gson
) : AgentTool {

    override val name: String = "search_web"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_search_web)

    override val title: String =
        textProvider.string(R.string.agent_tool_search_web_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_search_web_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_search_web_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_search_web_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "query" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_web_param_query)
            ),
            "engine" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_search_web_param_engine),
                enum = listOf("baidu", "google", "bing", "duckduckgo")
            ),
            "num_results" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_search_web_param_num_results)
            )
        ),
        required = listOf("query")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, SearchWebParams::class.java)
            val engine = when (params.engine?.lowercase()) {
                "google" -> WebSearchService.SearchEngine.GOOGLE
                "bing" -> WebSearchService.SearchEngine.BING
                "duckduckgo" -> WebSearchService.SearchEngine.DUCKDUCKGO
                else -> WebSearchService.SearchEngine.BAIDU
            }

            webSearchService.search(
                query = params.query,
                engine = engine,
                numResults = params.num_results ?: 5
            ).fold(
                onSuccess = { result ->
                    AgentToolResult(
                        content = buildString {
                            appendLine(
                                textProvider.string(
                                    R.string.agent_tool_search_web_success,
                                    result.results.size
                                )
                            )
                            appendLine()
                            appendLine("搜索引擎: ${result.engine.displayName}")
                            appendLine("搜索词: ${result.query}")
                            appendLine()

                            result.results.forEachIndexed { index, item ->
                                appendLine("--- 结果 ${index + 1} ---")
                                appendLine("标题: ${item.title}")
                                appendLine("链接: ${item.url}")
                                appendLine("摘要: ${item.snippet}")
                                appendLine()
                            }
                        }
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_search_web_failed,
                            error.message ?: "未知错误"
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_search_web_failed,
                    e.message ?: "未知错误"
                ),
                isError = true
            )
        }
    }

    private data class SearchWebParams(
        val query: String,
        val engine: String?,
        val num_results: Int?
    )
}
