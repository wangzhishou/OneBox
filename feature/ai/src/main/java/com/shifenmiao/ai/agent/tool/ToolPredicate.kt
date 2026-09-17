package com.shifenmiao.ai.agent.tool

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ToolCatalogItem

/**
 * 工具谓词 —— 借鉴 Google ADK 0.7 ToolPredicate 的可组合工具筛选。
 *
 * 应用点收拢在 [com.shifenmiao.ai.service.PromptAssemblyService] 内部：
 * buildRequestTools 产出最终工具集前、buildFollowUpToolsAfterDiscovery
 * 扩展活跃集时各过一遍谓词链，全部谓词通过才保留进请求 tools 参数。
 * 通过 Hilt @IntoSet 注入（见 di/ToolPredicateModule），新增筛选规则
 * 只需新增一个谓词类并注册，无需改动现有优先级链。
 *
 * 签名为 name-based：谓词只拿到工具名 + 注册表已缓存的目录元数据
 * ([ToolCatalogItem]，O(1) 内存查表)，不触发 [AgentTool] 实例化。
 * 每个 Agent 轮次要判定的候选工具有上百个，若传实例意味着每轮走一遍
 * Hilt 注入图，正是注册表目录缓存要避免的事。
 * [catalogItem] 为 null 表示目录中查不到元数据（如未注册的隐式工具），
 * 是否放行由各谓词自行决定（见各实现的 fail-open / fail-closed 语义）。
 */
fun interface ToolPredicate {

    /** 返回 true 表示该工具在当前上下文中可见（可进入请求工具集） */
    suspend fun isToolVisible(
        toolName: String,
        catalogItem: ToolCatalogItem?,
        context: ToolFilterContext
    ): Boolean
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
    ToolPredicate { toolName, catalogItem, context ->
        isToolVisible(toolName, catalogItem, context) &&
            other.isToolVisible(toolName, catalogItem, context)
    }

fun ToolPredicate.or(other: ToolPredicate): ToolPredicate =
    ToolPredicate { toolName, catalogItem, context ->
        isToolVisible(toolName, catalogItem, context) ||
            other.isToolVisible(toolName, catalogItem, context)
    }

fun ToolPredicate.not(): ToolPredicate =
    ToolPredicate { toolName, catalogItem, context ->
        !isToolVisible(toolName, catalogItem, context)
    }
