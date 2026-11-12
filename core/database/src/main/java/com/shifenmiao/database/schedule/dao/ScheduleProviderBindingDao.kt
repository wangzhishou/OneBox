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
import com.shifenmiao.database.schedule.entity.ScheduleProviderBindingEntity

@Dao
interface ScheduleProviderBindingDao {

    @Query("SELECT * FROM schedule_provider_binding WHERE local_event_id = :localEventId")
    suspend fun getByLocalEventId(localEventId: String): List<ScheduleProviderBindingEntity>

    @Query(
        "SELECT * FROM schedule_provider_binding WHERE local_event_id = :localEventId AND provider_type = :providerType LIMIT 1"
    )
    suspend fun getByLocalEventIdAndProviderType(
        localEventId: String,
        providerType: String,
    ): ScheduleProviderBindingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(binding: ScheduleProviderBindingEntity)

    @Query("DELETE FROM schedule_provider_binding WHERE id = :bindingId")
    suspend fun deleteById(bindingId: String)

    @Query("DELETE FROM schedule_provider_binding WHERE local_event_id = :localEventId")
    suspend fun deleteByLocalEventId(localEventId: String)
}

