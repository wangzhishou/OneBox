package com.wanbaohe.a2ui.state

import com.wanbaohe.a2ui.domain.model.A2uiAction
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

data class A2uiActionEvent(
    val surfaceId: String,
    val sourceComponentId: String,
    val action: A2uiAction,
    val actionId: String,
)

@Singleton
class A2uiActionBus @Inject constructor() {

    private val _events = MutableSharedFlow<A2uiActionEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<A2uiActionEvent> = _events.asSharedFlow()

    suspend fun emit(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    ) {
        _events.emit(A2uiActionEvent(surfaceId, sourceComponentId, action, actionId))
    }

    fun tryEmit(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    ): Boolean = _events.tryEmit(A2uiActionEvent(surfaceId, sourceComponentId, action, actionId))
}
