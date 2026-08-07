package com.shifenmiao.database.schedule.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 本地日程事件与外部 provider 事件的绑定关系。
 *
 * 一条本地事件可以绑定多个 provider（如系统日历 / Google Calendar），
 * 因此独立建表，而不是把所有 provider 信息强塞进 schedule_event。
 */
@Entity(
    tableName = "schedule_provider_binding",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["local_event_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["local_event_id"]),
        Index(value = ["provider_type", "remote_event_id"], unique = true),
        Index(value = ["local_event_id", "provider_type"], unique = true),
    ]
)
data class ScheduleProviderBindingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "local_event_id") val localEventId: String,
    @ColumnInfo(name = "provider_type") val providerType: String,
    @ColumnInfo(name = "remote_event_id") val remoteEventId: String,
    @ColumnInfo(name = "provider_calendar_id") val providerCalendarId: String? = null,
    @ColumnInfo(name = "provider_account_id") val providerAccountId: String? = null,
    @ColumnInfo(name = "provider_payload") val providerPayload: String? = null,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

