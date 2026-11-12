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

/** 待办编辑页用来展示「已在日程」的那一条本地事件。 */
@Immutable
data class LinkedScheduleSnapshot(
    val eventId: String,
    val startUtcMillis: Long,
    val isAllDay: Boolean,
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

