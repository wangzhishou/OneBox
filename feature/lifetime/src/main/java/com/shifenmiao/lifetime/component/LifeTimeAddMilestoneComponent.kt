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
