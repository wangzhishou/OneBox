package com.shifenmiao.ai.agent.tool

import com.google.gson.Gson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.ToolFunctionDef
import com.shifenmiao.model.event.PermissionRequest
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.ai.agent.callback.ToolCallback
import com.shifenmiao.ai.agent.tool.expression.AgentToolExpressionValidationResult
import com.shifenmiao.ai.agent.tool.expression.AgentToolExpressionValidator
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Agent 工具注册表 —— 工具的发现、查找与执行中枢。
 *
 * 核心设计：
 * 1. 通过 Hilt @IntoMap 注入 Map<String, Provider<AgentTool>>，实现工具懒加载：
 *    未被调用的工具不会创建实例，节省内存。
 * 2. 提供 getToolDefinitions() 转换为 OpenAI API 的 tools 参数格式。
 * 3. executeTool() 负责统一注入执行上下文、执行前表达式校验与结果截断（4096 字符）。
 * 4. isToolCallSupported() 判断当前 Provider 是否支持 Function Calling 协议。
 *
 * 使用示例：
 * ```
 * val tools = registry.getToolDefinitions()       // 构建 API 请求
 * val result = registry.executeTool("get_time", "{}") // 执行工具
 * ```
 */
@Singleton
class AgentToolRegistry @Inject constructor(
    private val toolProviders: Map<String, @JvmSuppressWildcards Provider<AgentTool>>,
    private val expressionValidator: AgentToolExpressionValidator,
    private val gson: Gson,
) {
    companion object {
        /** 默认工具返回结果最大字符数，超出部分截断 */
        private const val DEFAULT_MAX_RESULT_LENGTH = 4096

        /** 发现/枚举类工具的结果截断阈值（通常返回大量候选数据） */
        private const val DISCOVERY_MAX_RESULT_LENGTH = 8192

        /** 发现类工具名称集合 */
        val DISCOVERY_TOOL_NAMES = setOf(
            "discover_tools",
            "discover_apps"
        )
    }

    /**
     * 工具定义缓存。
     * 仅在工具真正被请求时才构建对应 definition，避免选中工具很少时仍初始化全量 schema。
     */
    private val cachedToolDefinitions = mutableMapOf<String, ToolDefinition>()

    /** 获取所有已注册工具的名称列表 */
    fun getRegisteredToolNames(): List<String> = toolProviders.keys.toList()

    /** 检查是否有任何已注册工具 */
    fun hasTools(): Boolean {
        return toolProviders.isNotEmpty()
    }

    /**
     * 将已注册工具转换为 OpenAI API 的 ToolDefinition 列表。
     * 用于填充 ChatCompletionRequest.tools 字段。
     */
    /**
     * 将已注册工具转换为 OpenAI API 的 ToolDefinition 列表。
     * 结果会被缓存，单次对话内工具列表不变时无需重复创建。
     */
    fun getToolDefinitions(allowedNames: Set<String>? = null): List<ToolDefinition> {
        val targetNames = if (allowedNames == null) {
            toolProviders.keys
                .filter { name -> toolProviders[name]?.get()?.visibleToUser != false }
                .toSet()
        } else {
            resolveToolNames(allowedNames)
        }
        return targetNames.mapNotNull(::getOrCreateToolDefinition)
    }

    fun resolveToolNames(toolNames: Set<String>): Set<String> {
        if (toolNames.isEmpty()) return emptySet()
        val resolved = LinkedHashSet<String>()
        val visiting = mutableSetOf<String>()

        fun visit(name: String) {
            if (name !in toolProviders || !resolved.add(name)) return
            if (!visiting.add(name)) return
            toolProviders[name]
                ?.get()
                ?.dependencies
                .orEmpty()
                .forEach(::visit)
            visiting.remove(name)
        }

        toolNames.forEach(::visit)
        return resolved
    }

    /** 清除工具定义缓存（工具注册变化时调用） */
    fun invalidateToolDefinitionsCache() {
        cachedToolDefinitions.clear()
    }

    /**
     * 判断指定引擎是否支持 Function Calling（tool_calls 协议）。
     * 由 AiEngine.supportToolCalls 字段驱动，支持服务端远程配置。
     * 不支持的引擎将降级为普通聊天，不发送 tools 参数。
     */
    fun isToolCallSupported(engine: AiEngine): Boolean {
        return engine.model.supportToolCalls
    }

    /**
     * 判断指定工具是否为交互式工具（需要用户输入）。
     * 用于 AgentLoopExecutor/AIChatComponent 决定是否交由全局交互宿主承接。
     */
    fun isInteractiveTool(toolName: String): Boolean {
        val provider = toolProviders[toolName] ?: return false
        return provider.get() is InteractiveAgentTool
    }

    /** 判断工具是否存在于注册表。 */
    fun containsTool(toolName: String): Boolean = toolName in toolProviders

    // ── 工具元数据缓存 ─────────────────────────────────────────────────────
    // [ToolCatalogItem] 的所有字段在编译期就确定 ([AgentTool] 接口默认值 + override),
    // 无运行时可变状态, 完全可以一次性构建并缓存. 避免每次 [getToolCatalogItem] /
    // [getToolCatalogItems] / [getVisibleTools] 都触发 [Provider.get] 实例化全部工具 +
    // 注入 Hilt 依赖图.
    //
    // 实例化只发生在 [catalogItems] 首次访问时 (lazy 块退出后, 工具实例可被 GC 回收),
    // 后续查询均为 O(1) / O(n) 内存操作, 不再触碰 [toolProviders].
    //
    // 执行路径 ([executeTool] / [validatePreExecutionExpression] 等) 仍按需实例化,
    // 与目录查询彻底解耦.
    //
    // ⚠️ 预热提醒: 首次访问会触发 ~60 次 [Provider.get] (含 Hilt 依赖图解析),
    // 主线程上首次访问会引入卡顿. 请在后台线程 (例如 [App.onCreate] 里 launch 一个
    // [Dispatchers.Default] 协程) 调用 [warmUp] 预热, 主线程上任何目录查询都是 O(1).
    private val catalogItems: Map<String, ToolCatalogItem> by lazy {
        buildMap(toolProviders.size) {
            for (name in toolProviders.keys.sorted()) {
                val provider = toolProviders[name] ?: continue
                val item = buildCatalogItem(name, provider) ?: continue
                put(name, item)
            }
        }
    }

    /**
     * 预热 [catalogItems] / [cachedCatalogVersion] 缓存.
     *
     * 调用后会立即触发 [Provider.get] 实例化全部工具 (含 Hilt 依赖图解析),
     * 因此**必须从后台线程调用**, 否则会卡主线程.
     *
     * 推荐挂载点: `App.onCreate()` 启动一个 [Dispatchers.Default] 协程调一次即可.
     * 预热完成后, 主线程上的所有目录查询都是 O(1) Map lookup.
     *
     * 线程安全: 内部 [lazy] 默认 [LazyThreadSafetyMode.SYNCHRONIZED],
     * 多个线程同时调用 [warmUp] / 任意目录查询方法, 只会有一处真正实例化.
     */
    fun warmUp() {
        // 触达 lazy 字段即可; 读 [cachedCatalogVersion] 是最便宜的方式
        cachedCatalogVersion
    }

    /**
     * 提取工具的目录元数据.
     *
     * 本方法在 [catalogItems] lazy 块中被调用, 只在注册表首次访问时执行.
     * 返回后 `tool` 局部引用即出作用域, 工具实例可被 GC; 只保留 [ToolCatalogItem] 数据.
     */
    private fun buildCatalogItem(name: String, provider: Provider<AgentTool>): ToolCatalogItem? {
        val tool = provider.get()
        return tool.toCatalogItem(isInteractive = tool is InteractiveAgentTool)
    }

    /**
     * 获取工具的目录元数据。
     * 目录层和授权层通过该方法读取工具声明信息.
     *
     * O(1) 查表, 不触发任何 [Provider.get].
     */
    fun getToolCatalogItem(toolName: String): ToolCatalogItem? = catalogItems[toolName]

    /**
     * 获取全部工具的目录元数据快照, 按名称排序.
     *
     * 返回的列表是不可变快照, 不触发任何 [Provider.get].
     */
    fun getToolCatalogItems(): List<ToolCatalogItem> =
        catalogItems.values.sortedBy { it.name }

    /**
     * 缓存的 catalog 版本号, 由 [catalogItems] 派生.
     *
     * 与原实现一致: 所有工具 [ToolCatalogItem.name] hash + [ToolCatalogItem.version] 之和.
     * 工具实现列表变化时 (新增/删除/升级) 该值会变, 调用方可据此失效缓存.
     */
    fun getCatalogVersion(): Int = cachedCatalogVersion

    private val cachedCatalogVersion: Int by lazy {
        catalogItems.entries.sumOf { (name, item) -> name.hashCode() + item.version }
    }

    // ── 目录查询 ───────────────────────────────────────────────────────────
    // 工具目录的唯一权威来源是这里的 in-memory 注册表.
    // 历史上有 ToolCatalogRepository 把目录序列化到 DB 缓存, 但
    // 1) [ToolCatalogItem] 全部字段在编译期就确定, 无运行时可变状态;
    // 2) DB round-trip 会丢失 per-mode 信息 (如 [ChatWorkingMode] 集合退化为 Boolean);
    // 3) 注册表最多几十个工具, 重建 O(n) 远低于一次 IO 查询.
    // 因此下面的查询全部走 in-memory, 不再依赖 DB.

    /** 工具中心可见工具 (visibleToUser = true). */
    fun getVisibleTools(): List<ToolCatalogItem> =
        getToolCatalogItems().filter { it.visibleToUser }

    /**
     * 获取工具目录, 默认只返回用户可见工具; [includeHidden] = true 时包含隐藏工具 (供系统类工具自身使用).
     */
    fun getTools(includeHidden: Boolean = false): List<ToolCatalogItem> =
        if (includeHidden) {
            getToolCatalogItems()
        } else {
            getVisibleTools()
        }

    /** 按名称取工具目录项, 找不到返回 null. */
    fun getToolByName(name: String): ToolCatalogItem? = getToolCatalogItem(name)

    /**
     * 按依赖链展开 [toolNames]: 给定集合 + 每个工具的 dependencies 闭包, 去重保序.
     * 用于 discover_tools / agent 循环里动态扩展首轮 tools 时, 自动补齐依赖.
     */
    fun resolveDependencies(toolNames: Collection<String>): List<String> {
        if (toolNames.isEmpty()) return emptyList()
        val toolMap = getToolCatalogItems().associateBy { it.name }
        val resolved = LinkedHashSet<String>()
        val visiting = mutableSetOf<String>()

        fun visit(name: String) {
            if (name !in toolMap || !resolved.add(name)) return
            if (!visiting.add(name)) return
            toolMap[name]?.dependencies.orEmpty().forEach(::visit)
            visiting.remove(name)
        }

        toolNames.forEach(::visit)
        return resolved.toList()
    }

    /**
     * 多条件目录搜索: 关键词模糊匹配 (title / summary / keywords), 可叠加 category 过滤和名称白名单.
     * 纯 in-memory 操作; 工具量级 O(n) 远低于一次 IO, 无需缓存.
     */
    fun queryTools(
        query: String = "",
        category: ToolCategory? = null,
        limit: Int = Int.MAX_VALUE,
        onlyNames: Set<String>? = null,
        includeHidden: Boolean = false
    ): List<ToolCatalogItem> {
        val normalizedQuery = query.trim()
        val tools = getTools(includeHidden = includeHidden)
        return tools.asSequence()
            .filter { tool -> onlyNames == null || tool.name in onlyNames }
            .filter { tool -> category == null || tool.category == category }
            .filter { tool ->
                normalizedQuery.isBlank() || matchesQuery(tool, normalizedQuery)
            }
            .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
            .take(limit.coerceAtLeast(1))
            .toList()
    }

    private fun matchesQuery(tool: ToolCatalogItem, query: String): Boolean {
        return tool.name.contains(query, ignoreCase = true) ||
            tool.title.contains(query, ignoreCase = true) ||
            tool.summary.contains(query, ignoreCase = true) ||
            tool.description.contains(query, ignoreCase = true) ||
            tool.keywords.any { it.contains(query, ignoreCase = true) } ||
            tool.examples.any { it.contains(query, ignoreCase = true) }
    }

    fun getExecutionPolicy(toolName: String, arguments: String = ""): AgentToolExecutionPolicy? {
        val tool = toolProviders[toolName]?.get() ?: return null
        return AgentToolExecutionPolicy(
            toolName = tool.name,
            requiresLogin = tool.requiresLogin,
            requiredPermissions = tool.requiredPermissions,
            permissionRequest = tool.permissionRequest,
            requiresConfirmation = tool.shouldRequireConfirmation(arguments),
            confirmationTitle = tool.confirmationTitle,
            confirmationToolPresentation = tool.confirmationToolPresentation,
            parallelizable = tool.parallelizable,
            isInteractive = tool is InteractiveAgentTool
        )
    }

    fun validatePreExecutionExpression(
        toolName: String,
        arguments: String
    ): AgentToolResult? {
        val tool = toolProviders[toolName]?.get() ?: return null
        return validateBeforeExecution(tool, arguments)
    }

    /**
     * 按名称执行指定工具。
     *
     * - Provider.get() 实现按需实例化（懒加载）
     * - 统一执行 [AgentTool.preExecutionExpression] 客户端私有校验
     * - 统一构建 [AgentToolExecutionContext]
     * - 结果超过 4096 字符自动截断
     *
     * @param toolName 工具名称
     * @param arguments LLM 传入的 JSON 参数字符串
     * @return 工具执行结果
     */
    suspend fun executeTool(
        toolName: String,
        arguments: String,
        toolCallId: String? = null,
        interactionOwnerId: String? = null
    ): AgentToolResult {
        val provider = toolProviders[toolName]
            ?: return AgentToolResult(
                content = "Tool '$toolName' not found in registry",
                isError = true
            )

        return try {
            val tool = provider.get()
            validateBeforeExecution(tool, arguments)?.let { return it }
            val executionContext = buildExecutionContext(
                toolCallId = toolCallId,
                interactionOwnerId = interactionOwnerId
            )
            val executed = if (tool is ContextAwareAgentTool) {
                tool.execute(arguments, executionContext)
            } else {
                tool.execute(arguments)
            }
            truncateResultIfNeeded(
                maybeInjectDeepLinks(tool, executed),
                toolName = toolName
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = "Tool execution failed: ${e.message ?: "Unknown error"}",
                isError = true
            )
        }
    }

    /**
     * callback 子工具也统一走这里，避免再绕过 executionContext。
     */
    suspend fun executeToolWithCallback(
        toolName: String,
        arguments: String,
        callback: ToolCallback,
        toolCallId: String? = null,
        interactionOwnerId: String? = null
    ): AgentToolResult {
        val provider = toolProviders[toolName]
            ?: return AgentToolResult(
                content = "Tool '$toolName' not found in registry",
                isError = true
            )

        return try {
            val tool = provider.get()
            validateBeforeExecution(tool, arguments)?.let { return it }
            val executionContext = buildExecutionContext(
                toolCallId = toolCallId,
                interactionOwnerId = interactionOwnerId
            )
            val executed = when {
                tool is ContextAwareCallbackAgentTool -> {
                    tool.execute(arguments, executionContext, callback)
                }

                tool is ContextAwareAgentTool -> {
                    tool.execute(arguments, executionContext)
                }

                else -> {
                    tool.execute(arguments, callback)
                }
            }
            truncateResultIfNeeded(
                maybeInjectDeepLinks(tool, executed),
                toolName = toolName
            )
        } catch (e: Exception) {
            AgentToolResult(
                content = "Tool execution failed: ${e.message ?: "Unknown error"}",
                isError = true
            )
        }
    }

    /**
     * 将工具声明的静态 deepLinks 与 result JSON 中动态返回的 deepLinks 合并。
     *
     * 行为：
     * - 工具失败时不注入（无意义）。
     * - 工具未声明 deepLinks 且 result 不含 deepLinks 字段 → 原样返回（零开销）。
     * - result 是 JSON：合并两个列表并写入 `deepLinks` 字段（保留工具自带的字段顺序）。
     * - result 是纯文本：若工具声明了静态 deepLinks，追加 markdown 链接 `[label](uri)`。
     */
    private fun maybeInjectDeepLinks(
        tool: AgentTool,
        result: AgentToolResult,
    ): AgentToolResult {
        if (result.isError) return result
        val staticLinks = tool.deepLinks
        val content = result.content

        val trimmed = content.trimStart()
        return if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
            injectIntoJsonResult(content, staticLinks)
        } else if (staticLinks.isNotEmpty()) {
            val markdownLinks = staticLinks.joinToString(separator = "\n\n") { link ->
                "[${link.label}](${link.uri})"
            }
            result.copy(content = if (content.isBlank()) markdownLinks else "$content\n\n$markdownLinks")
        } else {
            result
        }
    }

    private fun injectIntoJsonResult(
        content: String,
        staticLinks: List<ToolDeepLink>,
    ): AgentToolResult {
        return runCatching {
            val element = gson.fromJson(content, com.google.gson.JsonObject::class.java) ?: return@runCatching null
            val existing = parseDeepLinks(element)
            if (existing.isEmpty() && staticLinks.isEmpty()) return@runCatching null
            val merged = staticLinks + existing
            element.add("deepLinks", gson.toJsonTree(merged))
            AgentToolResult(content = gson.toJson(element), isError = false)
        }.getOrNull() ?: AgentToolResult(content = content, isError = false)
    }

    private fun parseDeepLinks(element: com.google.gson.JsonObject): List<ToolDeepLink> {
        if (!element.has("deepLinks")) return emptyList()
        return runCatching {
            val arr = element.getAsJsonArray("deepLinks")
            gson.fromJson(arr, Array<ToolDeepLink>::class.java)?.toList().orEmpty()
        }.getOrElse { emptyList() }
    }

    private fun buildExecutionContext(
        toolCallId: String? = null,
        interactionOwnerId: String? = null
    ): AgentToolExecutionContext {
        return AgentToolExecutionContext(
            toolCallId = toolCallId,
            interactionOwnerId = interactionOwnerId
        )
    }

    private fun validateBeforeExecution(
        tool: AgentTool,
        arguments: String
    ): AgentToolResult? {
        val expression = tool.preExecutionExpression
        if (expression.isBlank()) return null

        return when (val result = expressionValidator.validate(expression, arguments)) {
            AgentToolExpressionValidationResult.Allowed -> null
            is AgentToolExpressionValidationResult.Denied -> {
                AgentToolResult(
                    content = result.reason,
                    isError = true
                )
            }
        }
    }

    private fun truncateResultIfNeeded(result: AgentToolResult, toolName: String = ""): AgentToolResult {
        val maxLength = resolveMaxLength(toolName)
        return if (result.content.length > maxLength) {
            result.copy(
                content = result.content.take(maxLength) + "\n...[result truncated: ${result.content.length} chars total, showing first $maxLength chars]"
            )
        } else {
            result
        }
    }

    private fun resolveMaxLength(toolName: String): Int {
        if (toolName in DISCOVERY_TOOL_NAMES) return DISCOVERY_MAX_RESULT_LENGTH
        val provider = toolProviders[toolName] ?: return DEFAULT_MAX_RESULT_LENGTH
        val toolMax = provider.get().maxResultLength
        return if (toolMax > 0) toolMax else DEFAULT_MAX_RESULT_LENGTH
    }

    private fun getOrCreateToolDefinition(name: String): ToolDefinition? {
        cachedToolDefinitions[name]?.let { return it }
        val tool = toolProviders[name]?.get() ?: return null
        return ToolDefinition(
            function = ToolFunctionDef(
                name = name,
                description = tool.description,
                parameters = tool.parametersSchema
            )
        ).also { cachedToolDefinitions[name] = it }
    }
}

data class AgentToolExecutionPolicy(
    val toolName: String,
    val requiresLogin: Boolean = false,
    val requiredPermissions: List<String> = emptyList(),
    val permissionRequest: PermissionRequest = PermissionRequest.ALL,
    val requiresConfirmation: Boolean = false,
    val confirmationTitle: String = "",
    val confirmationToolPresentation: String = "",
    val parallelizable: Boolean = true,
    val isInteractive: Boolean = false
)

