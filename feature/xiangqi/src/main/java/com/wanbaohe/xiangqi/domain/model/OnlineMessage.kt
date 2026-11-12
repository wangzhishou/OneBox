/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
