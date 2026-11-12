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

package com.shifenmiao.lifetime.data

import com.shifenmiao.database.FeatureDatabase
import com.shifenmiao.database.lifetime.dao.CountdownEventDao
import com.shifenmiao.database.lifetime.entity.CountdownEventEntity
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 倒数日仓库
 */
@Singleton
class CountdownEventRepository @Inject constructor(
    database: FeatureDatabase
) {
    private val dao: CountdownEventDao = database.countdownEventDao()

    val allCountdownsFlow: Flow<List<CountdownEvent>> = dao.getAllCountdowns()
        .map { entities -> entities.map { it.toDomainModel() } }

    suspend fun addCountdown(event: CountdownEvent): Long {
        return dao.insertCountdown(event.toEntity())
    }

    suspend fun updateCountdown(event: CountdownEvent) {
        dao.updateCountdown(event.toEntity())
    }

    suspend fun deleteCountdown(event: CountdownEvent) {
        dao.deleteCountdown(event.toEntity())
    }

    suspend fun deleteCountdownById(id: Long) {
        dao.deleteCountdownById(id)
    }

    suspend fun getCountdownById(id: Long): CountdownEvent? {
        return dao.getCountdownById(id)?.toDomainModel()
    }

    suspend fun getPresetCountdownsCount(): Int = dao.getPresetCountdownsCount()

    /** 批量插入预置节日（用 IGNORE 策略避免重复） */
    suspend fun insertPresets(presets: List<CountdownEventEntity>) {
        dao.insertCountdowns(presets)
    }
}

private fun CountdownEventEntity.toDomainModel() = CountdownEvent(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.let { LocalDate.ofEpochDay(it) },
    isLunarTarget = isLunarTarget,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = note,
    color = color,
    sortOrder = sortOrder,
    isPreset = isPreset,
    isFromHoliday = isFromHoliday,
    createdAt = createdAt,
)

private fun CountdownEvent.toEntity() = CountdownEventEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.toEpochDay(),
    isLunarTarget = isLunarTarget,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = note,
    color = color,
    sortOrder = sortOrder,
    isPreset = isPreset,
    isFromHoliday = isFromHoliday,
    createdAt = createdAt,
)
