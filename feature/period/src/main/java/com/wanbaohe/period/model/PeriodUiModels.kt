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

package com.wanbaohe.period.model

import com.shifenmiao.database.period.entity.PeriodRecordEntity
import java.time.LocalDate

data class PeriodRecordUi(
    val id: String,
    val recordDate: LocalDate,
    val isPeriodStart: Boolean,
    val isPeriodEnd: Boolean,
    val flowIntensity: PeriodFlow,
    val symptoms: List<PeriodSymptom>,
    val mood: PeriodMood,
    val note: String?,
    val cycleDay: Int?,
    val status: PeriodStatus,
)

data class PeriodMonthGroup(
    val yearMonth: java.time.YearMonth,
    val records: List<PeriodRecordUi>,
    val isCollapsed: Boolean = false,
)

data class PeriodStatsUi(
    val averageCycleDays: Int = PeriodCalculations.DEFAULT_CYCLE_DAYS,
    val averagePeriodDays: Int = PeriodCalculations.DEFAULT_PERIOD_DAYS,
    val cycleDelta: Int? = null,
    val periodDelta: Int? = null,
    val nextPeriodStart: LocalDate? = null,
    val cycleTrend: List<Pair<java.time.YearMonth, Int>> = emptyList(),
    val symptomCounts: Map<PeriodSymptom, Int> = emptyMap(),
    val symptomTotal: Int = 0,
)

data class PeriodUiState(
    val selectedTab: PeriodTab = PeriodTab.RECORDS,
    val records: List<PeriodRecordUi> = emptyList(),
    val monthGroups: List<PeriodMonthGroup> = emptyList(),
    val stats: PeriodStatsUi = PeriodStatsUi(),
    val statsRange: PeriodStatsRange = PeriodStatsRange.SIX_MONTHS,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val collapsedMonths: Set<String> = emptySet(),
    val today: LocalDate = LocalDate.now(),
    val calendarMonth: java.time.YearMonth = java.time.YearMonth.now(),
    val calendarPeriodDays: Set<LocalDate> = emptySet(),
    val calendarRecordIds: Map<LocalDate, String> = emptyMap(),
)

/** 编辑/新增表单状态 */
data class PeriodEditorState(
    val isEditing: Boolean = false,
    val recordId: String? = null,
    val recordDate: LocalDate = LocalDate.now(),
    val isPeriodStart: Boolean = true,
    val isPeriodEnd: Boolean = false,
    val flowIntensity: PeriodFlow = PeriodFlow.MEDIUM,
    val symptoms: Set<PeriodSymptom> = emptySet(),
    val mood: PeriodMood = PeriodMood.HAPPY,
    val note: String = "",
    val showDatePicker: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val isSaving: Boolean = false,
)

fun PeriodRecordEntity.toUi(
    startDates: List<LocalDate>,
    averagePeriodDays: Int,
    nextStart: LocalDate?,
): PeriodRecordUi {
    val date = LocalDate.ofEpochDay(recordDate)
    val cycleDay = PeriodCalculations.cycleDayOf(date, startDates)
    val status = when {
        PeriodCalculations.isInPeriodWindow(date, startDates, averagePeriodDays) -> PeriodStatus.PERIOD
        PeriodCalculations.isInOvulationWindow(date, nextStart) -> PeriodStatus.OVULATION
        else -> PeriodStatus.NORMAL
    }
    return PeriodRecordUi(
        id = id,
        recordDate = date,
        isPeriodStart = isPeriodStart,
        isPeriodEnd = isPeriodEnd,
        flowIntensity = PeriodFlow.fromName(flowIntensity),
        symptoms = PeriodSymptom.parse(symptoms),
        mood = PeriodMood.fromName(mood),
        note = note,
        cycleDay = cycleDay,
        status = status,
    )
}
