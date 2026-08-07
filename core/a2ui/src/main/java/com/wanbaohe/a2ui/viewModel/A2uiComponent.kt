package com.wanbaohe.a2ui.viewModel

import com.arkivanov.decompose.ComponentContext
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.a2ui.catalog.A2uiComponentRegistry
import com.wanbaohe.a2ui.catalog.A2uiThemeMapper
import com.wanbaohe.a2ui.domain.model.A2uiAction
import com.wanbaohe.a2ui.state.A2uiActionBus
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder
import com.wanbaohe.a2ui.transport.A2uiSurfaceRequest
import com.wanbaohe.a2ui.transport.A2uiTransport
import com.wanbaohe.a2ui.ui.A2uiViewerContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class A2uiComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onGoBack: () -> Unit,
    private val surfaceHolder: A2uiSurfaceHolder,
    private val transport: A2uiTransport,
    private val actionBus: A2uiActionBus,
    private val messageHandler: com.wanbaohe.a2ui.transport.A2uiMessageHandler,
    private val registry: A2uiComponentRegistry,
    private val themeMapper: A2uiThemeMapper,
    defaultDispatchersHolder: DispatchersHolder,
) : BaseComponent(defaultDispatchersHolder, componentContext) {

    val surfaceId: String = "a2ui_main"

    val isConnected: StateFlow<Boolean> = transport.isConnected

    val viewerContext: A2uiViewerContext =
        A2uiViewerContext(surfaceHolder, actionBus, themeMapper, registry)

    init {
        componentScope.launch {
            actionBus.events.collect { event ->
                event.action.event?.let { actionEvent ->
                    transport.sendAction(
                        surfaceId = event.surfaceId,
                        sourceComponentId = event.sourceComponentId,
                        action = A2uiAction(event = actionEvent),
                        actionId = event.actionId,
                    )
                }
            }
        }
    }

    fun connect() {
        transport.connect(A2uiSurfaceRequest(surfaceId = surfaceId))
    }

    fun disconnect() {
        transport.disconnect()
        surfaceHolder.remove(surfaceId)
    }

    fun onGoBack() {
        disconnect()
        onGoBack()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): A2uiComponent
    }
}
