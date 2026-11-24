package com.shifenmiao.database.ai

import androidx.room.withTransaction
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.entity.ConversationEntity
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AIConversationTitleSource
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.RoleType
import java.util.Date
import javax.inject.Inject

class EditorConversationRepository @Inject constructor(private val database: AppDatabase) {
    suspend fun save(type: AIConversationEntryType, title: String, engine: AiEngine,
                     usage: EditorAiUsage, input: String, output: String) {
        require(type.isEditor)
        val conversation = Conversation(
            id = usage.id, entryType = type, entryRefId = usage.action, engine = engine,
            title = title, titleSource = AIConversationTitleSource.SYSTEM,
            lastActiveAt = usage.createdAt, messageCount = 2,
            lastUserMessagePreview = input.take(240), lastMessagePreview = output.take(240),
        )
        val question = MessageEntity(
            conversationId = usage.id, completionId = usage.id, question = input,
            engine = usage.engine, model = usage.model, title = title,
            requestProtocol = engine.requestProtocol.name, createdAt = Date(usage.createdAt),
            entryType = type, entryRefId = usage.action, totalTokens = usage.totalTokens ?: 0,
        )
        database.withTransaction {
            database.messageDao().insertAll(listOf(
                question,
                question.copy(
                    role = RoleType.ASSISTANT.value, question = "", answer = output,
                    promptTokens = usage.inputTokens ?: 0, completionTokens = usage.outputTokens ?: 0,
                ),
            ))
            database.conversationDao().insertReplace(ConversationEntity.fromConversation(conversation))
        }
    }

    suspend fun recent(type: AIConversationEntryType, action: String): List<EditorHistoryResult> =
        database.messageDao().getEditorResults(type.name, action)
}

data class EditorHistoryResult(
    val id: Int,
    val answer: String,
    val model: String,
    val createdAt: Date,
)
