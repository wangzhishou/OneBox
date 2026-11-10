package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.lifetime.data.MilestoneAiInsightRepository
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.MilestoneInsightService
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.shifenmiao.database.lifetime.entity.MilestoneAiInsightEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Immutable
data class MilestoneDetailUiState(
    val milestone: PersonalMilestone? = null,
    val currentInsight: String = "",
    val insights: List<MilestoneAiInsightEntity> = emptyList(),
    val isLoadingInsight: Boolean = false,
)

class LifeTimeMilestoneDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val milestoneId: Long,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val repository: PersonalMilestoneRepository,
    private val insightRepository: MilestoneAiInsightRepository,
    private val insightService: MilestoneInsightService,
    @Suppress("unused") private val aiPromptExecutor: AIPromptExecutor,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(MilestoneDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeMilestone()
        observeInsights()
    }

    /**
     * 观察而不是一次性读取：编辑返回后标题/日期要刷新。
     * 只在该详情第一次拿到记录时生成一次灵感，之后不再自动生成。
     */
    private fun observeMilestone() {
        var insightRequested = false
        repository.allMilestonesFlow
            .onEach { items ->
                val milestone = items.firstOrNull { it.id == milestoneId }
                _uiState.emit(_uiState.value.copy(milestone = milestone))
                if (milestone != null && !insightRequested) {
                    insightRequested = true
                    // 生成要联网，别卡住观察流
                    componentScope.launch { generateNewInsight() }
                }
            }
            .launchIn(componentScope)
    }

    private fun observeInsights() {
        componentScope.launch {
            insightRepository.observeByMilestone(milestoneId).collect { items ->
                _uiState.emit(
                    _uiState.value.copy(
                        insights = items,
                        currentInsight = items.firstOrNull()?.content.orEmpty(),
                    )
                )
            }
        }
    }

    private suspend fun generateNewInsight() {
        _uiState.emit(_uiState.value.copy(isLoadingInsight = true))
        val milestone = _uiState.value.milestone
        if (milestone == null) {
            _uiState.emit(_uiState.value.copy(isLoadingInsight = false))
            return
        }
        val result = insightService.generateAndSave(milestone)
        if (result is MilestoneInsightService.GenerationResult.Failed) {
            // 失败不写入历史，但记录错误原因到日志即可；UI 由 isLoadingInsight 关闭表达
        }
        _uiState.emit(_uiState.value.copy(isLoadingInsight = false))
    }

    fun refreshInsight() {
        componentScope.launch { generateNewInsight() }
    }

    fun deleteInsight(id: Long) {
        componentScope.launch { insightRepository.deleteInsight(id) }
    }

    fun updateNote(note: String) {
        componentScope.launch {
            val current = _uiState.value.milestone ?: return@launch
            val updated = current.copy(note = note.takeIf { it.isNotBlank() })
            repository.updateMilestone(updated)
            _uiState.emit(_uiState.value.copy(milestone = updated))
        }
    }

    fun editMilestone() {
        onNavigate(Screen.LifeTimeAddMilestone(milestoneId = milestoneId))
    }

    fun deleteMilestone() {
        componentScope.launch {
            insightRepository.deleteByMilestone(milestoneId)
            _uiState.value.milestone?.let { repository.deleteMilestone(it) }
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            milestoneId: Long,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit
        ): LifeTimeMilestoneDetailComponent
    }
}
