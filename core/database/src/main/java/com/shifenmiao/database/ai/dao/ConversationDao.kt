/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
