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

package com.shifenmiao.database.period.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.period.entity.PeriodRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeriodRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PeriodRecordEntity)

    @Query("SELECT * FROM period_record WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PeriodRecordEntity?

    @Query("SELECT * FROM period_record WHERE record_date = :epochDay LIMIT 1")
    suspend fun getByDate(epochDay: Long): PeriodRecordEntity?

    @Query("SELECT * FROM period_record ORDER BY record_date DESC, created_at DESC")
    fun observeAll(): Flow<List<PeriodRecordEntity>>

    @Query("SELECT * FROM period_record ORDER BY record_date DESC, created_at DESC")
    suspend fun getAll(): List<PeriodRecordEntity>

    @Query("SELECT * FROM period_record WHERE record_date BETWEEN :startEpochDay AND :endEpochDay ORDER BY record_date DESC")
    fun observeInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<PeriodRecordEntity>>

    @Query("DELETE FROM period_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM period_record")
    suspend fun deleteAll()
}
