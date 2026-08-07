package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.storage.AIChatStorage
import com.shifenmiao.storage.AppSharedStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AIFeatureSettingsComponent
    }
}

