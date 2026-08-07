package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.ai.entity.ConversationToolPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationToolPolicyDao {

    /**
     * 保存会话级工具策略。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationToolPolicyEntity)

    @Query("SELECT * FROM conversation_tool_policy WHERE conversation_id = :conversationId LIMIT 1")
    suspend fun getByConversationId(conversationId: String): ConversationToolPolicyEntity?

    @Query("SELECT * FROM conversation_tool_policy WHERE conversation_id = :conversationId LIMIT 1")
    fun observeByConversationId(conversationId: String): Flow<ConversationToolPolicyEntity?>

    @Query("DELETE FROM conversation_tool_policy WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)
}
