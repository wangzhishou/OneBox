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

