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

package com.wanbaohe.period.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.period.entity.PeriodRecordEntity
import com.shifenmiao.database.period.repo.PeriodRecordRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.period.R
import com.wanbaohe.period.model.PeriodCalculations
import com.wanbaohe.period.model.PeriodFlow
import com.wanbaohe.period.model.PeriodMood
import com.wanbaohe.period.model.PeriodRecordUi
import com.wanbaohe.period.model.PeriodStatsRange
import com.wanbaohe.period.model.PeriodStatsUi
import com.wanbaohe.period.model.PeriodSymptom
import com.wanbaohe.period.model.toUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 经期记录业务门面 — UI 与 Agent 共用的唯一写入入口。
 * 职责：入参校验 → Repository 写库 → ActivityLogRecorder 落审计日志。
 */
@Singleton
class PeriodService @Inject constructor(
    private val repository: PeriodRecordRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    data class RecordInput(
        val recordDate: LocalDate,
        val isPeriodStart: Boolean = false,
        val isPeriodEnd: Boolean = false,
        val flowIntensity: PeriodFlow = PeriodFlow.NONE,
        val symptoms: List<PeriodSymptom> = emptyList(),
        val mood: PeriodMood = PeriodMood.NORMAL,
        val note: String? = null,
    )

    data class RecordView(
        val id: String,
        val recordDate: LocalDate,
        val isPeriodStart: Boolean,
        val isPeriodEnd: Boolean,
        val flowIntensity: PeriodFlow,
        val symptoms: List<PeriodSymptom>,
        val mood: PeriodMood,
        val note: String?,
        val cycleDay: Int?,
    )

    // ── 写入 ─────────────────────────────────────────

    suspend fun addRecord(
        input: RecordInput,
        actor: String,
        source: String,
    ): Result<String> = runCatching {
        require(!input.isPeriodStart || !input.isPeriodEnd) {
            "period_start_and_end_same_day"
        }
        val existing = repository.getByDate(input.recordDate.toEpochDay())
        if (existing != null) {
            // 同一天只允许一条记录：转为更新，避免 UI / AI 重复记录产生重复卡片
            repository.upsert(
                existing.copy(
                    isPeriodStart = input.isPeriodStart,
                    isPeriodEnd = input.isPeriodEnd,
                    flowIntensity = input.flowIntensity.name,
                    symptoms = PeriodSymptom.toCsv(input.symptoms),
                    mood = input.mood.name,
                    note = input.note?.trim()?.takeIf { it.isNotEmpty() },
                    updatedAt = System.currentTimeMillis(),
                )
            )
            logChange(
                entityId = existing.id,
                actor = actor,
                action = "UPDATE",
                source = source,
                title = AppContext.getString(R.string.period_log_record_updated),
                date = input.recordDate,
            )
            return@runCatching existing.id
        }
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        repository.upsert(
            PeriodRecordEntity(
                id = id,
                recordDate = input.recordDate.toEpochDay(),
                isPeriodStart = input.isPeriodStart,
                isPeriodEnd = input.isPeriodEnd,
                flowIntensity = input.flowIntensity.name,
                symptoms = PeriodSymptom.toCsv(input.symptoms),
                mood = input.mood.name,
                note = input.note?.trim()?.takeIf { it.isNotEmpty() },
                createdAt = now,
                updatedAt = now,
            )
        )
        logChange(
            entityId = id,
            actor = actor,
            action = "CREATE",
            source = source,
            title = AppContext.getString(R.string.period_log_record_created),
            date = input.recordDate,
        )
        id
    }

    suspend fun updateRecord(
        recordId: String,
        input: RecordInput,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        require(!input.isPeriodStart || !input.isPeriodEnd) {
            "period_start_and_end_same_day"
        }
        val previous = repository.getById(recordId) ?: error("period_record_not_found")
        repository.upsert(
            previous.copy(
                recordDate = input.recordDate.toEpochDay(),
                isPeriodStart = input.isPeriodStart,
                isPeriodEnd = input.isPeriodEnd,
                flowIntensity = input.flowIntensity.name,
                symptoms = PeriodSymptom.toCsv(input.symptoms),
                mood = input.mood.name,
                note = input.note?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis(),
            )
        )
        logChange(
            entityId = recordId,
            actor = actor,
            action = "UPDATE",
            source = source,
            title = AppContext.getString(R.string.period_log_record_updated),
            date = input.recordDate,
        )
    }

    suspend fun deleteRecord(
        recordId: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val previous = repository.getById(recordId)
        repository.deleteById(recordId)
        activityLogRecorder.recordPeriod(
            entityId = recordId,
            entityType = "PeriodRecord",
            actorType = actor,
            actionType = "DELETE",
            source = source,
            title = AppContext.getString(R.string.period_log_record_deleted),
            description = previous?.let { LocalDate.ofEpochDay(it.recordDate).toString() } ?: "",
            appTitle = AppContext.getString(R.string.period_title),
        )
    }

    // ── 只读 ─────────────────────────────────────────

    fun observeAll(): Flow<List<PeriodRecordUi>> = repository.observeAll().map { entities ->
        val starts = entities.filter { it.isPeriodStart }.map { LocalDate.ofEpochDay(it.recordDate) }
        val ends = entities.filter { it.isPeriodEnd }.map { LocalDate.ofEpochDay(it.recordDate) }
        val avgPeriod = PeriodCalculations.averagePeriodDays(starts, ends)
        val avgCycle = PeriodCalculations.averageCycleDays(starts)
        val nextStart = PeriodCalculations.nextPeriodStart(starts, avgCycle)
        entities
            .sortedByDescending { it.recordDate }
            .map { it.toUi(starts, avgPeriod, nextStart) }
    }

    suspend fun getRecord(id: String): PeriodRecordEntity? = repository.getById(id)

    suspend fun getRecordUi(id: String): RecordView? {
        val entity = repository.getById(id) ?: return null
        return entity.toView()
    }

    suspend fun getAllViews(limit: Int = 20): List<RecordView> {
        return repository.getAll()
            .take(limit.coerceIn(1, 100))
            .map { it.toView() }
    }

    suspend fun getStats(range: PeriodStatsRange = PeriodStatsRange.SIX_MONTHS): PeriodStatsUi {
        val entities = repository.getAll()
        val starts = entities.filter { it.isPeriodStart }.map { LocalDate.ofEpochDay(it.recordDate) }
        val ends = entities.filter { it.isPeriodEnd }.map { LocalDate.ofEpochDay(it.recordDate) }
        val avgCycle = PeriodCalculations.averageCycleDays(starts)
        val avgPeriod = PeriodCalculations.averagePeriodDays(starts, ends)
        val nextStart = PeriodCalculations.nextPeriodStart(starts, avgCycle)
        val since = when (range) {
            PeriodStatsRange.SIX_MONTHS -> LocalDate.now().minusMonths(6)
            PeriodStatsRange.DAY -> LocalDate.now()
            PeriodStatsRange.WEEK -> LocalDate.now().minusDays(6)
            PeriodStatsRange.MONTH -> LocalDate.now().minusMonths(1)
        }
        val rangedEntities = entities.filter { LocalDate.ofEpochDay(it.recordDate) >= since }
        val trend = PeriodCalculations.cycleTrendPoints(starts, months = Int.MAX_VALUE)
            .filter { (yearMonth, _) -> !yearMonth.isBefore(YearMonth.from(since)) }
        val symptomCounts = PeriodCalculations.symptomCounts(
            rangedEntities.map { PeriodSymptom.parse(it.symptoms) }
        )
        return PeriodStatsUi(
            averageCycleDays = avgCycle,
            averagePeriodDays = avgPeriod,
            cycleDelta = PeriodCalculations.cycleDelta(starts),
            periodDelta = PeriodCalculations.periodDelta(starts, ends),
            nextPeriodStart = nextStart,
            cycleTrend = trend,
            symptomCounts = symptomCounts,
            symptomTotal = symptomCounts.values.sum(),
        )
    }

    /** 供列表页做「周期第 N 天」等展示 */
    suspend fun getCycleContext(): CycleContext {
        val entities = repository.getAll()
        val starts = entities.filter { it.isPeriodStart }.map { LocalDate.ofEpochDay(it.recordDate) }
        val ends = entities.filter { it.isPeriodEnd }.map { LocalDate.ofEpochDay(it.recordDate) }
        val avgPeriod = PeriodCalculations.averagePeriodDays(starts, ends)
        val avgCycle = PeriodCalculations.averageCycleDays(starts)
        return CycleContext(
            startDates = starts.sorted(),
            endDates = ends.sorted(),
            averagePeriodDays = avgPeriod,
            averageCycleDays = avgCycle,
            nextPeriodStart = PeriodCalculations.nextPeriodStart(starts, avgCycle),
        )
    }

    data class CycleContext(
        val startDates: List<LocalDate>,
        val endDates: List<LocalDate>,
        val averagePeriodDays: Int,
        val averageCycleDays: Int,
        val nextPeriodStart: LocalDate?,
    )

    // ── 内部 ─────────────────────────────────────────

    private suspend fun PeriodRecordEntity.toView(): RecordView {
        val entityStarts = repository.getAll()
            .filter { it.isPeriodStart }
            .map { LocalDate.ofEpochDay(it.recordDate) }
        val date = LocalDate.ofEpochDay(recordDate)
        return RecordView(
            id = id,
            recordDate = date,
            isPeriodStart = isPeriodStart,
            isPeriodEnd = isPeriodEnd,
            flowIntensity = PeriodFlow.fromName(flowIntensity),
            symptoms = PeriodSymptom.parse(symptoms),
            mood = PeriodMood.fromName(mood),
            note = note,
            cycleDay = PeriodCalculations.cycleDayOf(date, entityStarts),
        )
    }

    private suspend fun logChange(
        entityId: String,
        actor: String,
        action: String,
        source: String,
        title: String,
        date: LocalDate,
    ) {
        activityLogRecorder.recordPeriod(
            entityId = entityId,
            entityType = "PeriodRecord",
            actorType = actor,
            actionType = action,
            source = source,
            title = title,
            description = date.toString(),
            appTitle = AppContext.getString(R.string.period_title),
        )
    }

    suspend fun hasAnyRecord(): Boolean = repository.observeAll().first().isNotEmpty()

    companion object {
        const val ACTOR_USER = "USER"
        const val ACTOR_AGENT = "AGENT"

        const val SOURCE_UI = "ui:period"
        const val SOURCE_AGENT_ADD = "agent_tool:add_period_record"
        const val SOURCE_AGENT_UPDATE = "agent_tool:update_period_record"
        const val SOURCE_AGENT_DELETE = "agent_tool:delete_period_record"

        const val DEFAULT_PERIOD_DAYS_DISPLAY = PeriodCalculations.DEFAULT_PERIOD_DAYS

        fun isInPeriodWindow(
            date: LocalDate,
            startDates: List<LocalDate>,
            averagePeriodDays: Int,
        ): Boolean = PeriodCalculations.isInPeriodWindow(date, startDates, averagePeriodDays)

        fun isInOvulationWindow(date: LocalDate, nextStart: LocalDate?): Boolean =
            PeriodCalculations.isInOvulationWindow(date, nextStart)
    }
}
