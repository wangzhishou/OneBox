package com.wanbaohe.blessingwall.service

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

    suspend fun saveWish(type: BlessingType, content: String) {
        repository.saveWish(
            date = todayString(),
            type = type.key,
            content = content.trim(),
        )
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
