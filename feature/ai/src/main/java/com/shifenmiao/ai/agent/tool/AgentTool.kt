package com.shifenmiao.ai.agent.tool

import com.shifenmiao.ai.agent.callback.ToolCallback
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel

/**
 * 工具中心展示元数据 —— 服务于 UI 目录、搜索和默认绑定策略。
 *
 * 所有属性都有合理默认值，简单工具无需覆盖。
 */
interface ToolCatalogMetadata {
    /** 工具在 UI 中的展示标题，默认使用 name */
    val title: String
        get() = ""

    /** 一句话摘要，用于工具中心列表展示 */
    val summary: String
        get() = ""

    /** 工具分类，影响默认启用策略和目录分组。SYSTEM 分类的工具默认发给 LLM */
    val category: ToolCategory
        get() = ToolCategory.BUSINESS

    /** 搜索关键词，用于工具发现和用户检索 */
    val keywords: List<String>
        get() = emptyList()

    /** 使用示例，用于工具中心展示和搜索匹配 */
    val examples: List<String>
        get() = emptyList()

    /** 依赖的其他工具名称，注册表会自动补齐依赖链 */
    val dependencies: List<String>
        get() = emptyList()

    /**
     * 是否首轮注入到 LLM。
     *
     * 例如 discover_tools / discover_apps 这类路由/发现工具会进入首轮，
     * 普通业务工具则默认不进入首轮，等待 discover_tools / discover_apps 或用户显式授权后再放开。
     */
    val bootstrap: Boolean
        get() = false

    /** 是否在用户可见的工具中心目录中展示。false 表示隐藏但对 LLM 仍可用 */
    val visibleToUser: Boolean
        get() = true

    /**
     * 是否为发现/路由类工具（如 discover_tools / discover_apps）。
     *
     * 发现类工具的搜索结果会在 Agent Loop 中触发 follow-up 工具扩展
     * （见 PromptAssemblyService.buildFollowUpToolsAfterDiscovery），
     * 且通常需要更大的结果截断阈值（通过 [ToolExecutionConfig.maxResultLength] 声明）。
     */
    val isDiscoveryTool: Boolean
        get() = false

    /** 排序权重，越小越靠前 */
    val sortOrder: Int
        get() = 0

    /** 工具版本号，catalogVersion 变化时触发数据库同步 */
    val version: Int
        get() = 1
}

/**
 * 工具安全与权限策略 —— 服务于执行前校验、权限申请和确认弹窗。
 *
 * 这些属性仅在客户端执行链路中使用，不进入 LLM tools schema。
 */
interface ToolSecurityPolicy {
    /** 执行前是否需要用户确认，用于敏感或危险操作 */
    val requiresConfirmation: Boolean
        get() = false

    /**
     * 是否需要确认（支持按当前参数动态决定）。
     *
     * 默认沿用静态 [requiresConfirmation]，
     * 仅在同一个工具内部不同 action 需要不同确认策略时重写。
     */
    fun shouldRequireConfirmation(arguments: String): Boolean = requiresConfirmation

    /**
     * 客户端私有登录检查标志。
     *
     * 当工具需要用户登录才能执行时，设置为 true。
     * 执行前会通过 [AgentToolLoginChecker] 检查用户登录状态。
     */
    val requiresLogin: Boolean
        get() = false

    /** 风险等级，影响工具中心的视觉标识和确认策略 */
    val riskLevel: ToolRiskLevel
        get() = ToolRiskLevel.SAFE

    /**
     * 客户端私有的执行前表达式。
     *
     * 该字段只在 Android 端工具执行前校验，不进入 tool catalog、system prompt 或 LLM tools 描述。
     * 表达式由受限 DSL 解析器执行，禁止 Kotlin Script / 反射 / 动态代码。
     *
     * 支持示例：
     * - args.path != null && args.path != ""
     * - args.quality >= 1 && args.quality <= 100
     */
    val preExecutionExpression: String
        get() = ""

    /**
     * 客户端私有 Android 权限列表。
     *
     * 只用于工具执行前的客户端权限申请，不进入 Prompt / tools schema。
     */
    val requiredPermissions: List<String>
        get() = permissionRequest.permissions.toList()

    /**
     * 客户端私有权限请求类型，用于复用全局权限说明与系统申请流程。
     */
    val permissionRequest: PermissionRequest
        get() = PermissionRequest.ALL

    /** 客户端私有确认弹窗标题。 */
    val confirmationTitle: String
        get() = ""

    /** 客户端私有确认弹窗动作描述。 */
    val confirmationToolPresentation: String
        get() = ""
}

