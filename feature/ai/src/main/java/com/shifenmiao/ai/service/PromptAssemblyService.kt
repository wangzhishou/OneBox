package com.shifenmiao.ai.service

import com.google.gson.JsonParser
import com.shifenmiao.ai.agent.AgentLoopExecutor
import com.shifenmiao.ai.agent.tool.ToolFilterContext
import com.shifenmiao.ai.agent.tool.ToolPredicate
import com.shifenmiao.ai.agent.tool.builtin.MemoryGetTool
import com.shifenmiao.ai.agent.tool.builtin.MemoryWriteTool
import com.shifenmiao.ai.agent.tool.builtin.UseSkillTool
import com.shifenmiao.ai.component.ToolConfigResolver
import com.shifenmiao.ai.memory.MemoryRepository
import com.shifenmiao.ai.prompt.SystemPromptRepository
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.skill.SkillRepository
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
 * - ToolPredicate 谓词链收口在本类内部（[buildRequestTools] 与
 *   [buildFollowUpToolsAfterDiscovery] 两条产出工具集的路径统一过链），
 *   避免只在某一条调用链过滤时被另一条路径绕过
 *
 * 注：因为依赖 ToolConfigResolver（每个组件实例独立构造），无法作为 Hilt 单例注入，
 * 由 AIChatComponent 手动构造并传给 AgentLoopOrchestrator。
 */
