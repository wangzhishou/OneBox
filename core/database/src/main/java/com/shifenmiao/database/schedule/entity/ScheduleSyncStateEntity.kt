package com.shifenmiao.database.schedule.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Provider 级别的同步游标状态。
 *
 * 目前先按 provider_type 存储一份状态，后续如果一个 provider 需要多账号，
 * 可以将主键扩展为 provider+account 的复合键或引入独立 id。
 */
@Entity(tableName = "schedule_sync_state")
data class ScheduleSyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "provider_type") val providerType: String,
    @ColumnInfo(name = "account_id") val accountId: String? = null,
    @ColumnInfo(name = "sync_token") val syncToken: String? = null,
    @ColumnInfo(name = "last_full_sync_at") val lastFullSyncAt: Long? = null,
    @ColumnInfo(name = "last_delta_sync_at") val lastDeltaSyncAt: Long? = null,
    @ColumnInfo(name = "last_error_message") val lastErrorMessage: String? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

