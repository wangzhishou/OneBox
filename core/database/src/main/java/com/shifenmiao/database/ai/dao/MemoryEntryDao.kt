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
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.ai.entity.MemoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryEntryDao {

    /**
     * 按 kind + enabled 观察条目（管理页列表 / 注入用 profile 条目），按写入时间升序。
     */
    @Query("SELECT * FROM memory_entry WHERE kind = :kind AND enabled = :enabled ORDER BY created_at ASC")
    fun observeByKind(kind: String, enabled: Boolean): Flow<List<MemoryEntryEntity>>

    /**
     * 按 kind + enabled 一次性取条目，按写入时间升序。
     */
    @Query("SELECT * FROM memory_entry WHERE kind = :kind AND enabled = :enabled ORDER BY created_at ASC")
    suspend fun getByKind(kind: String, enabled: Boolean): List<MemoryEntryEntity>

    /**
     * 按写入时间倒序取 log 候选（注入侧在 Kotlin 侧做日期分桶与关键词过滤）。
     */
    @Query("SELECT * FROM memory_entry WHERE kind = :kind AND enabled = :enabled ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentByKind(kind: String, enabled: Boolean, limit: Int): List<MemoryEntryEntity>

    @Query("SELECT * FROM memory_entry WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MemoryEntryEntity?

    @Insert
    suspend fun insert(entity: MemoryEntryEntity): Long

    @Update
    suspend fun update(entity: MemoryEntryEntity)

    @Query("DELETE FROM memory_entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * log 条目计数（滚动淘汰水位判断用，含停用条目）。
     */
    @Query("SELECT COUNT(*) FROM memory_entry WHERE kind = :kind")
    suspend fun countByKind(kind: String): Int

    /**
     * 删除最旧的 [count] 条 log（滚动淘汰用）。
     */
    @Query(
        """
        DELETE FROM memory_entry WHERE id IN (
            SELECT id FROM memory_entry WHERE kind = :kind ORDER BY created_at ASC LIMIT :count
        )
        """
    )
    suspend fun deleteOldestByKind(kind: String, count: Int)

    /**
     * 清空指定 kind 的全部条目（管理页一键清空 log 用）。
     */
    @Query("DELETE FROM memory_entry WHERE kind = :kind")
    suspend fun clearByKind(kind: String)
}
