package com.wanbaohe.a2ui.transport

import com.wanbaohe.a2ui.domain.model.A2uiMessage
import com.wanbaohe.a2ui.state.A2uiSurfaceHolder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiMessageHandler @Inject constructor(
    private val surfaceHolder: A2uiSurfaceHolder,
    private val transport: A2uiTransport,
) {

    init {
        transport.addMessageListener("a2ui_handler") { message ->
            handle(message)
        }
    }

    fun handle(message: A2uiMessage): Boolean = when (message) {
        is A2uiMessage.CreateSurface -> {
            val state = surfaceHolder.getOrCreate(message.surfaceId)
            state.applyCreateSurface(message)
            true
        }

        is A2uiMessage.UpdateComponents -> {
            surfaceHolder.get(message.surfaceId)?.applyUpdateComponents(message)
            true
        }

        is A2uiMessage.UpdateDataModel -> {
            surfaceHolder.get(message.surfaceId)?.applyUpdateDataModel(message)
            true
        }

        is A2uiMessage.DeleteSurface -> {
            surfaceHolder.remove(message.surfaceId)
            true
        }

        is A2uiMessage.CallFunction -> {
            handleCallFunction(message)
            true
        }

        is A2uiMessage.ActionResponse,
        is A2uiMessage.FunctionResponse,
        is A2uiMessage.ErrorResponse -> false
    }

    private fun handleCallFunction(message: A2uiMessage.CallFunction) {
        val call = message.callFunction
        val allowedRemoteFunctions = setOf("getScreenResolution", "getDeviceInfo")
        if (call.call !in allowedRemoteFunctions) {
            transport.sendFunctionError(
                functionCallId = message.functionCallId,
                code = "INVALID_FUNCTION_CALL",
                message = "Function '${call.call}' is clientOnly or not registered.",
            )
            return
        }

        val result = when (call.call) {
            "getScreenResolution" -> JsonObject(
                mapOf(
                    "width" to JsonPrimitive(1080),
                    "height" to JsonPrimitive(1920),
                )
            )

            "getDeviceInfo" -> JsonObject(
                mapOf("platform" to JsonPrimitive("android"))
            )

            else -> null
        }

        if (message.wantResponse) {
            if (result != null) {
                transport.sendFunctionResponse(
                    functionCallId = message.functionCallId,
                    call = call.call,
                    value = result,
                )
            } else {
                transport.sendFunctionError(
                    functionCallId = message.functionCallId,
                    code = "FUNCTION_EXECUTION_ERROR",
                    message = "Function '${call.call}' returned no value.",
                )
            }
        }
    }
}
