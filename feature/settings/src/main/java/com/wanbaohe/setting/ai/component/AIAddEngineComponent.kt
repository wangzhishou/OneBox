package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
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
    @Assisted("initialProtocol") initialProtocol: String,
    @Assisted val onGoBack: () -> Unit,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val initialDraft = aiEngineCatalogManager.createLocalEngineDraft()
        .withInitialProtocol(initialProtocol)

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

    private fun AiEngine.withInitialProtocol(initialProtocol: String): AiEngine {
        return when (AiRequestProtocol.fromValue(initialProtocol.takeIf { it.isNotBlank() })) {
            AiRequestProtocol.JEV -> copy(
                requestProtocol = AiRequestProtocol.JEV,
                authType = AuthType.BEARER,
                requestUrl = requestUrl.ifBlank { UrlConstants.TYPESAFE_AI_BASE_URL },
                requestPath = requestPath.ifBlank { UrlConstants.JEV_SYSTEMONE_ENDPOINT },
                proxyUrl = proxyUrl.ifBlank { UrlConstants.RELEASE_URL },
                proxyPath = proxyPath.ifBlank { UrlConstants.JEV_PROXY_PATH },
            )
            else -> this
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialProtocol") initialProtocol: String,
            onGoBack: () -> Unit,
        ): AIAddEngineComponent
    }
}
