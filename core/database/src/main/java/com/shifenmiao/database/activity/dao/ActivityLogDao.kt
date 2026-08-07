package com.shifenmiao.database.activity.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.activity.entity.ActivityLogEntity

@Dao
interface ActivityLogDao {

    /**
     * 插入或替换（基于 dedup_key UNIQUE 约束）。
     * 同一 dedupKey 的旧记录会被新记录覆盖。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ActivityLogEntity): Long

    /**
     * 分页查询，按创建时间降序。
     */
    @Query("SELECT * FROM activity_log ORDER BY created_at DESC")
    fun pagingSource(): PagingSource<Int, ActivityLogEntity>

    /**
     * 按分类分页查询。
     */
    @Query("SELECT * FROM activity_log WHERE category = :category ORDER BY created_at DESC")
    fun pagingSourceByCategory(category: String): PagingSource<Int, ActivityLogEntity>

    /**
     * 按 ID 查询单条。
     */
    @Query("SELECT * FROM activity_log WHERE id = :id")
    suspend fun getById(id: Long): ActivityLogEntity?

    /**
     * 按 dedupKey 查询单条。
     */
    @Query("SELECT * FROM activity_log WHERE dedup_key = :dedupKey LIMIT 1")
    suspend fun getByDedupKey(dedupKey: String): ActivityLogEntity?

    /**
     * 删除单条。
     */
    @Query("DELETE FROM activity_log WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /**
     * 按 dedupKey 删除（如删除某个 AI 对话的全部历史）。
     */
    @Query("DELETE FROM activity_log WHERE dedup_key = :dedupKey")
    suspend fun deleteByDedupKey(dedupKey: String): Int


    @Query(
        """
        DELETE FROM activity_log
        WHERE category = :category
          AND (
            payload LIKE '%' || :primaryPayloadFragment || '%'
            OR payload LIKE '%' || :secondaryPayloadFragment || '%'
          )
        """
    )
    suspend fun deleteByCategoryAndAnyPayloadContains(
        category: String,
        primaryPayloadFragment: String,
        secondaryPayloadFragment: String,
    ): Int

    /**
     * 清空全部。
     */
    @Query("DELETE FROM activity_log")
    suspend fun deleteAll()

    /**
     * 总条数（用于 UI 判空）。
     */
    @Query("SELECT COUNT(*) FROM activity_log")
    suspend fun count(): Int

    /**
     * 按 screenRoute 前缀查询最近的记录（用于 PDF 工具等场景）。
     */
    @Query("SELECT * FROM activity_log WHERE screen_route LIKE :routePrefix || '%' ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentByScreenRoute(routePrefix: String, limit: Int): List<ActivityLogEntity>

    /**
     * 按 screenRoute 前缀 + 标题模糊搜索。
     */
    @Query("SELECT * FROM activity_log WHERE screen_route LIKE :routePrefix || '%' AND title LIKE '%' || :query || '%' ORDER BY created_at DESC LIMIT :limit")
    suspend fun searchByScreenRoute(routePrefix: String, query: String, limit: Int): List<ActivityLogEntity>
}

