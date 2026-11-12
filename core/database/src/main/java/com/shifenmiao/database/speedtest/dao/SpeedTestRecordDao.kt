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

package com.shifenmiao.database.speedtest.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.speedtest.entity.SpeedTestRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestRecordDao {

    /** 插入一条测速记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SpeedTestRecordEntity): Long

    /** 按时间倒序获取所有记录（Flow，实时更新） */
    @Query("SELECT * FROM speed_test_record ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<SpeedTestRecordEntity>>

    /** 清空所有记录 */
    @Query("DELETE FROM speed_test_record")
    suspend fun clearAll()

    /** 获取记录总数 */
    @Query("SELECT COUNT(*) FROM speed_test_record")
    suspend fun getCount(): Int
}

