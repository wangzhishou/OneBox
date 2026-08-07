package com.wanbaohe.schedule.model

import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import com.shifenmiao.database.schedule.entity.ScheduleSyncStateEntity

internal fun ScheduleEventEntity.toModel(): ScheduleEvent {
    return ScheduleEvent(
        id = id,
        providerType = providerType.toScheduleProviderType(),
        remoteEventId = remoteEventId,
        calendarAccountId = calendarAccountId,
        linkedTaskId = linkedTaskId,
        title = title,
        description = description,
        location = location,
        startUtcMillis = startUtcMillis,
        endUtcMillis = endUtcMillis,
        isAllDay = isAllDay,
        timeZoneId = timeZoneId,
        recurrenceRule = recurrenceRule,
        syncStatus = syncStatus.toScheduleSyncStatus(),
        syncVersion = syncVersion,
        providerPayload = providerPayload,
        lastSyncedAt = lastSyncedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun ScheduleSyncStateEntity.toModel(): ScheduleSyncState {
    return ScheduleSyncState(
        providerType = providerType.toScheduleProviderType(),
        accountId = accountId,
        syncToken = syncToken,
        lastFullSyncAt = lastFullSyncAt,
        lastDeltaSyncAt = lastDeltaSyncAt,
        lastErrorMessage = lastErrorMessage,
        updatedAt = updatedAt,
    )
}

internal fun ScheduleProviderType.toStorageValue(): String = name

internal fun ScheduleSyncStatus.toStorageValue(): String = name

private fun String.toScheduleProviderType(): ScheduleProviderType {
    return ScheduleProviderType.entries.firstOrNull { it.name == this } ?: ScheduleProviderType.LOCAL
}

private fun String.toScheduleSyncStatus(): ScheduleSyncStatus {
    return ScheduleSyncStatus.entries.firstOrNull { it.name == this } ?: ScheduleSyncStatus.LOCAL_ONLY
}

