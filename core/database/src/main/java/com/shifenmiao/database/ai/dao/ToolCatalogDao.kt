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
