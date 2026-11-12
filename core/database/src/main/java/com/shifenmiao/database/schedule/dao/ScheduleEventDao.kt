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

package com.shifenmiao.database.schedule.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {

    @Query("SELECT * FROM schedule_event ORDER BY start_utc_millis ASC, updated_at DESC")
    fun observeAll(): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE linked_task_id = :taskId ORDER BY start_utc_millis ASC, updated_at DESC")
    fun observeByLinkedTaskId(taskId: String): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE linked_task_id = :taskId ORDER BY updated_at DESC")
    suspend fun getByLinkedTaskId(taskId: String): List<ScheduleEventEntity>

    @Query("SELECT * FROM schedule_event WHERE id = :eventId LIMIT 1")
    suspend fun getById(eventId: String): ScheduleEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: ScheduleEventEntity)

    @Query("DELETE FROM schedule_event WHERE id = :eventId")
    suspend fun deleteById(eventId: String)
}

