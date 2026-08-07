package com.shifenmiao.common.recent

import com.shifenmiao.database.recent_access.dao.RecentAccessDao
import com.shifenmiao.database.recent_access.entity.RecentAccessEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 最近访问记录 Repository — 供文件浏览器、ActivityLog、以及任何需要"最近打开位置"的模块共用。
 *
 * 统一用数据库持久化，替代之前散落在 SharedPreferences / 内存中的实现。
 */
@Singleton
class RecentAccessRepository @Inject constructor(
    private val dao: RecentAccessDao,
) {

    companion object {
        const val TYPE_FILE = "file"
        const val TYPE_FOLDER = "folder"
        const val DEFAULT_LIMIT = 50
    }

    /**
     * 记录一次文件访问（文件或文件夹）。
     *
     * @param uri URI 字符串（作为唯一键，同一 URI 会覆盖旧记录）
     * @param displayName 显示名称
     * @param accessType "file" 或 "folder"
     * @param pathHint 路径提示（可选）
     */
    suspend fun recordAccess(
        uri: String,
        displayName: String,
        accessType: String,
        pathHint: String? = null,
    ) {
        dao.upsert(
            RecentAccessEntity(
                uri = uri,
                displayName = displayName,
                accessType = accessType,
                pathHint = pathHint,
                accessedAt = System.currentTimeMillis(),
            )
        )
    }

    /**
     * 观察所有最近访问记录（按时间倒序）。
     */
    fun observeAll(limit: Int = DEFAULT_LIMIT): Flow<List<RecentAccessEntity>> {
        return dao.observeAll(limit)
    }

    /**
     * 观察指定类型的最近访问记录。
     */
    fun observeByType(
        type: String,
        limit: Int = DEFAULT_LIMIT,
    ): Flow<List<RecentAccessEntity>> {
        return dao.observeByType(type, limit)
    }

    /**
     * 一次性获取所有最近访问记录。
     */
    suspend fun getAll(limit: Int = DEFAULT_LIMIT): List<RecentAccessEntity> {
        return dao.getAll(limit)
    }

    /**
     * 删除指定 URI 的记录。
     */
    suspend fun remove(uri: String) {
        dao.deleteByUri(uri)
    }

    /**
     * 清空所有记录。
     */
    suspend fun clearAll() {
        dao.clearAll()
    }

    /**
     * 删除超过指定天数的旧记录。
     */
    suspend fun deleteOlderThan(days: Int) {
        val cutoff = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000L
        dao.deleteOlderThan(cutoff)
    }
}
