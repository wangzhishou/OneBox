package com.shifenmiao.ai.agent.tool.builtin.visual

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.InteractiveAgentTool
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.automation.AIAction
import com.wanbaohe.visual.automation.service.AutomationTaskResult
import com.wanbaohe.visual.automation.service.AutomationTaskStatus
import com.wanbaohe.visual.automation.service.VisualAutomationService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * automate_ui_task - 一键式 UI 自动化编排器。
 *
 * 内部循环: screenshot → AI decide → execute,直到 done / error / step limit。
 *
 * 实现为 [InteractiveAgentTool]:
 * - 用户启动后必须等任务结束才能继续对话(否则 LLM 拿到半截结果会幻觉)。
 * - 交互式工具无执行超时限制,生命周期跟随用户取消或工具自身结束信号。
 */
@Singleton
class AutomateUiTaskTool @Inject constructor(
    private val service: VisualAutomationService,
    private val textProvider: AgentToolTextProvider,
) : InteractiveAgentTool {

    override val name: String = "automate_ui_task"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_automate_ui_task)

    override val title: String =
        textProvider.string(R.string.agent_tool_automate_ui_task_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_automate_ui_task_summary)

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_automate_ui_task_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_automate_ui_task_examples)

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.DANGEROUS

    override val requiresConfirmation: Boolean = true

    override val parallelizable: Boolean = false

    override val maxResultLength: Int = 4096

    override val confirmationTitle: String =
        textProvider.string(R.string.agent_tool_automate_ui_task_title)

    override val confirmationToolPresentation: String =
        textProvider.string(R.string.agent_tool_automate_ui_task_summary)

    override val parametersSchema = ToolParameters(
        type = "object",
        properties = mapOf(
            "taskDescription" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(
                    R.string.agent_tool_automate_ui_task_param_task_description
                )
            ),
            "maxSteps" to ToolParameterProperty(
                type = "integer",
                description = textProvider.string(
                    R.string.agent_tool_automate_ui_task_param_max_steps
                )
            ),
        ),
        required = listOf("taskDescription"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val params = parseArgs(arguments)
        val taskDescription = params["taskDescription"] as? String
        if (taskDescription.isNullOrBlank()) {
            return AgentToolResult(
                content = "Missing required parameter: taskDescription",
                isError = true,
            )
        }
        val maxSteps = (params["maxSteps"] as? Number)?.toInt()?.coerceIn(1, 100) ?: 20

        return try {
            val result = service.runAutomationTask(taskDescription, maxSteps)
            buildResult(result)
        } catch (t: Throwable) {
            AgentToolResult(
                content = "automate_ui_task failed: ${t.message ?: "unknown"}",
                isError = true,
            )
        }
    }

    private fun buildResult(result: AutomationTaskResult): AgentToolResult {
        val lastActionDescription = result.steps.lastOrNull()?.let { describeAction(it.action) }
            ?: "none"
        val content = when (result.status) {
            AutomationTaskStatus.COMPLETED -> textProvider.string(
                R.string.agent_tool_automate_ui_task_completed,
                result.steps.size,
                result.message.ifBlank { lastActionDescription },
            )
            AutomationTaskStatus.FAILED -> textProvider.string(
                R.string.agent_tool_automate_ui_task_failed,
                result.steps.size,
                result.message,
            )
            AutomationTaskStatus.STEP_LIMIT_REACHED -> textProvider.string(
                R.string.agent_tool_automate_ui_task_step_limit,
                result.steps.size,
                lastActionDescription,
            )
        }
        val isError = result.status != AutomationTaskStatus.COMPLETED
        return AgentToolResult(content = content, isError = isError)
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

    @Suppress("UNCHECKED_CAST")
    private fun parseArgs(arguments: String): Map<String, Any?> {
        if (arguments.isBlank() || arguments == "{}") return emptyMap()
        return runCatching {
            com.google.gson.Gson().fromJson(arguments, Map::class.java) as Map<String, Any?>
        }.getOrElse { emptyMap() }
    }
}