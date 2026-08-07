package com.wanbaohe.a2ui.transport.impl

import com.wanbaohe.a2ui.domain.model.A2uiAction
import com.wanbaohe.a2ui.domain.model.A2uiComponent
import com.wanbaohe.a2ui.domain.model.A2uiMessage
import com.wanbaohe.a2ui.domain.model.A2uiVersion
import com.wanbaohe.a2ui.domain.model.DynamicValue
import com.wanbaohe.a2ui.transport.A2uiMessageListener
import com.wanbaohe.a2ui.transport.A2uiSurfaceRequest
import com.wanbaohe.a2ui.transport.A2uiTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class StubA2uiTransport @Inject constructor() : A2uiTransport {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val listeners = ConcurrentHashMap<String, A2uiMessageListener>()
    private var currentSurfaceId: String? = null

    override fun connect(request: A2uiSurfaceRequest) {
        if (_isConnected.value && currentSurfaceId == request.surfaceId) return
        _isConnected.value = true
        currentSurfaceId = request.surfaceId
        notifyListeners(buildSampleSurface(request.surfaceId))
    }

    override fun sendAction(
        surfaceId: String,
        sourceComponentId: String,
        action: A2uiAction,
        actionId: String,
    ) {
        val event = action.event ?: return
        if (event.name == "submit") {
            notifyListeners(
                A2uiMessage.ActionResponse(
                    version = A2uiVersion.V1_0,
                    actionId = actionId,
                    value = JsonPrimitive("ok"),
                )
            )
        }
    }

    override fun sendFunctionResponse(functionCallId: String, call: String, value: JsonObject?) {
        notifyListeners(
            A2uiMessage.FunctionResponse(
                version = A2uiVersion.V1_0,
                functionCallId = functionCallId,
                call = call,
                value = value,
            )
        )
    }

    override fun sendFunctionError(functionCallId: String, code: String, message: String) {
        notifyListeners(
            A2uiMessage.ErrorResponse(
                version = A2uiVersion.V1_0,
                code = code,
                message = message,
                functionCallId = functionCallId,
            )
        )
    }

    override fun addMessageListener(id: String, listener: A2uiMessageListener) {
        listeners[id] = listener
    }

    override fun removeMessageListener(id: String) {
        listeners.remove(id)
    }

    override fun disconnect() {
        _isConnected.value = false
        currentSurfaceId = null
    }

    private fun notifyListeners(message: A2uiMessage) {
        listeners.values.forEach { it.invoke(message) }
    }

    private fun buildSampleSurface(surfaceId: String): A2uiMessage.CreateSurface =
        A2uiMessage.CreateSurface(
            version = A2uiVersion.V1_0,
            surfaceId = surfaceId,
            catalogId = "basic",
            components = sampleComponents(),
            dataModel = JsonObject(
                mapOf(
                    "name" to JsonPrimitive(""),
                    "subscribe" to JsonPrimitive(false),
                    "volume" to JsonPrimitive(50),
                )
            ),
        )

    private fun sampleComponents(): List<A2uiComponent> = listOf(
        A2uiComponent(
            id = "root",
            type = "Column",
            properties = mapOf(
                "padding" to DynamicValue.from(16),
                "spacing" to DynamicValue.from(12),
            ),
            children = com.wanbaohe.a2ui.domain.model.ChildList.Array(
                listOf("title_card", "name_field", "subscribe_check", "volume_slider", "submit_btn")
            ),
        ),
        A2uiComponent(
            id = "title_card",
            type = "Card",
            properties = mapOf(
                "padding" to DynamicValue.from(16),
            ),
            children = com.wanbaohe.a2ui.domain.model.ChildList.Array(
                listOf("title_text", "subtitle_text")
            ),
        ),
        A2uiComponent(
            id = "title_text",
            type = "Text",
            properties = mapOf(
                "text" to DynamicValue.from("A2UI 演示"),
                "style" to DynamicValue.from("titleLarge"),
            ),
        ),
        A2uiComponent(
            id = "subtitle_text",
            type = "Text",
            properties = mapOf(
                "text" to DynamicValue.from("由 Stub Transport 生成的示例界面"),
                "style" to DynamicValue.from("bodyMedium"),
            ),
        ),
        A2uiComponent(
            id = "name_field",
            type = "TextField",
            properties = mapOf(
                "label" to DynamicValue.from("名称"),
                "placeholder" to DynamicValue.from("请输入名称"),
                "value" to DynamicValue.Pointer("/name"),
            ),
        ),
        A2uiComponent(
            id = "subscribe_check",
            type = "CheckBox",
            properties = mapOf(
                "label" to DynamicValue.from("订阅更新"),
                "checked" to DynamicValue.Pointer("/subscribe"),
            ),
        ),
        A2uiComponent(
            id = "volume_slider",
            type = "Slider",
            properties = mapOf(
                "label" to DynamicValue.from("音量"),
                "value" to DynamicValue.Pointer("/volume"),
                "min" to DynamicValue.from(0),
                "max" to DynamicValue.from(100),
            ),
        ),
        A2uiComponent(
            id = "submit_btn",
            type = "Button",
            properties = mapOf(
                "label" to DynamicValue.from("提交"),
                "variant" to DynamicValue.from("filled"),
            ),
            action = A2uiAction(
                event = com.wanbaohe.a2ui.domain.model.A2uiActionEvent(
                    name = "submit",
                    wantResponse = true,
                )
            ),
        ),
    )
}
