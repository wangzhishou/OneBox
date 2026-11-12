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

package com.shifenmiao.database.recordcenter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: HealthRecordEntity)

    @Query("DELETE FROM health_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM health_record WHERE type = :type ORDER BY happened_at DESC")
    fun observeByType(type: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_record WHERE type = :type AND happened_at BETWEEN :from AND :to ORDER BY happened_at ASC")
    fun observeRange(type: String, from: Long, to: Long): Flow<List<HealthRecordEntity>>

    // 每个 type 的最新一条记录（按 happened_at 最大值；同 type 同毫秒时间戳的极端并列会同时返回）
    @Query(
        """
        SELECT r.* FROM health_record r
        INNER JOIN (
            SELECT type, MAX(happened_at) AS max_happened_at
            FROM health_record
            GROUP BY type
        ) latest ON latest.type = r.type AND latest.max_happened_at = r.happened_at
        ORDER BY r.type ASC
        """
    )
    fun observeLatestPerType(): Flow<List<HealthRecordEntity>>

    // 全部类型在 [from, +∞) 区间内的记录(趋势聚合页用),时间正序
    @Query("SELECT * FROM health_record WHERE happened_at >= :from ORDER BY happened_at ASC")
    fun observeSinceAllTypes(from: Long): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_record WHERE id = :id")
    suspend fun getById(id: String): HealthRecordEntity?

    @Query("SELECT * FROM health_record WHERE type = :type AND happened_at BETWEEN :from AND :to ORDER BY happened_at ASC")
    suspend fun getRangeOnce(type: String, from: Long, to: Long): List<HealthRecordEntity>

    @Query(
        """
        SELECT r.* FROM health_record r
        INNER JOIN (
            SELECT type, MAX(happened_at) AS max_happened_at
            FROM health_record
            GROUP BY type
        ) latest ON latest.type = r.type AND latest.max_happened_at = r.happened_at
        ORDER BY r.type ASC
        """
    )
    suspend fun getLatestPerTypeOnce(): List<HealthRecordEntity>
}
