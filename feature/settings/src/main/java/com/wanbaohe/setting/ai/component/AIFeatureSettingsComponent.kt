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

package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow

class AIFeatureSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted private val unusedOnNavigate: (Screen) -> Unit,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val isConversationTitleSummaryEnabled: StateFlow<Boolean> =
        AIChatStorage.isEnableConversationTitleSummary
    val isExpandedReasoningChat: StateFlow<Boolean> =
        AppSharedStorage.isExpandedReasoningChat
    val isExpandedPrompt: StateFlow<Boolean> =
        AppSharedStorage.isExpandedPrompt
    val isExpandedToolCall: StateFlow<Boolean> =
        AppSharedStorage.isExpandedToolCall
    val maxAgentIterations: StateFlow<Int> =
        AIChatStorage.maxAgentIterations
    val maxAgentIterationsRange: IntRange =
        AIChatStorage.MIN_MAX_AGENT_ITERATIONS..AIChatStorage.MAX_MAX_AGENT_ITERATIONS
    val isContextCompactionEnabled: StateFlow<Boolean> =
        AIChatStorage.isEnableContextCompaction

    fun updateConversationTitleSummaryEnabled(enabled: Boolean) {
        AIChatStorage.saveIsEnableConversationTitleSummary(enabled)
    }

    fun updateExpandedReasoningChat(enabled: Boolean) {
        AppSharedStorage.saveIsExpandedReasoningChat(enabled)
    }

    fun updateExpandedPrompt(enabled: Boolean) {
        AppSharedStorage.saveIsExpandedPrompt(enabled)
    }

    fun updateExpandedToolCall(enabled: Boolean) {
        AppSharedStorage.saveIsExpandedToolCall(enabled)
    }

    fun updateMaxAgentIterations(value: Int) {
        AIChatStorage.saveMaxAgentIterations(value)
    }

    fun updateContextCompactionEnabled(enabled: Boolean) {
        AIChatStorage.saveIsEnableContextCompaction(enabled)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AIFeatureSettingsComponent
    }
}

