package com.shifenmiao.ai.agent.tool

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode

/**
 * 工具谓词 —— 借鉴 Google ADK 0.7 ToolPredicate 的可组合工具筛选。
 *
 * 在 [com.shifenmiao.ai.component.AgentLoopOrchestrator.buildRequestTools] 中
 * 对候选工具逐个判定，全部谓词通过才保留进请求 tools 参数。
 * 通过 Hilt @IntoSet 注入（见 di/ToolPredicateModule），新增筛选规则
 * 只需新增一个谓词类并注册，无需改动现有优先级链。
 */
fun interface ToolPredicate {

    /** 返回 true 表示该工具在当前上下文中可见（可进入请求工具集） */
    suspend fun isToolVisible(tool: AgentTool, context: ToolFilterContext): Boolean
}

/**
 * 谓词判定上下文 —— 字段取自 [com.shifenmiao.ai.component.ToolConfigResolver.EffectiveToolConfig]
 * 与当前会话，均为 buildRequestTools 链路实际可拿到的信息。
 */
data class ToolFilterContext(
    val conversation: Conversation,
    val workingMode: ChatWorkingMode,
    /** 当前引擎的请求协议，端侧推理为 [AiRequestProtocol.LOCAL_ON_DEVICE] */
    val protocol: AiRequestProtocol,
    val boundToolNames: Set<String>?,
    val selectedToolNames: List<String>,
    val memoryEnabled: Boolean,
    val skillsEnabled: Boolean,
    val toolsSupported: Boolean,
)

fun ToolPredicate.and(other: ToolPredicate): ToolPredicate =
    ToolPredicate { tool, context ->
        isToolVisible(tool, context) && other.isToolVisible(tool, context)
    }

fun ToolPredicate.or(other: ToolPredicate): ToolPredicate =
    ToolPredicate { tool, context ->
        isToolVisible(tool, context) || other.isToolVisible(tool, context)
    }

fun ToolPredicate.not(): ToolPredicate =
    ToolPredicate { tool, context -> !isToolVisible(tool, context) }
