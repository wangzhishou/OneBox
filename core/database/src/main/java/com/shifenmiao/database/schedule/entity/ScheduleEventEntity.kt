package com.shifenmiao.database.schedule.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 本地日程事件实体。
 *
 * 设计目标：
 * - 先支持本地事件
 * - 为未来 Google Calendar / 系统日历 / CalDAV 同步预留字段
 * - 与待办任务通过 [linkedTaskId] 形成弱关联，避免实体耦合
 */
@Entity(
    tableName = "schedule_event",
    indices = [
        Index(value = ["linked_task_id"]),
        Index(value = ["start_utc_millis"]),
        Index(value = ["provider_type"]),
        Index(value = ["calendar_account_id"]),
        Index(value = ["provider_type", "remote_event_id"], unique = true)
    ]
)
data class ScheduleEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "provider_type") val providerType: String = "LOCAL",
    @ColumnInfo(name = "remote_event_id") val remoteEventId: String? = null,
    @ColumnInfo(name = "calendar_account_id") val calendarAccountId: String? = null,
    @ColumnInfo(name = "linked_task_id") val linkedTaskId: String? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "location") val location: String? = null,
    @ColumnInfo(name = "start_utc_millis") val startUtcMillis: Long,
    @ColumnInfo(name = "end_utc_millis") val endUtcMillis: Long,
    @ColumnInfo(name = "is_all_day") val isAllDay: Boolean = false,
    @ColumnInfo(name = "time_zone_id") val timeZoneId: String = "UTC",
    @ColumnInfo(name = "recurrence_rule") val recurrenceRule: String? = null,
    @ColumnInfo(name = "sync_status") val syncStatus: String = "LOCAL_ONLY",
    @ColumnInfo(name = "sync_version") val syncVersion: String? = null,
    @ColumnInfo(name = "provider_payload") val providerPayload: String? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

