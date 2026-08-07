package com.shifenmiao.ai.prompt

import com.shifenmiao.ai.repository.PromptRecord
import com.shifenmiao.ai.repository.PromptRepository
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.ai.utils.AiUtils
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.model.Source
import com.shifenmiao.network.api.RemoteId
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.RemoteConfigStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptManager @Inject constructor(
    private val promptRepository: PromptRepository,
    private val messageListUseCase: MessageListUseCase,
) {

    suspend fun prepareInitializationPlan(conversation: Conversation): PromptInitializationPlan? {
        if (!AiUtils.isPrompt(conversation)) return null

        // 重要：conversation.promptId 语义是 item_prompt.id（资源表主键），不是 item.id。
        // 之前把 promptId 当作 itemId 传给 getPromptRecordByItemId 是错误的本地命中路径。
        val localPromptId = conversation.promptId?.takeIf { it > 0 }
        val remotePromptId = RemoteId.of(conversation.promptRemoteId)

        if (localPromptId != null) {
            val localRecord = promptRepository.getPromptRecordById(localPromptId)
            if (localRecord != null) {
                return PromptInitializationPlan(
                    initialPrompt = localRecord.toLoadResult(),
                    refreshRemotePromptId = localRecord.resolveRefreshRemotePromptId()
                )
            }
        }

        // 本地未命中：用 remoteId 兜底，绝不用 localPromptId 调 API。
        return if (remotePromptId != null && localPromptId != null &&
            conversation.entryType == AIConversationEntryType.PROMPT
        ) {
            // 把同步来的 prompt 落到与 conversation.promptId 同一资源主键上
            // （1:1 关联场景下保持 prompt 表 PK 与 itemId 解耦，避免污染）
            PromptInitializationPlan(
                initialPrompt = fetchAndPersistRemotePrompt(remotePromptId, localPromptId)
            )
        } else {
            PromptInitializationPlan()
        }
    }

    suspend fun refreshPrompt(remoteId: RemoteId, localPromptId: Int): PromptLoadResult? {
        return fetchAndPersistRemotePrompt(remoteId, localPromptId)
    }

    private fun PromptRecord.resolveRefreshRemotePromptId(): RemoteId? {
        if (source != Source.REMOTE) return null
        val remoteId = RemoteId.of(remoteId) ?: return null
        if (!shouldRefreshRemotePrompt(this)) return null
        return remoteId
    }

    private fun shouldRefreshRemotePrompt(record: PromptRecord): Boolean {
        val updateInterval = RemoteConfigStorage.getRemoteConfig().aiAgentUpdateInterval
            ?: Constants.AI_AGENT_UPDATE_INTERVAL
        return record.updatedAtMillis + updateInterval < System.currentTimeMillis()
    }

    private suspend fun fetchAndPersistRemotePrompt(
        remoteId: RemoteId,
        localPromptId: Int,
    ): PromptLoadResult? {
        val prompt = messageListUseCase.getPrompt(remoteId) ?: return null
        val source = prompt.source ?: Source.REMOTE
        // localPromptId 已经是 item_prompt 表的本地主键
        val resolvedPromptId = if (source == Source.REMOTE) {
            promptRepository.upsertPromptAtItemId(prompt, localPromptId)
        } else {
            promptRepository.upsertPrompt(prompt, source)
        }
        return PromptLoadResult(
            prompt = prompt.copy(
                id = if (source == Source.REMOTE) resolvedPromptId else prompt.id,
                source = source,
            ),
            localPromptId = resolvedPromptId,
            isSystemPrompt = source == Source.SYSTEM,
            updatedAtMillis = System.currentTimeMillis(),
        )
    }

    private fun PromptRecord.toLoadResult(): PromptLoadResult {
        return PromptLoadResult(
            prompt = prompt,
            localPromptId = id,
            isSystemPrompt = isSystemPrompt,
            updatedAtMillis = updatedAtMillis,
        )
    }
}

data class PromptLoadResult(
    val prompt: ChatPrompt,
    val localPromptId: Int? = null,
    val isSystemPrompt: Boolean = false,
    val updatedAtMillis: Long? = null,
)

data class PromptInitializationPlan(
    val initialPrompt: PromptLoadResult? = null,
    val refreshRemotePromptId: RemoteId? = null,
)

