package com.shifenmiao.ai.service

import com.google.gson.JsonParser
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.component.ToolConfigResolver
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.tool.ToolSelectionResult
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.model.ai.ToolCall
import com.t8rin.logger.makeLog

/**
 * 提示词组装服务 —— 从 AgentLoopOrchestrator 中抽离，专职负责：
 * 1. 工具筛选（prepareToolSelection, buildRequestTools）
 * 2. 有效对话 prompt 构建（buildEffectiveConversation）
 * 3. 工具发现后的 follow-up 工具扩展（buildFollowUpToolsAfterDiscovery）
 *
 * 设计原则：
 * - 纯业务逻辑，不持有 UI 状态
 * - 无状态，所有方法均为纯函数或仅依赖注入的单例服务
 * - AgentLoopOrchestrator 通过委托调用此类，自身只负责协调
 *
 * 注：因为依赖 ToolConfigResolver（每个组件实例独立构造），无法作为 Hilt 单例注入，
 * 由 AIChatComponent 手动构造并传给 AgentLoopOrchestrator。
 */
class PromptAssemblyService(
    private val agentLoopExecutor: AgentLoopExecutor,
    private val toolConfigResolver: ToolConfigResolver,
    private val systemPromptRepository: SystemPromptRepository,
    private val agentToolRegistry: AgentToolRegistry,
) {
    fun prepareToolSelection(
        engine: com.shifenmiao.model.ai.AiEngine,
    ): ToolSelectionResult {
        return ToolSelectionResult
    }

    suspend fun buildRequestTools(): List<ToolDefinition>? {
        val effectiveConfig = toolConfigResolver.resolve()
        val allVisibleTools = agentToolRegistry.getVisibleTools()
        if (allVisibleTools.isEmpty()) return null

        // 规则 1: 首轮 = 开启. 规则 2: 绑定 = 开启.
        // 开启集 = 持久化 binding ∪ policy.selectedToolNames.
        // 不再叠加 bootstrap: bootstrap 已在 [ToolConfigResolver.resolveFresh] 的
        // shouldBootstrapDefaults 阶段被烘焙进 policy.selectedToolNames,
        // 用户在工具中心显式清空时不会被 bootstrap 反扑覆盖.
        val selectedNames = effectiveConfig.policy.selectedToolNames
        val toolNames = (effectiveConfig.boundToolNames.orEmpty() + selectedNames).distinct()

        if (toolNames.isEmpty()) return null
        val tools = agentLoopExecutor.toolRegistry.getToolDefinitions(toolNames.toSet())
        return tools.takeIf { it.isNotEmpty() }
    }

    /**
     * 构建带有系统 prompt 的有效对话。
     *
     * @param planInjection PLAN→AGENT 切换时注入的计划文本
     */
    suspend fun buildEffectiveConversation(
        baseConversation: Conversation,
        preResolvedConfig: ToolConfigResolver.EffectiveToolConfig? = null,
        planInjection: String = "",
    ): Conversation {
        val effectiveToolConfig = preResolvedConfig ?: toolConfigResolver.resolve()
        val promptBudget = systemPromptRepository.calculatePromptBudget(baseConversation.engine.model)

        val composition = systemPromptRepository.composeConversationPrompt(
            conversation = baseConversation,
            workingMode = effectiveToolConfig.policy.workingMode,
            taskPrompt = planInjection,
            tokenBudget = promptBudget
        )
        return baseConversation.copy(prompt = composition.mergedPrompt)
    }

    /**
     * 工具发现后扩展 follow-up 工具集（并集去重策略）。
     *
     * 核心逻辑：下一轮可用工具 = 当前轮已有工具 ∪ discover_tools 新发现工具。
     * - 保证工具不丢失：原始工具在后续轮次仍可调用
     * - 保证不重复：通过 LinkedHashSet 按名称去重
     * - 保证安全：仅扩展工具目录中已注册的可见工具
     *
     * 这样 LLM 可以在第1轮通过 discover_tools 搜索工具，
     * 第2轮直接调用新发现的工具，同时继续使用原始工具。
     */
    suspend fun buildFollowUpToolsAfterDiscovery(
        results: List<Pair<ToolCall, AgentToolResult>>,
        currentTools: List<ToolDefinition>?,
    ): List<ToolDefinition>? {
        // Step 1: 识别本轮调用了哪些发现类工具
        val calledDiscoveryTools = results
            .map { (call, _) -> call.function.name }
            .filter { it in AgentToolRegistry.DISCOVERY_TOOL_NAMES }
            .toSet()

        // Step 2: 从发现类工具的成功结果中提取推荐的工具名称
        // 校验范围使用工具目录，支持动态扩展未在策略中的工具
        val catalogNames = agentToolRegistry.getVisibleTools()
            .map { it.name }
            .toSet()
        val discoveredNames = results
            .filter { (call, result) ->
                call.function.name in AgentToolRegistry.DISCOVERY_TOOL_NAMES && !result.isError
            }
            .flatMap { (_, result) -> extractRecommendedToolNames(result.content) }
            .plus(buildImplicitFollowUpToolNames(calledDiscoveryTools))
            .filter { it in catalogNames }
            .distinct()

        if (discoveredNames.isEmpty()) return currentTools

        // Step 3: 当前轮已有工具名称（保留，不丢弃）
        val currentNames = currentTools
            ?.map { it.function.name }
            ?.toSet()
            .orEmpty()

        // Step 4: 新发现工具及其依赖链，校验在目录中存在
        val expandedNames = agentToolRegistry
            .resolveDependencies(discoveredNames)
            .filter { it in catalogNames }

        // Step 5: 并集去重 — 当前工具 ∪ 新发现工具 ∪ 发现类工具本身
        val mergedNames = linkedSetOf<String>().apply {
            addAll(currentNames)                                                // 保留当前轮所有工具
            addAll(AgentToolRegistry.DISCOVERY_TOOL_NAMES.filter { it in catalogNames })           // 保留发现类工具
            addAll(expandedNames)                                               // 追加新发现的工具
        }

        if (mergedNames.isEmpty()) return currentTools

        val mergedTools = agentLoopExecutor.toolRegistry.getToolDefinitions(mergedNames)
            .takeIf { it.isNotEmpty() }
            ?: return currentTools

        // Trace: 记录工具扩展情况，便于调试动态扩展行为
        val addedNames = mergedNames - currentNames
        if (addedNames.isNotEmpty()) {
            "buildFollowUpToolsAfterDiscovery: current=${currentNames.size} " +
                "added=${addedNames} merged=${mergedTools.size}"
                .makeLog("PromptAssembly")
        }

        return mergedTools
    }

    private fun buildImplicitFollowUpToolNames(calledDiscoveryTools: Set<String>): List<String> {
        return buildList {
            if ("discover_apps" in calledDiscoveryTools) add("navigate_app_screen")
        }
    }

    /**
     * 从 discover_tools 响应中提取工具名称。
     * 响应格式：JSON 数组 ["name1", "name2"]（精简格式，不含 title/summary，节省 token）
     */
    private fun extractRecommendedToolNames(content: String): List<String> {
        if (content.isBlank()) return emptyList()
        return runCatching {
            val root = JsonParser.parseString(content)
            // 新格式：JSON 数组 ["name1", "name2"]
            if (root.isJsonArray) {
                root.asJsonArray.mapNotNull { elem ->
                    elem.asString?.takeIf { it.isNotBlank() }
                }
            } else {
                // 兼容旧格式：{"matchedTools": [{"name": "xxx", ...}]}
                val matchedTools = root.asJsonObject.getAsJsonArray("matchedTools")
                    ?: run {
                        "discover_tools result has no 'matchedTools' array"
                            .makeLog("PromptAssembly")
                        return@runCatching emptyList()
                    }
                matchedTools.mapNotNull { element ->
                    element.asJsonObject?.get("name")?.asString?.takeIf { it.isNotBlank() }
                }
            }
        }.onFailure { e ->
            "Failed to parse discover_tools result: ${e.message}"
                .makeLog("PromptAssembly")
        }.getOrDefault(emptyList())
    }
}
