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

package com.shifenmiao.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.ai.usecase.TokenUsageUseCase
import com.shifenmiao.database.ai.dao.ModelUsageStat
import com.shifenmiao.database.ai.dao.TopQueryStat
import com.shifenmiao.database.ai.dao.TokenUsageSummary
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenUsageComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    private val tokenUsageUseCase: TokenUsageUseCase,
) : BaseComponent(dispatchersHolder, componentContext) {

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): TokenUsageComponent
    }

    private val _uiState = MutableStateFlow<TokenUsageUiState>(TokenUsageUiState.Loading)
    val uiState: StateFlow<TokenUsageUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = TokenUsageUiState.Loading
        componentScope.launch(ioDispatcher) {
            try {
                val summary = tokenUsageUseCase.getSummary()
                val modelStats = tokenUsageUseCase.getModelDistribution()
                val topQueries = tokenUsageUseCase.getTopQueries(limit = 10)

                if (summary.totalTokens == 0L && modelStats.isEmpty()) {
                    _uiState.value = TokenUsageUiState.Empty
                } else {
                    _uiState.value = TokenUsageUiState.Content(
                        summary = summary,
                        modelStats = modelStats,
                        topQueries = topQueries,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = TokenUsageUiState.Error(e.message ?: "")
            }
        }
    }
}

sealed interface TokenUsageUiState {
    data object Loading : TokenUsageUiState
    data object Empty : TokenUsageUiState
    data class Error(val message: String) : TokenUsageUiState
    data class Content(
        val summary: TokenUsageSummary,
        val modelStats: List<ModelUsageStat>,
        val topQueries: List<TopQueryStat>,
    ) : TokenUsageUiState
}
