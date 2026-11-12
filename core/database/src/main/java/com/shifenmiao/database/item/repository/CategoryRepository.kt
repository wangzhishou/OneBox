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

package com.shifenmiao.database.item.repository

import com.shifenmiao.database.item.dao.CategoryDao
import com.shifenmiao.database.item.dao.ItemEntityDao
import com.shifenmiao.database.item.entity.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val itemDao: ItemEntityDao
) {
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertOrUpdateCategory(category: Category) {
        categoryDao.insertOrUpdateCategory(category)
    }

    suspend fun deleteCategory(id: Int) {
        itemDao.deleteItemCategoryCrossRefsByCategoryId(id)
        categoryDao.deleteCategory(id)
    }

    suspend fun updateCategoryName(id: Int, name: String) {
        categoryDao.updateCategoryName(id, name)
    }
}
