package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.ai.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplace(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE conversation_id = :conversationId")
    suspend fun getConversationByConversationId(conversationId: String): ConversationEntity?

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: String)

    @Query("DELETE FROM conversations WHERE conversation_id = :conversationId")
    suspend fun deleteConversationByConversationId(conversationId: String): Int

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()

    @Query("SELECT * FROM conversations WHERE entry_type = :entryType ORDER BY last_active_at DESC LIMIT 1")
    suspend fun getLastConversationByType(entryType: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE entry_type = :entryType AND history_visible = 0 ORDER BY last_active_at DESC LIMIT 1")
    suspend fun getLastHiddenConversationByType(entryType: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE entry_type = :entryType ORDER BY last_active_at DESC LIMIT :limit")
    suspend fun getConversationsByType(entryType: String, limit: Int): List<ConversationEntity>

    @Query("SELECT * FROM conversations WHERE entry_type = :entryType AND history_visible = 1 ORDER BY last_active_at DESC LIMIT :limit")
    suspend fun getVisibleConversationsByType(entryType: String, limit: Int): List<ConversationEntity>

    @Query(
        """
        SELECT * FROM conversations
        WHERE entry_type = :entryType
          AND ((:entryRefId IS NULL AND entry_ref_id IS NULL) OR entry_ref_id = :entryRefId)
        ORDER BY last_active_at DESC
        LIMIT 1
        """
    )
    suspend fun getLastConversationByEntry(
        entryType: String,
        entryRefId: String?,
    ): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE conversation_id = :conversationId LIMIT 1")
    fun observeConversation(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations ORDER BY last_active_at DESC")
    fun observeAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE history_visible = 1 ORDER BY last_active_at DESC")
    fun observeHistoryConversations(): Flow<List<ConversationEntity>>

    @Query("UPDATE conversations SET title = :title, title_source = :titleSource WHERE conversation_id = :conversationId")
    suspend fun updateTitle(
        conversationId: String,
        title: String,
        titleSource: String,
    ): Int
}
