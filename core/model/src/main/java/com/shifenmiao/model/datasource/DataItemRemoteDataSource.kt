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

package com.shifenmiao.model.datasource

import com.shifenmiao.core.constants.Constants
import com.shifenmiao.model.CategoryList
import com.shifenmiao.model.DataItemLIst

interface DataItemRemoteDataSource {

    /**
     * 增量同步条目。
     *
     * @param categoryDocumentId 分类 documentId（Strapi v5 稳定标识），null 表示不过滤。
     * @param forceRefresh 是否强制走网络，绕过 HTTP 缓存。
     * @return 同步结果与本次同步的服务器时间（用于记录 lastSyncAt）。
     */
    suspend fun syncDataItems(
        listType: Int,
        categoryDocumentId: String?,
        pageNumber: Int,
        updatedAfter: String?,
        pageSize: Int = Constants.PAGE_SIZE,
        forceRefresh: Boolean = false,
    ): SyncResult<DataItemLIst>

    /**
     * 增量同步分类。
     *
     * @param forceRefresh 是否强制走网络，绕过 HTTP 缓存。
     */
    suspend fun syncCategories(
        updatedAfter: String?,
        forceRefresh: Boolean = false,
    ): SyncResult<CategoryList>

}

/**
 * 同步接口的统一返回结构，包含业务数据与服务器时间戳。
 */
data class SyncResult<T>(
    val data: T,
    val serverTime: String?,
)
