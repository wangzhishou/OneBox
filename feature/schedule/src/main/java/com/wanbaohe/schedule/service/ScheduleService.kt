package com.wanbaohe.schedule.service

import android.content.Context
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.database.schedule.entity.ScheduleProviderBindingEntity
import com.shifenmiao.database.schedule.entity.ScheduleSyncStateEntity
import com.shifenmiao.database.schedule.repo.ScheduleRepository
import com.wanbaohe.schedule.integration.SystemCalendarProviderAdapter
import com.wanbaohe.schedule.model.ScheduleProviderType
import com.wanbaohe.schedule.model.ScheduleSyncStatus
import com.wanbaohe.schedule.model.toModel
import com.wanbaohe.schedule.model.toStorageValue
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * 日程写操作入口。
 *
 * 当前实现优先保证：
 * - 本地事件可独立存在
 * - 与待办通过 linkedTaskId 弱关联
 * - 为未来 Google Calendar 增量同步保留 provider/sync token 字段
 */
@Singleton
class ScheduleService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ScheduleRepository,
) {

    data class EventInput(
        val title: String,
        val description: String? = null,
        val location: String? = null,
        val startUtcMillis: Long,
        val endUtcMillis: Long,
        val isAllDay: Boolean = false,
        val timeZoneId: String = TimeZone.getDefault().id,
        val linkedTaskId: String? = null,
        val recurrenceRule: String? = null,
    )

    suspend fun createLocalEvent(
        input: EventInput,
        source: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeTitle = input.title.trim()
            require(safeTitle.isNotBlank()) { "title cannot be blank" }

            val safeStart = minOf(input.startUtcMillis, input.endUtcMillis)
            val safeEnd = max(input.startUtcMillis, input.endUtcMillis)
            val now = System.currentTimeMillis()
            val eventId = UUID.randomUUID().toString()

            repository.upsertEvent(
                ScheduleEventEntity(
                    id = eventId,
                    providerType = ScheduleProviderType.LOCAL.toStorageValue(),
                    linkedTaskId = input.linkedTaskId,
                    title = safeTitle,
                    description = input.description?.trim()?.takeIf { it.isNotBlank() },
                    location = input.location?.trim()?.takeIf { it.isNotBlank() },
                    startUtcMillis = safeStart,
                    endUtcMillis = safeEnd,
                    isAllDay = input.isAllDay,
                    timeZoneId = input.timeZoneId,
                    recurrenceRule = input.recurrenceRule,
                    syncStatus = ScheduleSyncStatus.LOCAL_ONLY.toStorageValue(),
                    providerPayload = source,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            eventId
        }
    }

    suspend fun createTaskDeadlineEvent(
        linkedTaskId: String,
        title: String,
        description: String?,
        dueAtMillis: Long,
        source: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            val safeTitle = title.trim().ifBlank { throw IllegalArgumentException("title cannot be blank") }
            val safeDescription = description?.trim()?.takeIf { it.isNotBlank() }
            val existingLocalTaskEvent = repository
                .getEventsByLinkedTaskId(linkedTaskId)
                .firstOrNull { it.providerType == ScheduleProviderType.LOCAL.toStorageValue() }

            val eventId = existingLocalTaskEvent?.id ?: UUID.randomUUID().toString()

            repository.upsertEvent(
                ScheduleEventEntity(
                    id = eventId,
                    providerType = ScheduleProviderType.LOCAL.toStorageValue(),
                    linkedTaskId = linkedTaskId,
                    title = safeTitle,
                    description = safeDescription,
                    startUtcMillis = dueAtMillis,
                    endUtcMillis = dueAtMillis,
                    timeZoneId = TimeZone.getDefault().id,
                    syncStatus = ScheduleSyncStatus.LOCAL_ONLY.toStorageValue(),
                    providerPayload = source,
                    createdAt = existingLocalTaskEvent?.createdAt ?: now,
                    updatedAt = now,
                )
            )

            syncSystemCalendarBindingIfPresent(eventId = eventId)
            eventId
        }
    }

    suspend fun syncTaskDeadlineEventIfExists(
        linkedTaskId: String,
        title: String,
        description: String?,
        dueAtMillis: Long,
        source: String,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            repository
                .getEventsByLinkedTaskId(linkedTaskId)
                .firstOrNull { it.providerType == ScheduleProviderType.LOCAL.toStorageValue() }
                ?: return@runCatching false

            createTaskDeadlineEvent(
                linkedTaskId = linkedTaskId,
                title = title,
                description = description,
                dueAtMillis = dueAtMillis,
                source = source,
            ).getOrThrow()
            true
        }
    }

    suspend fun saveEventToSystemCalendar(
        localEventId: String,
        calendarId: Long,
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val localEvent = repository.getEvent(localEventId)
                ?: error("local schedule event not found")
            val binding = repository.getProviderBinding(
                localEventId = localEventId,
                providerType = ScheduleProviderType.SYSTEM_CALENDAR.toStorageValue(),
            )
            val now = System.currentTimeMillis()

            val remoteEventId = binding?.remoteEventId?.toLongOrNull()?.let { remoteId ->
                SystemCalendarProviderAdapter.updateEvent(
                    context = context,
                    remoteEventId = remoteId,
                    calendarId = calendarId,
                    event = localEvent.toModel(),
                ).getOrThrow()
                remoteId
            } ?: SystemCalendarProviderAdapter.insertEvent(
                context = context,
                calendarId = calendarId,
                event = localEvent.toModel(),
            ).getOrThrow()

            repository.upsertProviderBinding(
                ScheduleProviderBindingEntity(
                    id = binding?.id ?: UUID.randomUUID().toString(),
                    localEventId = localEventId,
                    providerType = ScheduleProviderType.SYSTEM_CALENDAR.toStorageValue(),
                    remoteEventId = remoteEventId.toString(),
                    providerCalendarId = calendarId.toString(),
                    providerAccountId = binding?.providerAccountId,
                    providerPayload = binding?.providerPayload,
                    lastSyncedAt = now,
                    createdAt = binding?.createdAt ?: now,
                    updatedAt = now,
                )
            )

            repository.upsertEvent(
                localEvent.copy(
                    remoteEventId = remoteEventId.toString(),
                    calendarAccountId = calendarId.toString(),
                    syncStatus = ScheduleSyncStatus.SYNCED.toStorageValue(),
                    lastSyncedAt = now,
                    updatedAt = now,
                )
            )

            remoteEventId
        }
    }

    suspend fun deleteEventWithProviderBindings(localEventId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.getProviderBindings(localEventId).forEach { binding ->
                when (binding.providerType) {
                    ScheduleProviderType.SYSTEM_CALENDAR.toStorageValue() -> {
                        binding.remoteEventId.toLongOrNull()?.let { remoteId ->
                            SystemCalendarProviderAdapter.deleteEvent(
                                context = context,
                                remoteEventId = remoteId,
                            ).getOrThrow()
                        }
                    }

                    else -> Unit
                }
            }

            repository.deleteProviderBindings(localEventId = localEventId)
            repository.deleteEvent(eventId = localEventId)
        }
    }

    suspend fun deleteEventsByLinkedTaskId(linkedTaskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.getEventsByLinkedTaskId(linkedTaskId).forEach { event ->
                deleteEventWithProviderBindings(event.id).getOrThrow()
            }
        }
    }

    private suspend fun syncSystemCalendarBindingIfPresent(eventId: String) {
        val localEvent = repository.getEvent(eventId) ?: return
        val binding = repository.getProviderBinding(
            localEventId = eventId,
            providerType = ScheduleProviderType.SYSTEM_CALENDAR.toStorageValue(),
        ) ?: return

        val remoteEventId = binding.remoteEventId.toLongOrNull() ?: return
        val calendarId = binding.providerCalendarId?.toLongOrNull() ?: return
        val now = System.currentTimeMillis()

        SystemCalendarProviderAdapter.updateEvent(
            context = context,
            remoteEventId = remoteEventId,
            calendarId = calendarId,
            event = localEvent.toModel(),
        ).getOrThrow()

        repository.upsertProviderBinding(
            binding.copy(
                lastSyncedAt = now,
                updatedAt = now,
            )
        )

        repository.upsertEvent(
            localEvent.copy(
                remoteEventId = remoteEventId.toString(),
                calendarAccountId = calendarId.toString(),
                syncStatus = ScheduleSyncStatus.SYNCED.toStorageValue(),
                lastSyncedAt = now,
                updatedAt = now,
            )
        )
    }

    suspend fun updateGoogleSyncState(
        accountId: String?,
        syncToken: String?,
        lastErrorMessage: String?,
        lastFullSyncAt: Long? = null,
        lastDeltaSyncAt: Long? = null,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            repository.upsertSyncState(
                ScheduleSyncStateEntity(
                    providerType = ScheduleProviderType.GOOGLE_CALENDAR.toStorageValue(),
                    accountId = accountId,
                    syncToken = syncToken,
                    lastFullSyncAt = lastFullSyncAt,
                    lastDeltaSyncAt = lastDeltaSyncAt,
                    lastErrorMessage = lastErrorMessage,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }
}

