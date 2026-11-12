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

package com.shifenmiao.database.item.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.item.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY updated_at DESC, id ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM category ORDER BY updated_at DESC, id ASC")
    suspend fun getAllCategoriesList(): List<Category>

    @Query("SELECT * FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Query("DELETE FROM category WHERE id = :id")
    suspend fun deleteCategory(id: Int)

    @Query("UPDATE category SET name = :name WHERE id = :id")
    suspend fun updateCategoryName(id: Int, name: String)

    @Query("UPDATE category SET updated_at = :updatedAt WHERE id = :id")
    suspend fun updateCategoryUpdatedAt(id: Int, updatedAt: Long)

    /**
     * 删除 item 的所有分类关联（业务侧 "重置 item 分类" 用）。
     * 委托给 item_category 关联表，定义在 CategoryDao 上以匹配旧调用方。
     */
    @Query("DELETE FROM item_category WHERE item_id = :itemId")
    suspend fun deleteCategoriesByItemId(itemId: Int)

    /**
     * 按 name 查找分类；若存在则更新（保留原 id），若不存在则插入。
     * 返回该分类的最终 id（用于 item_category 关联表）。
     */
    @Transaction
    suspend fun insertOrUpdateCategory(category: Category): Long {
        val existing = getCategoryByName(category.name)
        return if (existing != null) {
            updateCategoryFromSync(
                id = existing.id,
                name = category.name,
                canEdit = category.canEdit,
                updatedAt = category.updatedAt,
            )
            existing.id.toLong()
        } else {
            insert(category.copy(id = 0))
        }
    }

    @Query(
        """
        UPDATE category SET
            name = :name,
            can_edit = :canEdit,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateCategoryFromSync(
        id: Int,
        name: String,
        canEdit: Boolean,
        updatedAt: Long,
    ): Int
}
