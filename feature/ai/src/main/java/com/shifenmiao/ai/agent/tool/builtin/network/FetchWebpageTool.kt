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
import com.shifenmiao.network.service.WebFetchService
import javax.inject.Inject

/**
 * Agent 工具：获取网页内容
 *
 * 支持多种提取模式：text/html/links/images/all
 */
class FetchWebpageTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val webFetchService: WebFetchService,
    private val gson: Gson
) : AgentTool {

    override val name: String = "fetch_webpage"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_fetch_webpage)

    override val title: String =
        textProvider.string(R.string.agent_tool_fetch_webpage_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_fetch_webpage_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_fetch_webpage_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_fetch_webpage_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_fetch_webpage_param_url)
            ),
            "extract_mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_fetch_webpage_param_extract_mode),
                enum = listOf("text", "html", "links", "images", "all")
            ),
            "max_length" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_fetch_webpage_param_max_length)
            )
        ),
        required = listOf("url")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, FetchWebpageParams::class.java)
            val extractMode = when (params.extract_mode?.lowercase()) {
                "html" -> WebFetchService.ExtractMode.HTML
                "links" -> WebFetchService.ExtractMode.LINKS
                "images" -> WebFetchService.ExtractMode.IMAGES
                "all" -> WebFetchService.ExtractMode.ALL
                else -> WebFetchService.ExtractMode.TEXT
            }

            webFetchService.fetch(
                url = params.url,
                extractMode = extractMode,
                maxLength = params.max_length ?: 5000
            ).fold(
                onSuccess = { result ->
                    AgentToolResult(
                        content = buildString {
                            appendLine(textProvider.string(R.string.agent_tool_fetch_webpage_success))
                            appendLine()
                            appendLine("URL: ${result.url}")
                            appendLine("标题: ${result.title ?: "无"}")
                            appendLine("提取模式: ${result.extractMode.name}")
                            appendLine("内容长度: ${result.content.length} 字符")
                            appendLine()
                            appendLine("=== 内容 ===")
                            append(result.content)
                        }
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_fetch_webpage_failed,
                            error.message ?: "未知错误"
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_fetch_webpage_failed,
                    e.message ?: "未知错误"
                ),
                isError = true
            )
        }
    }

    private data class FetchWebpageParams(
        val url: String,
        val extract_mode: String?,
        val max_length: Int?
    )
}
