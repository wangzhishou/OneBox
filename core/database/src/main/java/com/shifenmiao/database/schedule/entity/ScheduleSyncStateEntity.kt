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

