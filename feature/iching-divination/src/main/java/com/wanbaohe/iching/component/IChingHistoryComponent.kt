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

package com.wanbaohe.iching.component

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.iching.data.IChingHistoryRecord
import com.wanbaohe.iching.data.IChingHistoryRepository
import com.wanbaohe.iching.domain.IChingTextLibrary
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IChingHistoryUiState(
    val records: List<IChingHistoryRecord> = emptyList(),
)

class IChingHistoryComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted private val onOpenRecord: (String) -> Unit,
    private val historyRepository: IChingHistoryRepository,
    private val textLibrary: IChingTextLibrary,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _uiState = MutableStateFlow(
        IChingHistoryUiState(records = historyRepository.records.value)
    )
    val uiState = _uiState.asStateFlow()

    init {
        componentScope.launch {
            historyRepository.records.collect { records ->
                _uiState.update { it.copy(records = records) }
            }
        }
    }

    fun openRecord(id: String) = onOpenRecord(id)

    /** 卦名/卦辞/爻辞按当前 locale 从文本库解析(历史记录只存卦号) */
    fun hexagramText(number: Int) = textLibrary.hexagram(number)

    fun deleteRecord(id: String) = historyRepository.remove(id)

    fun clearHistory() = historyRepository.clear()

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onOpenRecord: (String) -> Unit,
        ): IChingHistoryComponent
    }
}

