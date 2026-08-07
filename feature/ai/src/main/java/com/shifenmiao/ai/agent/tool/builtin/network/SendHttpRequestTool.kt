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
import com.shifenmiao.network.service.HttpRequestService
import javax.inject.Inject

/**
 * Agent 工具：发送 HTTP 请求
 *
 * 支持自定义方法、头部、请求体，适用于 API 调试等场景
 */
class SendHttpRequestTool @Inject constructor(
    private val textProvider: AgentToolTextProvider,
    private val httpRequestService: HttpRequestService,
    private val gson: Gson
) : AgentTool {

    override val name: String = "send_http_request"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_send_http_request)

    override val title: String =
        textProvider.string(R.string.agent_tool_send_http_request_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_send_http_request_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_send_http_request_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_send_http_request_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_send_http_request_confirm_title)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_send_http_request_param_url)
            ),
            "method" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_send_http_request_param_method),
                enum = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD")
            ),
            "headers" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_send_http_request_param_headers)
            ),
            "body" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_send_http_request_param_body)
            ),
            "content_type" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_send_http_request_param_content_type)
            )
        ),
        required = listOf("url", "method")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, HttpRequestParams::class.java)

            val method = try {
                HttpRequestService.HttpMethod.valueOf(params.method.uppercase())
            } catch (e: Exception) {
                HttpRequestService.HttpMethod.GET
            }

            // 解析 headers
            val headers = if (params.headers != null) {
                try {
                    @Suppress("UNCHECKED_CAST")
                    gson.fromJson(params.headers, Map::class.java) as? Map<String, String> ?: emptyMap()
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }

            httpRequestService.request(
                HttpRequestService.RequestConfig(
                    url = params.url,
                    method = method,
                    headers = headers,
                    body = params.body,
                    contentType = params.content_type
                )
            ).fold(
                onSuccess = { response ->
                    AgentToolResult(
                        content = buildString {
                            appendLine(
                                textProvider.string(
                                    R.string.agent_tool_send_http_request_success,
                                    response.statusCode
                                )
                            )
                            appendLine()
                            appendLine("URL: ${params.url}")
                            appendLine("方法: ${params.method}")
                            appendLine("状态码: ${response.statusCode} ${response.statusMessage}")
                            appendLine("响应时间: ${response.responseTimeMs}ms")

                            if (response.contentType != null) {
                                appendLine("Content-Type: ${response.contentType}")
                            }

                            appendLine()
                            appendLine("=== 响应头 ===")
                            response.headers.forEach { (name, values) ->
                                values.forEach { value ->
                                    appendLine("  $name: $value")
                                }
                            }

                            if (response.body != null) {
                                appendLine()
                                appendLine("=== 响应体 ===")
                                appendLine(response.body)
                            }
                        }
                    )
                },
                onFailure = { error ->
                    AgentToolResult(
                        content = textProvider.string(
                            R.string.agent_tool_send_http_request_failed,
                            error.message ?: "未知错误"
                        ),
                        isError = true
                    )
                }
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_send_http_request_failed,
                    e.message ?: "未知错误"
                ),
                isError = true
            )
        }
    }

    private data class HttpRequestParams(
        val url: String,
        val method: String,
        val headers: String?,
        val body: String?,
        val content_type: String?
    )
}
