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

class BrowserNavigateTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_navigate"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_navigate)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_navigate_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_navigate_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_navigate_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_navigate_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_navigate_param_action),
                enum = listOf(
                    "open_url", "go_back", "go_forward", "reload",
                    "new_tab", "switch_tab", "close_tab"
                )
            ),
            "url" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_navigate_param_url)
            ),
            "tab_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_navigate_param_tab_id)
            )
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val params = if (arguments.isBlank()) BrowserNavigateParams() else {
                gson.fromJson(arguments, BrowserNavigateParams::class.java)
            }
            when (params.action?.trim()) {
                "open_url" -> {
                    val url = params.url?.trim().orEmpty()
                    if (url.isBlank()) {
                        return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_browser_navigate_missing_url),
                            isError = true
                        )
                    }
                    val result = automationService.navigateToUrl(url)
                    formatResult(result)
                }
                "go_back" -> formatResult(automationService.goBack())
                "go_forward" -> formatResult(automationService.goForward())
                "reload" -> formatResult(automationService.reload())
                "new_tab" -> {
                    val result = automationService.createTab(params.url.orEmpty())
                    formatResult(result)
                }
                "switch_tab" -> {
                    val tabId = params.tab_id?.trim().orEmpty()
                    if (tabId.isBlank()) {
                        return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_browser_navigate_missing_tab_id),
                            isError = true
                        )
                    }
                    formatResult(automationService.switchTab(tabId))
                }
                "close_tab" -> {
                    val tabId = params.tab_id?.trim().orEmpty()
                    if (tabId.isBlank()) {
                        return AgentToolResult(
                            content = textProvider.string(R.string.agent_tool_browser_navigate_missing_tab_id),
                            isError = true
                        )
                    }
                    formatResult(automationService.closeTab(tabId))
                }
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_browser_navigate_unknown_action,
                        params.action.orEmpty()
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_navigate_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private fun formatResult(result: com.shifenmiao.interfaces.browser.BrowserActionResult): AgentToolResult {
        return AgentToolResult(
            content = gson.toJson(
                mapOf(
                    "tool" to name,
                    "success" to result.success,
                    "message" to result.message
                ) + result.data
            ),
            isError = !result.success
        )
    }

    private data class BrowserNavigateParams(
        val action: String? = null,
        val url: String? = null,
        val tab_id: String? = null
    )
}
