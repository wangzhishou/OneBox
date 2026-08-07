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
import com.shifenmiao.network.service.UrlCheckService
import javax.inject.Inject

/**
 * Agent 工具：检查 URL 状态
 *
 * 检查 URL 的可访问性、响应状态、重定向链等信息
 */
class CheckUrlTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val urlCheckService: UrlCheckService,
    private val gson: Gson
) : AgentTool {

    override val name: String = "check_url"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_check_url)

    override val title: String =
        textProvider.string(R.string.agent_tool_check_url_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_check_url_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_check_url_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_check_url_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_check_url_param_url)
            ),
            "timeout" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_check_url_param_timeout)
            )
        ),
        required = listOf("url")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, CheckUrlParams::class.java)

            urlCheckService.check(
                url = params.url,
                timeoutSeconds = params.timeout?.toLong() ?: 10L
            ).fold(
                onSuccess = { result ->
                    AgentToolResult(
                        content = buildString {
                            appendLine(textProvider.string(R.string.agent_tool_check_url_success))
                            appendLine()
                            appendLine("URL: ${result.url}")
                            appendLine("可访问: ${if (result.isAccessible) "是" else "否"}")

                            if (result.statusCode != null) {
                                appendLine("状态码: ${result.statusCode} ${result.statusMessage ?: ""}")
                            }

                            appendLine("响应时间: ${result.responseTimeMs}ms")

                            if (result.finalUrl != null && result.finalUrl != result.url) {
                                appendLine("最终URL: ${result.finalUrl}")
                            }

                            if (result.redirectChain.isNotEmpty()) {
                                appendLine("重定向链: ${result.redirectChain.size}次")
                                result.redirectChain.forEachIndexed { index, url ->
                                    appendLine("  ${index + 1}. $url")
                                }
                            }

                            if (result.contentType != null) {
                                appendLine("Content-Type: ${result.contentType}")
                            }

                            if (result.serverHeader != null) {
                                appendLine("服务器: ${result.serverHeader}")
                            }

                            if (result.ipAddress != null) {
                                appendLine("IP地址: ${result.ipAddress}")
                            }

                            if (result.error != null) {
                                appendLine()
                                appendLine("错误: ${result.error}")
                            }
                        }
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_check_url_failed,
                            error.message ?: "未知错误"
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_check_url_failed,
                    e.message ?: "未知错误"
                ),
                isError = true
            )
        }
    }

    private data class CheckUrlParams(
        val url: String,
        val timeout: Int?
    )
}
