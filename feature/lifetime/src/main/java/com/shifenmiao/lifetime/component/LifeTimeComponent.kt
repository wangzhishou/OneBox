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

package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.CountdownEventRepository
import com.shifenmiao.lifetime.data.FrequencyEventRepository
import com.shifenmiao.lifetime.data.LifeTimeRepository
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.CountdownCalculator
import com.shifenmiao.lifetime.domain.CountdownSeedService
import com.shifenmiao.lifetime.domain.FrequencyCalculator
import com.shifenmiao.lifetime.domain.LifeTimeCalculator
import com.shifenmiao.lifetime.domain.LifeTimeData
import com.shifenmiao.lifetime.domain.PersonalMilestoneCalculator
import com.shifenmiao.lifetime.domain.RemainingLifeData
import com.shifenmiao.lifetime.domain.model.CountdownEvent
import com.shifenmiao.lifetime.domain.model.CountdownStatus
import com.shifenmiao.lifetime.domain.model.FrequencyEvent
import com.shifenmiao.lifetime.domain.model.FrequencyEventStats
import com.shifenmiao.lifetime.domain.model.MilestoneStatus
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.shifenmiao.lifetime.ui.TimeDisplayMode
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import java.time.LocalDate

@Immutable
data class LifeTimeUiState(
    val startDate: LocalDate? = null,
    val targetAge: Int = 80,
    val pastTimeData: LifeTimeData = LifeTimeData(),
    val remainingLifeData: RemainingLifeData = RemainingLifeData(),
    val frequencyEventStats: List<FrequencyEventStats> = emptyList(),
    val allEvents: List<FrequencyEvent> = emptyList(),
    val personalMilestones: List<MilestoneStatus> = emptyList(),
    val countdowns: List<CountdownStatus> = emptyList(),
    val timeDisplayMode: TimeDisplayMode = TimeDisplayMode.PAST,
    val isLifeProgressExpanded: Boolean = true,
    val isLoading: Boolean = false,
    val isDemoMode: Boolean = false
)

class LifeTimeComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onNavigate: (Screen) -> Unit,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: LifeTimeRepository,
    private val frequencyEventRepository: FrequencyEventRepository,
    private val personalMilestoneRepository: PersonalMilestoneRepository,
    private val countdownRepository: CountdownEventRepository,
    private val countdownSeedService: CountdownSeedService,
    private val countdownCalculator: CountdownCalculator,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(LifeTimeUiState())
    val uiState = _uiState.asStateFlow()

    private var currentEnabledEvents: List<FrequencyEvent> = emptyList()

    init {
        loadDates()
        loadTargetAge()
        loadFrequencyEvents()
        loadAllEvents()
        loadPersonalMilestones()
        loadCountdowns()
        initializePresetEvents()
        seedPresetCountdowns()
        startRealTimeUpdate()
    }

    private fun loadDates() {
        repository.birthDateFlow
            .onEach { startDate ->
                val isRealDateSet = startDate != null
                // 演示数据别用整 25 年：余天会恒为 0，看着像 bug
                val dateToUse = startDate ?: LocalDate.now().minusYears(25).minusDays(137)
                _uiState.emit(
                    _uiState.value.copy(
                        startDate = dateToUse,
                        isLoading = false,
                        isDemoMode = !isRealDateSet
                    )
                )
                updateTimeData(dateToUse)
                updateFrequencyStats(dateToUse)
            }
            .launchIn(componentScope)
    }

    private fun loadTargetAge() {
        repository.targetAgeFlow
            .onEach { age ->
                _uiState.emit(_uiState.value.copy(targetAge = age))
                _uiState.value.startDate?.let {
                    updateTimeData(it)
                    updateFrequencyStats(it)
                }
            }
            .launchIn(componentScope)
    }

    private fun startRealTimeUpdate() {
        componentScope.launch {
            while (isActive) {
                val current = _uiState.value
                current.startDate?.let { updateTimeData(it) }
                val now = LocalDate.now()
                val refreshed = countdownCalculator.calculateAll(
                    current.countdowns.map { it.event },
                    now,
                )
                if (refreshed != current.countdowns) {
                    _uiState.emit(current.copy(countdowns = refreshed))
                }
                delay(1000L)
            }
        }
    }

    private suspend fun updateTimeData(startDate: LocalDate) {
        val targetAge = _uiState.value.targetAge
        val pastTime = LifeTimeCalculator.calculatePastTime(startDate)
        val remaining = LifeTimeCalculator.calculateRemainingLife(startDate, targetAge)

        _uiState.emit(
            _uiState.value.copy(
                pastTimeData = pastTime,
                remainingLifeData = remaining
            )
        )
    }

    private suspend fun updateFrequencyStats(startDate: LocalDate) {
        val targetAge = _uiState.value.targetAge
        val stats = FrequencyCalculator.calculateAll(currentEnabledEvents, startDate, targetAge)
        _uiState.emit(_uiState.value.copy(frequencyEventStats = stats))
    }

    private fun initializePresetEvents() {
        componentScope.launch { frequencyEventRepository.initializePresetEvents() }
    }

    private fun seedPresetCountdowns() {
        componentScope.launch { countdownSeedService.seedIfNeeded() }
    }

    private fun loadFrequencyEvents() {
        frequencyEventRepository.enabledEventsFlow
            .onEach { events ->
                currentEnabledEvents = events
                _uiState.value.startDate?.let { updateFrequencyStats(it) }
            }
            .launchIn(componentScope)
    }

    private fun loadAllEvents() {
        frequencyEventRepository.allEventsFlow
            .onEach { events ->
                _uiState.emit(_uiState.value.copy(allEvents = events))
            }
            .launchIn(componentScope)
    }

    private fun loadPersonalMilestones() {
        personalMilestoneRepository.allMilestonesFlow
            .onEach { milestones ->
                val birthDate = _uiState.value.startDate ?: LocalDate.now()
                val statuses = PersonalMilestoneCalculator.calculateAll(milestones, birthDate)
                _uiState.emit(_uiState.value.copy(personalMilestones = statuses))
            }
            .launchIn(componentScope)
    }

    private fun loadCountdowns() {
        countdownRepository.allCountdownsFlow
            .onEach { events ->
                val statuses = countdownCalculator.calculateAll(events)
                _uiState.emit(_uiState.value.copy(countdowns = statuses))
            }
            .launchIn(componentScope)
    }

    fun toggleTimeDisplayMode() {
        componentScope.launch {
            val newMode = when (_uiState.value.timeDisplayMode) {
                TimeDisplayMode.PAST -> TimeDisplayMode.REMAINING
                TimeDisplayMode.REMAINING -> TimeDisplayMode.PAST
            }
            _uiState.emit(_uiState.value.copy(timeDisplayMode = newMode))
        }
    }

    fun toggleLifeProgressExpanded() {
        componentScope.launch {
            _uiState.emit(_uiState.value.copy(isLifeProgressExpanded = !_uiState.value.isLifeProgressExpanded))
        }
    }

    fun toggleEventEnabled(event: FrequencyEvent) {
        componentScope.launch { frequencyEventRepository.toggleEnabled(event.id, !event.isEnabled) }
    }

    fun deleteFrequencyEvent(event: FrequencyEvent) {
        componentScope.launch { frequencyEventRepository.deleteEvent(event) }
    }

    fun deletePersonalMilestone(milestone: PersonalMilestone) {
        componentScope.launch { personalMilestoneRepository.deleteMilestone(milestone) }
    }

    fun deleteCountdown(event: CountdownEvent) {
        componentScope.launch { countdownRepository.deleteCountdown(event) }
    }

    fun navigateToSettings() {
        onNavigate(Screen.LifeTimeSettings)
    }

    fun navigateToAddEvent() {
        onNavigate(Screen.LifeTimeAddEvent())
    }

    fun navigateToEditEvent(eventId: Long) {
        onNavigate(Screen.LifeTimeAddEvent(eventId = eventId))
    }

    fun navigateToAddMilestone() {
        onNavigate(Screen.LifeTimeAddMilestone())
    }

    fun navigateToAddCountdown() {
        onNavigate(Screen.LifeTimeAddCountdown())
    }

    fun navigateToMilestoneDetail(milestoneId: Long) {
        onNavigate(Screen.LifeTimeMilestoneDetail(milestoneId = milestoneId))
    }

    fun navigateToCountdownDetail(countdownId: Long) {
        onNavigate(Screen.LifeTimeCountdownDetail(countdownId = countdownId))
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onNavigate: (Screen) -> Unit,
            onGoBack: () -> Unit
        ): LifeTimeComponent
    }
}
