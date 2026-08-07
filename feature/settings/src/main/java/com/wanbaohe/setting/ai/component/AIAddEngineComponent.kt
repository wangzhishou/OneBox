package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.model.ai.AiEngine
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIAddEngineComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val initialDraft = aiEngineCatalogManager.createLocalEngineDraft()

    private val _draft = MutableStateFlow(initialDraft)
    val draft: StateFlow<AiEngine> = _draft.asStateFlow()

    private var _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    var showValidationErrors: Boolean = false
        private set

    fun updateDraft(transform: (AiEngine) -> AiEngine) {
        _draft.value = transform(_draft.value)
    }

    fun setShowValidationErrors(value: Boolean) {
        showValidationErrors = value
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val draft = _draft.value
        _isSaving.value = true
        aiEngineCatalogManager.saveEngineConfigOnly(draft) { success ->
            _isSaving.value = false
            onComplete(success)
        }
    }

    fun hasDraftChanged(): Boolean = _draft.value != initialDraft

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): AIAddEngineComponent
    }
}
