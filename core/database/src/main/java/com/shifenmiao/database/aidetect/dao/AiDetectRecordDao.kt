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

package com.shifenmiao.database.aidetect.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.aidetect.entity.AiDetectRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiDetectRecordDao {

    /** 插入一条检测记录,返回自增 id */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AiDetectRecordEntity): Long

    /** 按时间倒序获取所有记录(Flow,实时更新) */
    @Query("SELECT * FROM ai_detect_record ORDER BY createdAt DESC")
    fun getAll(): Flow<List<AiDetectRecordEntity>>

    /** 删除指定记录 */
    @Query("DELETE FROM ai_detect_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 清空所有记录 */
    @Query("DELETE FROM ai_detect_record")
    suspend fun clearAll()

    /** 超出保留上限的最旧记录 id 列表(用于裁剪及清理缩略图文件) */
    @Query(
        "SELECT id FROM ai_detect_record ORDER BY createdAt DESC LIMIT -1 OFFSET :keepCount"
    )
    suspend fun getIdsBeyond(keepCount: Int): List<Long>

    /** 按 id 列表删除记录 */
    @Query("DELETE FROM ai_detect_record WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    /** 按 id 查询单条记录 */
    @Query("SELECT * FROM ai_detect_record WHERE id = :id")
    suspend fun getById(id: Long): AiDetectRecordEntity?
}
