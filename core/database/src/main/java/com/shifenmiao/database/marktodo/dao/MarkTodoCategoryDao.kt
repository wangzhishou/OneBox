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
import androidx.room.Update
import com.shifenmiao.database.marktodo.entity.MarkTodoCategoryEntity

@Dao
interface MarkTodoCategoryDao {

    @Query("SELECT * FROM marktodo_category ORDER BY sort_order ASC, created_at DESC")
    suspend fun getAll(): List<MarkTodoCategoryEntity>

    @Query("SELECT COUNT(*) FROM marktodo_category")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: MarkTodoCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(category: MarkTodoCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<MarkTodoCategoryEntity>)

    @Update
    suspend fun update(category: MarkTodoCategoryEntity)

    @Query("UPDATE marktodo_category SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :categoryId")
    suspend fun updateSortOrder(categoryId: String, sortOrder: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT MAX(sort_order) FROM marktodo_category")
    suspend fun getMaxSortOrder(): Int?

    @Query("DELETE FROM marktodo_category")
    suspend fun deleteAll()

    @Query("DELETE FROM marktodo_category WHERE id = :categoryId")
    suspend fun deleteById(categoryId: String)

    @Query(
        """
        SELECT COUNT(*) FROM marktodo_category
        WHERE lower(trim(title)) = lower(trim(:title))
        """
    )
    suspend fun countByNormalizedTitle(title: String): Int
}
