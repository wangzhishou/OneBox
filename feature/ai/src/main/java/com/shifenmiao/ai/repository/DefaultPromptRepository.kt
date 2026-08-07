package com.shifenmiao.ai.repository

import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.utils.DataBaseUtils
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.ChatPrompt
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultPromptRepository @Inject constructor(
    private val appDatabase: AppDatabase,
) : PromptRepository {

    override suspend fun getPromptRecordById(id: Int): PromptRecord? {
        return appDatabase.chatPromptDao().getPromptById(id)?.let { entity ->
            PromptRecord(
                id = entity.id,
                remoteId = entity.remoteId,
                prompt = DataBaseUtils.promptEntityToPrompt(entity),
                source = entity.source,
                updatedAtMillis = entity.updatedAt,
                isSystemPrompt = entity.isSystemPreset(),
            )
        }
    }

    override suspend fun getPromptRecordByItemId(itemId: Int): PromptRecord? {
        // 通过 link 表查本地资源行
        val promptId = appDatabase.chatPromptDao().getPromptLinkByItemId(itemId) ?: return null
        return appDatabase.chatPromptDao().getPromptById(promptId)?.let { entity ->
            PromptRecord(
                id = entity.id,
                remoteId = entity.remoteId,
                prompt = DataBaseUtils.promptEntityToPrompt(entity),
                source = entity.source,
                updatedAtMillis = entity.updatedAt,
                isSystemPrompt = entity.isSystemPreset(),
            )
        }
    }

    override suspend fun upsertPrompt(prompt: ChatPrompt, source: Source): Int {
        val entity = DataBaseUtils.promptToPromptEntity(prompt, source = source)
        return if (source == Source.REMOTE) {
            appDatabase.chatPromptDao().upsertRemotePrompt(entity)
        } else {
            appDatabase.chatPromptDao().upsertLocalPrompt(entity)
        }
    }

    override suspend fun upsertPromptAtItemId(
        prompt: ChatPrompt,
        itemId: Int,
        source: Source,
    ): Int {
        val entity = DataBaseUtils.promptToPromptEntity(
            prompt.copy(id = itemId),
            source = source,
        )
        return if (source == Source.REMOTE) {
            appDatabase.chatPromptDao().upsertRemotePrompt(entity)
        } else {
            appDatabase.chatPromptDao().upsertLocalPrompt(entity)
        }
    }
}
