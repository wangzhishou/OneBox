package com.wanbaohe.xiangqi.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Generic online game message. The payload fields (fromFile/fromRank/etc.)
 * are reused as coordinate slots across game types:
 *   - xiangqi: fromFile/fromRank -> toFile/toRank
 *   - gomoku:  fromFile -> x, fromRank -> y, toFile/toRank unused
 *
 * [seq] is a monotonically increasing sequence number used for ACK-based
 * reliable delivery.  seq == 0 means the message does not require ACK.
 */
@Serializable
data class OnlineMessage(
    val type: String,
    val gameType: String = "xiangqi",
    val roomId: String = "",
    val senderSide: String = "",
    val seq: Int = 0,
    val fromFile: Int = 0,
    val fromRank: Int = 0,
    val toFile: Int = 0,
    val toRank: Int = 0,
    val fen: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(raw: String): OnlineMessage {
            val element = json.parseToJsonElement(raw)
            val payload = element.unwrapRelayPayload()
            return json.decodeFromJsonElement(serializer(), payload)
        }

        fun toJson(msg: OnlineMessage): String =
            json.encodeToString(serializer(), msg)

        private fun JsonElement.unwrapRelayPayload(): JsonElement {
            val root = runCatching { jsonObject }.getOrNull() ?: return this
            return root.objectChild("data")
                ?: root.objectChild("payload")
                ?: root.objectChild("message")
                ?: root.stringChild("data")?.let(::fromJsonElementString)
                ?: root.stringChild("payload")?.let(::fromJsonElementString)
                ?: root.stringChild("message")?.let(::fromJsonElementString)
                ?: this
        }

        private fun JsonObject.objectChild(key: String): JsonElement? =
            get(key)?.takeIf { it is JsonObject }

        private fun JsonObject.stringChild(key: String): String? =
            get(key)?.let { element -> runCatching { element.jsonPrimitive.content }.getOrNull() }
                ?.takeIf { it.trimStart().startsWith("{") }

        private fun fromJsonElementString(raw: String): JsonElement =
            json.parseToJsonElement(raw)
    }
}
