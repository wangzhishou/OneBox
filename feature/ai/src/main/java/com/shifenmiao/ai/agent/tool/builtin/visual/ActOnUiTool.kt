package com.shifenmiao.ai.agent.tool.builtin.visual

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.automation.AIAction
import com.shifenmiao.model.automation.AutomationResult
import com.wanbaohe.visual.automation.service.VisualAutomationService
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.JsonParser

/**
 * act_on_ui - 在当前 Activity 上执行单个触摸动作。
 *
 * 入参复用 [AIAction.parse] 的视觉自动化 JSON 协议:
 * {"action":"click","x":320,"y":640,"reason":"tap confirm"}
 * 这样 LLM 在 screenshot_ui 看到的图后,直接复用同一套坐标语义调本工具,零协议切换成本。
 *
 * 风险:
 * - 直接在用户屏幕上模拟触摸、输入文字、返回上一页。
 * - 不可逆操作(可能触发删除/发送/购买等),必须 requiresConfirmation = true。
 * - 触摸操作互斥,必须 parallelizable = false,避免与同轮其他触摸工具并发。
 */
@Singleton
class ActOnUiTool @Inject constructor(
    private val service: VisualAutomationService,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "act_on_ui"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_act_on_ui)

    override val title: String =
        textProvider.string(R.string.agent_tool_act_on_ui_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_act_on_ui_summary)

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_act_on_ui_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_act_on_ui_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val maxResultLength: Int = 512

    override val parametersSchema = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_act_on_ui_param_action
                )
            ),
        ),
        required = listOf("action"),
    )

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_act_on_ui_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_act_on_ui_summary)

    override suspend fun execute(arguments: String): AgentToolResult {
        val actionJson = extractActionJson(arguments)
        val action = AIAction.parse(actionJson)
        if (action is AIAction.Error) {
            return AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_act_on_ui_parse_failed,
                    action.message,
                ),
                isError = true,
            )
        }

        val result = service.executeAction(action)
        return when (result) {
            is AutomationResult.Success -> AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_act_on_ui_success,
                    describeAction(action),
                ),
            )
            is AutomationResult.Failure -> AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_act_on_ui_failed,
                    result.message,
                ),
                isError = true,
            )
        }
    }

    /**
     * 兼容 LLM 两种常见写法：
     * 1. 直接把 JSON 对象作为 `action` 参数传入：{"action":"wait",...}
     * 2. 把 JSON 对象字符串化后再作为 `action` 参数传入：{"action":"{\"action\":\"wait\",...}"}
     */
    private fun extractActionJson(arguments: String): String {
        val trimmed = arguments.trim()
        if (!trimmed.startsWith("{")) return trimmed
        return runCatching {
            val root = JsonParser.parseString(trimmed)
            if (!root.isJsonObject) return@runCatching trimmed
            val actionElement = root.asJsonObject.get("action")
            when {
                actionElement == null -> trimmed
                actionElement.isJsonObject -> actionElement.toString()
                actionElement.isJsonPrimitive -> {
                    val actionString = actionElement.asString.trim()
                    if (actionString.startsWith("{")) actionString else trimmed
                }
                else -> trimmed
            }
        }.getOrDefault(trimmed)
    }

    private fun describeAction(action: AIAction): String = when (action) {
        is AIAction.Click -> "click(${action.x}, ${action.y})"
        is AIAction.LongPress -> "long_press(${action.x}, ${action.y}) ${action.durationMs}ms"
        is AIAction.Swipe -> "swipe(${action.fromX},${action.fromY}) -> (${action.toX},${action.toY})"
        is AIAction.InputText -> "input_text(${action.text})"
        is AIAction.GoBack -> "go_back"
        is AIAction.Wait -> "wait ${action.durationMs}ms"
        is AIAction.Done -> "done(${action.message})"
        is AIAction.Error -> "error(${action.message})"
    }
}