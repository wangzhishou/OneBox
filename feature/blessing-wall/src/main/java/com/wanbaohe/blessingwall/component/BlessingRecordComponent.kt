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

package com.wanbaohe.blessingwall.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.blessingwall.model.BlessingType
import com.wanbaohe.blessingwall.model.effectiveAt
import com.wanbaohe.blessingwall.service.BlessingCalendarService
import com.wanbaohe.blessingwall.service.BlessingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class BlessingRecordComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val blessingService: BlessingService,
    private val blessingCalendarService: BlessingCalendarService,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    private var monthRecordsJob: Job? = null

    private val _uiState = MutableStateFlow(BlessingRecordUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMonthRecords(YearMonth.now())
        componentScope.launch {
            RemoteConfigStorage.rulesChanged.collect {
                val remoteTabTexts = RemoteConfigStorage.getRemoteConfig()
                    .blessingWallTabTexts
                    .orEmpty()
                    .mapNotNull { tabText ->
                        BlessingType.fromKey(tabText.type.orEmpty())?.let { it to tabText }
                    }
                    .toMap()
                _uiState.update { it.copy(remoteTabTexts = remoteTabTexts) }
            }
        }
    }

    private fun loadMonthRecords(yearMonth: YearMonth) {
        monthRecordsJob?.cancel()
        monthRecordsJob = componentScope.launch {
            val calendarDayLabels = withContext(defaultDispatcher) {
                blessingCalendarService.getMonthLabels(yearMonth)
            }
            _uiState.update {
                it.copy(
                    currentYearMonth = yearMonth,
                    calendarDayLabels = calendarDayLabels,
                )
            }
            combine(
                blessingService.observeMonthRecords(yearMonth.format(monthFormatter)),
                blessingService.observeTabCustomizationSnapshots(),
            ) { records, snapshots ->
                val customizationsByDate = records.associate { record ->
                    record.date to snapshots.effectiveAt(record.date)
                }
                records to customizationsByDate
            }.collect { (records, customizationsByDate) ->
                _uiState.update { state ->
                    val selectedDate = state.selectedDate
                    val selectedDayRecords = if (selectedDate != null) {
                        records.filter { it.date == selectedDate }
                    } else {
                        records
                    }
                    state.copy(
                        currentYearMonth = yearMonth,
                        monthRecords = records,
                        selectedDayRecords = selectedDayRecords,
                        tabCustomizationsByDate = customizationsByDate,
                    )
                }
            }
        }
    }

    fun previousMonth() {
        val prev = _uiState.value.currentYearMonth.minusMonths(1)
        _uiState.update {
            it.copy(
                currentYearMonth = prev,
                selectedDate = null,
                calendarDayLabels = emptyMap(),
                monthRecords = emptyList(),
                selectedDayRecords = emptyList(),
            )
        }
        loadMonthRecords(prev)
    }

    fun nextMonth() {
        val next = _uiState.value.currentYearMonth.plusMonths(1)
        _uiState.update {
            it.copy(
                currentYearMonth = next,
                selectedDate = null,
                calendarDayLabels = emptyMap(),
                monthRecords = emptyList(),
                selectedDayRecords = emptyList(),
            )
        }
        loadMonthRecords(next)
    }

    fun selectMonth(yearMonth: YearMonth) {
        _uiState.update {
            it.copy(
                currentYearMonth = yearMonth,
                selectedDate = null,
                calendarDayLabels = emptyMap(),
                monthRecords = emptyList(),
                selectedDayRecords = emptyList(),
            )
        }
        loadMonthRecords(yearMonth)
    }

    fun onDateSelected(date: String?) {
        val records = _uiState.value.monthRecords
        val selectedDayRecords = if (date != null) {
            records.filter { it.date == date }
        } else {
            records
        }
        _uiState.update { it.copy(selectedDate = date, selectedDayRecords = selectedDayRecords) }
    }

    fun openWall(date: String, type: BlessingType) {
        onNavigate(Screen.BlessingWall(date = date, initialType = type.key))
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): BlessingRecordComponent
    }
}
