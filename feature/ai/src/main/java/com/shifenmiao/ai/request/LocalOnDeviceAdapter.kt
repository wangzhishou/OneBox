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

package com.shifenmiao.ai.request

import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.unified.LlmStreamEvent
import com.shifenmiao.model.ai.unified.LlmTurnRequest
import com.shifenmiao.model.channel.OnDeviceAiCapabilities
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class LocalOnDeviceAdapter @Inject constructor(
    private val onDeviceLlmService: OnDeviceLlmService,
) : LlmProviderAdapter {

    override val protocol: AiRequestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE

    override fun streamTurn(
        conversation: Conversation,
        request: LlmTurnRequest,
    ): Flow<LlmStreamEvent> = flow {
        if (!OnDeviceAiCapabilities.fromFlavor().isLocalChatSupported) {
            emit(LlmStreamEvent.Error(errorMessage = ON_DEVICE_LLM_UNSUPPORTED_ERROR))
            return@flow
        }

        val modelName = request.model
        when (val prepared = onDeviceLlmService.prepare(modelName)) {
            is LocalLlmPrepareResult.Failure -> {
                emit(LlmStreamEvent.Error(errorMessage = prepared.error.toMessage()))
                return@flow
            }

            is LocalLlmPrepareResult.Success -> Unit
        }

        emit(
            LlmStreamEvent.ResponseStarted(
                responseId = "local-${UUID.randomUUID()}",
                model = modelName,
            )
        )

        val sessionId = "local-session-${conversation.id}"
        val model = conversation.engine.model
        val generateRequest = OnDeviceLlmRequest(
            sessionId = sessionId,
            modelId = modelName,
            messages = request.messages,
            temperature = model.temperature,
            topP = model.topP,
            maxTokens = model.maxTokens,
        )

        try {
            onDeviceLlmService.generate(generateRequest).collect { event ->
                when (event) {
                    is LocalLlmGenerateEvent.Token -> emit(LlmStreamEvent.TextDelta(event.text))
                    is LocalLlmGenerateEvent.Completed -> emit(
                        LlmStreamEvent.Completed(finishReason = event.finishReason)
                    )

                    is LocalLlmGenerateEvent.Failed -> emit(
                        LlmStreamEvent.Error(errorMessage = event.message)
                    )
                }
            }
        } catch (e: CancellationException) {
            withContext(NonCancellable) { onDeviceLlmService.cancel(sessionId) }
            throw e
        }
    }
}

private fun LocalLlmError.toMessage(): String = when (this) {
    LocalLlmError.ModelFileMissing -> "Local model file is missing"
    LocalLlmError.UnsupportedAbi -> "Device ABI not supported by local runtime"
    LocalLlmError.NotEnoughMemory -> "Not enough memory to load local model"
    LocalLlmError.RuntimeLoadFailed -> "Local runtime failed to load model"
    LocalLlmError.ContextTooLong -> "Local model context window exceeded"
    LocalLlmError.GenerationCancelled -> "Local generation cancelled"
    is LocalLlmError.Unknown -> message
}
