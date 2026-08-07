package com.wanbaohe.a2ui.transport

import com.wanbaohe.a2ui.domain.model.A2uiMessage
import com.wanbaohe.a2ui.domain.model.A2uiMessageEnvelope
import com.wanbaohe.a2ui.domain.model.A2uiVersion
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class A2uiJsonParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun parseMessage(jsonString: String): A2uiMessage? = runCatching {
        val envelope = json.decodeFromString(A2uiMessageEnvelope.serializer(), jsonString)
        envelope.toMessage()?.let { message ->
            if (A2uiVersion.isSupported(message.version)) message else null
        }
    }.getOrNull()

    fun serializeMessage(message: A2uiMessage): String = json.encodeToString(A2uiMessage.serializer(), message)
}
