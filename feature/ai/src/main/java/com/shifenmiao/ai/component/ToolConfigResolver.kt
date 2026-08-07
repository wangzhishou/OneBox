package com.shifenmiao.ai.component

import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.ai.service.PromptTemplateToolService
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ConversationToolPolicy

/**
 * 工具配置解析器 —— 从 AgentLoopOrchestrator 中抽离。
 *
 * 职责：
 * 1. 解析当前会话的有效工具配置（[EffectiveToolConfig]）
 * 2. 请求级缓存，避免同一请求内多次 DB 查询
 * 3. 工作模式快照，防止请求过程中的竞态条件
 * 4. 工具绑定同步 (AGENT / PROMPT 持久化场景)
 *
 * 缓存生命周期：
 * - [snapshot] 在请求开始时调用，缓存配置
 * - [clearCache] 在请求结束或模式变更时调用
 *
 * 首轮默认策略 (统一为 [AgentTool] 的 [com.shifenmiao.model.ai.tool.ToolCatalogItem.bootstrapModes] 兜底):
 * - boundToolNames (AGENT override / PROMPT scope) 有 → 用它
 * - 否则 → 所有 visible tools 中 `defaultWorkingMode in bootstrapModes` 的子集
 * - 首次访问时通过 [shouldBootstrapDefaults] 把这份默认集烘焙进
 *   [com.shifenmiao.model.ai.tool.ConversationToolPolicy.selectedToolNames]
 *
 * 历史上曾有 CHAT / ASSISTANT entry-scoped default (基于 [ToolCategory.SYSTEM] 过滤),
 * 现已删除: "SYSTEM 分类" 不再用于首轮默认, 它仅是工具中心的展示分组.
 */
class ToolConfigResolver(
    private val resolveBoundToolNames: suspend () -> Set<String>?,
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository,
    private val promptTemplateToolService: PromptTemplateToolService,
    private val agentToolRegistry: AgentToolRegistry,
    private val toolBindingRepository: ToolBindingRepository,
    private val conversationProvider: () -> Conversation,
) {
    data class EffectiveToolConfig(
        val policy: ConversationToolPolicy,
        val boundToolNames: Set<String>?
    )

    /**
     * 请求级快照缓存。
     *
     * 仅由 [snapshot] 写入，[resolve] 读取。
     * [buildToolCenterUiState] 等 UI 读取路径直接调用 [resolveFresh] 绕过缓存，
     * 确保始终拿到最新 DB 状态。
     */
    @Volatile
    private var cachedConfig: EffectiveToolConfig? = null

    /**
     * 解析有效工具配置。有请求级快照时直接返回缓存值，否则从 DB 实时计算。
     *
     * 优先级链：boundToolNames → promptScopedToolNames → bootstrapModes (in-memory)
     */
    suspend fun resolve(): EffectiveToolConfig {
        cachedConfig?.let { return it }
        return resolveFresh()
    }

    /**
     * 始终从 DB / in-memory 实时计算有效配置，忽略且不影响缓存。
     *
     * UI 展示路径（如 buildToolCenterUiState）应调用此方法，
     * 避免复用请求级快照导致展示过期数据。
     */
    suspend fun resolveFresh(): EffectiveToolConfig {
        val conversation = conversationProvider()
        val boundToolNames = resolveBoundToolNames()
        val defaultWorkingMode = conversationToolPolicyRepository.effectiveDefaultWorkingMode(conversation)
        val promptScopedToolNames = if (boundToolNames == null) {
            promptTemplateToolService.getPromptScopedToolNames(conversation.promptId)
        } else {
            null
        }
        if (boundToolNames == null && promptScopedToolNames != null) {
            promptTemplateToolService.ensurePromptDefaultsEnabled(conversation = conversation)
        }
        // 首轮默认集:
        //   - 有显式 binding (AGENT override / PROMPT scope) → 用 binding
        //   - 否则 → in-memory 工具中 `workingMode in bootstrapModes` 的子集
        // 不再有 "SYSTEM 分类" 特殊通道; [ToolCategory.SYSTEM] 仅为工具中心 UI 分组.
        val defaultEnabledToolNames = when {
            boundToolNames != null -> boundToolNames.toList().sorted()
            promptScopedToolNames != null -> promptScopedToolNames.toList().sorted()
            else -> {
                agentToolRegistry.getVisibleTools()
                    .filter { defaultWorkingMode in it.bootstrapModes }
                    .map { it.name }
                    .sorted()
            }
        }
        val storedPolicy = conversationToolPolicyRepository.getPolicy(conversation)
        val shouldBootstrapDefaults = conversation.id.isNotBlank() &&
            defaultEnabledToolNames.isNotEmpty() &&
            storedPolicy == null
        if (shouldBootstrapDefaults) {
            conversationToolPolicyRepository.savePolicy(
                conversation = conversation,
                policy = ConversationToolPolicy(
                    workingMode = defaultWorkingMode,
                    selectedToolNames = defaultEnabledToolNames
                )
            )
        }
        val effectiveStoredPolicy = storedPolicy
            ?: conversationToolPolicyRepository.getPolicy(conversation)
        val policy = effectiveStoredPolicy ?: ConversationToolPolicy(
            workingMode = defaultWorkingMode,
            selectedToolNames = defaultEnabledToolNames
        )
        return EffectiveToolConfig(
            policy = policy,
            boundToolNames = boundToolNames ?: promptScopedToolNames
        )
    }

    /**
     * 快照当前配置并缓存。在请求开始时调用，
     * 整个请求生命周期内 [resolve] 复用同一配置，防止竞态条件。
     */
    suspend fun snapshot(): EffectiveToolConfig {
        cachedConfig?.let { return it }
        val config = resolveFresh()
        cachedConfig = config
        return config
    }

    /** 清除缓存。在请求结束或工作模式变更时调用。 */
    fun clearCache() {
        cachedConfig = null
    }

    /**
     * 同步当前 scope 的默认绑定（用户在工具中心手动开关工具时调用）。
     *
     * 只服务于 AGENT / PROMPT (它们的 binding 是首轮默认的来源之一).
     * CHAT / ASSISTANT 已统一走 [bootstrapModes] 兜底, 不再持久化.
     */
    suspend fun syncDefaultBindings(conversation: Conversation, enabledToolNames: List<String>) {
        when (conversation.entryType) {
            com.shifenmiao.model.ai.AIConversationEntryType.AGENT -> {
                conversation.entryRefId?.toIntOrNull()?.let { agentId ->
                    toolBindingRepository.replaceAgentBindings(agentId, enabledToolNames)
                }
            }
            com.shifenmiao.model.ai.AIConversationEntryType.PROMPT -> {
                val promptId = conversation.promptId
                    ?: conversation.entryRefId?.toIntOrNull()
                promptId?.let { toolBindingRepository.replacePromptBindings(it, enabledToolNames) }
            }
            else -> Unit
        }
    }
}