/**
 * 工具执行后的用户引导动作 —— 控制工具成功后向用户/UI 提供的可操作入口。
 *
 * 两类 deep link 来源：
 * 1. **静态声明**：覆盖 [deepLinks] 返回固定列表（适合单一目标场景，如主题设置）。
 * 2. **动态注入**：在 result JSON 的 `deepLinks` 数组里返回（适合带 id 等动态参数的目标）。
 *
 * 框架在 [AgentToolRegistry.executeTool] 末尾把两种来源合并并写入 result.content，
 * 既供 LLM 读取（可在最终回复中自然引用 `[标题](onebox://...)` 形式的 markdown 链接），
 * 也供 UI 渲染为可点击的跳转卡片。
 */
interface ToolResultActions {
    /**
     * 工具执行成功后，固定展示的 deep link 列表。
     *
     * 适合不依赖 result 数据的单一目标场景。
     * 动态 deeplink（带 id 等参数）请直接在 result JSON 的 `deepLinks` 数组里返回。
     */
    val deepLinks: List<ToolDeepLink>
        get() = emptyList()
}

/**
 * 工具执行配置 —— 控制结果截断和并行策略。
 */
interface ToolExecutionConfig {
    /**
     * 工具结果最大字符数。0 或负值表示使用默认值 (4096)。
     * 发现/枚举类工具可返回更大值（如 8192）。
     */
    val maxResultLength: Int
        get() = 0

    /**
     * 单次执行超时毫秒数。0 或负值表示使用全局默认
     * （远程配置 agentToolTimeoutSeconds,未下发时本地兜底 60 秒）。
     * 耗时型工具（如 AI 生成图片、大文件处理）应返回更大的值。
     */
    val executionTimeoutMs: Long
        get() = 0L

    /**
     * 是否允许与同一轮中其他工具并行执行。
     *
     * 默认 true:大多数纯查询、读取类工具都是无副作用的,可以并行。
     * 仅当工具:
     *  - 修改共享资源 (DB 写、设置改动)
     *  - 依赖前序工具的副作用 (顺序敏感)
     *  - 触发互斥的 UI / 原生 picker
     * 时改为 false。
     *
     * Registry 在构造 [AgentToolExecutionPolicy] 时透传,
     * AgentLoopExecutor 据此决定单轮 tool_calls 走串行还是并发。
     * 注意:requiresConfirmation / requiresLogin / interactive / 非空 requiredPermissions
     * 一律视为不可并行,无需在这里手动声明。
     */
    val parallelizable: Boolean
        get() = true

    /**
     * 失败重试策略，默认 [RetryPolicy.NONE] 不重试。
     *
     * 已接入执行层：AgentLoopExecutor 在工具返回 isError=true 时按该策略重试
     * （每次尝试独立计时，重试间隔用可取消的 delay），重试不算新的 tool call
     * 轮次，只在最终结果出来后触发一次完成/持久化回调。
     *
     * 注意：requiresConfirmation / 交互式 / requiresLogin / 声明了非空权限的工具
     * 在执行层被强制跳过重试（避免重复弹窗/重复权限申请/交互重入），
     * 即使这里声明了 policy 也不会生效。
     */
    val retryPolicy: RetryPolicy
        get() = RetryPolicy.NONE
}

/**
 * 工具重试策略声明。
 *
 * 已接入执行层：通过 [ToolExecutionConfig.retryPolicy] 声明，
 * AgentLoopExecutor 在工具返回 isError=true 时按策略重试。
 * 安全约束：requiresConfirmation / 交互式 / requiresLogin / 声明了非空权限的
 * 工具在执行层被强制跳过重试，即使声明了 policy。
 *
 * @param maxRetries 最大重试次数（0 = 不重试）
 * @param delayMs 重试间隔毫秒数（0 = 立即重试），用可取消的 delay 实现
 * @param retryableErrors 可重试的错误匹配串，空集表示所有错误都可重试
 */
data class RetryPolicy(
    val maxRetries: Int = 0,
    val delayMs: Long = 0,
    val retryableErrors: Set<String> = emptySet(),
) {
    /**
     * 判断一次失败结果是否可重试。
     *
     * 匹配规则：[retryableErrors] 为空集 → 所有错误可重试；
     * 非空 → 任一条目（忽略大小写）包含于错误内容 [errorContent] 即视为可重试。
     * 由于 [AgentToolResult] 仅有 content/isError，匹配基于 content 文本，
     * 工具可在 content 中使用固定前缀/错误码（如 "timeout"、"HTTP 429"）提高匹配精度。
     */
    fun isRetryable(errorContent: String): Boolean {
        if (retryableErrors.isEmpty()) return true
        return retryableErrors.any { errorContent.contains(it, ignoreCase = true) }
    }

    companion object {
        /** 不重试（默认） */
        val NONE = RetryPolicy()

        /** 网络请求常用：最多 2 次重试，间隔 1 秒 */
        val NETWORK_DEFAULT = RetryPolicy(maxRetries = 2, delayMs = 1000)

        /** 外部 API 常用：最多 1 次重试，间隔 500ms */
        val API_DEFAULT = RetryPolicy(maxRetries = 1, delayMs = 500)
    }
}

