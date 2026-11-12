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

package com.shifenmiao.database.bookkeeping.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.bookkeeping.entity.BookkeepingCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookkeepingCategoryDao {

    @Query("SELECT COUNT(*) FROM bookkeeping_category")
    suspend fun count(): Int

    @Query("SELECT * FROM bookkeeping_category WHERE type = :type ORDER BY sort_order ASC, created_at ASC")
    fun observeByType(type: Int): Flow<List<BookkeepingCategoryEntity>>

    @Query("SELECT * FROM bookkeeping_category ORDER BY type ASC, sort_order ASC, created_at ASC")
    suspend fun getAll(): List<BookkeepingCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: BookkeepingCategoryEntity): Long

    @Update
    suspend fun update(entity: BookkeepingCategoryEntity)

    @Query("UPDATE bookkeeping_category SET sort_order = :sortOrder, updated_at = :updatedAt WHERE id = :categoryId")
    suspend fun updateSortOrder(categoryId: String, sortOrder: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM bookkeeping_category WHERE id = :categoryId AND is_default = 0")
    suspend fun deleteCustomById(categoryId: String)

    @Query("DELETE FROM bookkeeping_category")
    suspend fun deleteAll()
}
