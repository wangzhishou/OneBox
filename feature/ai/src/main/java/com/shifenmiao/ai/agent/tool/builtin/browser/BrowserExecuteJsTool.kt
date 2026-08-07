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

class BrowserExecuteJsTool @Inject constructor(
    private val automationService: BrowserAutomationService,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider
) : AgentTool {

    override val name: String = "browser_execute_js"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_browser_execute_js)

    override val title: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_summary)

    override val category: ToolCategory = ToolCategory.NETWORK

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_browser_execute_js_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_browser_execute_js_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SENSITIVE

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_confirm_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_browser_execute_js_confirm_presentation)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "script" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_browser_execute_js_param_script)
            )
        ),
        required = listOf("script")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        if (!automationService.isAvailable.value) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_browser_not_available),
                isError = true
            )
        }
        return try {
            val params = if (arguments.isBlank()) BrowserExecuteJsParams() else {
                gson.fromJson(arguments, BrowserExecuteJsParams::class.java)
            }
            val script = params.script?.trim().orEmpty()
            if (script.isBlank()) {
                return AgentToolResult(
                    content = textProvider.string(R.string.agent_tool_browser_execute_js_missing_script),
                    isError = true
                )
            }
            val result = automationService.executeJavaScript(script)
            AgentToolResult(
                content = gson.toJson(
                    mapOf(
                        "tool" to name,
                        "executed" to true,
                        "result" to result
                    )
                )
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_browser_execute_js_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    private data class BrowserExecuteJsParams(
        val script: String? = null
    )
}