/**
 * AI Agent 工具抽象接口。
 *
 * 每个工具实现此接口后通过 Hilt @IntoMap 注册到 [AgentToolRegistry]，
 * 即可被 Agent Loop 按需发现和调用。
 *
 * 工具生命周期由 Hilt Provider 管理：未被调用时不会实例化（懒加载）。
 *
 * 支持两种执行方式：
 * 1. 简单执行：execute(arguments) - 适用于不需要回调的工具
 * 2. 回调执行：execute(arguments, callback) - 适用于需要嵌套调用或 Screen 导航的工具
 *
 * 接口按职责拆分为三个子接口：
 * - [ToolCatalogMetadata] — 工具中心 UI 展示、搜索和默认绑定
 * - [ToolSecurityPolicy] — 权限检查、确认弹窗、风险等级
 * - [ToolExecutionConfig] — 结果截断、并行策略
 *
 * 使用示例：
 * ```
 * // 简单工具
 * class GetCurrentTimeTool @Inject constructor() : AgentTool {
 *     override val name = "get_current_time"
 *     override val description = "获取当前时间"
 *     override val parametersSchema = ToolParameters()
 *     override suspend fun execute(arguments: String) = AgentToolResult("2026-04-05T12:00:00+08:00")
 * }
 *
 * // 回调工具（支持嵌套调用和 Screen 导航）
 * class SelectFileTool @Inject constructor() : AgentTool {
 *     override val name = "select_file"
 *     override val description = "让用户选择文件"
 *     override val parametersSchema = ToolParameters()
 *
 *     override suspend fun execute(arguments: String): AgentToolResult {
 *         return AgentToolResult("需要回调支持", isError = true)
 *     }
 *
 *     override suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
 *         val result = callback.navigateToScreen<FileBrowserResult> { onResult ->
 *             Screen.FileBrowser(onResult = onResult)
 *         }
 *         return AgentToolResult("用户选择了: ${result?.selectedUri}")
 *     }
 * }
 * ```
 */
interface AgentTool : ToolCatalogMetadata, ToolSecurityPolicy, ToolExecutionConfig, ToolResultActions {

    // ── 执行引擎核心属性 ──────────────────────────────

    /** 工具唯一标识符，与 OpenAI function name 对应 */
    val name: String

    /** 工具功能描述，发送给 LLM 以辅助决策 */
    val description: String

    /** 参数 JSON Schema，使用 ToolParameters 结构 */
    val parametersSchema: ToolParameters

    // ── ToolCatalogMetadata 默认值（引用核心属性）──────────

    /** 工具在 UI 中的展示标题，默认使用 [name] */
    override val title: String
        get() = name

    /** 一句话摘要，默认使用 [description] */
    override val summary: String
        get() = description

    /**
     * 执行工具逻辑（简单版本）。
     *
     * 适用于不需要嵌套调用或 Screen 导航的工具。
     * 需要回调能力的工具应重写 [execute(arguments, callback)] 方法。
     *
     * @param arguments LLM 返回的 JSON 格式参数字符串
     * @return 工具执行结果
     */
    suspend fun execute(arguments: String): AgentToolResult

    /**
     * 执行工具逻辑（回调版本）。
     *
     * 支持嵌套调用和 Screen 导航的工具应重写此方法。
     * 默认实现直接委托给 [execute(arguments)]。
     *
     * @param arguments LLM 返回的 JSON 格式参数字符串
     * @param callback 工具回调接口，支持 [ToolCallback.callTool] 和 [ToolCallback.navigateToScreen]
     * @return 工具执行结果
     */
    suspend fun execute(arguments: String, callback: ToolCallback): AgentToolResult {
        return execute(arguments)
    }

    /**
     * 将工具的执行元数据转换为目录项。
     * 目录层只关心工具能力声明，不直接持有执行器实例。
     */
    fun toCatalogItem(isInteractive: Boolean = this is InteractiveAgentTool): ToolCatalogItem {
        return ToolCatalogItem(
            name = name,
            title = title,
            summary = summary,
            description = description,
            category = category,
            keywords = keywords,
            examples = examples,
            dependencies = dependencies,
            bootstrap = bootstrap,
            visibleToUser = visibleToUser,
            isDiscoveryTool = isDiscoveryTool,
            requiresConfirmation = requiresConfirmation,
            isInteractive = isInteractive,
            riskLevel = riskLevel,
            sortOrder = sortOrder,
            version = version
        )
    }
}
