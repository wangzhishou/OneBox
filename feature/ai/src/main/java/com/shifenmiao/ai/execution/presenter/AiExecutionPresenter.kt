package com.shifenmiao.ai.execution.presenter

import android.content.Context
import android.util.LruCache
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shifenmiao.ai.agent.ToolCallRecord
import com.shifenmiao.ai.component.AgentToolCallUIState
import com.shifenmiao.ai.execution.model.AiExecutionPhase
import com.shifenmiao.ai.execution.model.AiExecutionUiModel
import com.shifenmiao.ai.execution.model.DeepLinkItemUiModel
import com.shifenmiao.ai.execution.model.ExecutionStepStatus
import com.shifenmiao.ai.execution.model.ExecutionStepUiModel
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.utils.appContext
import java.util.Collections

object AiExecutionPresenter {

    fun present(status: AgentToolCallUIState, context: Context): AiExecutionUiModel {
        return when (status) {
            AgentToolCallUIState.Idle -> AiExecutionUiModel()
            is AgentToolCallUIState.Planning -> {
                AiExecutionUiModel(
                    phase = AiExecutionPhase.RUNNING,
                    title = context.getString(R.string.ai_execution_running_title),
                    summary = context.getString(R.string.agent_tool_starting),
                    currentStepTitle = context.getString(R.string.ai_execution_step_prepare),
                    steps = status.steps.ifEmpty {
                        listOf(
                            ExecutionStepUiModel(
                                id = "plan_tools",
                                title = context.getString(R.string.ai_execution_step_prepare),
                                status = ExecutionStepStatus.RUNNING,
                                isSystemStep = true
                            )
                        )
                    }
                )
            }
            is AgentToolCallUIState.Executing -> {
                val currentStep = status.steps.firstOrNull { it.status == ExecutionStepStatus.RUNNING }
                    ?: status.steps.firstOrNull { it.status == ExecutionStepStatus.PENDING }
                AiExecutionUiModel(
                    phase = AiExecutionPhase.RUNNING,
                    title = context.getString(R.string.ai_execution_running_title),
                    summary = currentStep?.title?.let {
                        context.getString(R.string.ai_execution_current_step, it)
                    },
                    currentStepTitle = currentStep?.title,
                    progressText = status.steps.progressText(context),
                    steps = status.steps
                )
            }

            is AgentToolCallUIState.WaitingUserInput -> {
                val waitingInput = status.requestType == "INPUT"
                val currentStep = status.steps.firstOrNull {
                    it.status == ExecutionStepStatus.WAITING_USER
                } ?: status.steps.firstOrNull { it.status == ExecutionStepStatus.RUNNING }
                AiExecutionUiModel(
                    phase = AiExecutionPhase.WAITING_USER_ACTION,
                    title = context.getString(
                        if (waitingInput) {
                            R.string.ai_execution_waiting_input_title
                        } else {
                            R.string.ai_execution_waiting_user_title
                        }
                    ),
                    summary = context.getString(
                        if (waitingInput) {
                            R.string.ai_execution_waiting_input_summary
                        } else {
                            R.string.ai_execution_waiting_user_summary
                        }
                    ),
                    currentStepTitle = currentStep?.title ?: status.toolName,
                    progressText = status.steps.progressText(context),
                    steps = status.steps,
                    primaryActionLabel = context.getString(R.string.ai_execution_action_continue),
                    secondaryActionLabel = context.getString(R.string.ai_execution_action_cancel)
                )
            }

            is AgentToolCallUIState.WaitingLLM -> {
                val currentStep = status.steps.firstOrNull { it.status == ExecutionStepStatus.RUNNING }
                AiExecutionUiModel(
                    phase = AiExecutionPhase.WAITING_FINAL_RESPONSE,
                    title = context.getString(R.string.ai_execution_waiting_response_title),
                    summary = context.getString(R.string.ai_execution_waiting_response_summary),
                    currentStepTitle = currentStep?.title,
                    progressText = status.steps.progressText(context),
                    steps = status.steps
                )
            }

            is AgentToolCallUIState.MaxIterationsReached -> {
                AiExecutionUiModel(
                    phase = AiExecutionPhase.PAUSED,
                    title = context.getString(R.string.ai_execution_paused_title),
                    summary = context.getString(
                        R.string.ai_execution_paused_summary_with_iteration,
                        status.iteration,
                        status.maxIterations
                    ),
                    progressText = status.steps.progressText(context),
                    steps = status.steps,
                    primaryActionLabel = context.getString(R.string.ai_execution_action_continue),
                    secondaryActionLabel = context.getString(R.string.ai_execution_action_cancel)
                )
            }
        }
    }

