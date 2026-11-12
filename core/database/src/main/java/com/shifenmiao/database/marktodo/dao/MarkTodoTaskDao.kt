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

package com.shifenmiao.database.marktodo.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.marktodo.entity.MarkTodoTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkTodoTaskDao {

    @Query(
        """
        SELECT * FROM marktodo_task
        WHERE deleted_at IS NULL
          AND ((:categoryId IS NULL AND category_id IS NULL) OR category_id = :categoryId)
        ORDER BY sort_order ASC, created_at ASC
        """
    )
    suspend fun getByCategoryId(categoryId: String?): List<MarkTodoTaskEntity>

    @Query("SELECT * FROM marktodo_task WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    suspend fun getAll(): List<MarkTodoTaskEntity>

    @Query(
        """
        SELECT * FROM marktodo_task
        WHERE deleted_at IS NULL
          AND (title LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%')
        ORDER BY updated_at DESC
        LIMIT :limit
        """
    )
    suspend fun search(query: String, limit: Int): List<MarkTodoTaskEntity>

    @Query("SELECT * FROM marktodo_task WHERE deleted_at IS NULL ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<MarkTodoTaskEntity>>

    @Query("SELECT * FROM marktodo_task WHERE id = :taskId AND deleted_at IS NULL")
    suspend fun getById(taskId: String): MarkTodoTaskEntity?

    @Query("SELECT * FROM marktodo_task WHERE id = :taskId")
    suspend fun getByIdIncludingDeleted(taskId: String): MarkTodoTaskEntity?

    @Query("SELECT * FROM marktodo_task WHERE deleted_at IS NOT NULL ORDER BY deleted_at ASC")
    suspend fun getSoftDeleted(): List<MarkTodoTaskEntity>

    @Query("SELECT * FROM marktodo_task WHERE category_id = :categoryId")
    suspend fun getAllByCategoryIdIncludingDeleted(categoryId: String): List<MarkTodoTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: MarkTodoTaskEntity)

    @Query("UPDATE marktodo_task SET is_completed = :isCompleted, completed_at = CASE WHEN :isCompleted THEN :updatedAt ELSE NULL END, updated_at = :updatedAt WHERE id = :taskId AND deleted_at IS NULL")
    suspend fun setCompleted(taskId: String, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE marktodo_task SET is_starred = :isStarred, updated_at = :updatedAt WHERE id = :taskId AND deleted_at IS NULL")
    suspend fun setStarred(taskId: String, isStarred: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE marktodo_task SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :taskId AND deleted_at IS NULL")
    suspend fun updateSortOrder(taskId: String, sortOrder: Int, updatedAt: Long = System.currentTimeMillis())

    @Query(
        """
        SELECT MAX(sort_order) FROM marktodo_task
        WHERE deleted_at IS NULL
          AND ((:categoryId IS NULL AND category_id IS NULL) OR category_id = :categoryId)
        """
    )
    suspend fun getMaxSortOrder(categoryId: String?): Int?

    @Query("UPDATE marktodo_task SET deleted_at = :deletedAt, updated_at = :deletedAt WHERE id = :taskId AND deleted_at IS NULL")
    suspend fun softDeleteById(taskId: String, deletedAt: Long): Int

    @Query("UPDATE marktodo_task SET deleted_at = NULL, updated_at = :restoredAt WHERE id = :taskId AND deleted_at = :deletedAt")
    suspend fun restoreByIdAndDeletedAt(taskId: String, deletedAt: Long, restoredAt: Long): Int

    @Query("DELETE FROM marktodo_task WHERE id = :taskId AND deleted_at = :deletedAt")
    suspend fun purgeByIdAndDeletedAt(taskId: String, deletedAt: Long): Int

    @Query("DELETE FROM marktodo_task")
    suspend fun deleteAll()

    @Query(
        """
        SELECT COUNT(*) FROM marktodo_task
        WHERE deleted_at IS NULL
          AND ((:categoryId IS NULL AND category_id IS NULL) OR category_id = :categoryId)
          AND lower(trim(title)) = lower(trim(:title))
        """
    )
    suspend fun countByCategoryIdAndNormalizedTitle(categoryId: String?, title: String): Int
}
