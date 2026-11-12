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

package com.shifenmiao.online.datasource

import com.shifenmiao.model.CategoryList
import com.shifenmiao.model.DataItemLIst
import com.shifenmiao.model.datasource.DataItemRemoteDataSource
import com.shifenmiao.model.datasource.SyncResult
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.utils.NetworkUtils
import retrofit2.Response
import java.io.IOException

class DataItemRemoteDataSourceImpl(
    private val apiService: ApiService
) : DataItemRemoteDataSource {

    override suspend fun syncDataItems(
        listType: Int,
        categoryDocumentId: String?,
        pageNumber: Int,
        updatedAfter: String?,
        pageSize: Int,
        forceRefresh: Boolean,
    ): SyncResult<DataItemLIst> {
        val response = NetworkUtils.safeApiCall {
            apiService.fetchItemsSync(
                updatedAfter = updatedAfter,
                listType = listType,
                category = categoryDocumentId?.takeIf { it.isNotBlank() },
                page = pageNumber,
                pageSize = pageSize,
                forceRefresh = forceRefresh,
            )
        }
        val body = requireSuccessBody(response, "Failed to sync items")
        return SyncResult(
            data = body,
            serverTime = body.meta.serverTime,
        )
    }

    override suspend fun syncCategories(
        updatedAfter: String?,
        forceRefresh: Boolean,
    ): SyncResult<CategoryList> {
        val response = NetworkUtils.safeApiCall {
            apiService.fetchCategoriesSync(
                updatedAfter = updatedAfter,
                forceRefresh = forceRefresh,
            )
        }
        val body = requireSuccessBody(response, "Failed to sync categories")
        return SyncResult(
            data = body,
            serverTime = body.meta?.serverTime,
        )
    }

    /**
     * 提取成功响应体；对 HTTP 4xx/5xx 解析服务端错误体后再抛出，避免只显示兜底文案。
     */
    private fun <T> requireSuccessBody(response: retrofit2.Response<T>?, fallback: String): T {
        if (response == null) throw IOException(fallback)
        if (!response.isSuccessful) {
            var message = ""
            NetworkUtils.handleErrorResponse(
                response,
                onFriendlyErrorTip = { message = it },
                onFail = {}
            )
            throw IOException(message.takeIf { it.isNotBlank() } ?: fallback)
        }
        return response.body() ?: throw IOException(fallback)
    }
}
