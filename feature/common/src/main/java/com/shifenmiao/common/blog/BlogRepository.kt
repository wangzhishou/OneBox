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

package com.shifenmiao.common.blog

import androidx.paging.PagingData
import com.shifenmiao.model.blog.BlogItem
import kotlinx.coroutines.flow.Flow

/**
 * 博客/文章列表仓库。
 *
 * 列表从本地 Room 分页读取；[refresh] 负责从 `/api/blogs` 全量拉取并持久化。
 */
interface BlogRepository {

    /**
     * 返回指定 [blogType] 的文章分页流。
     */
    fun pagingFlow(blogType: Int): Flow<PagingData<BlogItem>>

    /**
     * 强制刷新指定 [blogType] 的数据，成功后替换本地缓存。
     */
    suspend fun refresh(blogType: Int): Result<Unit>
}
