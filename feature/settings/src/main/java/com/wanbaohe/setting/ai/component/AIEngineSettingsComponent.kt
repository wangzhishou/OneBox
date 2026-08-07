package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AIEngineSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    aiEngineManager: AIEngineManager,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val allEngines: StateFlow<List<AiEngine>> =
        aiEngineCatalogManager.observeAvailableEngines()
            .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentAIEngine = aiEngineManager.currentAIEngine
    val fastAIEngine = aiEngineManager.fastAIEngine
    val isRefreshing = aiEngineCatalogManager.isRefreshing
    val lastRefreshError = aiEngineCatalogManager.lastRefreshError
    val localOwnedEngineKeys: StateFlow<Set<String>> = aiEngineCatalogManager.observeLocalOwnedEngineIdentityKeys()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun ensureCatalogRefreshed() {
        aiEngineCatalogManager.refreshCatalog(forceUpdate = true)
    }

    fun createLocalEngineDraft(): AiEngine = aiEngineCatalogManager.createLocalEngineDraft()

    fun saveLocalEngine(engine: AiEngine, onComplete: (Boolean) -> Unit = {}) {
        aiEngineCatalogManager.saveEngineConfigOnly(engine, onComplete)
    }

    fun deleteLocalEngine(engine: AiEngine, onComplete: (Boolean) -> Unit = {}) {
        aiEngineCatalogManager.deleteLocalEngine(engine, onComplete)
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): AIEngineSettingsComponent
    }
}

