package com.shifenmiao.ai.agent.tool.builtin.browser

import com.google.gson.Gson
import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import javax.inject.Inject

class BrowserScreenshotTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_screenshot"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_screenshot)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_screenshot_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_screenshot_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_screenshot_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_screenshot_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = emptyMap(),
        required = emptyList()
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val pageInfo = automationService.getPageInfo()
            val base64 = automationService.captureScreenshot()
            if (base64 == null) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_browser_screenshot_failed),
                    isError = true
                )
            }
            AgentToolResult(
                content = gson.toJson(
                    mapOf(
                        "tool" to name,
                        "success" to true,
                        "format" to "png",
                        "encoding" to "base64",
                        "url" to (pageInfo?.url ?: ""),
                        "title" to (pageInfo?.title ?: ""),
                        "image_base64" to base64,
                        "size_bytes" to (base64.length * 3 / 4)
                    )
                )
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_screenshot_failed,
                ),
                isError = true
            )
        }
    }
}