class PromptAssemblyService(
    private val agentLoopExecutor: AgentLoopExecutor,
    private val toolConfigResolver: ToolConfigResolver,
    private val systemPromptRepository: SystemPromptRepository,
    private val agentToolRegistry: AgentToolRegistry,
    private val memoryRepository: MemoryRepository,
    private val skillRepository: SkillRepository,
    private val toolPredicates: Set<ToolPredicate> = emptySet(),
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
        // 隐式系统工具（visibleToUser = false）：不进工具中心、不参与 bootstrap、
        // 不受 selectedToolNames 约束，只受全局 + 会话两个开关控制。
        // 强制并集放在最后，保证老会话（已有 policy 行）也能用上。
        // 模型不支持 Function Calling 时不并集（正文无法加载，发了也用不上）。
        val implicitToolNames = buildList {
            if (!effectiveConfig.toolsSupported) return@buildList
            if (effectiveConfig.memoryEnabled) {
                add(MemoryWriteTool.TOOL_NAME)
                add(MemoryGetTool.TOOL_NAME)
            }
            if (effectiveConfig.skillsEnabled) {
                add(UseSkillTool.TOOL_NAME)
            }
        }
        val toolNames = (effectiveConfig.boundToolNames.orEmpty() + selectedNames + implicitToolNames).distinct()

        if (toolNames.isEmpty()) return null
        val tools = agentLoopExecutor.toolRegistry.getToolDefinitions(toolNames.toSet())
        // 谓词链收口：产出最终请求工具集前统一过一遍（云端协议谓词全部放行，集不变）
        return applyToolPredicates(tools).takeIf { it.isNotEmpty() }
    }

    /**
     * 谓词链筛选：对候选工具逐个过 [toolPredicates]，全部通过才保留。
     * 判定基于工具名 + 注册表已缓存的目录元数据（O(1) 查表），
     * 不触发 AgentTool 实例化 —— 每个 Agent 轮次要判定上百个候选，
     * 实例化路径会走一遍 Hilt 注入图，正是注册表目录缓存要避免的事。
     * 云端协议下 ProtocolToolPredicate 一律放行，云端引擎最终工具集与改造前一致。
     */
    private suspend fun applyToolPredicates(tools: List<ToolDefinition>): List<ToolDefinition> {
        if (tools.isEmpty() || toolPredicates.isEmpty()) return tools
        // resolve() 命中请求级快照（buildRequestTools 刚走过同一缓存），无额外 DB 开销
        val effectiveConfig = toolConfigResolver.resolve()
        val conversation = toolConfigResolver.currentConversation()
        val context = ToolFilterContext(
            conversation = conversation,
            workingMode = effectiveConfig.policy.workingMode,
            protocol = conversation.engine.requestProtocol,
            boundToolNames = effectiveConfig.boundToolNames,
            selectedToolNames = effectiveConfig.policy.selectedToolNames,
            memoryEnabled = effectiveConfig.memoryEnabled,
            skillsEnabled = effectiveConfig.skillsEnabled,
            toolsSupported = effectiveConfig.toolsSupported,
        )
        return tools.filter { definition ->
            val name = definition.function.name
            toolPredicates.all { predicate ->
                predicate.isToolVisible(name, agentToolRegistry.getToolCatalogItem(name), context)
            }
        }
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

        // 记忆/技能注入与 planInjection 同构：门控判定后取 fragment，关闭即不注入。
        // 模型不支持工具调用时：SKILLS 层不注入（正文无法加载，清单无意义）；
        // MEMORY 层保留但省略提示不带 memory_get 指引。
        val memoryFragment = if (effectiveToolConfig.memoryEnabled) {
            memoryRepository.buildPromptFragment(
                tokenBudget = promptBudget,
                includeSearchHint = effectiveToolConfig.toolsSupported
            )
        } else {
            null
        }
        val skillsFragment = if (effectiveToolConfig.skillsEnabled && effectiveToolConfig.toolsSupported) {
            skillRepository.buildPromptFragment(promptBudget)
        } else {
            null
        }

        val composition = systemPromptRepository.composeConversationPrompt(
            conversation = baseConversation,
            workingMode = effectiveToolConfig.policy.workingMode,
            taskPrompt = planInjection,
            memoryFragment = memoryFragment,
            skillsFragment = skillsFragment,
            tokenBudget = promptBudget
        )
        // droppedLayers 无任何消费方，层被整层丢弃时这里留一条排查日志
        if (composition.droppedLayers.isNotEmpty()) {
            "buildEffectiveConversation: budget=$promptBudget " +
                "droppedLayers=${composition.droppedLayers.map { "${it.key}(p=${it.priority})" }}"
                .makeLog("PromptAssembly")
        }
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
        val discoveryToolNames = agentToolRegistry.getDiscoveryToolNames()
        val calledDiscoveryTools = results
            .map { (call, _) -> call.function.name }
            .filter { it in discoveryToolNames }
            .toSet()

        // Step 2: 从发现类工具的成功结果中提取推荐的工具名称
        // 校验范围使用工具目录，支持动态扩展未在策略中的工具
        val catalogNames = agentToolRegistry.getVisibleTools()
            .map { it.name }
            .toSet()
        val discoveredNames = results
            .filter { (call, result) ->
                call.function.name in discoveryToolNames && !result.isError
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
            addAll(discoveryToolNames.filter { it in catalogNames })            // 保留发现类工具
            addAll(expandedNames)                                               // 追加新发现的工具
        }

        if (mergedNames.isEmpty()) return currentTools

        val mergedTools = agentLoopExecutor.toolRegistry.getToolDefinitions(mergedNames)
            .takeIf { it.isNotEmpty() }
            ?: return currentTools

        // 谓词链收口：发现类工具扩展活跃集同样过链（扩展的是名字集合，name-based
        // 谓词天然适用），避免端侧白名单被 discover_tools 路径绕过。
        // 过滤后为空说明新增项全部被谓词拦下，维持当前工具集即可。
        val filteredTools = applyToolPredicates(mergedTools)
        if (filteredTools.isEmpty()) return currentTools

        // Trace: 记录工具扩展情况，便于调试动态扩展行为
        val addedNames = mergedNames - currentNames
        if (addedNames.isNotEmpty()) {
            "buildFollowUpToolsAfterDiscovery: current=${currentNames.size} " +
                "added=${addedNames} merged=${filteredTools.size}"
                .makeLog("PromptAssembly")
        }

        return filteredTools
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
