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

package com.shifenmiao.database.schedule.repo

import com.shifenmiao.database.schedule.dao.ScheduleEventDao
import com.shifenmiao.database.schedule.dao.ScheduleProviderBindingDao
import com.shifenmiao.database.schedule.dao.ScheduleSyncStateDao
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.database.schedule.entity.ScheduleProviderBindingEntity
import com.shifenmiao.database.schedule.entity.ScheduleSyncStateEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val eventDao: ScheduleEventDao,
    private val providerBindingDao: ScheduleProviderBindingDao,
    private val syncStateDao: ScheduleSyncStateDao,
) {

    fun observeEvents(): Flow<List<ScheduleEventEntity>> = eventDao.observeAll()

    fun observeEventsByLinkedTaskId(taskId: String): Flow<List<ScheduleEventEntity>> {
        return eventDao.observeByLinkedTaskId(taskId = taskId)
    }

    suspend fun getEventsByLinkedTaskId(taskId: String): List<ScheduleEventEntity> {
        return eventDao.getByLinkedTaskId(taskId = taskId)
    }

    suspend fun getEvent(eventId: String): ScheduleEventEntity? = eventDao.getById(eventId)

    suspend fun upsertEvent(event: ScheduleEventEntity) {
        eventDao.upsert(event)
    }

    suspend fun getProviderBindings(localEventId: String): List<ScheduleProviderBindingEntity> {
        return providerBindingDao.getByLocalEventId(localEventId = localEventId)
    }

    suspend fun getProviderBinding(
        localEventId: String,
        providerType: String,
    ): ScheduleProviderBindingEntity? {
        return providerBindingDao.getByLocalEventIdAndProviderType(
            localEventId = localEventId,
            providerType = providerType,
        )
    }

    suspend fun upsertProviderBinding(binding: ScheduleProviderBindingEntity) {
        providerBindingDao.upsert(binding)
    }

    suspend fun deleteProviderBinding(bindingId: String) {
        providerBindingDao.deleteById(bindingId = bindingId)
    }

    suspend fun deleteProviderBindings(localEventId: String) {
        providerBindingDao.deleteByLocalEventId(localEventId = localEventId)
    }

    suspend fun deleteEvent(eventId: String) {
        eventDao.deleteById(eventId)
    }

    fun observeSyncState(providerType: String): Flow<ScheduleSyncStateEntity?> {
        return syncStateDao.observeByProvider(providerType = providerType)
    }

    suspend fun upsertSyncState(state: ScheduleSyncStateEntity) {
        syncStateDao.upsert(state)
    }
}

