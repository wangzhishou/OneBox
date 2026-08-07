package com.wanbaohe.a2ui.transport

import com.wanbaohe.a2ui.domain.model.A2uiAction
import com.wanbaohe.a2ui.domain.model.A2uiMessage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject

data class A2uiSurfaceRequest(
    val surfaceId: String,
    val catalogId: String = "basic",
    val endpoint: String? = null,
    val initialData: JsonObject? = null,
)

typealias A2uiMessageListener = (A2uiMessage) -> Unit

interface A2uiTransport {
    val isConnected: StateFlow<Boolean>

    fun connect(request: A2uiSurfaceRequest)

    fun sendAction(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    )

    fun sendFunctionResponse(functionCallId: String, call: String, value: JsonObject?)

    fun sendFunctionError(functionCallId: String, code: String, message: String)

    fun addMessageListener(id: String, listener: A2uiMessageListener)

    fun removeMessageListener(id: String)

    fun disconnect()
}
