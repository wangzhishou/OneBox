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

package com.shifenmiao.marktodo.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TodoTaskTimeRules {

    fun isOverdue(
        dueDateMillis: Long?,
        hasDueTime: Boolean,
        isCompleted: Boolean,
        now: Long,
        today: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (dueDateMillis == null || isCompleted) return false
        return if (hasDueTime) {
            dueDateMillis < now
        } else {
            dueDateMillis.toLocalDate(zoneId) < today
        }
    }

    fun isDueSoon(
        dueDateMillis: Long?,
        hasDueTime: Boolean,
        isCompleted: Boolean,
        now: Long,
        today: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): Boolean {
        if (dueDateMillis == null || isCompleted) return false
        return if (hasDueTime) {
            dueDateMillis in now..(now + ONE_DAY_MILLIS)
        } else {
            dueDateMillis.toLocalDate(zoneId) == today
        }
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()

    private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
}
