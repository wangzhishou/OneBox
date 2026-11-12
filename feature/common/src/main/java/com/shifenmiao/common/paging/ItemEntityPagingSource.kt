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

package com.shifenmiao.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.item.entity.ItemWithCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItemEntityPagingSource(
    private val appDatabase: AppDatabase,
    private val categoryId: Int
) : PagingSource<Int, ItemWithCategories>() {

    override fun getRefreshKey(state: PagingState<Int, ItemWithCategories>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ItemWithCategories> {
        return withContext(Dispatchers.IO) {
            val page = params.key ?: 1
            val pageSize = params.loadSize
            val items = if (categoryId > 0) {
                appDatabase.itemEntityDao().getItemsListByCategoryId(categoryId, page, pageSize)
            } else {
                appDatabase.itemEntityDao().getAllItemsList(page, pageSize)
            }
            val nextKey = if (items.isEmpty()) {
                null
            } else {
                page + 1
            }
            LoadResult.Page(
                data = items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = nextKey
            )
        }
    }
}