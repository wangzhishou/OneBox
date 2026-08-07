package com.wanbaohe.a2ui.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
sealed class A2uiMessage {
    abstract val version: String

    @Serializable
    @SerialName("createSurface")
    data class CreateSurface(
        override val version: String = A2uiVersion.V1_0,
        val surfaceId: String,
        val catalogId: String = "basic",
        val surfaceProperties: JsonObject = JsonObject(emptyMap()),
        val sendDataModel: Boolean = false,
        val components: List<A2uiComponent> = emptyList(),
        val dataModel: JsonObject = JsonObject(emptyMap()),
    ) : A2uiMessage()

    @Serializable
    @SerialName("updateComponents")
    data class UpdateComponents(
        override val version: String = A2uiVersion.V1_0,
        val surfaceId: String,
        val components: List<A2uiComponent> = emptyList(),
    ) : A2uiMessage()

    @Serializable
    @SerialName("updateDataModel")
    data class UpdateDataModel(
        override val version: String = A2uiVersion.V1_0,
        val surfaceId: String,
        val path: String = "/",
        val value: JsonElement? = null,
    ) : A2uiMessage()

    @Serializable
    @SerialName("deleteSurface")
    data class DeleteSurface(
        override val version: String = A2uiVersion.V1_0,
        val surfaceId: String,
    ) : A2uiMessage()

    @Serializable
    @SerialName("actionResponse")
    data class ActionResponse(
        override val version: String = A2uiVersion.V1_0,
        val actionId: String,
        val value: JsonElement? = null,
        val error: A2uiActionError? = null,
    ) : A2uiMessage()

    @Serializable
    @SerialName("callFunction")
    data class CallFunction(
        override val version: String = A2uiVersion.V1_0,
        val functionCallId: String,
        val wantResponse: Boolean = false,
        val callFunction: A2uiFunctionCall,
    ) : A2uiMessage()

    @Serializable
    @SerialName("functionResponse")
    data class FunctionResponse(
        override val version: String = A2uiVersion.V1_0,
        val functionCallId: String,
        val call: String,
        val value: JsonElement? = null,
    ) : A2uiMessage()

    @Serializable
    @SerialName("error")
    data class ErrorResponse(
        override val version: String = A2uiVersion.V1_0,
        val code: String,
        val message: String,
        val functionCallId: String? = null,
    ) : A2uiMessage()
}

@Serializable
data class A2uiActionError(
    val code: String,
    val message: String,
)

@Serializable
internal data class A2uiMessageEnvelope(
    val version: String,
    val createSurface: A2uiMessage.CreateSurface? = null,
    val updateComponents: A2uiMessage.UpdateComponents? = null,
    val updateDataModel: A2uiMessage.UpdateDataModel? = null,
    val deleteSurface: A2uiMessage.DeleteSurface? = null,
    val actionResponse: A2uiMessage.ActionResponse? = null,
    val callFunction: A2uiMessage.CallFunction? = null,
    val functionResponse: A2uiMessage.FunctionResponse? = null,
    val error: A2uiMessage.ErrorResponse? = null,
) {
    fun toMessage(): A2uiMessage? = when {
        createSurface != null -> createSurface.copy(version = version)
        updateComponents != null -> updateComponents.copy(version = version)
        updateDataModel != null -> updateDataModel.copy(version = version)
        deleteSurface != null -> deleteSurface.copy(version = version)
        actionResponse != null -> actionResponse.copy(version = version)
        callFunction != null -> callFunction.copy(version = version)
        functionResponse != null -> functionResponse.copy(version = version)
        error != null -> error.copy(version = version)
        else -> null
    }
}
