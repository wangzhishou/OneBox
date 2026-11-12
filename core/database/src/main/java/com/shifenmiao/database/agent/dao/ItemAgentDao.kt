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

package com.shifenmiao.database.agent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.model.Source

@Dao
interface ItemAgentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: ItemAgentEntity): Long

    @Query(
        """
        UPDATE item_agent SET
            title = :title,
            description = :description,
            header = :header,
            body = :body,
            prompt = :prompt,
            updated_at = :updateTime
        WHERE id = :id
        """
    )
    suspend fun updateAgentPreservingRemoteFields(
        id: Int,
        title: String,
        description: String?,
        header: String?,
        body: String?,
        prompt: String?,
        updateTime: Long,
    ): Int

    @Transaction
    suspend fun upsertLocalAgent(agent: ItemAgentEntity): Int {
        if (agent.id <= 0) {
            return insertAgent(agent.copy(id = 0)).toInt()
        }
        val existing = getAgentById(agent.id)
        return if (existing != null) {
            updateAgentPreservingRemoteFields(
                id = existing.id,
                title = agent.title,
                description = agent.description,
                header = agent.header,
                body = agent.body,
                prompt = agent.prompt,
                updateTime = agent.updatedAt,
            )
            existing.id
        } else {
            insertAgent(agent.copy(id = 0)).toInt()
        }
    }

    @Transaction
    suspend fun upsertRemoteAgent(agent: ItemAgentEntity): Int {
        // 同步主键 documentId 优先，空时降级 remoteId（防御旧数据）
        val existing = agent.documentId?.takeIf { it.isNotBlank() }
            ?.let { getAgentByDocumentId(documentId = it, source = agent.source) }
            ?: agent.remoteId?.let { getAgentByRemoteId(remoteId = it, source = agent.source) }
        return if (existing != null) {
            // 必须用 UPDATE，不能用 REPLACE：REPLACE 会先删除旧行再插入，
            // 触发 item_agent_link.agent_id 的 FK CASCADE，导致 link 被误删。
            updateAgentPreservingRemoteFields(
                id = existing.id,
                title = agent.title,
                description = agent.description,
                header = agent.header,
                body = agent.body,
                prompt = agent.prompt,
                updateTime = agent.updatedAt,
            )
            agent.documentId?.takeIf { it.isNotBlank() }
                ?.let { backfillDocumentIdIfMissing(existing.id, it) }
            existing.id
        } else {
            insertAgent(agent.copy(id = 0)).toInt()
        }
    }

    @Query("SELECT * FROM item_agent WHERE id = :id")
    suspend fun getAgentById(id: Int): ItemAgentEntity?

    @Query("SELECT * FROM item_agent WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getAgentByRemoteId(remoteId: Int, source: Source = Source.REMOTE): ItemAgentEntity?

    @Query("SELECT * FROM item_agent WHERE source = :source AND document_id = :documentId LIMIT 1")
    suspend fun getAgentByDocumentId(documentId: String, source: Source = Source.REMOTE): ItemAgentEntity?

    /** 命中老数据（按 remoteId 匹配）后回填 document_id；已有值时不覆盖。 */
    @Query("UPDATE item_agent SET document_id = :documentId WHERE id = :id AND (document_id IS NULL OR document_id = '')")
    suspend fun backfillDocumentIdIfMissing(id: Int, documentId: String)

    @Query("SELECT * FROM item_agent WHERE source = :source AND title = :title LIMIT 1")
    suspend fun getAgentByTitleAndSource(title: String, source: Source): ItemAgentEntity?

    // ── Link 查询 ──

    @Query("SELECT agent_id FROM item_agent_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getAgentLinkByItemId(itemId: Int): Int?

    @Query("SELECT item_id FROM item_agent_link WHERE agent_id = :agentId")
    suspend fun getItemIdsByAgentId(agentId: Int): List<Int>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAgentLink(link: com.shifenmiao.database.item.entity.ItemAgentLink)

    @Query("DELETE FROM item_agent_link WHERE item_id = :itemId")
    suspend fun deleteAgentLinkByItemId(itemId: Int): Int

    @Query("DELETE FROM item_agent WHERE id = :id")
    suspend fun deleteAgentById(id: Int)

    @Query("DELETE FROM item_agent WHERE source = :source")
    suspend fun deleteBySource(source: Source)

    @Query("DELETE FROM item_agent")
    suspend fun deleteAll()
}
