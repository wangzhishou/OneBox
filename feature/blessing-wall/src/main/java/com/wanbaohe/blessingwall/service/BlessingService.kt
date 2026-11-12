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

package com.wanbaohe.blessingwall.service

import com.shifenmiao.database.blessing.entity.BlessingWishEntity
import com.shifenmiao.database.blessing.repo.BlessingRepository
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.BlessingTabCustomizationSnapshot
import com.wanbaohe.blessingwall.model.DailyBlessingRecord
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.effectiveAt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlessingService @Inject constructor(
    private val repository: BlessingRepository,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun todayString(): String = LocalDate.now().format(dateFormatter)

    suspend fun bless(type: BlessingType) {
        repository.incrementCount(date = todayString(), type = type.key)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeTodayCounts(): Flow<Map<BlessingType, Int>> {
        return observeCurrentDate().flatMapLatest(::observeCounts)
    }

    fun observeCounts(date: String): Flow<Map<BlessingType, Int>> {
        return repository.observeByDate(date).map { records ->
            BlessingType.entries.associateWith { type ->
                records.find { it.type == type.key }?.count ?: 0
            }
        }
    }

    fun observeMonthRecords(yearMonth: String): Flow<List<DailyBlessingRecord>> {
        val start = "$yearMonth-01"
        val end = "$yearMonth-31"
        return combine(
            repository.observeInDateRange(start, end),
            repository.observeWishesInDateRange(start, end),
        ) { records, wishes ->
            records.groupBy { it.date }
                .map { (date, list) ->
                    DailyBlessingRecord(
                        date = date,
                        counts = BlessingType.entries.associateWith { type ->
                            list.find { it.type == type.key }?.count ?: 0
                        },
                        wishes = BlessingType.entries.associateWith { type ->
                            wishes.find { it.date == date && it.type == type.key }?.content.orEmpty()
                        },
                        total = list.sumOf { it.count }
                    )
                }
                .sortedByDescending { it.date }
        }
    }

    /**
     * 今天墙上的心愿：当天写过就用当天的（包括清空），否则沿用最近一次没写空的。
     * 记录页仍按当天原句展示，不把沿用的心愿记成新的一天。
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeEffectiveWishes(): Flow<Map<BlessingType, String>> {
        return observeCurrentDate().flatMapLatest { today ->
            repository.observeAllWishes().map { wishes -> effectiveWishes(wishes, today) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeWishes(): Flow<Map<BlessingType, String>> {
        return observeCurrentDate().flatMapLatest(::observeWishes)
    }

    fun observeWishes(date: String): Flow<Map<BlessingType, String>> {
        return repository.observeWishesByDate(date).map { wishes ->
            BlessingType.entries.associateWith { type ->
                wishes.find { it.type == type.key }?.content.orEmpty()
            }
        }
    }

    suspend fun saveWish(type: BlessingType, content: String, date: String = todayString()) {
        repository.saveWish(
            date = date,
            type = type.key,
            content = content.trim(),
        )
    }

    private fun effectiveWishes(
        wishes: List<BlessingWishEntity>,
        today: String,
    ): Map<BlessingType, String> {
        return BlessingType.entries.associateWith { type ->
            val rows = wishes.filter { it.type == type.key && it.date <= today }
            val todayRow = rows.find { it.date == today }
            if (todayRow != null) {
                todayRow.content
            } else {
                rows.filter { it.content.isNotBlank() }.maxByOrNull { it.date }?.content.orEmpty()
            }
        }
    }

    fun observeTabCustomizationSnapshots(): Flow<List<BlessingTabCustomizationSnapshot>> {
        return repository.observeTabConfigs().map { configs ->
            configs.mapNotNull { config ->
                BlessingType.fromKey(config.type)?.let { type ->
                    BlessingTabCustomizationSnapshot(
                        date = config.date,
                        type = type,
                        title = config.title,
                        subtitle = config.subtitle,
                    )
                }
            }
        }
    }

    fun observeEffectiveTabCustomizations(
        date: String,
    ): Flow<Map<BlessingType, BlessingTabCustomization>> {
        return observeTabCustomizationSnapshots().map { snapshots ->
            snapshots.effectiveAt(date)
        }
    }

    fun observeTodayEffectiveTabCustomizations(): Flow<Map<BlessingType, BlessingTabCustomization>> {
        return combine(
            observeTabCustomizationSnapshots(),
            observeCurrentDate(),
        ) { snapshots, date ->
            snapshots.effectiveAt(date)
        }
    }

    suspend fun saveTabCustomization(type: BlessingType, title: String, subtitle: String) {
        repository.saveTabConfig(
            date = todayString(),
            type = type.key,
            title = title.trim(),
            subtitle = subtitle.trim(),
        )
    }

    private fun observeCurrentDate(): Flow<String> = flow {
        while (true) {
                emit(todayString())
                delay(millisUntilTomorrow())
        }
    }.distinctUntilChanged()

    private fun millisUntilTomorrow(): Long {
        val now = ZonedDateTime.now()
        val tomorrow = now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
        return Duration.between(now, tomorrow).toMillis().coerceAtLeast(1_000L)
    }
}
