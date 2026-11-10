package com.shifenmiao.lifetime.component

import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.lifetime.data.PersonalMilestoneRepository
import com.shifenmiao.lifetime.domain.model.PersonalMilestone
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class AddMilestoneUiState(
    val isLoading: Boolean = false,
    /** 非空表示编辑已有纪念日 */
    val milestone: PersonalMilestone? = null,
)

/**
 * 纪念日新建 / 编辑共用。
 * [milestoneId] 为空时是新建；带 id 时先读出原记录预填表单，保存走 update。
 */
class LifeTimeAddMilestoneComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val milestoneId: Long?,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val personalMilestoneRepository: PersonalMilestoneRepository
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(AddMilestoneUiState(isLoading = milestoneId != null))
    val uiState = _uiState.asStateFlow()

    init {
        milestoneId?.let(::loadMilestone)
    }

    private fun loadMilestone(id: Long) {
        componentScope.launch {
            val milestone = personalMilestoneRepository.getMilestoneById(id)
            _uiState.emit(AddMilestoneUiState(isLoading = false, milestone = milestone))
            // 记录已被删掉时不要留在空白编辑页
            if (milestone == null) onGoBack()
        }
    }

    fun save(milestone: PersonalMilestone) {
        componentScope.launch {
            if (milestone.id == 0L) {
                personalMilestoneRepository.addMilestone(milestone)
            } else {
                personalMilestoneRepository.updateMilestone(milestone)
            }
            onGoBack()
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            milestoneId: Long?,
            onGoBack: () -> Unit
        ): LifeTimeAddMilestoneComponent
    }
}
