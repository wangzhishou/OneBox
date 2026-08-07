package com.wanbaohe.schedule.model

import androidx.compose.runtime.Immutable

@Immutable
enum class ScheduleProviderType {
    LOCAL,
    SYSTEM_CALENDAR,
    GOOGLE_CALENDAR,
}

@Immutable
enum class ScheduleSyncStatus {
    LOCAL_ONLY,
    PENDING_UPLOAD,
    SYNCED,
    PENDING_DELETE,
    CONFLICT,
    FAILED,
}

@Immutable
data class ScheduleEvent(
    val id: String,
    val providerType: ScheduleProviderType,
    val remoteEventId: String? = null,
    val calendarAccountId: String? = null,
    val linkedTaskId: String? = null,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startUtcMillis: Long,
    val endUtcMillis: Long,
    val isAllDay: Boolean = false,
    val timeZoneId: String,
    val recurrenceRule: String? = null,
    val syncStatus: ScheduleSyncStatus,
    val syncVersion: String? = null,
    val providerPayload: String? = null,
    val lastSyncedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

@Immutable
data class ScheduleSyncState(
    val providerType: ScheduleProviderType,
    val accountId: String? = null,
    val syncToken: String? = null,
    val lastFullSyncAt: Long? = null,
    val lastDeltaSyncAt: Long? = null,
    val lastErrorMessage: String? = null,
    val updatedAt: Long,
)

