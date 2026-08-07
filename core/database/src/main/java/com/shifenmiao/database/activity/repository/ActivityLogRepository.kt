package com.shifenmiao.database.activity.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.activity.dao.ActivityLogDao
import com.shifenmiao.database.activity.entity.ActivityLogEntity
import com.shifenmiao.model.activity.ActivityCategory
import com.shifenmiao.model.activity.ActivityLogEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 活动日志数据仓库 — 唯一的读写入口。
 *
 * 所有 feature 模块通过 [ActivityLogRecorder]（门面）写入，
 * UI 层通过 [observePaged] / [observePagedByCategory] 读取。
 */
@Singleton
class ActivityLogRepository @Inject constructor(
    private val dao: ActivityLogDao
) {

    // ── 写入 ─────────────────────────────────────────

    /**
     * 记录一条活动日志。同 [ActivityLogEntry.dedupKey] 的旧记录会被覆盖。
     */
    suspend fun record(entry: ActivityLogEntry): Long {
        return dao.upsert(entry.toEntity())
    }

    // ── 读取 ─────────────────────────────────────────

    /**
     * 分页观察全部日志（按时间降序）。
     */
    fun observePaged(pageSize: Int = 20): Flow<PagingData<ActivityLogEntry>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = 2),
            pagingSourceFactory = { dao.pagingSource() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    /**
     * 分页观察某一分类的日志。
     */
    fun observePagedByCategory(
        category: ActivityCategory,
        pageSize: Int = 20
    ): Flow<PagingData<ActivityLogEntry>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = 2),
            pagingSourceFactory = { dao.pagingSourceByCategory(category.name) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    // ── 删除 ─────────────────────────────────────────

    suspend fun deleteById(id: Long): Int = dao.deleteById(id)

    suspend fun deleteByDedupKey(dedupKey: String): Int = dao.deleteByDedupKey(dedupKey)


    suspend fun deleteByCategoryAndAnyPayloadContains(
        category: ActivityCategory,
        primaryPayloadFragment: String,
        secondaryPayloadFragment: String,
    ): Int {
        return dao.deleteByCategoryAndAnyPayloadContains(
            category = category.name,
            primaryPayloadFragment = primaryPayloadFragment,
            secondaryPayloadFragment = secondaryPayloadFragment,
        )
    }

    suspend fun deleteAll() = dao.deleteAll()

    // ── 查询 ─────────────────────────────────────────

    suspend fun getById(id: Long): ActivityLogEntry? = dao.getById(id)?.toDomain()

    suspend fun getByDedupKey(dedupKey: String): ActivityLogEntry? =
        dao.getByDedupKey(dedupKey)?.toDomain()

    /**
     * 查询某个 screenRoute 前缀下的最近记录。
     */
    suspend fun getRecentByScreenRoute(routePrefix: String, limit: Int = 20): List<ActivityLogEntry> =
        dao.getRecentByScreenRoute(routePrefix, limit).map { it.toDomain() }

    /**
     * 按 screenRoute 前缀 + 标题模糊搜索。
     */
    suspend fun searchByScreenRoute(routePrefix: String, query: String, limit: Int = 20): List<ActivityLogEntry> =
        dao.searchByScreenRoute(routePrefix, query, limit).map { it.toDomain() }
}

// ── Mapper ───────────────────────────────────────────

internal fun ActivityLogEntry.toEntity() = ActivityLogEntity(
    id = id,
    category = category.name,
    appTitle = appTitle,
    title = title,
    description = description,
    screenRoute = screenRoute,
    payload = payload,
    thumbnailUri = thumbnailUri,
    dedupKey = dedupKey,
    createdAt = createdAt
)

internal fun ActivityLogEntity.toDomain() = ActivityLogEntry(
    id = id,
    category = ActivityCategory.fromName(category),
    appTitle = appTitle,
    title = title,
    description = description,
    screenRoute = screenRoute,
    payload = payload,
    thumbnailUri = thumbnailUri,
    dedupKey = dedupKey,
    createdAt = createdAt
)

