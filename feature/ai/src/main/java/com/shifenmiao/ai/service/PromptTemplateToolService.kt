package com.shifenmiao.ai.service

import com.shifenmiao.ai.agent.tool.ConversationToolPolicyRepository
import com.shifenmiao.ai.agent.tool.ToolBindingRepository
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ConversationToolPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptTemplateToolService @Inject constructor(
    private val toolBindingRepository: ToolBindingRepository,
    private val conversationToolPolicyRepository: ConversationToolPolicyRepository
) {

    suspend fun getPromptScopedToolNames(promptId: Int?): Set<String>? {
        if (promptId == null || promptId <= 0) return null
        // "未绑定" 语义: 调用方应拿到 null 而非空集, 让上层 fallback 到 entry-scoped / bootstrap.
        return toolBindingRepository.getPromptBoundToolNames(promptId)
    }

    suspend fun ensurePromptDefaultsEnabled(
        conversation: Conversation
    ): Boolean {
        val promptId = conversation.promptId
        if (promptId == null || promptId <= 0) return false

        val existingPolicy = conversationToolPolicyRepository.getPolicy(conversation)
        if (existingPolicy != null) return false

        val defaultTools = toolBindingRepository.getPromptBoundToolNames(promptId)
            ?: return false
        if (defaultTools.isEmpty()) return false

        conversationToolPolicyRepository.savePolicy(
            conversation = conversation,
            policy = ConversationToolPolicy(
                selectedToolNames = defaultTools.toList().sorted()
            )
        )
        return true
    }
}
