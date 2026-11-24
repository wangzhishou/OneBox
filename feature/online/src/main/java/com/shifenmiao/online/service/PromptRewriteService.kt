package com.shifenmiao.online.service

import androidx.annotation.StringRes
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.common.ai.EditorAiPromptKey
import com.shifenmiao.common.ai.EditorAiPrompts
import com.shifenmiao.database.ai.EditorConversationRepository
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.online.R
import javax.inject.Inject

enum class PromptRewriteAction(
    @StringRes val label: Int,
    val promptKey: EditorAiPromptKey,
) {
    Create(R.string.prompt_ai_create, EditorAiPromptKey.PROMPT_CREATE),
    Enhance(R.string.prompt_ai_enhance, EditorAiPromptKey.PROMPT_ENHANCE),
    Format(R.string.prompt_ai_format, EditorAiPromptKey.PROMPT_FORMAT),
    Condense(R.string.prompt_ai_condense, EditorAiPromptKey.PROMPT_CONDENSE),
}

class PromptRewriteService @Inject constructor(
    private val executor: AIPromptExecutor,
    private val history: EditorConversationRepository,
    private val editorAiPrompts: EditorAiPrompts,
) {
    suspend fun recent(action: PromptRewriteAction) =
        history.recent(AIConversationEntryType.PROMPT_EDIT, action.name)

    fun effectiveEngine() = executor.effectiveEngine()

    fun withAccess(onAllowed: () -> Unit) =
        executor.runWithAccessGate(source = "prompt_editor_ai", onAllowed = onAllowed)

    suspend fun rewrite(
        action: PromptRewriteAction,
        original: String,
        requirement: String,
        billingLabel: String,
        onDelta: (String) -> Unit = {},
    ): com.shifenmiao.common.ai.AIPromptResult {
        val input = if (action == PromptRewriteAction.Create) {
            requirement.trim()
        } else {
            buildString {
                if (requirement.isNotBlank()) {
                    appendLine("Additional editing request:")
                    appendLine(requirement.trim())
                    appendLine()
                }
                appendLine("Prompt to edit:")
                append(original)
            }
        }
        val systemPrompt = buildString {
            append(editorAiPrompts.get(EditorAiPromptKey.PROMPT_SYSTEM))
            appendLine()
            append(editorAiPrompts.get(action.promptKey))
        }
        return executor.executeStreaming(
            input = input,
            systemPrompt = systemPrompt,
            onDelta = onDelta,
            billingDesc = billingLabel,
            editorCall = AIPromptExecutor.EditorCall(
                AIConversationEntryType.PROMPT_EDIT,
                action.name,
                billingLabel,
            ),
        )
    }

    fun preservesProtectedText(original: String, candidate: String): Boolean =
        variables.findAll(original).map { it.value }.groupingBy { it }.eachCount() ==
            variables.findAll(candidate).map { it.value }.groupingBy { it }.eachCount() &&
            fencedBlocks.findAll(original).map { it.value }.toList() ==
            fencedBlocks.findAll(candidate).map { it.value }.toList()

    private companion object {
        val variables = Regex("""\$\{[^{}\r\n]+\}|\{\{[^{}\r\n]+\}\}""")
        val fencedBlocks = Regex("""(?ms)^ {0,3}(`{3,}|~{3,})[^\r\n]*\r?\n.*?^ {0,3}\1[ \t]*(?=\r?$)""")
    }
}
