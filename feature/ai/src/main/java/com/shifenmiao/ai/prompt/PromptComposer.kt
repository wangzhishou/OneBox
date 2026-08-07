package com.shifenmiao.ai.prompt

import com.shifenmiao.ai.context.TokenEstimator

enum class PromptLayerType(
    val key: String,
    val title: String,
    /** 优先级（数值越小越重要），裁剪时低优先级先被丢弃 */
    val priority: Int = 50
) {
    SYSTEM_RULES(
        key = "system_rules",
        title = "System Rules",
        priority = 0  // 最高优先级，永不裁剪
    ),
    ENVIRONMENT_CONTEXT(
        key = "environment_context",
        title = "Environment Context",
        priority = 60
    ),
    INTERACTION_PROTOCOL(
        key = "interaction_protocol",
        title = "Interaction Protocol",
        priority = 40
    ),
    AGENT_EXECUTION_PROTOCOL(
        key = "agent_execution_protocol",
        title = "Agent Execution Protocol",
        priority = 10
    ),
    AGENT_ROLE(
        key = "agent_role",
        title = "Agent Role",
        priority = 20
    ),
    TASK_PROMPT(
        key = "task_prompt",
        title = "Task Prompt",
        priority = 15
    ),
    USER_OVERRIDE(
        key = "user_override",
        title = "User Override",
        priority = 5  // 用户自定义覆盖，几乎不可裁剪
    )
}

data class PromptLayer(
    val key: String,
    val title: String,
    val content: String,
    val required: Boolean = true,
    /** 优先级（数值越小越重要），默认从 PromptLayerType 继承 */
    val priority: Int = 50
)

fun PromptLayer(
    type: PromptLayerType,
    content: String,
    required: Boolean = true,
    priority: Int = type.priority
): PromptLayer = PromptLayer(
    key = type.key,
    title = type.title,
    content = content,
    required = required,
    priority = priority
)

data class PromptComposition(
    val mergedPrompt: String,
    val layers: List<PromptLayer>,
    /** 被裁剪掉的 layer（预算不足时丢弃的） */
    val droppedLayers: List<PromptLayer> = emptyList()
)

object PromptComposer {

    /**
     * 组合提示词层为最终 prompt。
     *
     * @param layers 所有提示词层
     * @param tokenBudget 可选的 token 预算上限，超出时按优先级裁剪低优先级层
     */
    fun compose(
        layers: List<PromptLayer>,
        tokenBudget: Int = 0
    ): PromptComposition {
        val normalized = layers
            .map { it.copy(content = it.content.trim()) }
            .filter { it.required || it.content.isNotBlank() }

        // 按优先级排序（低数值 = 高优先级先保留）
        val sorted = normalized.sortedBy { it.priority }

        val selected: List<PromptLayer>
        val dropped: List<PromptLayer>

        if (tokenBudget > 0) {
            val selection = selectWithinBudget(sorted, tokenBudget)
            selected = selection.selected
            dropped = selection.dropped
        } else {
            selected = sorted
            dropped = emptyList()
        }

        // 输出时仍按原始定义顺序（保持 prompt 结构可读性）
        val ordered = selected.sortedBy { layer ->
            PromptLayerType.entries.indexOfFirst { it.key == layer.key }
                .takeIf { it >= 0 } ?: Int.MAX_VALUE
        }

        val mergedPrompt = ordered.joinToString(separator = "\n\n") { layer ->
            buildString {
                append("### ")
                append(layer.title)
                append('\n')
                append(layer.content)
                append('\n')
            }
        }

        return PromptComposition(
            mergedPrompt = mergedPrompt,
            layers = ordered,
            droppedLayers = dropped
        )
    }

    /**
     * 在 token 预算内选择 layers，高优先级优先保留。
     * required=true 的层始终保留（即使超标）。
     */
    private fun selectWithinBudget(
        sortedByPriority: List<PromptLayer>,
        tokenBudget: Int
    ): SelectionResult {
        val selected = mutableListOf<PromptLayer>()
        val dropped = mutableListOf<PromptLayer>()
        var usedTokens = 0

        for (layer in sortedByPriority) {
            val layerTokens = TokenEstimator.estimateText(layer.content) + 10 // 10 for title overhead
            if (layer.required || usedTokens + layerTokens <= tokenBudget) {
                selected.add(layer)
                usedTokens += layerTokens
            } else {
                dropped.add(layer)
            }
        }

        return SelectionResult(selected, dropped)
    }

    private data class SelectionResult(
        val selected: List<PromptLayer>,
        val dropped: List<PromptLayer>
    )
}