    fun presentHistory(
        toolCallsJson: String,
        context: Context,
        gson: Gson = Gson()
    ): AiExecutionUiModel {
        val records = parseRecords(toolCallsJson, gson)
        if (records.isEmpty()) return AiExecutionUiModel()

        val hasFailure = records.any { it.isError }
        val steps = buildList {
            addAll(records.map { record ->
                val arguments = record.arguments
                    .takeIf { it.isNotBlank() && it != "{}" }
                val result = record.result.takeIf { it.isNotBlank() }
                val debugInfo = buildString {
                    append("tool=")
                    append(record.name)
                    if (record.id.isNotBlank()) {
                        append(" · callId=")
                        append(record.id)
                    }
                }.takeIf { it.isNotBlank() }
                val deepLinks = extractDeepLinks(record.result, gson, context)
                ExecutionStepUiModel(
                    id = record.id,
                    title = ToolExecutionTextResolver.resolveTitle(record.name, record.displayTitle),
                    subtitle = record.displaySummary?.takeIf { it.isNotBlank() },
                    status = if (record.isError) {
                        ExecutionStepStatus.FAILED
                    } else {
                        ExecutionStepStatus.DONE
                    },
                    detail = if (record.isError) record.result.takeIf { it.isNotBlank() } else null,
                    debugInfo = debugInfo,
                    arguments = arguments,
                    result = result,
                    deepLinks = deepLinks,
                )
            })
            // 历史回看时保留“生成回复”步骤，帮助用户理解完整流程
            add(
                ExecutionStepUiModel(
                    id = "final_response",
                    title = context.getString(R.string.ai_execution_step_generate_response),
                    status = if (hasFailure) ExecutionStepStatus.PENDING else ExecutionStepStatus.DONE,
                    isSystemStep = true
                )
            )
        }

        val completedCount = steps.count { it.status == ExecutionStepStatus.DONE }
        val summary = if (hasFailure) {
            context.getString(R.string.ai_execution_failed_summary)
        } else {
            context.getString(R.string.ai_execution_progress_completed, completedCount)
        }
        return AiExecutionUiModel(
            phase = if (hasFailure) AiExecutionPhase.FAILED else AiExecutionPhase.COMPLETED,
            title = context.getString(
                if (hasFailure) R.string.ai_execution_failed_title else R.string.ai_execution_completed_title
            ),
            summary = summary,
            progressText = context.getString(R.string.ai_execution_progress, completedCount, steps.size),
            steps = steps
        )
    }

    /**
     * 从 tool result JSON 里抽取 deepLinks 数组，转为 [DeepLinkItemUiModel] 列表。
     * - 解析失败 / 字段缺失 → 返回空列表
     * - 任何元素的 uri 缺失 → 跳过该元素
     * - guidance 缺失时使用 default 模板 "点击打开：%s"
     */
    private fun extractDeepLinks(
        content: String?,
        gson: Gson,
        context: Context,
    ): List<DeepLinkItemUiModel> {
        if (content.isNullOrBlank()) return emptyList()
        return runCatching {
            val element = gson.fromJson(content, com.google.gson.JsonObject::class.java) ?: return@runCatching emptyList()
            val arr = element.getAsJsonArray("deepLinks") ?: return@runCatching emptyList()
            val defaultTemplate = context.getString(R.string.agent_tool_default_deeplink_guidance)
            arr.mapNotNull { entry ->
                val obj = entry.asJsonObject
                val uri = obj.get("uri")?.asString?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val label = obj.get("label")?.asString?.takeIf { it.isNotBlank() } ?: uri
                val rawGuidance = obj.get("guidance")?.asString?.takeIf { it.isNotBlank() }
                val primary = obj.get("primary")?.asBoolean ?: false
                DeepLinkItemUiModel(
                    uri = uri,
                    label = label,
                    guidance = rawGuidance ?: defaultTemplate.format(label),
                    primary = primary,
                )
            }
        }.getOrElse { emptyList() }
    }

    private fun parseRecords(toolCallsJson: String, gson: Gson): List<ToolCallRecord> {
        if (toolCallsJson.isBlank()) return emptyList()
        return runCatching {
            val type = object : TypeToken<List<ToolCallRecord>>() {}.type
            gson.fromJson<List<ToolCallRecord>>(toolCallsJson, type).orEmpty()
        }.getOrElse { emptyList() }
    }

    private fun List<ExecutionStepUiModel>.progressText(context: Context): String? {
        if (isEmpty()) return null
        val completedCount = count { it.status == ExecutionStepStatus.DONE }
        return context.getString(R.string.ai_execution_progress, completedCount, size)
    }
}

/**
 * Tool 文案解析器：
 * - 优先按约定读取字符串资源 agent_tool_<name>_title，支持多语言
 * - 无对应资源时 fallback 到工具侧传来的 displayTitle
 * - 仍无命中时查最小化常用工具词典
 * - 都未命中时直接展示 toolName（仅做下划线/连字符格式化）
 */
object ToolExecutionTextResolver {
    private val commonToolTitles = mapOf(
        "read_file" to "读取文件",
        "write_file" to "更新文件",
        "apply_patch" to "更新内容",
        "grep_search" to "检索相关内容",
        "semantic_search" to "分析相关代码",
        "file_search" to "查找文件",
        "run_in_terminal" to "执行检查",
        "get_device_info" to "获取设备信息",
        "get_current_time" to "获取当前时间",
        "discover_tools" to "工具发现",
        "discover_apps" to "应用发现",
        "navigate_app_screen" to "页面跳转",
    )

    fun resolveTitle(toolName: String, preferredTitle: String? = null): String {
        val resourceTitle = resolveTitleFromResources(toolName)
        if (resourceTitle != null) return resourceTitle

        preferredTitle?.takeIf { it.isNotBlank() }?.let { return it }
        commonToolTitles[toolName]?.let { return it }
        return toolName
            .trim()
            .ifBlank { "tool" }
            .replace('_', ' ')
            .replace('-', ' ')
    }

    private val resourceTitleCache = LruCache<String, String>(64)
    private val noResourceTools = Collections.synchronizedSet(HashSet<String>())

    private fun resolveTitleFromResources(toolName: String): String? {
        if (toolName.isBlank()) return null
        resourceTitleCache.get(toolName)?.let { return it }
        if (toolName in noResourceTools) return null

        val resId = appContext.resources.getIdentifier(
            "agent_tool_${toolName}_title",
            "string",
            appContext.packageName
        )
        if (resId == 0) {
            noResourceTools.add(toolName)
            return null
        }
        return appContext.getString(resId).also { resourceTitleCache.put(toolName, it) }
    }
}

