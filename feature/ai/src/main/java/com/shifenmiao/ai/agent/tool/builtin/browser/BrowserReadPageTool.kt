package com.shifenmiao.ai.agent.tool.builtin.browser

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

class BrowserReadPageTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_read_page"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_read_page)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_read_page_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_read_page_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_read_page_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_read_page_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val maxResultLength: Int = 16384

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "mode" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_read_page_param_mode),
                enum = listOf("full_text", "dom", "info", "snapshot")
            ),
            "selector" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_read_page_param_selector)
            ),
            "max_length" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(R.string.agent_tool_browser_read_page_param_max_length)
            )
        ),
        required = listOf("mode")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val params = if (arguments.isBlank()) BrowserReadPageParams() else {
                gson.fromJson(arguments, BrowserReadPageParams::class.java)
            }
            when (params.mode?.trim()) {
                "full_text" -> {
                    val maxLength = params.max_length?.takeIf { it > 0 } ?: 8192
                    val text = automationService.extractPageText(maxLength)
                    AgentToolResult(
                        content = gson.toJson(
                            mapOf(
                                "tool" to name,
                                "mode" to "full_text",
                                "content" to text,
                                "truncated" to ((text?.length ?: 0) >= maxLength)
                            )
                        )
                    )
                }
                "dom" -> {
                    val selector = params.selector?.trim()?.takeIf { it.isNotBlank() } ?: "body"
                    val html = automationService.extractDom(selector)
                    AgentToolResult(
                        content = gson.toJson(
                            mapOf(
                                "tool" to name,
                                "mode" to "dom",
                                "selector" to selector,
                                "html" to html,
                                "found" to (html != null)
                            )
                        )
                    )
                }
                "info" -> {
                    val info = automationService.getPageInfo()
                        ?: return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_browser_read_page_no_info),
                            isError = true
                        )
                    AgentToolResult(
                        content = gson.toJson(
                            mapOf(
                                "tool" to name,
                                "mode" to "info",
                                "url" to info.url,
                                "title" to info.title,
                                "canGoBack" to info.canGoBack,
                                "canGoForward" to info.canGoForward,
                                "isLoading" to info.isLoading
                            )
                        )
                    )
                }
                "snapshot" -> {
                    val snapshot = automationService.getSnapshot()
                        ?: return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_browser_read_page_no_info),
                            isError = true
                        )
                    AgentToolResult(
                        content = gson.toJson(
                            mapOf(
                                "tool" to name,
                                "mode" to "snapshot",
                                "url" to snapshot.pageInfo.url,
                                "title" to snapshot.pageInfo.title,
                                "tabCount" to snapshot.tabs.size,
                                "activeTab" to snapshot.tabs.firstOrNull { it.isActive }?.url,
                                "textPreview" to snapshot.textPreview
                            )
                        )
                    )
                }
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_browser_read_page_unknown_mode,
                        params.mode.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_read_page_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private data class BrowserReadPageParams(
        val mode: String? = null,
        val selector: String? = null,
        val max_length: Int? = null
    )
}
