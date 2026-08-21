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
