package com.shifenmiao.ai.component

import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import com.shifenmiao.model.ai.unified.LlmMessage
import com.shifenmiao.storage.AIChatStorage
import java.util.concurrent.atomic.AtomicInteger

/**
 * 模式切换管理器 —— 负责工作模式（ASK/PLAN/AGENT）的切换、计划文本捕获和上下文过渡标记注入。
 *
 * 从 AgentLoopOrchestrator 中抽离，职责边界：
 * 1. 模式切换（DB 持久化 + 计划文本快照）
 * 2. PLAN→AGENT 计划文本提取
 * 3. 模式过渡标记注入到 LLM 上下文
 *
 * 并发约束：
 * lastModeTransition 通过 @Volatile 保证可见性，transitionMarkerRemainingInjections
 * 使用 AtomicInteger 保证递减原子性，switchMode 和 injectTransitionMarker
 * 可能在不同协程中调用。
 */
class ModeTransitionManager(
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository,
) {

    /** 最近一次模式切换记录，下次请求时注入上下文分隔标记 */
    @Volatile
    private var lastModeTransition: ModeTransition? = null

    /** 模式标记剩余注入次数（多轮注入策略），AtomicInteger 保证递减原子性 */
    private val transitionMarkerRemainingInjections = AtomicInteger(0)

    /**
     * 切换工作模式。
     *
     * @param conversation 当前会话
     * @param targetMode 目标模式
     * @param currentAnswerText 当前 assistant 回答文本（用于 PLAN→AGENT 时捕获计划）
     * @return 切换结果，如果模式未变化返回 null
     */
    suspend fun switchMode(
        conversation: Conversation,
        targetMode: ChatWorkingMode,
        currentAnswerText: String,
    ): ModeSwitchResult? {
        val currentPolicy = conversationToolPolicyRepository.getPolicy(conversation)
            ?: ConversationToolPolicy(
                workingMode = conversationToolPolicyRepository.effectiveDefaultWorkingMode(conversation)
            )

        if (currentPolicy.workingMode == targetMode) return null

        val previousMode = currentPolicy.workingMode

        conversationToolPolicyRepository.savePolicy(
            conversation = conversation,
            policy = currentPolicy.copy(workingMode = targetMode)
        )

        // 全局记忆用户显式选择的模式，新会话默认沿用
        AIChatStorage.saveLastChatWorkingMode(targetMode)

        val capturedPlan = capturePlanIfNeeded(previousMode, targetMode, currentAnswerText)

        val transition = ModeTransition(
            from = previousMode,
            to = targetMode,
            timestamp = System.currentTimeMillis(),
            capturedPlanText = capturedPlan
        )
        lastModeTransition = transition
        // Phase 4.2: 设置多轮注入计数
        transitionMarkerRemainingInjections.set(TRANSITION_MARKER_MAX_INJECTIONS)

        return ModeSwitchResult(transition = transition)
    }

    /**
     * 提取 PLAN→AGENT 的计划文本注入。
     *
     * 仅当最近一次模式切换是 PLAN→AGENT 时返回非空文本。
     * 将原始计划文本解析为结构化格式，提升 LLM 对计划的理解。
     */
    fun extractPlanForInjection(): String {
        val transition = lastModeTransition ?: return ""
        if (transition.from != ChatWorkingMode.PLAN ||
            transition.to != ChatWorkingMode.AGENT
        ) return ""

        val planText = transition.capturedPlanText
        if (planText.isBlank()) return ""

        val structured = parsePlanStructure(planText)
        return formatPlanForInjection(structured, planText)
    }

    /**
     * 在上下文消息中注入模式切换分隔标记。
     *
     * Phase 4.2 多轮注入策略：
     * 标记注入后保留 [TRANSITION_MARKER_MAX_INJECTIONS] 轮，
     * 确保 LLM 在多轮对话中仍能感知模式变更上下文。
     * 每次注入递减，达到 0 后自动清除。
     */
    fun injectTransitionMarker(messages: MutableList<LlmMessage>) {
        val transition = lastModeTransition ?: return
        if (transitionMarkerRemainingInjections.get() <= 0) {
            lastModeTransition = null
            return
        }

        val markerText = buildModeTransitionText(transition)
        val insertIndex = if (messages.isNotEmpty() && messages[0].role == "system") 1 else 0
        if (insertIndex <= messages.size) {
            messages.add(
                insertIndex,
                LlmMessage.createTextMessage(
                    role = "system",
                    text = markerText
                )
            )
        }

        if (transitionMarkerRemainingInjections.decrementAndGet() <= 0) {
            lastModeTransition = null
        }
    }

    /** 重置模式切换状态（会话重置时调用） */
    fun reset() {
        lastModeTransition = null
        transitionMarkerRemainingInjections.set(0)
    }

    /** PLAN→AGENT 切换时捕获当前计划文本，超出长度限制时截断 */
    private fun capturePlanIfNeeded(
        previousMode: ChatWorkingMode,
        targetMode: ChatWorkingMode,
        currentAnswerText: String,
    ): String {
        if (previousMode != ChatWorkingMode.PLAN || targetMode != ChatWorkingMode.AGENT) return ""
        val trimmed = currentAnswerText.trim()
        if (trimmed.length <= MAX_PLAN_INJECTION_LENGTH) return trimmed
        return trimmed.take(MAX_PLAN_INJECTION_LENGTH) + "\n...[truncated]"
    }

    /**
     * 解析计划文本为结构化数据。
     *
     * 尝试按 Markdown 标题（## / **粗体**）识别 PLAN 模式的标准输出结构：
     * - 目标理解
     * - 当前已知
     * - 关键假设 / 待确认项
     * - 推荐步骤
     * - 风险与依赖
     * - 下一步建议
     */
    private fun parsePlanStructure(text: String): PlanStructure {
        val sections = mutableMapOf<String, String>()
        val lines = text.lines()
        var currentSection: String? = null
        val currentContent = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            val sectionKey = PLAN_SECTION_PATTERNS.entries.firstOrNull { (_, pattern) ->
                pattern.containsMatchIn(trimmed)
            }
            if (sectionKey != null) {
                if (currentSection != null) {
                    sections[currentSection] = currentContent.toString().trim()
                }
                currentSection = sectionKey.key
                currentContent.clear()
                // 保留标题行后面的同行内容
                val afterMarker = trimmed.replaceFirst(STRIP_MARKERS_REGEX, "")
                    .replaceFirst(STRIP_PREFIX_REGEX, "")
                if (afterMarker.isNotBlank()) {
                    currentContent.appendLine(afterMarker)
                }
            } else if (currentSection != null) {
                currentContent.appendLine(line)
            }
        }
        if (currentSection != null) {
            sections[currentSection] = currentContent.toString().trim()
        }

        return PlanStructure(sections = sections, hasSections = sections.isNotEmpty())
    }

    /**
     * 将结构化计划格式化为注入文本。
     * 如果有识别到结构化分节，使用 XML 标签包裹各节；否则回退到原始文本。
     */
    private fun formatPlanForInjection(structure: PlanStructure, rawText: String): String {
        if (!structure.hasSections) {
            return "[Plan from previous PLAN mode]\n$rawText"
        }

        return buildString {
            appendLine("[Plan from previous PLAN mode]")
            appendLine("The following plan was generated in PLAN mode. Execute it step by step using available tools.")
            appendLine()

            structure.sections["goal"]?.let {
                appendLine("<plan-goal>")
                appendLine(it)
                appendLine("</plan-goal>")
            }
            structure.sections["known_facts"]?.let {
                appendLine("<plan-known-facts>")
                appendLine(it)
                appendLine("</plan-known-facts>")
            }
            structure.sections["assumptions"]?.let {
                appendLine("<plan-assumptions>")
                appendLine(it)
                appendLine("</plan-assumptions>")
            }
            structure.sections["steps"]?.let {
                appendLine("<plan-steps>")
                appendLine(it)
                appendLine("</plan-steps>")
            }
            structure.sections["risks"]?.let {
                appendLine("<plan-risks>")
                appendLine(it)
                appendLine("</plan-risks>")
            }
            structure.sections["next_step"]?.let {
                appendLine("<plan-next-step>")
                appendLine(it)
                appendLine("</plan-next-step>")
            }

            // 未识别的节段回退到末尾
            val knownKeys = setOf("goal", "known_facts", "assumptions", "steps", "risks", "next_step")
            structure.sections.filterKeys { it !in knownKeys }.forEach { (key, content) ->
                appendLine("<plan-$key>")
                appendLine(content)
                appendLine("</plan-$key>")
            }
        }.trimEnd()
    }

    private fun buildModeTransitionText(transition: ModeTransition): String {
        val fromLabel = transition.from.name.lowercase()
        val toLabel = transition.to.name.lowercase()
        return buildString {
            append("[Mode switched: $fromLabel → $toLabel] ")
            when {
                transition.from == ChatWorkingMode.AGENT &&
                    transition.to == ChatWorkingMode.ASK -> {
                    append("Previous messages may contain tool calls from agent mode. ")
                    append("Focus on the user's current question and respond directly.")
                }
                transition.from == ChatWorkingMode.PLAN &&
                    transition.to == ChatWorkingMode.AGENT -> {
                    append("User is now in agent mode. You can use tools to execute the plan.")
                }
                else -> {
                    append("Adjust your response style to match the new mode.")
                }
            }
        }
    }

    /** 模式切换记录 */
    data class ModeTransition(
        val from: ChatWorkingMode,
        val to: ChatWorkingMode,
        val timestamp: Long,
        /** PLAN→AGENT 切换时捕获的计划文本，避免后续消息覆盖 */
        val capturedPlanText: String = ""
    )

    /** 模式切换结果 */
    data class ModeSwitchResult(
        val transition: ModeTransition,
    )

    /** 解析后的计划结构 */
    data class PlanStructure(
        val sections: Map<String, String>,
        val hasSections: Boolean,
    )

    companion object {
        /** 模式标记最大注入轮次 */
        const val TRANSITION_MARKER_MAX_INJECTIONS = 3

        /** PLAN 文本注入最大字符数，超出时截断并添加 [truncated] 标记 */
        const val MAX_PLAN_INJECTION_LENGTH = 4000

        /** 剥离 Markdown 标题标记（## / **） */
        private val STRIP_MARKERS_REGEX = Regex("^[#*\\s]+")

        /** 剥离标题前缀（如“目标理解:”） */
        private val STRIP_PREFIX_REGEX = Regex("^[^:：]*[:：]\\s*")

        /** 计划分节的正则匹配模式 */
        private val PLAN_SECTION_PATTERNS = linkedMapOf(
            "goal" to Regex("(?:目标理解|goal\\s*understanding|理解目标|目标)", RegexOption.IGNORE_CASE),
            "known_facts" to Regex("(?:当前已知|known\\s*facts|已知信息|已知条件)", RegexOption.IGNORE_CASE),
            "assumptions" to Regex("(?:关键假设|assumptions|待确认|假设)", RegexOption.IGNORE_CASE),
            "steps" to Regex("(?:推荐步骤|recommended\\s*steps|执行步骤|步骤|行动计划)", RegexOption.IGNORE_CASE),
            "risks" to Regex("(?:风险|risk|依赖|dependencies)", RegexOption.IGNORE_CASE),
            "next_step" to Regex("(?:下一步|next\\s*step|建议|后续)", RegexOption.IGNORE_CASE),
        )
    }
}
