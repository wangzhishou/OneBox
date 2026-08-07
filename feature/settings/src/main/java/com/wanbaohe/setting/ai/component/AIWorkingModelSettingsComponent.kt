package com.wanbaohe.setting.ai.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AIWorkingModelSettingsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    private val aiEngineManager: AIEngineManager,
    aiEngineCatalogManager: AIEngineCatalogManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    val allEngines: StateFlow<List<AiEngine>> = aiEngineCatalogManager.observeAvailableEngines()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val modelsByProvider: StateFlow<Map<String, List<AiModel>>> = aiEngineCatalogManager.observeModelsByProvider()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val currentAIEngine = aiEngineManager.currentAIEngine
    val fastAIEngine = aiEngineManager.fastAIEngine
    val duelEngineA = aiEngineManager.duelEngineA
    val duelEngineB = aiEngineManager.duelEngineB

    enum class WorkingModelSlot {
        DEFAULT,
        FAST,
        DUEL_A,
        DUEL_B,
    }

    fun switchDefaultEngine(engine: AiEngine) {
        aiEngineManager.switchEngine(engine)
    }

    fun switchDefaultModel(engine: AiEngine, model: AiModel) {
        aiEngineManager.switchModel(engine, model)
    }

    fun switchFastEngine(engine: AiEngine) {
        aiEngineManager.switchFastEngine(engine)
    }

    fun switchFastModel(engine: AiEngine, model: AiModel) {
        aiEngineManager.switchFastModel(engine, model)
    }

    fun switchDuelEngineA(engine: AiEngine) {
        aiEngineManager.setDuelEngineA(engine)
    }

    fun switchDuelModelA(model: AiModel) {
        aiEngineManager.switchDuelModelA(model)
    }

    fun switchDuelEngineB(engine: AiEngine) {
        aiEngineManager.setDuelEngineB(engine)
    }

    fun switchDuelModelB(model: AiModel) {
        aiEngineManager.switchDuelModelB(model)
    }

    fun switchEngine(slot: WorkingModelSlot, engine: AiEngine) {
        when (slot) {
            WorkingModelSlot.DEFAULT -> switchDefaultEngine(engine)
            WorkingModelSlot.FAST -> switchFastEngine(engine)
            WorkingModelSlot.DUEL_A -> switchDuelEngineA(engine)
            WorkingModelSlot.DUEL_B -> switchDuelEngineB(engine)
        }
    }

    fun switchModel(slot: WorkingModelSlot, engine: AiEngine, model: AiModel) {
        when (slot) {
            WorkingModelSlot.DEFAULT -> switchDefaultModel(engine, model)
            WorkingModelSlot.FAST -> switchFastModel(engine, model)
            WorkingModelSlot.DUEL_A -> {
                switchDuelEngineA(engine.copy(model = model))
                switchDuelModelA(model)
            }
            WorkingModelSlot.DUEL_B -> {
                switchDuelEngineB(engine.copy(model = model))
                switchDuelModelB(model)
            }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): AIWorkingModelSettingsComponent
    }
}

