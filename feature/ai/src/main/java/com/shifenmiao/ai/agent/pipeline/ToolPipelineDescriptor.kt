package com.shifenmiao.ai.agent.pipeline

/**
 * 工具链式调用描述符 —— 描述工具之间的依赖和执行顺序。
 *
 * 当前 Agent Loop 通过 discover_tools/discover_apps → execute 的隐式模式实现工具链，
 * 此模型将链路关系显式化，为后续更复杂的多步骤编排奠定基础。
 *
 * **WIP**: 当前仅作为设计蓝图，尚未集成到 AgentLoopOrchestrator 或 AgentLoopExecutor。
 * ToolPipelineRegistry 不会被任何生产代码调用。
 *
 * 设计目标（对标 Claude/OpenCode 的 tool chaining）：
 * 1. 声明式描述工具之间的输入/输出依赖
 * 2. 支持条件分支（根据上一步结果决定下一步工具）
 * 3. 支持并行执行组（无依赖的工具可并行）
 * 4. 可序列化，支持从配置或 DB 加载
 */
@Deprecated("WIP: Pipeline orchestration not yet wired into AgentLoop", level = DeprecationLevel.WARNING)
data class ToolPipelineDescriptor(
    /** Pipeline 唯一标识 */
    val id: String,
    /** Pipeline 名称（用于日志和 UI 展示） */
    val name: String,
    /** 描述 */
    val description: String = "",
    /** 步骤列表（按执行顺序排列） */
    val steps: List<PipelineStep> = emptyList(),
    /** 触发条件（哪些工具调用可以触发此 pipeline） */
    val triggerToolNames: Set<String> = emptySet(),
)

/**
 * Pipeline 中的单个步骤。
 */
sealed class PipelineStep {
    abstract val id: String
    abstract val toolName: String

    /** 单工具执行步骤 */
    data class Single(
        override val id: String,
        override val toolName: String,
        /** 步骤描述 */
        val description: String = "",
        /** 是否可选（失败时不中断 pipeline） */
        val optional: Boolean = false,
        /** 结果截断策略 */
        val truncationPolicy: TruncationPolicy = TruncationPolicy.DEFAULT,
    ) : PipelineStep()

    /** 并行执行组（组内工具无依赖，可并行） */
    data class ParallelGroup(
        override val id: String,
        override val toolName: String = "__parallel_group__",
        /** 并行执行的工具列表 */
        val tools: List<Single> = emptyList(),
        /** 是否要求全部成功 */
        val requireAll: Boolean = false,
    ) : PipelineStep()

    /** 条件分支（根据上一步结果选择不同工具） */
    data class Conditional(
        override val id: String,
        override val toolName: String = "__conditional__",
        /** 条件分支列表 */
        val branches: List<Branch> = emptyList(),
        /** 默认分支（无条件匹配时执行） */
        val defaultTool: Single? = null,
    ) : PipelineStep()
}

/**
 * 条件分支描述。
 */
data class Branch(
    /** 条件表达式（简单匹配：上一步结果包含指定关键词） */
    val condition: String,
    /** 匹配时执行的步骤 */
    val step: PipelineStep.Single,
)

/**
 * 结果截断策略。
 */
enum class TruncationPolicy {
    /** 默认策略（4096 字符，智能截断） */
    DEFAULT,
    /** 不截断（适用于关键数据） */
    NONE,
    /** 只保留摘要（适用于大量文本） */
    SUMMARY_ONLY,
    /** 自定义字符限制 */
    CUSTOM,
}

/**
 * Pipeline 注册表 —— 管理所有可用的 pipeline 定义。
 *
 * 目前作为内存态注册表，后续可迁移到 DB 配置。
 */
@Suppress("DEPRECATION")
object ToolPipelineRegistry {

    private val pipelines = mutableMapOf<String, ToolPipelineDescriptor>()

    fun register(pipeline: ToolPipelineDescriptor) {
        pipelines[pipeline.id] = pipeline
    }

    fun getById(id: String): ToolPipelineDescriptor? = pipelines[id]

    fun findByTriggerTool(toolName: String): List<ToolPipelineDescriptor> {
        return pipelines.values.filter { toolName in it.triggerToolNames }
    }

    fun getAll(): List<ToolPipelineDescriptor> = pipelines.values.toList()

    /**
     * 注册内置 pipeline。
     * 在 App 启动时调用。
     */
    fun registerBuiltins() {
        // 示例：discover_tools → execute pipeline
        register(
            ToolPipelineDescriptor(
                id = "discover_and_execute",
                name = "Discover & Execute",
                description = "Discover available tools, then execute the recommended ones",
                triggerToolNames = setOf("discover_tools", "discover_apps"),
                steps = listOf(
                    PipelineStep.Single(
                        id = "discover",
                        toolName = "discover_tools",
                        description = "Discover available tools based on user intent",
                        truncationPolicy = TruncationPolicy.SUMMARY_ONLY,
                    ),
                    PipelineStep.Conditional(
                        id = "execute_discovered",
                        branches = listOf(
                            Branch(
                                condition = "recommendedTools",
                                step = PipelineStep.Single(
                                    id = "execute",
                                    toolName = "__dynamic__",
                                    description = "Execute discovered tools",
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
