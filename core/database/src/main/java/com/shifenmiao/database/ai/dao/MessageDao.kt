package com.shifenmiao.database.ai.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.database.ai.entity.MessageWithImages
import com.shifenmiao.database.ai.entity.MessageWithSearchResults
import com.shifenmiao.database.ai.entity.MessageWithAllRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplace(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertNew(message: MessageEntity): Long

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM message WHERE conversation_id = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String): Int

    @Query("DELETE FROM message WHERE completion_id = :completionId")
    suspend fun deleteMessagesByCompletionId(completionId: String): Int

    @Query("UPDATE message SET title = :title WHERE conversation_id = :conversationId")
    suspend fun updateTitlesByConversationId(conversationId: String, title: String): Int

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    fun getMessagesForConversation(conversationId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId AND created_at < :currentTime ORDER BY created_at DESC")
    fun getMessagesBeforeCurrentTime(conversationId: String, currentTime: Long): PagingSource<Int, MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun getMessagesByConversationId(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM message ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getMessagesForPage(
        limit: Int,
        offset: Int
    ): List<MessageEntity>

    @Query(
        """
    SELECT *, MIN(created_at) as created_at
    FROM message
    GROUP BY conversation_id
    ORDER BY created_at DESC
    LIMIT :limit OFFSET :offset
    """
    )
    fun getEarliestMessagesForPage(
        limit: Int,
        offset: Int
    ): PagingSource<Int, MessageEntity>

    @Query(
        """
    SELECT *, MIN(created_at) as created_at
    FROM message
    GROUP BY conversation_id
    ORDER BY created_at DESC
    """
    )
    fun getHistoryMessageList(): PagingSource<Int, MessageEntity>

    @Query(
        """
    SELECT *, MIN(created_at) as created_at
    FROM message
    GROUP BY conversation_id
    ORDER BY created_at DESC
    LIMIT :limit OFFSET :offset
    """
    )
    fun getEarliestMessagesForPageFlow(
        limit: Int,
        offset: Int
    ): Flow<List<MessageEntity>>

    @Query("DELETE FROM message")
    suspend fun deleteAll()

    @Transaction
    suspend fun insertQuestionAndAnswer(question: MessageEntity, answer: MessageEntity) {
        insertReplace(question)
        insertReplace(answer)
    }

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at DESC LIMIT :limit")
    fun getMessagesFlowPaged(conversationId: String, limit: Int = 50): Flow<List<MessageEntity>>

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    suspend fun getMessageListOnce(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM message WHERE conversation_id = :conversationId AND created_at < :currentTime ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getMessagesBeforeCurrentTimeFlow(
        conversationId: String,
        currentTime: Long,
        limit: Int,
        offset: Int
    ): List<MessageEntity>

    @Query("SELECT * FROM message WHERE id = :contentBefore LIMIT 1")
    fun getById(contentBefore: String?): MessageEntity?

    @Query("SELECT * FROM message WHERE question LIKE :searchString OR answer LIKE :searchString ORDER BY created_at DESC")
    fun searchQuestionOrAnswer(searchString: String): Flow<List<MessageEntity>>

    @Query("UPDATE message SET expired = :expired WHERE completion_id = :completionId")
    suspend fun updateMessageEntityExpiredByCompletionId(completionId: String, expired: Boolean = true): Int

    @Query("SELECT * FROM message WHERE completion_id = :completionId")
    fun queryQuestionAndAnswerByCompletionId(completionId: String): List<MessageEntity>

    /**
     * 流式问答缓存查询：按「问题 + systemPrompt + 引擎/模型」内容键找最新一条有效回答。
     *
     * user/assistant 两条消息共享 completion_id；systemPrompt 落在 conversations 表。
     * [since] 为有效期的起始时间戳（毫秒），永久缓存传 0。
     */
    @Query(
        """
        SELECT a.* FROM message a
        INNER JOIN message q ON q.completion_id = a.completion_id AND q.role = 'user'
        INNER JOIN conversations c ON c.conversation_id = a.conversation_id
        WHERE a.role = 'assistant'
          AND a.expired = 0
          AND a.entry_type = 'STREAM_QA'
          AND a.answer != ''
          AND q.question = :question
          AND c.prompt = :systemPrompt
          AND a.engine = :engine
          AND a.model = :model
          AND a.created_at >= :since
        ORDER BY a.created_at DESC
        LIMIT 1
        """
    )
    suspend fun findLatestStreamAnswer(
        question: String,
        systemPrompt: String,
        engine: String,
        model: String,
        since: Long,
    ): MessageEntity?

    @Query(
        """
        SELECT conversation_id AS conversationId, COUNT(*) AS messageCount
        FROM message
        WHERE expired = 0 AND conversation_id IN (:conversationIds)
        GROUP BY conversation_id
        """
    )
    suspend fun getActiveMessageCountsByConversationIds(
        conversationIds: List<String>
    ): List<ConversationMessageCount>

    @Transaction
    @Query("SELECT * FROM message WHERE id = :messageId")
    suspend fun getMessageWithImages(messageId: Int): MessageWithImages?

    @Transaction
    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun getMessagesWithImagesByConversationId(conversationId: String): Flow<List<MessageWithImages>>

    @Transaction
    @Query("SELECT * FROM message WHERE id = :messageId")
    suspend fun getMessageWithSearchResults(messageId: Int): MessageWithSearchResults?

    @Transaction
    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun getMessagesWithSearchResultsByConversationId(conversationId: String): Flow<List<MessageWithSearchResults>>

    @Transaction
    @Query("SELECT * FROM message WHERE id = :messageId")
    suspend fun getMessageWithAllRelations(messageId: Int): MessageWithAllRelations?

    @Transaction
    @Query("SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY created_at ASC")
    fun getMessagesWithAllRelationsByConversationId(conversationId: String): Flow<List<MessageWithAllRelations>>

    @Query(
        """
        SELECT DISTINCT question FROM message
        WHERE role = 'user' AND question != ''
        ORDER BY created_at DESC
        LIMIT 50
        """
    )
    fun getRecentDistinctQuestions(): Flow<List<String>>

    @Query(
        """
        SELECT
            COALESCE(SUM(total_tokens), 0) as total_tokens,
            COALESCE(SUM(prompt_tokens), 0) as prompt_tokens,
            COALESCE(SUM(completion_tokens), 0) as completion_tokens,
            COUNT(*) as request_count
        FROM message
        WHERE expired = 0 AND total_tokens > 0 AND role = 'assistant'
        """
    )
    suspend fun getTokenUsageSummary(): TokenUsageSummary?

    @Query(
        """
        SELECT
            model,
            engine,
            SUM(total_tokens) as total_tokens,
            COUNT(*) as request_count
        FROM message
        WHERE expired = 0 AND total_tokens > 0 AND role = 'assistant'
        GROUP BY model, engine
        ORDER BY total_tokens DESC
        """
    )
    suspend fun getTokenUsageByModel(): List<ModelUsageStat>

    @Query(
        """
        SELECT
            conversation_id,
            question,
            title,
            entry_type,
            entry_ref_id,
            model,
            engine,
            total_tokens as total_tokens
        FROM message
        WHERE expired = 0 AND total_tokens > 0 AND role = 'user' AND question != ''
        ORDER BY total_tokens DESC
        LIMIT :limit
        """
    )
    suspend fun getTopTokenQueries(limit: Int = 20): List<TopQueryStat>
}
