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

package com.shifenmiao.model.automation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * AI returned automation action.
 * Supports click, swipe, input text, go back, wait, etc.
 *
 * Promoted from feature/visual-automation/model/AIAction.kt to core/model
 * so it can be shared across features without circular module dependencies.
 */
@Serializable
sealed class AIAction {

    @Serializable
    data class Click(
        val x: Int,
        val y: Int,
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class LongPress(
        val x: Int,
        val y: Int,
        val durationMs: Int = 800,
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class Swipe(
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int,
        val durationMs: Int = 300,
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class InputText(
        val text: String,
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class GoBack(
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class Wait(
        val durationMs: Int = 1000,
        val reason: String = ""
    ) : AIAction()

    @Serializable
    data class Done(
        val message: String = ""
    ) : AIAction()

    @Serializable
    data class Error(
        val message: String
    ) : AIAction()

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        /**
         * Parse an AI-returned JSON string into an [AIAction].
         *
         * AI return format example:
         * ```json
         * { "action": "click", "x": 320, "y": 640, "reason": "Tap confirm" }
         * { "action": "input_text", "text": "Hello", "reason": "Type query" }
         * { "action": "swipe", "fromX": 300, "fromY": 800, "toX": 300, "toY": 400 }
         * { "action": "go_back" }
         * { "action": "wait", "durationMs": 2000 }
         * { "action": "done", "message": "Task completed" }
         * ```
         */
        fun parse(jsonString: String): AIAction {
            val trimmed = jsonString.trim()
            val actionJson = if (trimmed.startsWith("{")) {
                trimmed
            } else {
                val codeBlock = trimmed.substringAfter("```json").substringBefore("```")
                    .ifBlank { trimmed.substringAfter("```").substringBefore("```") }
                codeBlock.ifBlank { trimmed }
            }

            return try {
                val wrapper = json.decodeFromString(ActionWrapper.serializer(), actionJson)
                when (wrapper.action.lowercase()) {
                    "click" -> Click(
                        x = wrapper.x ?: 0,
                        y = wrapper.y ?: 0,
                        reason = wrapper.reason ?: ""
                    )
                    "long_press" -> LongPress(
                        x = wrapper.x ?: 0,
                        y = wrapper.y ?: 0,
                        durationMs = wrapper.durationMs ?: 800,
                        reason = wrapper.reason ?: ""
                    )
                    "swipe" -> Swipe(
                        fromX = wrapper.fromX ?: 0,
                        fromY = wrapper.fromY ?: 0,
                        toX = wrapper.toX ?: 0,
                        toY = wrapper.toY ?: 0,
                        durationMs = wrapper.durationMs ?: 300,
                        reason = wrapper.reason ?: ""
                    )
                    "input_text", "input" -> InputText(
                        text = wrapper.text ?: "",
                        reason = wrapper.reason ?: ""
                    )
                    "go_back", "back" -> GoBack(reason = wrapper.reason ?: "")
                    "wait" -> Wait(
                        durationMs = wrapper.durationMs ?: 1000,
                        reason = wrapper.reason ?: ""
                    )
                    "done", "finish", "completed" -> Done(message = wrapper.message ?: "")
                    else -> Error("Unknown action: ${wrapper.action}")
                }
            } catch (e: Exception) {
                Error("Failed to parse action: ${e.message}. Raw: ${actionJson.take(200)}")
            }
        }
    }
}

@Serializable
private data class ActionWrapper(
    val action: String,
    val x: Int? = null,
    val y: Int? = null,
    val fromX: Int? = null,
    val fromY: Int? = null,
    val toX: Int? = null,
    val toY: Int? = null,
    val text: String? = null,
    val durationMs: Int? = null,
    val reason: String? = null,
    val message: String? = null
)