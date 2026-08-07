package com.shifenmiao.database.ai.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.ai.entity.ToolCatalogEntity
import kotlinx.coroutines.flow.Flow

/**
 * 工具目录持久化 DAO —— 仅服务于导出 / 导入 / 备份场景.
 * 运行时目录查询请走 AgentToolRegistry, 不要在 hot path 调本 DAO.
 */
@Dao
interface ToolCatalogDao {

    /** 批量 upsert; 用于 snapshot 写回 + import 写入. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ToolCatalogEntity>)

    /** 读取全部 (BUILT_IN + IMPORTED), 按 sort_order 排序. */
    @Query("SELECT * FROM tool_catalog ORDER BY sort_order ASC, title ASC")
    suspend fun getAll(): List<ToolCatalogEntity>

    /** 观察全部记录 (UI 监听 import 变化用). */
    @Query("SELECT * FROM tool_catalog ORDER BY sort_order ASC, title ASC")
    fun observeAll(): Flow<List<ToolCatalogEntity>>

    /** 按 [source] 过滤 (BUILT_IN / IMPORTED). */
    @Query("SELECT * FROM tool_catalog WHERE source = :source ORDER BY sort_order ASC, title ASC")
    suspend fun getBySource(source: String): List<ToolCatalogEntity>

    /** 按名称查找. */
    @Query("SELECT * FROM tool_catalog WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): ToolCatalogEntity?

    /** 删除不在白名单的记录; 用于 snapshot 时清理已下线的工具. */
    @Query("DELETE FROM tool_catalog WHERE name NOT IN (:names)")
    suspend fun deleteMissing(names: List<String>)

    /** 删除指定 [source] 的全部记录. */
    @Query("DELETE FROM tool_catalog WHERE source = :source")
    suspend fun deleteBySource(source: String)

    /** 清空表. 慎用. */
    @Query("DELETE FROM tool_catalog")
    suspend fun clearAll()
}
