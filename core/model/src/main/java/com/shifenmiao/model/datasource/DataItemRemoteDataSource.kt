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
