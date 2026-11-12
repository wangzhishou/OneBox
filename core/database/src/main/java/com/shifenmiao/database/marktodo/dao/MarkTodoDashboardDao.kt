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
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.marktodo.model.CategoryWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkTodoDashboardDao {

    @Transaction
    @Query("SELECT * FROM marktodo_category ORDER BY is_pinned DESC, sort_order ASC, created_at DESC")
    suspend fun getCategoriesWithTasks(): List<CategoryWithTasks>

    /**
     * 观察分类及其任务的变化（响应式）
     * 当数据库中的分类或任务发生变化时，会自动触发更新
     */
    @Transaction
    @Query("SELECT * FROM marktodo_category ORDER BY is_pinned DESC, sort_order ASC, created_at DESC")
    fun observeCategoriesWithTasks(): Flow<List<CategoryWithTasks>>
}

