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
import com.shifenmiao.database.ai.entity.ConversationMemoryPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationMemoryPolicyDao {

    /**
     * 保存会话级记忆/技能开关。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConversationMemoryPolicyEntity)

    /** 初始化策略行（已存在则不动），配合字段级更新消除 read-modify-write 竞态 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: ConversationMemoryPolicyEntity)

    /** 字段级更新：只改 memory_enabled */
    @Query("UPDATE conversation_memory_policy SET memory_enabled = :enabled, updated_at = :updatedAt WHERE conversation_id = :conversationId")
    suspend fun setMemoryEnabled(conversationId: String, enabled: Boolean, updatedAt: Long)

    /** 字段级更新：只改 skills_enabled */
    @Query("UPDATE conversation_memory_policy SET skills_enabled = :enabled, updated_at = :updatedAt WHERE conversation_id = :conversationId")
    suspend fun setSkillsEnabled(conversationId: String, enabled: Boolean, updatedAt: Long)

    @Query("SELECT * FROM conversation_memory_policy WHERE conversation_id = :conversationId LIMIT 1")
    suspend fun getByConversationId(conversationId: String): ConversationMemoryPolicyEntity?

    @Query("SELECT * FROM conversation_memory_policy WHERE conversation_id = :conversationId LIMIT 1")
    fun observeByConversationId(conversationId: String): Flow<ConversationMemoryPolicyEntity?>

    @Query("DELETE FROM conversation_memory_policy WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)
}
