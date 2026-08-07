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

