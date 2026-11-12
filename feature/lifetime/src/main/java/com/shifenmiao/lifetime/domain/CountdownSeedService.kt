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

package com.shifenmiao.lifetime.domain

import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.data.LifeTimeRepository
import com.shifenmiao.lifetime.data.holiday.HolidayProvider
import com.shifenmiao.lifetime.data.holiday.PresetHoliday
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 倒数日种子服务：
 * - 首次进入时插入 [HolidayProvider] 给出的未来 [withinDays] 天节日
 * - 幂等：以 DataStore 里的播种标记为准；老库里已有预置节日时只补标记，不重复插入
 */
@Singleton
class CountdownSeedService @Inject constructor(
    private val repository: CountdownEventRepository,
    private val holidayProvider: HolidayProvider,
    private val lifeTimeRepository: LifeTimeRepository,
) {

    suspend fun seedIfNeeded(
        now: LocalDate = LocalDate.now(),
        withinDays: Long = DEFAULT_LOOKAHEAD_DAYS,
    ) {
        // 播过种就不再播：用户把节日全删掉也不该复活
        if (lifeTimeRepository.isCountdownPresetsSeeded()) return
        // 老用户升级上来时库里已经有预置节日，直接补标记，避免重复插入
        if (repository.getPresetCountdownsCount() > 0) {
            lifeTimeRepository.markCountdownPresetsSeeded()
            return
        }
        val presets = holidayProvider.upcomingHolidays(now, withinDays)
        if (presets.isEmpty()) return
        repository.insertPresets(presets.map { it.toEntity() })
        lifeTimeRepository.markCountdownPresetsSeeded()
    }

    companion object {
        const val DEFAULT_LOOKAHEAD_DAYS = 90L
    }
}

private fun PresetHoliday.toEntity() = com.shifenmiao.database.lifetime.entity.CountdownEventEntity(
    name = name,
    iconKey = iconKey,
    targetDate = targetDate?.toEpochDay(),
    isLunarTarget = isLunar,
    lunarMonth = lunarMonth,
    lunarDay = lunarDay,
    note = null,
    color = null,
    sortOrder = sortOrder,
    isPreset = true,
    isFromHoliday = true,
    createdAt = System.currentTimeMillis(),
)
